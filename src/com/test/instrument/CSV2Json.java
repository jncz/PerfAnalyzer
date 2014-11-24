package com.test.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class CSV2Json {

	private static final String SPERATOR = "DataHead";
	private static final String SPERATOR_END = "DataEnd";
	private static final String MAPPING_SPERATOR = "mapping";
	
	public static JSONArray toJson(File f){
		try{
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			List<List<String>> dss = new ArrayList<List<String>>();
			List<String> ds = new ArrayList<String>();
			String temp = null;
			
			Map<String,String> mapping = new HashMap<String,String>();
			boolean inMappingSection = false;
			boolean outMappingSection = false;
			while((temp = br.readLine()) != null){
				if(temp.equals(SPERATOR)){
					ds = new ArrayList<String>();
					dss.add(ds);
				}
				if(temp.equals(SPERATOR_END)){
					outMappingSection = true;
				}
				if(inMappingSection && !outMappingSection){
					String[] ss = temp.split("=");
					mapping.put(ss[1], ss[0]);
				}
				if(temp.equals(MAPPING_SPERATOR)){
					inMappingSection = true;
				}
				if(valid(temp)){
					ds.add(temp);
				}
			}
			JSONArray arr = new JSONArray();
			for(List<String> s:dss){
				String[] datas = s.toArray(new String[0]);
				if(datas.length > 0){
					JSONArray obj2 = CSV2Json.toJson(datas,mapping);
					arr.add(obj2);
				}
			}
			return arr;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	private static void toMD5(String[] datas) {
		StringBuilder sb = new StringBuilder();
		for(String d:datas){
			sb.append(d);
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bs = md.digest(sb.toString().getBytes());
			byte[] bs2 = md.digest(sb.toString().getBytes());
			boolean b = md.isEqual(bs, bs2);
			System.out.println(b);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private static boolean valid(String temp) {
		if(temp != null && !temp.trim().equals("")){
			String[] tokens = temp.split(",");
			if(tokens.length >=3){
				String o1 = tokens[0];
				if(!isValidClassNameAndMethod(o1)){
					return false;
				}
				if(!isAnyNotNumberExceptFirst(tokens)){
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private static boolean isAnyNotNumberExceptFirst(String[] tokens) {
		try{
			for(int i=1;i<tokens.length;i++){
				if(tokens[i].trim().equals("") && i == tokens.length -1){
					continue;
				}
				Integer.parseInt(tokens[i].trim());
			}
		}catch(Exception e){
			return false;
		}
		return true;
	}

	private static boolean isValidClassNameAndMethod(String o1) {
		//TODO
		return true;
	}

	private static JSONArray toJson(String[] data, Map<String, String> mapping){
		Map<String, JSONObject> map = new HashMap<String,JSONObject>();
		JSONArray totalObjs = new JSONArray();
		for(String d:data){
			if(d == null){
				continue;
			}
			String[] tokens = d.split(",");
			String key = tokens[0];//com.spss.test.Person.say or com.spss.test.Person.say|com.spss.test.Child.say
			String[] tokens2 = key.split("\\|");//com.spss.test.Person.say
			String lastToken = tokens2[tokens2.length-1];//com.spss.test.Person.say
			String fullname = mapping.get(lastToken);
			int idx = fullname.lastIndexOf(".");
			String className = fullname.substring(0, idx);
			String methodName = fullname.substring(idx+1);
			
			JSONObject obj = createObj(tokens,className,methodName);
			map.put(key, obj);
		}
		for(String d:data){
			if(d == null){
				continue;
			}

			String[] tokens = d.split(",");
			String key = tokens[0];//com.spss.test.Person.say or com.spss.test.Person.say|com.spss.test.Child.say
			if(key.indexOf("|") == key.lastIndexOf("|")){
//				String fullname = mapping.get(key);
				totalObjs.add(map.get(key));
			}
			String[] tokens2 = key.split("\\|");//com.spss.test.Person.say
			for(int i=0;i<tokens2.length;i++){
				String t = createKey(tokens2,i);
				JSONObject obj = map.get(t);
				if(obj == null){
					continue;
				}
				JSONArray arrs = (JSONArray) obj.get("nextcall");
				if(arrs == null){
					arrs = new JSONArray();
				}
				if(i+1 < tokens2.length){
					JSONObject child = map.get(createKey(tokens2,(i+1)));
					if(!arrs.contains(child)){						
						arrs.add(child);
					}
					obj.put("nextcall", arrs);
				}
			}
		}
		return totalObjs;
	}
	
	private static String createKey(String[] tokens2, int i) {
		String s = "";
		for(int x=0;x<=i;x++){
			s += tokens2[x]+"|";
		}
		//s = s.substring(0, s.length()-1);
		return s;
	}

	private static JSONObject createObj(String[] ss, String className, String methodName) {
		JSONObject obj = new JSONObject();
		obj.put("class", className.trim());
		obj.put("method", methodName.trim());
		
		String[] values = new String[ss.length-1];
		for(int i=1;i<ss.length;i++){
			values[i-1] = ss[i];
		}
		obj.put("callTimes", values[0].trim());
		JSONArray array = new JSONArray();
		int totalCost = 0;
		for(int i=1;i<values.length;i++){
			array.add(values[i].trim());
			totalCost += Integer.parseInt(values[i].trim());
		}
		int meanCost = totalCost/(values.length-1);
		
		obj.put("callCosts", array);
		obj.put("callMeanCost", ""+meanCost);
		return obj;
	}
}
