package com.test.instrument.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Filter {
	private List<String> includes = new ArrayList<String>();
	private List<String> excludes = new ArrayList<String>();

	public Filter(List<String> excludes,List<String> includes){
		this.excludes = excludes;
		this.includes = includes;
		sort();
	}
	private void sort() {
		/**
		 * com.spss.test
		 * com.spss
		 * exclude列表，会按照长的在上的顺序排序，最长的先做匹配
		 */
		Collections.sort(this.excludes, new Comparator<String>(){

			@Override
			public int compare(String name1, String name2) {
				int len1 = name1.split("\\.").length;
				int len2 = name2.split("\\.").length;
				return len2-len1;
			}});
		
		/**
		 * com.spss.test
		 * com.spss
		 * include列表，会按照短小在上的顺序排序，这样一旦匹配了第一个就算满足条件了
		 */
		Collections.sort(this.includes, new Comparator<String>(){

			@Override
			public int compare(String name1, String name2) {
				int len1 = name1.split("\\.").length;
				int len2 = name2.split("\\.").length;
				return len2-len1;
			}});
	}
	/**
	 * if true, the class will be filtered out
	 * 如果相同的包名在两个列表中都有，则以include为准
	 * 如果短的包名在exclude列表中，长的在include列表中，则以长的为准
	 * 如果长的包名在exclude列表中，短的在include列表中，则以长的为准
	 * @param className
	 * @return
	 */
	public boolean filterOut(String className){
		if(className.indexOf("/")!=-1){
			className = className.replace("/", ".");
		}
		String s1 = getLongMatchFromInclude(className);
		String s2 = getLongMatchFromExclude(className);
		if(s1 != null && s2 != null){
			int len1 = s1.split("\\.").length;
			int len2 = s2.split("\\.").length;
			
			if(len1 > len2){
				//include
				return false;
			}else{
				//exclude
				return true;
			}
		}
		if(s1 == null && s2 == null){
			return false;
		}
		if(s1 == null && s2 != null){
			return true;
		}
		return false;
	}
	private String getLongMatchFromInclude(String className) {
		for(String line:includes){
			if(className.startsWith(line)){
				return line;
			}
		}
		return null;
	}
	private String getLongMatchFromExclude(String className) {
		for(String line:excludes){
			if(className.startsWith(line)){
				return line;
			}
		}
		return null;
	}
}
