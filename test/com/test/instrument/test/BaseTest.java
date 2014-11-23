package com.test.instrument.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.test.instrument.Config;
import com.test.instrument.util.Util;


public class BaseTest {
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
		System.setProperty("config", targetF.getAbsolutePath());
	}
}
