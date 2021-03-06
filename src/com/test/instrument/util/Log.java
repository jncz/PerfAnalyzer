package com.test.instrument.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.test.instrument.Config;


public class Log {

	private final String DATA_END_MARKER = "DataEnd";
	private RandomAccessFile rf;
	private List<String> buffer;
	private int buffersize = 50;
	
	private Log(RandomAccessFile f){
		this.rf = f;
		this.buffer = new ArrayList<String>();
	}

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
		File f = new File(Config.getDataFolder(),executorName);
		if(!f.exists()){
			f.mkdirs();
		}
		File datafile = new File(f,""+System.currentTimeMillis());
		try {
			if(!datafile.exists()){
				datafile.createNewFile();
			}
			RandomAccessFile rfs = new RandomAccessFile(datafile,"rw");
			return rfs;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void warning(String string) {
		throw new UnsupportedOperationException();
	}

	public static void error(String msg) {
		System.err.print(msg);
	}

}
