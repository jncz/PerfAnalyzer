/************************************************************************
 ** IBM Confidential
 ** 
 ** OCO Source Materials
 **
 ** IBM SPSS Analytic Catalyst
 **
 ** (C) Copyright IBM Corp. 2014
 **
 ** The source code for this program is not published or otherwise divested of its trade secrets, 
 ** irrespective of what has been deposited with the U.S. Copyright Office.
 ************************************************************************/
package com.test.instrument.servlet;

import java.sql.Date;

import com.ibm.json.java.JSONObject;
import com.test.instrument.persist.FileOp;

/**
 * @author liping
 *
 */
public class DeleteRecordAction extends BaseAction implements Action {

	/* (non-Javadoc)
	 * @see com.test.instrument.servlet.Action#execute(java.lang.String[])
	 */
	@Override
	public String execute(String[] params) {
		int len = params.length;
		if(len < 2){
			return error();
		}
		String timestamp = params[len - 1];
		String executor = params[len - 2];
		
		if(!valid(timestamp)){
			return error();
		}
		
		JSONObject obj = FileOp.delData(executor,Long.parseLong(timestamp));
		return obj.toString();
	}

	private boolean valid(String timestamp) {
		try{
			new Date(Long.parseLong(timestamp));
		}catch(Exception e){
			return false;
		}
		return true;
	}
}
