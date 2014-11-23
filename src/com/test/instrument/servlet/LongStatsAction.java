package com.test.instrument.servlet;

import com.ibm.json.java.JSONArray;
import com.test.instrument.persist.FileOp;

public class LongStatsAction implements Action {

	@Override
	public String execute(String[] params) {
		String opv = params[params.length-1];
		JSONArray obj = FileOp.getLongTermStats(opv);
		return obj.toString();
	}

}
