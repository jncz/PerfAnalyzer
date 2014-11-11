package com.test.instrument;

import java.util.ArrayList;
import java.util.List;

public class Includer {
	private static final List<String> includes = new ArrayList<String>();
	public static List<String> init(){
		String agenthome = System.getProperty("agenthome");
		if(agenthome == null || agenthome.equals("")){
			Log.log("use -Dagenthome to speicify the agent home");
		}
		String filepath = agenthome+"/include.txt";//System.getProperty("agent.config.exclude.file");
		includes.addAll(ConfigLoader.load(filepath));
		return includes;
	}
}
