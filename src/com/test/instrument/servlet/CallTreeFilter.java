package com.test.instrument.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CallTreeFilter implements Filter {
	private static final String CONTENT_TYPE_JSON = "application/json";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<String,Action> route = new HashMap<String,Action>();
	static{
		route.put("/calltree", new ListNamesAction());
		route.put("/calltree/?", new StatsAction());
		route.put("/calltree/archive", new ArchiveAction());
		route.put("/calltree/archive/rebuild", new RebuildAction());
		route.put("/calltree/stats/?", new LongStatsAction());
	}
	
	private static final Action nullAction = new NullAction();
	private String[] excludes;

	private String[] getPathParam(HttpServletRequest req) {
		List<String> partList = new ArrayList<String>();
		String path = req.getServletPath();
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
		return new String[]{path};
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
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
//		HttpServletRequest req = (HttpServletRequest)arg0;
//		HttpServletResponse resp = (HttpServletResponse)arg1;
//		if(isExclude(req.getServletPath())){
//			arg2.doFilter(arg0, arg1);
//			return;
//		}
//		
//		String[] params = getPathParam(req);
//		
//		String result = call(match(req.getServletPath(),params),params);
//		resp.setContentType(CONTENT_TYPE_JSON);
//		resp.getWriter().write(result);
//		resp.getWriter().flush();
		arg2.doFilter(arg0, arg1);
	}

	private Action match(String path,String[] parts) {
		switch(parts.length){
			case 1:
				if(path.equalsIgnoreCase("/calltree")){
					return new ListNamesAction();
				}
				break;
			case 2:
				if(path.equalsIgnoreCase("/calltree/archive")){
					return new ArchiveAction();
				}else{
					return new StatsAction();
				}
			case 3:
				if(path.equalsIgnoreCase("/calltree/archive/rebuild")){
					return new RebuildAction();
				}else{
					if(parts[1].equalsIgnoreCase("stats")){
						return new LongStatsAction();
					}
				}
				break;
		}
		return nullAction;
	}

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		String exclude = cfg.getInitParameter("exclude");
		excludes = exclude.split(",");
	}
	
	private boolean isExclude(String path){
		for(String exclude : excludes){
			if(path.endsWith("."+exclude)){
				return true;
			}
		}
		
		return false;
	}

}
