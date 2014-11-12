package com.test.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stats {
	private static final Map<Long,Stack<String>> methodNameContainer  = new ConcurrentHashMap<Long,Stack<String>>();
	private static final Map<Long,Stack<Long>>   methodCostContainer  = new ConcurrentHashMap<Long,Stack<Long>>();
	private static final Map<Long,Map<String,StatsDetail>> methodStatsContainer = new ConcurrentHashMap<Long,Map<String,StatsDetail>>();//Collections.synchronizedSortedMap(new TreeMap<String,StatsDetail>());
	
	private static final Pattern p = Pattern.compile("com.spss.nextgen.rest.*.CAExecute");
	
	public static void push(String className,String methodName,long randomId){
		try{
			Stack<String> methodNameStack = methodNameContainer.get(randomId);
			if(methodNameStack == null){
				methodNameStack = new Stack<String>();
				methodNameContainer.put(randomId, methodNameStack);
			}
			String key = toKey(className,methodName,randomId);
			methodNameStack.push(key);
			Stack<Long> methodCostStack = methodCostContainer.get(randomId);
			if(methodCostStack == null){
				methodCostStack = new Stack<Long>();
				methodCostContainer.put(randomId, methodCostStack);
			}
			methodCostStack.push(System.currentTimeMillis());
		}catch(Exception e){
			Log.info("push exception");
		}
	}
	public static void pop(long randomId){
		try{
			Stack<String> methodNameStack = methodNameContainer.get(randomId);
			Stack<Long>   methodCostStack = methodCostContainer.get(randomId);
			Map<String, StatsDetail> statsMap = methodStatsContainer.get(randomId);
			if(statsMap == null){
				statsMap = new HashMap<String,StatsDetail>();
				methodStatsContainer.put(randomId, statsMap);
			}
			long etime = System.currentTimeMillis();
			String key = "";
			long cost = etime - methodCostStack.pop();
			String appEntry = null;
			Iterator<String> eles = methodNameStack.iterator();
			while(eles.hasNext()){
				String k = eles.next();
				key += k+"|";
				if(appEntry == null){
					appEntry = findAppEntry(k);
				}
			}
			
			if(key.lastIndexOf(".")!=-1){
				key = key.substring(0, key.length()-1);
			}
			StatsDetail detail = statsMap.get(key);
			if(detail == null){
				detail = new StatsDetail(cost,1);
			}else{
				detail.incrTime(cost);
			}
			statsMap.put(key, detail);
			String mname = methodNameStack.pop();
//			if(key.indexOf("com.spss.nextgen.rest.project.executors.CreateProject.CAExecute|com.spss.ca.service.impl.ProjectServiceImpl.createProject|com.spss.ca.service.impl.ProjectServiceImpl.createProject|com.spss.ca.service.impl.ProjectServiceImpl.createProjectJob")!=-1){
//				Log.info("key: "+key+" - "+detail);
//			}
//			if(mname.equals("com.spss.ca.service.impl.ProjectServiceImpl.createProjectJob")){
//				Log.info("MethodName: "+mname);
//			}
			if(methodNameStack.isEmpty()){
				//output the tree 
				logMap(statsMap,appEntry);
				statsMap.clear();
			}
		}catch(Exception e){
			e.printStackTrace();
			Log.info("pop exception");
		}
	}
	
	private static String findAppEntry(String k) {
		Matcher matcher = p.matcher(k);
		if(matcher.find()){
			return k;
		}
		return null;
	}
	private static void logMap(Map<String, StatsDetail> m2, String appEntry) {
		Log log = Log.inst(appEntry);
//		Log.info("Stack empty");
		log.out("DataHead");
		Set<Entry<String, StatsDetail>> set = m2.entrySet();
		Iterator<Entry<String, StatsDetail>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, StatsDetail> entry = it.next();
			StatsDetail detail = entry.getValue();
			log.out(entry.getKey()+", "+detail.exeTime+", "+toStr(detail.exeCost));
//			Log.info(entry.getKey()+", "+detail.exeTime+", "+toStr(detail.exeCost));
		}
		log.out("DataEnd");
//		Log.info("Output End");
		log.close();
	}
	private static String toStr(List<Long> exeCost) {
		String s = "";
		for(Long cost:exeCost){
			s += cost+",";
		}
		return s;
	}
	private static String toKey(String className,String methodName,Long randomId){
		return className+"."+methodName;//+"._"+randomId;
	}
	
	static class StatsDetail{
		private List<Long> exeCost = new ArrayList<Long>();//ms
		private int exeTime;//call times
		public StatsDetail(long cost,int time){
			this.exeCost.add(cost);
			this.exeTime = time;
		}
		/**
		 * increase the call time
		 * @param cost 
		 */
		public void incrTime(long cost){
			this.exeCost.add(cost);
			this.exeTime++;
		}
	}
	
	public static void main(String[] args){
//		Matcher mt = p.matcher("com.spss.nextgen.rest.project.version.whatelse.GetWhatElse.CAExecute");
//		Matcher mt = p.matcher("com.spss.nextgen.rest.help.executors.GetHelp.CAExecute");
//		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute");
		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute(java.lang.String[])");
		if(mt.find()){
			System.out.println("match");
		}else{
			System.out.println("not match");
		}
	}
}
