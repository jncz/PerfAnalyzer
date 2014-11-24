package com.test.instrument.test;

import org.junit.Test;

public class CharCodeTest {

	@Test
	public void testPrintCode(){
		String a= "a";
		
		System.out.println(a.codePointAt(0));
		
		System.out.println((char)98);
		System.out.println("z".codePointAt(0));
		System.out.println("aa".codePointAt(0));
	}
}
