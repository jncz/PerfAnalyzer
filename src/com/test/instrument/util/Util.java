package com.test.instrument.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Util {

	public static void close(Closeable fw) {
		if(fw != null){
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isEmptyString(String str) {
		return str == null || str.trim().equals("");
	}

	public static void copyPropertyFile(File srcF, File targetF) throws IOException {
		InputStream is = new FileInputStream(srcF);
		OutputStream os = new FileOutputStream(targetF);
		
		Properties p = new Properties();
		p.load(is);
		p.store(os, "for test");
		
		close(is);
		close(os);
	}

}
