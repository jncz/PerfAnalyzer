package com.test.instrument.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.test.instrument.Log;


public class Config {
	private static final Properties p = new Properties();
	private static final String KEY_INCLUDE = "include";
	private static final String KEY_INCLUDE0 = "include0";
	private static final String KEY_EXCLUDE = "exclude";
	private static final String KEY_SYSEXCLUDE = "sysexclude";
	private static final String SPLIT_MARKER = ",";
	private static final String DEFAULT_CONFIG_FILE_NAME = "agent.config";
	
	private static String rootPath = null;
	private static boolean readed = false;
	public static void setAgentHome(String path){
		rootPath = path;
	}
	private static File getAgentHome(){
		if(rootPath == null){
			Log.error("root path is not set,get the value from agenthome");
			return new File(System.getProperty("agenthome"));
		}else{
			return new File(rootPath);
		}
	}
	
	public static File getDataFolder(){
		File f = new File(getAgentHome(),"data");
		return f;
	}
	public static void read() {
		if(readed){
			return;
		}
		File f = new File(getAgentHome(),DEFAULT_CONFIG_FILE_NAME);
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

	public static String get(String key) {
		return p.getProperty(key);
	}

	public static List<String> getIncludes() {
		String value = Config.get(KEY_INCLUDE);
		return toList(value);
	}


	/**
	 * It will combine the user defined exclude pattern and sys exclude pattern
	 * @return
	 */
	public static List<String> getExcludes() {
		String value = Config.get(KEY_EXCLUDE);
		String value2 = Config.get(KEY_SYSEXCLUDE);
		
		return toList(value+SPLIT_MARKER+value2);
	}

	public static boolean include0() {
		return Boolean.parseBoolean(p.getProperty(KEY_INCLUDE0,"true"));
	}

	private static List<String> toList(String value) {
		String[] items = value.split(SPLIT_MARKER);
		return Arrays.asList(items);
	}
}
