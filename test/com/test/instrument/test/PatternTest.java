package com.test.instrument.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;


public class PatternTest {

	@Test
	public void testAEPattern(){
		Pattern p = Pattern.compile("com.spss.ae.rest*.*executors.*.execute");
		Matcher mt = p.matcher("com.spss.ae.restsecurityapi.executors.Login.execute");
		
		Assert.assertTrue(mt.find());
	}
	@Test
	public void testAEPattern2(){
		Pattern p = Pattern.compile("^(com\\.spss\\.ae\\.)+(.)+(executors\\.\\w+\\.execute)$");
		Matcher mt = p.matcher("com.spss.ae.restsecurityapi.executors.Login.execute");
		
		Assert.assertTrue(mt.find());
	}
	@Test
	public void testCAPattern(){
		Pattern p = Pattern.compile("^(com\\.spss\\.nextgen\\.rest\\.)+(.)+(executors\\.\\w+\\.CAExecute)$");
		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute");
		
		Assert.assertTrue(mt.matches());
	}
	@Test
	public void testCAPattern2(){
		Pattern p = Pattern.compile("^(com.spss.nextgen.rest.)+(.)+(executors.\\w+.CAExecute)$");
		Matcher mt = p.matcher("com.spss.nextgen.rest.datamodel.executors.GetTargets.CAExecute");
		
		Assert.assertTrue(mt.matches());
	}
	@Test
	public void testCAPattern3(){
		Pattern p = Pattern.compile("^(com.spss.nextgen.rest.)+(.)+(executors.\\w+.CAExecute)$");
		Matcher mt = p.matcher("com.spss.nextgen.rest.AbstractExecutor.execute");
		
		Assert.assertTrue(mt.matches());
	}
	@Test
	public void testCAPattern4(){
		Pattern p = Pattern.compile("^(com\\.spss\\.ca\\.service\\.job\\.AsyncJob)+(.)+(execute)$");
		Matcher mt = p.matcher("com.spss.ca.service.job.AsyncJob.execute");
		
		Assert.assertTrue(mt.matches());
	}
	@Test
	public void testURLPattern(){
		Pattern p = Pattern.compile("/calltree");
		Matcher mt = p.matcher("/calltree");
		
		Assert.assertTrue(mt.find());
	}
	
	@Test
	public void testURLPattern2(){
		Pattern p = Pattern.compile("/calltree/?");
		Matcher mt = p.matcher("/calltree/executorName");
		
		Assert.assertTrue(mt.find());
	}
	@Test
	public void testURLPattern3(){
		Pattern p = Pattern.compile("/calltree/archive");
		Matcher mt = p.matcher("/calltree/archive");
		
		Assert.assertTrue(mt.find());
	}
	@Test
	public void testURLPattern4(){
		Pattern p = Pattern.compile("/calltree/archive/rebuild");
		Matcher mt = p.matcher("/calltree/archive/rebuild");
		
		Assert.assertTrue(mt.find());
	}
	@Test
	public void testURLPattern5(){
		Pattern p = Pattern.compile("/calltree/stats/?");
		Matcher mt = p.matcher("/calltree/stats/executorName");
		
		Assert.assertTrue(mt.find());
	}
}
