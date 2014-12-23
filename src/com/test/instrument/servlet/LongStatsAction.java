package com.test.instrument.servlet;

import com.ibm.json.java.JSONArray;
import com.test.instrument.persist.FileOp;

public class LongStatsAction implements Action {

	@Override
	public String execute(String[] params) {
		String timestamp = params[params.length-1];
		String opv = params[params.length-2];
		
		JSONArray obj = null;
		try{
			long l = Long.parseLong(timestamp);
			obj = FileOp.getLongTermStats(opv,l);
		}catch(Exception e){
			obj = FileOp.getLongTermStats(opv,0);
		}
		return obj.toString();
	}

}
