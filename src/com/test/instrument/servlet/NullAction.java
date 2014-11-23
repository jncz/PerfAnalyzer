package com.test.instrument.servlet;

import com.ibm.json.java.JSONObject;

public class NullAction implements Action {

	@Override
	public String execute(String[] params) {
		JSONObject obj = new JSONObject();
		obj.put("error", "not matched");
		return obj.toString();
	}

}
