package com.test.instrument.test;

import org.junit.Assert;
import org.junit.Test;

import com.test.instrument.Config;
import com.test.instrument.filter.Filter;


public class FilterTest extends BaseTest{
	
	@Test
	public void testFilter(){
		
		Filter filter = new Filter(Config.getExcludes(),Config.getIncludes());
		boolean result = filter.filterOut("com/esri/sde/sdk/pe/db/builtin/PeDBbuiltinPrimemChg");
		
		Assert.assertFalse(result);
	}
	@Test
	public void testFilter2(){
		Filter filter = new Filter(Config.getExcludes(),Config.getIncludes());
		boolean result = filter.filterOut("com/spss/sde/sdk/pe/db/builtin/PeDBbuiltinPrimemChg");
		
		Assert.assertFalse(result);
	}
}
