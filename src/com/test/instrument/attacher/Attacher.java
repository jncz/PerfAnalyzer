package com.test.instrument.attacher;

import javassist.ClassPool;
import javassist.CtClass;

public interface Attacher {
	public String attachPoint();
	public void attach(ClassPool pool,CtClass cl);
}
