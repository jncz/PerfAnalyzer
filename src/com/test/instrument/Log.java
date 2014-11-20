package com.test.instrument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Log {
//	private static final BlockingQueue<String[]> idxOutQueue = new ArrayBlockingQueue<String[]>(10000,true);
	
	private final String DATA_END_MARKER = "DataEnd";
	private RandomAccessFile rf;
	private List<String> buffer;
	private int buffersize = 50;
	
	private static final String agenthome = System.getProperty("agenthome");
	private static final String datafolder = agenthome+"/data/";
	
//	static{
//		startIdxOutputThread();
//	}
	private Log(RandomAccessFile f){
		this.rf = f;
		this.buffer = new ArrayList<String>();
	}
	
//	private static void startIdxOutputThread() {
//		new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				try {
//					while (true) {
//						String[] names = idxOutQueue.take();
//						consume(names);
//					}
//				} catch (InterruptedException ex) {}
//			}
//			
//			void consume(String[] names) {
//				String foldername = names[0];
//				String datafilename = names[1];
//				String idxFile = datafolder+foldername+"/idx";
//				RandomAccessFile f = null;
//				try {
//					f = new RandomAccessFile(new File(idxFile),"rw");
//					f.writeBytes(datafilename);
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally{
//					Util.close(f);
//				}
//			}
//			
//		}).start();
//	}
	public static Log inst(String executorName){
		synchronized(Log.class){
			RandomAccessFile rf = output(executorName);
			return new Log(rf);
		}
	}
	public void out(String msg){
		try {
			buffer.add(msg);
			if(buffer.size() >= buffersize){
				flushBuffer();
			}else if(DATA_END_MARKER.equals(msg)){
				flushBuffer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void flushBuffer() {
		if(rf != null){
			try {
				String content = createContentFromBuffer();
				rf.writeBytes(content);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	private String createContentFromBuffer() {
		StringBuilder sb = new StringBuilder();
		for(String c:buffer){
			sb.append(c);
			sb.append("\n");
		}
		buffer.clear();
		return sb.toString();
	}
	public void close(){
		try {
			if(rf != null){
				rf.close();
				rf = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void log(String string) {
		System.out.println(string);
	}

	public static void info(String string) {
		System.out.println(string);
	}

	private static RandomAccessFile output(String executorName) {
		if(executorName == null || executorName.equals("")){
			return null;
		}
		if(agenthome != null){
			String subdatafolder = datafolder+executorName;
			File f = new File(subdatafolder);
			if(!f.exists()){
				f.mkdirs();
			}
			String datafilePath = subdatafolder+"/"+System.currentTimeMillis();
			File datafile = new File(datafilePath);
			try {
				if(!datafile.exists()){
					datafile.createNewFile();
				}
//				appendToIndex(f,datafile);
				RandomAccessFile rfs = new RandomAccessFile(datafile,"rw");
				return rfs;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
//	private static void appendToIndex(File folder, File datafile) {
//		try {
//			idxOutQueue.put(new String[]{folder.getName(),datafile.getName()});
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public static void warning(String string) {
		
	}

	public static void error(String msg) {
		System.err.print(msg);
	}

}
