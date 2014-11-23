package com.test.instrument.persist;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.test.instrument.CSV2Json;
//import com.test.instrument.Config;
import com.test.instrument.servlet.AppConfig;
import com.test.instrument.util.Util;

public class FileOp {
	private static final String SUFFIX_JSON = ".json";
	private static final String STATS_FILE = "stats.json";

	public static JSONObject listFileNames(){
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = AppConfig.getDataFolder();
		if(f.exists()){			
			String[] names = f.list();
			for(String n:names){
				arr.add(n);
			}
		}
//		File aef = Config.getAEDataFolder();
//		
//		if(aef != null && aef.exists() && !aef.equals(f)){
//			String[] names = aef.list();
//			for(String n:names){
//				arr.add(n);
//			}
//		}
		obj.put("names", arr);
		return obj;
	}
	
	public static JSONObject getStatsData(String opv){
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = new File(AppConfig.getDataFolder(),opv);
//		if(Config.getAEDataFolder() != null && !f.equals(Config.getAEDataFolder()) && opv.startsWith("com.spss.ae")){
//			f = new File(Config.getAEDataFolder(),opv);
//		}
//			
		if(f.exists()){
			File[] fs = f.listFiles(new FileFilter(){

				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().endsWith(SUFFIX_JSON);
				}
				
			});
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
			File jsonFile = new File(latestFile.getParentFile(),latestFile.getName()+SUFFIX_JSON);
			if(jsonFile.exists()){
				try {
					arr = JSONArray.parse(new FileReader(jsonFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				arr = CSV2Json.toJson(latestFile);
			}
		}
		obj.put("data", arr);
		return obj;
	}
	/**
	 * 
	 * @param buildAll - if true, rebuild all of the stats data
	 */
	public static void archive(final boolean buildAll){
		File caf = AppConfig.getDataFolder();
//		File aef = Config.getAEDataFolder();
		File[] roots = new File[]{caf};
//		if(aef != null && !caf.equals(aef)){
//			roots = new File[]{caf,aef};
//		}
		final Map<String,JSONArray> statsMap = buildAll?new HashMap<String,JSONArray>():initStatsMap(roots);
		
		FileProcessor<File> processor = new FileProcessor<File>(){

			@Override
			public void process(File f) {
				JSONArray arr = CSV2Json.toJson(f);
				JSONObject statsObj = addToStats(arr,f.lastModified());
				
				String key = f.getParentFile().getName();
				JSONArray stats = statsMap.get(key);
				if(stats == null){
					stats = new JSONArray();
					statsMap.put(key, stats);
				}
				stats.add(statsObj);
				
				String newname = f.getName()+SUFFIX_JSON;
				FileWriter fw = null;
				File newfile = new File(f.getParent(),newname);
				try {
					fw = new FileWriter(newfile);
					arr.serialize(fw);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally{
					Util.close(fw);
					newfile.setLastModified(f.lastModified());
				}
			}
			
		};
		listFiles(roots,new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && !pathname.getName().toLowerCase().endsWith(SUFFIX_JSON) && (buildAll?true:!new File(pathname.getParentFile(),pathname.getName()+SUFFIX_JSON).exists());
			}},processor );
		
		flushStats(statsMap);
	}
	
	private static Map<String, JSONArray> initStatsMap(File[] dataRoots) {
		final Map<String,JSONArray> statsMap = new HashMap<String,JSONArray>();
		for(File dataRoot:dataRoots){
			if(dataRoot != null && dataRoot.exists()){
				File[] files = dataRoot.listFiles(new FileFilter(){
	
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}});
				
				for(File f:files){
					File statsFile = new File(f,STATS_FILE);
					if(statsFile.exists()){
						FileReader fr = null;
						try {
							fr = new FileReader(statsFile);
							JSONArray arr = JSONArray.parse(fr);
							statsMap.put(f.getName(), arr);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally{
							Util.close(fr);
						}
					}
				}
			}
		}
		return statsMap;
	}

	private static void flushStats(Map<String, JSONArray> statsMap) {
		File caroot = AppConfig.getDataFolder();
//		File aeroot = Config.getAEDataFolder();
		
		Set<Entry<String, JSONArray>> set = statsMap.entrySet();
		Iterator<Entry<String, JSONArray>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, JSONArray> entry = it.next();
			String filename = entry.getKey();
			JSONArray stats = entry.getValue();
			File parent = caroot;
//			if(aeroot != null && !caroot.equals(aeroot) && filename.startsWith("com.spss.ae")){
//				parent = aeroot;
//			}
			File statsFile = new File(new File(parent,filename),STATS_FILE);
			FileWriter fw = null;
			try{
				fw = new FileWriter(statsFile);
				stats.serialize(fw);
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				Util.close(fw);
			}
		}
	}

	protected static JSONObject addToStats(JSONArray arr, Long lastModifiedDate) {
		JSONObject obj = ((JSONObject)(((JSONArray)arr.get(0)).get(0)));
		JSONObject statsObj = new JSONObject();
//		statsObj.put("class",obj.get("class"));
//		statsObj.put("method",obj.get("method"));
		statsObj.put("callMeanCost",obj.get("callMeanCost"));
		statsObj.put("callTimes",obj.get("callTimes"));
		statsObj.put("createdDate", lastModifiedDate);
		
		return statsObj;
	}

	private static void listFiles(File[] roots,FileFilter filter,FileProcessor<File> processor){
		for(File f:roots){
			listFiles(f,filter,processor);
		}
	}
	private static void listFiles(File root,FileFilter filter,FileProcessor<File> processor){
		File[] files = root.listFiles();
		for(File f:files){
			if(f.isDirectory()){
				listFiles(f,filter,processor);
			}else{
				if(filter.accept(f)){
					processor.process(f);
				}
			}
		}
	}
	
	public static String process(List parts){
		String result = null;
		int len = parts.size();
		switch(len){
			case 1:
				JSONObject arr = FileOp.listFileNames();
				result = arr.toString();
				break;
			case 2:
				String v = (String) parts.get(1);
				if("archive".equalsIgnoreCase(v)){
					FileOp.archive(false);
					JSONObject obj = new JSONObject();
					obj.put("ok", 200);
					result = obj.toString();
				}else{
					result = FileOp.getStatsData(v).toString();
				}
				break;
			case 3:
				String v1 = (String) parts.get(1);
				String v2 = (String) parts.get(2);
				if("archive".equalsIgnoreCase(v1) && "rebuild".equalsIgnoreCase(v2)){
					FileOp.archive(true);
					JSONObject obj = new JSONObject();
					obj.put("ok", 200);
					result = obj.toString();
				}else if("stats".equalsIgnoreCase(v1)){
					JSONArray stats = FileOp.getLongTermStats(v2);
					result = stats.toString();
				}
				break;
		}
		return result;
	}

	public static JSONArray getLongTermStats(String foldername) {
		File caf = AppConfig.getDataFolder();
//		File aef = Config.getAEDataFolder();
		File parent = caf;
//		if(aef != null && !caf.equals(aef) && foldername.startsWith("com.spss.ae")){
//			parent = aef;
//		}
		File root = new File(parent,foldername);
		File f = new File(root,STATS_FILE);
		FileReader fr = null;
		try {
			fr = new FileReader(f);
			JSONArray arr = JSONArray.parse(fr);
			return arr;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			Util.close(fr);
		}
		return new JSONArray();
	}
}
