package com.test.instrument.servlet;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;

public class ListNamesAction implements Action {

	@Override
	public String execute(String[] params) {
		JSONObject obj = FileOp.listFileNames();
		return obj.toString();
	}
	
}
