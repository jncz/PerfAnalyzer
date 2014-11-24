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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.test.instrument.util.Log;

public class Stats {
	private static final Map<Long,Stack<Integer>> methodNameContainer  = new ConcurrentHashMap<Long,Stack<Integer>>();
	private static final Map<Long,Stack<Long>>   methodCostContainer  = new ConcurrentHashMap<Long,Stack<Long>>();
	private static final Map<Long,Map<String,StatsDetail>> methodStatsContainer = new ConcurrentHashMap<Long,Map<String,StatsDetail>>();//Collections.synchronizedSortedMap(new TreeMap<String,StatsDetail>());
	private static final Map<Long,String> entryNames = new ConcurrentHashMap<Long,String>();
	
	private static final Map<Long,Map<String,Integer>> fullClassNameToShortNameMap = new ConcurrentHashMap<Long,Map<String,Integer>>();
//	private static final Map<Integer,String> shortNameToFullClassNameMap = new ConcurrentHashMap<Integer,String>();

//	private static final Map<Long,List<String>> classInvolved = new ConcurrentHashMap<Long,List<String>>();
	
	private static final AtomicInteger chars = new AtomicInteger(0);
	
	private static final List<Pattern> patterns = createPatterns();
	private static List<Pattern> createPatterns() {
		String[] patternStr = Config.getEntryPattern();
		List<Pattern> patterns = new ArrayList<Pattern>();
		for(String p:patternStr){
			Pattern pattern = Pattern.compile(p);
			patterns.add(pattern);
		}
		return patterns;
	}
	public static void push(String className,String methodName,long randomId){
		try{
			Stack<Integer> methodNameStack = methodNameContainer.get(randomId);
			if(methodNameStack == null){
				methodNameStack = new Stack<Integer>();
				methodNameContainer.put(randomId, methodNameStack);
			}
			String key = toKey(className,methodName,randomId);
			
			Map<String, Integer> c2nMap = fullClassNameToShortNameMap.get(randomId);
			if(c2nMap == null){
				c2nMap = new HashMap<String,Integer>();
				fullClassNameToShortNameMap.put(randomId, c2nMap);
			}
			
			Integer shortKey = c2nMap.get(key);
			if(shortKey == null){
				shortKey = chars.incrementAndGet();
				c2nMap.put(key, shortKey);
//				shortNameToFullClassNameMap.put(shortKey, key);
			}
//			List<String> involvedClasses = classInvolved.get(randomId);
//			if(involvedClasses == null){
//				involvedClasses = new ArrayList<String>();
//				classInvolved.put(randomId, involvedClasses);
//			}
//			if(!involvedClasses.contains(key)){
//				involvedClasses.add(key);
//			}
			methodNameStack.push(shortKey);
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
			Stack<Integer> methodNameStack = methodNameContainer.get(randomId);
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
			Iterator<Integer> eles = methodNameStack.iterator();
			while(eles.hasNext()){
				Integer k = eles.next();
				key += k+"|";
				if(appEntry == null){
					appEntry = findAppEntry(k,randomId);
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
				logMap(statsMap,entryNames.get(randomId),randomId);
				statsMap.clear();
				entryNames.remove(randomId);
			}
		}catch(Exception e){
			e.printStackTrace();
			Log.info("pop exception");
		}
	}
	
	private static String findAppEntry(Integer k, long randomId) {
		Map<String, Integer> c2nMap = fullClassNameToShortNameMap.get(randomId);
		for(Pattern p:patterns){
			Set<String> keys = c2nMap.keySet();
			for(String key:keys){				
				Matcher mt = p.matcher(key);
				if(mt.find()){
					return key;
				}
			}
		}
		return null;
	}
	private static void logMap(Map<String, StatsDetail> m2, String appEntry, long randomId) {
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
		log.out("mapping");
		outputMapping(log,randomId);
		log.out("DataEnd");
//		Log.info("Output End");
		log.close();
	}
	private static void outputMapping(Log log, long randomId) {
		Map<String, Integer> c2nMap = fullClassNameToShortNameMap.get(randomId);
		if(c2nMap != null){
			Set<String> classes = c2nMap.keySet();
			for(String s:classes){
				Integer code = c2nMap.get(s);
				log.out(s+"="+code);
			}
			classes.clear();
			fullClassNameToShortNameMap.remove(randomId);
		}
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
