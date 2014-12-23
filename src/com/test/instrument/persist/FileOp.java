package com.test.instrument.persist;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.test.instrument.CSV2Json;
import com.test.instrument.servlet.AppConfig;
import com.test.instrument.servlet.Constants;
import com.test.instrument.util.Util;

public class FileOp {
	private static final String SUFFIX_JSON = ".json";
	private static final String STATS_FILE = "stats.json";
	private static final String FOLDER_BACKUP = "backup";

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
		obj.put("names", arr);
		return obj;
	}
	
	/**
	 * return every entry latest cost and avg cost.
	 * @return
	 */
	public static JSONObject listAllSummary(){
		JSONObject obj = new JSONObject();
		File f = AppConfig.getDataFolder();
		if(f.exists()){			
			File[] files = f.listFiles();
			for(File file:files){
				File statsFile = new File(file,STATS_FILE);
				String avgcost = "0";
				if(statsFile.exists()){
					FileReader fr = null;
					try {
						fr = new FileReader(statsFile);
						JSONArray arr = JSONArray.parse(fr);
						Iterator it = arr.iterator();
						int d = 0;
						int i = 0;
						while(it.hasNext()){
							JSONObject statsData = (JSONObject) it.next();
							String cost = (String) statsData.get("callMeanCost");
							d += Integer.parseInt(cost);
							i++;
						}
						avgcost = ""+((i!=0)?(d/i):0);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally{
						Util.close(fr);
					}
				}
				
				JSONObject item1 = getStatsData(file.getName());
				JSONArray item2 = (JSONArray) item1.get("data");
				JSONArray item3 = (JSONArray) item2.get(0);
				JSONObject item4 = (JSONObject) item3.get(0);
				String cost = (String) item4.get("callMeanCost");

				String[] costs = new String[]{avgcost,cost};
				JSONArray arr = new JSONArray();
				arr.add(costs[0]);
				arr.add(costs[1]);
				obj.put(file.getName(), arr);
			}
		}
		return obj;
	}
	public static JSONObject getStatsData(String opv){
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = new File(AppConfig.getDataFolder(),opv);
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
					long l1 = getFileLastModified(file);
					long l2 = getFileLastModified(latestFile);
					
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
					appendTime(arr, jsonFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				arr = CSV2Json.toJson(latestFile);
				appendTime(arr, latestFile);
			}
		}
		obj.put("data", arr);
		return obj;
	}

	/**
	 * @param file
	 * @return
	 */
	private static long getFileLastModified(File file) {
		String name = file.getName();
		if(name.endsWith(SUFFIX_JSON)){
			try{
				return Long.parseLong(name.substring(0,name.length()-SUFFIX_JSON.length()));
			}catch(Exception e){
				return file.lastModified();
			}
		}
		return Long.parseLong(name);
	}

	private static void appendTime(JSONArray arr, File jsonFile) {
		Iterator it = arr.iterator();
		while(it.hasNext()){
			JSONArray arr2 = (JSONArray) it.next();
			Iterator it2 = arr2.iterator();
			while(it2.hasNext()){
				JSONObject obj2 = (JSONObject) it2.next();
				obj2.put("createdTime", getFileLastModified(jsonFile));
			}
		}
	}
	/**
	 * 
	 * @param buildAll - if true, rebuild all of the stats data
	 */
	public static void archive(final boolean buildAll){
		File caf = AppConfig.getDataFolder();
		File[] roots = new File[]{caf};

		final List<File> newfiles = new ArrayList<File>();
		
		FileProcessor<File> collector = new FileProcessor<File>(){
			
			@Override
			public void process(File f) {
				newfiles.add(f);
			}
			
		};
		listFiles(roots,new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && !pathname.getName().toLowerCase().endsWith(SUFFIX_JSON) && (buildAll?true:!new File(pathname.getParentFile(),pathname.getName()+SUFFIX_JSON).exists());
			}},collector );
		
		if(newfiles.size() > 0){
			Map<String,JSONArray> statsMap = buildAll?new HashMap<String,JSONArray>():initStatsMap(roots);
			archiveFiles(newfiles, statsMap);
			flushStats(statsMap);
		}
	}

	private static void archiveFiles(final List<File> newfiles,
			Map<String, JSONArray> statsMap) {
		for(File f:newfiles){
			archiveFile(statsMap, f);
		}
	}

	private static void archiveFile(Map<String, JSONArray> statsMap, File f) {
		JSONArray arr = CSV2Json.toJson(f);
		JSONObject statsObj = addToStats(arr,Long.parseLong(f.getName()));
		
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
			newfile.setLastModified(getFileLastModified(f));
		}
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
		
		Set<Entry<String, JSONArray>> set = statsMap.entrySet();
		Iterator<Entry<String, JSONArray>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, JSONArray> entry = it.next();
			String filename = entry.getKey();
			JSONArray stats = entry.getValue();
			File parent = caroot;
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
		File parent = caf;
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

	public static JSONObject getStatsData(String executor, final long timestamp,
			final String direction) {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		File f = new File(AppConfig.getDataFolder(),executor);
		if(f.exists()){
			File[] fs = f.listFiles(new FileFilter(){

				@Override
				public boolean accept(File pathname) {
					long l1 = getFileLastModified(pathname);
					
					Time t1 = new Time(l1);
					Time t2 = new Time(timestamp);
					
					boolean c1 = !pathname.getName().endsWith(SUFFIX_JSON);
					
					if(goNext(direction)){
						return c1 && t1.after(t2);
					}else if(goPrevious(direction)){
						return c1 && t1.before(t2);
					}
					return c1;
				}
				
			});
			
			Arrays.sort(fs, new Comparator<File>(){

				@Override
				public int compare(File f1, File f2) {
					Time t1 = new Time(getFileLastModified(f1));
					Time t2 = new Time(getFileLastModified(f2));
					return t1.compareTo(t2);
				}});

			File foundFile = null;
			if(fs.length > 0){
				if(goNext(direction)){
					foundFile = fs[0];
				}else if(goPrevious(direction)){
					foundFile = fs[fs.length - 1];
				}
			}
			File jsonFile = new File(foundFile.getParentFile(),foundFile.getName()+SUFFIX_JSON);
			if(jsonFile.exists()){
				try {
					arr = JSONArray.parse(new FileReader(jsonFile));
					appendTime(arr, jsonFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				arr = CSV2Json.toJson(foundFile);
				appendTime(arr, foundFile);
			}
		}
		obj.put("data", arr);
		return obj;
	}

	protected static boolean goPrevious(String direction) {
		return Constants.DIR_P.equalsIgnoreCase(direction);
	}

	protected static boolean goNext(String direction) {
		return Constants.DIR_N.equalsIgnoreCase(direction);
	}

	/**
	 * @param executor
	 * @param parseLong
	 * @return
	 */
	public static JSONObject delData(String executor, final long timestamp) {
		moveToBackup(executor,timestamp);
		JSONObject data = getStatsData(executor,timestamp,Constants.DIR_P);
		if(data == null){
			data = getStatsData(executor,timestamp,Constants.DIR_N);
		}
		return data;
	}

	/**
	 * @param executor
	 * @param timestamp
	 */
	private static void moveToBackup(String executor, long timestamp) {
		File backup = new File(AppConfig.getDataFolder().getParentFile(),FOLDER_BACKUP);
		File targetFolder = new File(backup,executor);
		createIfAbsent(backup);
		createIfAbsent(targetFolder);
		
		File datafileFolder = new File(AppConfig.getDataFolder(),executor);
		File datafile = new File(datafileFolder,""+timestamp);
		File datafileJson = new File(datafileFolder,timestamp+SUFFIX_JSON);
		moveTo(datafile,targetFolder);
		moveTo(datafileJson,targetFolder);
		refreshStats(datafileFolder,executor,timestamp);
	}

	/**
	 * @param datafileFolder 
	 * @param timestamp 
	 * @param executor 
	 * 
	 */
	private static void refreshStats(File datafileFolder, String executor, long timestamp) {
		File statsFile = new File(datafileFolder,STATS_FILE);
		if(statsFile.exists()){
			FileReader fr = null;
			OutputStream os = null;
			try {
				fr = new FileReader(statsFile);
				JSONArray arr = JSONArray.parse(fr);
				int len = arr.size();
				JSONObject toBeDeleted = null;
				for(int i=0;i<len;i++){
					JSONObject obj = (JSONObject) arr.get(i);
					if(((Long)obj.get("createdDate")).longValue() == timestamp){
						toBeDeleted = obj;
						break;
					}
				}
				arr.remove(toBeDeleted);
				os = new FileOutputStream(statsFile);
				arr.serialize(os);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				Util.close(fr);
				Util.close(os);
			}
		}
	}

	/**
	 * @param datafile
	 * @param targetFolder
	 */
	private static void moveTo(File datafile, File targetFolder) {
		if(datafile.exists()){
			File targetFile = new File(targetFolder,datafile.getName());
			InputStream is = null;
			OutputStream os = null;
			try {
				targetFile.createNewFile();
				os = new FileOutputStream(targetFile);
				is = new FileInputStream(datafile);
				byte[] bs = new byte[2048];
				int idx = -1;
				while((idx = is.read(bs)) != -1){
					os.write(bs, 0, idx);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				Util.close(os);
				Util.close(is);
				datafile.delete();
			}
		}
		
	}

	/**
	 * @param backup
	 */
	private static void createIfAbsent(File backup) {
		if(!backup.exists()){
			backup.mkdirs();
		}
	}
}
