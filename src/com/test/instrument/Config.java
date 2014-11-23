package com.test.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.test.instrument.util.Util;



public class Config {
	private static final Properties p = new Properties();
	private static final String KEY_CA_ENTRY_POINT_PATTERN = "caEntryPointPattern";
	private static final String KEY_AE_ENTRY_POINT_PATTERN = "aeEntryPointPattern";
	private static final String KEY_INCLUDE = "include";
	private static final String KEY_INCLUDE0 = "include0";
	private static final String KEY_EXCLUDE = "exclude";
	private static final String KEY_SYSEXCLUDE = "sysexclude";
	private static final String SPLIT_MARKER = ",";
	private static final String KEY_DATA_FOLDER = "datafolder";
	
	private static boolean readed = false;
	
	public static File getDataFolder(){
		File f = new File(Config.get(KEY_DATA_FOLDER));

		return f;
	}
	private static void read() {
		if(readed){
			return;
		}
		if(System.getProperty("config") != null){
			File f = new File(System.getProperty("config"));
			if(f.exists()){
				InputStream is = null;
				try {
					is = new FileInputStream(f);
					p.clear();
					p.load(is);
					readed = true;
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally{
					Util.close(is);
				}
			}
		}
	}

	public static String get(String key) {
		read();
		return p.getProperty(key);
	}

	public static List<String> getIncludes() {
		read();
		String value = Config.get(KEY_INCLUDE);
		return toList(value);
	}


	/**
	 * It will combine the user defined exclude pattern and sys exclude pattern
	 * @return
	 */
	public static List<String> getExcludes() {
		read();
		String value = Config.get(KEY_EXCLUDE);
		String value2 = Config.get(KEY_SYSEXCLUDE);
		
		return toList(value+SPLIT_MARKER+value2);
	}

	public static boolean include0() {
		read();
		return Boolean.parseBoolean(p.getProperty(KEY_INCLUDE0,"true"));
	}

	private static List<String> toList(String value) {
		String[] items = value.split(SPLIT_MARKER);
		return Arrays.asList(items);
	}
	public static String getAEEntryPointPattern() {
		read();
		return Config.get(KEY_AE_ENTRY_POINT_PATTERN);
	}

	public static String getCAEntryPointPattern() {
		read();
		return Config.get(KEY_CA_ENTRY_POINT_PATTERN);
	}
}
