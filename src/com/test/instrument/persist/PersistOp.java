package com.test.instrument.persist;

import java.util.List;

import com.ibm.json.java.JSONObject;

public interface PersistOp {
	public JSONObject listExecutorNames();
	/**
	 * It will get the stats data for the specified executorName, 
	 * if label is null or not found, the latest one will be returned.
	 * The label can be set from the page, if you think the stats data is useful, you can keep it and give it a label
	 * @param executorName
	 * @param label
	 * @return
	 */
	public JSONObject getStatsData(String executorName,String label);
	public void archive(final boolean buildAll);
	public String process(List parts);
}
