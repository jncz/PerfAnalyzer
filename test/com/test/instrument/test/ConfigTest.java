package com.test.instrument.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.test.instrument.util.Config;
import com.test.instrument.util.Util;


public class ConfigTest {
	@Rule
	public static TemporaryFolder folder = new TemporaryFolder();
	
	private static File dataFolder = null;

	private static File targetF;
	@BeforeClass
	public static void init() throws URISyntaxException, IOException{
		dataFolder = folder.newFolder("agenthome");
		File srcF = new File(Config.class.getClassLoader().getResource("resources/agent.config").toURI());
		targetF = new File(dataFolder,"agent.config");
		Util.copyPropertyFile(srcF,targetF);
	}
	
	@AfterClass
	public static void destroy(){
		targetF.delete();
		dataFolder.delete();
		folder.delete();
	}
	@Before
	public void setup() throws URISyntaxException, IOException{
		Config.setAgentHome(dataFolder.getAbsolutePath());
	}
	
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
}
