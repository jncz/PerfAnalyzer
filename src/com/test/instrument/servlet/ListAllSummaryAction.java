package com.test.instrument.servlet;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;

public class ListAllSummaryAction implements Action {

	@Override
	public String execute(String[] params) {
		JSONObject obj = FileOp.listAllSummary();
		return obj.toString();
	}
	
}
