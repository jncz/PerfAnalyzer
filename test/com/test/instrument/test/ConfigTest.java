package com.test.instrument.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.test.instrument.Config;


public class ConfigTest extends BaseTest{
	
	@Test
	public void testReadConfig(){
		String include = Config.get("include");
		String exclude = Config.get("exclude");
		String sysexclude = Config.get("sysexclude");
		
		Assert.assertNotNull(include);
		Assert.assertNotNull(exclude);
		Assert.assertNotNull(sysexclude);
	}
	
	@Test
	public void testGetIncludes(){
		List<String> includes = Config.getIncludes();
		
		Assert.assertEquals(3, includes.size());
	}
	@Test
	public void testGetExcludes(){
		List<String> excludes = Config.getExcludes();
		
		Assert.assertEquals(8+5, excludes.size());
	}
	@Test
	public void testIs0Included(){
		boolean include = Config.include0();
		
		Assert.assertEquals(true, include);
	}
	
	@Test
	public void testGetMultiplePattern(){
		String[] pattern = Config.getEntryPattern();
		Assert.assertEquals(2, pattern.length);
	}
}
