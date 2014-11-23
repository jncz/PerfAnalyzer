package com.test.instrument.servlet;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;

public class ArchiveAction implements Action {

	@Override
	public String execute(String[] params) {
		FileOp.archive(false);
		JSONObject obj = new JSONObject();
		obj.put("ok", 200);
		String result = obj.toString();
		return result;
	}

}
