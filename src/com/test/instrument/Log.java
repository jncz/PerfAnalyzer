package com.test.instrument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Log {
	
	private static RandomAccessFile rf;

	public static void log(String string) {
		System.out.println(string);
		try {
			if(rf != null){
				rf.writeBytes(string);
				rf.writeBytes("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void info(String string) {
		System.out.println(string);
		try {
			if(rf != null){
				rf.writeBytes(string);
				rf.writeBytes("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void output(String executorName) {
		if(executorName == null || executorName.equals("")){
			return;
		}
		String agenthome = System.getProperty("agenthome");
		if(agenthome != null){
			String datafolder = agenthome+"/data/"+executorName;
			File f = new File(datafolder);
			if(!f.exists()){
				f.mkdirs();
			}
			String datafile = datafolder+"/"+System.currentTimeMillis();
			f = new File(datafile);
			try {
				if(!f.exists()){
					f.createNewFile();
				}
				rf = new RandomAccessFile(f,"rw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void closeOutput() {
		try {
			if(rf != null){
				rf.close();
				rf = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
