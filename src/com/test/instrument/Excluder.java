package com.test.instrument;

import java.util.ArrayList;
import java.util.List;

public class Excluder {
	private static final List<String> excludes = new ArrayList<String>();
	public static List<String> init(){
		String agenthome = System.getProperty("agenthome");
		if(agenthome == null || agenthome.equals("")){
			Log.log("use -Dagenthome to speicify the agent home");
		}
		String filepath = agenthome+"/exclude.txt";//System.getProperty("agent.config.exclude.file");
		excludes.addAll(ConfigLoader.load(filepath));
		return excludes;
	}
}
