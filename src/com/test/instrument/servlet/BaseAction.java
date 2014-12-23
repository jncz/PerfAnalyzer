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

import com.ibm.json.java.JSONObject;

/**
 * @author liping
 *
 */
public abstract class BaseAction implements Action {
	protected String error() {
		JSONObject obj = new JSONObject();
		obj.put("error", "not matched");
		return obj.toString();
	}
}
