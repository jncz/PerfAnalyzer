package com.test.instrument;

import java.io.File;
import java.sql.Time;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class FileOp {
	public static JSONObject listFileNames(String root){
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = new File(root+"/data");
		if(f.exists()){			
			String[] names = f.list();
			for(String n:names){
				arr.add(n);
			}
		}
		obj.put("names", arr);
		return obj;
	}
	
	public static JSONObject getStatsData(String path,String opv){
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = new File(path+"/data/"+opv);
		if(f.exists()){
			File[] fs = f.listFiles();
			File latestFile = null;
			for(File file:fs){
				if(latestFile == null){
					latestFile = file;
				}else{
					long l1 = file.lastModified();
					long l2 = latestFile.lastModified();
					
					Time t1 = new Time(l1);
					Time t2 = new Time(l2);
					if(t1.after(t2)){
						latestFile = file;
					}
				}
			}
			arr = CSV2Json.toJson(latestFile);
		}
		obj.put("data", arr);
		return obj;
	}
}
