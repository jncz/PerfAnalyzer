package com.test.instrument.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.test.instrument.util.Util;



public class AppConfig {
	private static final Properties p = new Properties();
	private static final String KEY_DATA_FOLDER = "datafolder";
	
	
	public static File getDataFolder(){
		read();
		File f = new File(AppConfig.get(KEY_DATA_FOLDER));

		return f;
	}
	private static void read() {
		InputStream is = null;
		try {
			is = AppConfig.class.getClassLoader().getResourceAsStream("config");
			p.clear();
			p.load(is);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			Util.close(is);
		}
	}

	public static String get(String key) {
		read();
		return p.getProperty(key);
	}
}
