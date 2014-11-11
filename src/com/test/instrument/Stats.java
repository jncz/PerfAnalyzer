package com.test.instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stats {
	private static final Stack<String> v = new Stack<String>();
	private static final Stack<Long> v2 = new Stack<Long>();
	
	private static String executorName = null;
	private static final Pattern p = Pattern.compile("com.spss.nextgen.rest.*.CAExecute");
	
	public static void push(String className,String methodName,String randomId){
		String key = toKey(className,methodName,randomId);
		v.push(key);
		v2.push(System.currentTimeMillis());
	}
	public static void pop(){
		Map<String,StatsDetail> m = new TreeMap<String,StatsDetail>();
		try{
			long etime = System.currentTimeMillis();
			String key = "";
			long cost = etime - v2.pop();
			Iterator<String> eles = v.iterator();
			while(eles.hasNext()){
				String k = eles.next();
				key += k+"|";
				findExecutor(k);
			}
			if(key.lastIndexOf(".")!=-1){
				key = key.substring(0, key.length()-1);
			}
			StatsDetail detail = m.get(key);
			if(detail == null){
				detail = new StatsDetail(cost,1);
			}else{
				detail.incrTime(cost);
			}
			m.put(key, detail);
		}catch(Exception e){
			e.printStackTrace();
		}
		v.pop();
		boolean empty = v.isEmpty();
		if(empty){
			//output the tree 
			logMap(m);
			m.clear();
			executorName = null;
		}
	}
	
	private static void findExecutor(String k) {
		if(executorName == null){
			Matcher matcher = p.matcher(k);
			if(matcher.find()){
				executorName = k;
			}
		}
	}
	private static void logMap(Map<String, StatsDetail> m2) {
		Log.output(executorName);
		Log.info("Stack empty");
		Set<Entry<String, StatsDetail>> set = m2.entrySet();
		Iterator<Entry<String, StatsDetail>> it = set.iterator();
		while(it.hasNext()){
			Entry<String, StatsDetail> entry = it.next();
			StatsDetail detail = entry.getValue();
			Log.log(entry.getKey()+", "+detail.exeTime+", "+toStr(detail.exeCost));
		}
		Log.info("Output End");
		Log.closeOutput();
		executorName = null;
	}
	private static String toStr(List<Long> exeCost) {
		String s = "";
		for(Long cost:exeCost){
			s += cost+",";
		}
		return s;
	}
	private static String toKey(String className,String methodName,String randomId){
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
		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute");
		if(mt.find()){
			System.out.println("match");
		}else{
			System.out.println("not match");
			
		}
		
	}
}
