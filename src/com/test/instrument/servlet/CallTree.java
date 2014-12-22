package com.test.instrument.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CallTree extends HttpServlet {
	private static final String CONTENT_TYPE_JSON = "application/json";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private static final Map<String,Action> route = new HashMap<String,Action>();
//	static{
//		route.put("/calltree", new ListNamesAction());
//		route.put("/calltree/?", new StatsAction());
//		route.put("/calltree/?/?/?", new StatsAction());//calltree/executorName/timestamp/(n/p) used for getting the next or previous executorName details
//		route.put("/calltree/archive", new ArchiveAction());
//		route.put("/calltree/archive/rebuild", new RebuildAction());
//		route.put("/calltree/stats/?", new LongStatsAction());
//	}
	
	private static final Action nullAction = new NullAction();

	private String[] getPathParam(HttpServletRequest req) {
		List<String> partList = new ArrayList<String>();
		String path = req.getPathInfo();
		if(path != null){
			String[] parts = path.split("/");
			for(String part:parts){
				part = part.trim();
				if(!part.equals("")){
					partList.add(part);
				}
			}
			return partList.toArray(new String[0]);
		}
		return new String[]{};
	}

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] params = getPathParam(req);
		
		String result = call(match(req.getServletPath(),params),params);
		resp.setContentType(CONTENT_TYPE_JSON);
		resp.getWriter().write(result);
		resp.getWriter().flush();
	}

	private Action match(String path,String[] parts) {
		switch(parts.length){
			case 0:
				return new ListNamesAction();
			case 1:
				if(parts[0].equalsIgnoreCase("archive")){
					return new ArchiveAction();
				}else if(parts[0].equalsIgnoreCase("summary")){
					return new ListAllSummaryAction();
				}else{
					return new StatsAction();
				}
			case 2:
				if(parts[0].equalsIgnoreCase("archive") && parts[1].equalsIgnoreCase("rebuild")){
					return new RebuildAction();
				}else{
					if(parts[0].equalsIgnoreCase("stats")){
						return new LongStatsAction();
					}
				}
				break;
			case 3:
				return new ConditionStatsAction();
		}
		return nullAction;
	}

	private String call(Action action, String[] params) {
		String result = "";
		if(action != null){
			result = action.execute(params);
		}else{
			result = nullAction.execute(params);
		}
		return result;
	}


}
