package com.test.instrument.servlet;

import java.sql.Date;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;
import com.test.instrument.servlet.Constants;

/**
 * used for returning the stats info based on the condition.
 * @author pli
 *
 */
public class ConditionStatsAction implements Action {

	@Override
	public String execute(String[] params) {
		int len = params.length;
		if(len < 3){
			return error();
		}
		String direction = params[len - 1];
		String timestamp = params[len - 2];
		String executor = params[len - 3];
		
		if(!valid(timestamp,direction)){
			return error();
		}
		
		JSONObject obj = FileOp.getStatsData(executor,Long.parseLong(timestamp),direction);
		return obj.toString();
	}

	private boolean valid(String timestamp, String direction) {
		try{
			new Date(Long.parseLong(timestamp));
		}catch(Exception e){
			return false;
		}
		if(!Constants.DIR_N.equalsIgnoreCase(direction) && !Constants.DIR_P.equalsIgnoreCase(direction)){
			return false;
		}
		return true;
	}

	private String error() {
		JSONObject obj = new JSONObject();
		obj.put("error", "not matched");
		return obj.toString();
	}

	
}
