package com.test.instrument.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.test.instrument.util.Log;


public class CallTreeFilter implements Filter {
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		arg2.doFilter(arg0, arg1);
	}

	

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		new FileCompactor().start();
		Log.info("FileCompactor started");
	}
	
	

}
