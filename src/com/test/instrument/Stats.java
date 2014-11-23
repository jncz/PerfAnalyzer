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

import com.test.instrument.util.Log;

public class Stats {
	private static final Map<Long,Stack<String>> methodNameContainer  = new ConcurrentHashMap<Long,Stack<String>>();
	private static final Map<Long,Stack<Long>>   methodCostContainer  = new ConcurrentHashMap<Long,Stack<Long>>();
	private static final Map<Long,Map<String,StatsDetail>> methodStatsContainer = new ConcurrentHashMap<Long,Map<String,StatsDetail>>();//Collections.synchronizedSortedMap(new TreeMap<String,StatsDetail>());
	private static final Map<Long,String> entryNames = new ConcurrentHashMap<Long,String>();
	
	private static final Pattern p1 = Pattern.compile(Config.getCAEntryPointPattern());
	private static final Pattern p2 = Pattern.compile(Config.getAEEntryPointPattern());
	
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
			if(appEntry != null){
				entryNames.put(randomId, appEntry);
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
			methodNameStack.pop();
			if(methodNameStack.isEmpty()){
				//output the tree 
				logMap(statsMap,entryNames.get(randomId));
				statsMap.clear();
				entryNames.remove(randomId);
			}
		}catch(Exception e){
			e.printStackTrace();
			Log.info("pop exception");
		}
	}
	
	private static String findAppEntry(String k) {
		Matcher matcher1 = p1.matcher(k);
		Matcher matcher2 = p2.matcher(k);
		
		if(matcher1.find() || matcher2.find()){
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
//		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute(java.lang.String[])");
		Pattern p2 = Pattern.compile("com.spss.ae.rest*.executors.*.execute");
		Matcher mt = p2.matcher("com.spss.ae.restfileapi.executors.AppendToFile.execute");
		if(mt.find()){
			System.out.println("match");
		}else{
			System.out.println("not match");
		}
	}
}
