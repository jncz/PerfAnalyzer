package com.test.instrument.servlet;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;


public class StatsAction implements Action {

	/**
	 *  /stats/com.spss.ca.xxxx.CreateProject.CAExecute
	 *  /stats/latest
	 */
	@Override
	public String execute(String[] params) {
		String opv = params[params.length-1];
		JSONObject obj = FileOp.getStatsData(opv);
		return obj.toString();
	}

}
