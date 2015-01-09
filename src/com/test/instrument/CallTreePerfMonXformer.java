package com.test.instrument;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import com.test.instrument.filter.Filter;
import com.test.instrument.util.Log;
/**
 * 
 * @author pli
 *
 */
public class CallTreePerfMonXformer implements ClassFileTransformer {
	private static Filter filter;
	static{
		filter = new Filter(Config.getExcludes(),Config.getIncludes());
	}
	public byte[] transform(ClassLoader loader, String classNameInternalForm,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] transformed = null;
		if(filter.filterOut(classNameInternalForm)){
			return null;
		}
		ClassPool pool = ClassPool.getDefault();
		pool.appendSystemPath();
		
		ClassPath cp = new LoaderClassPath(loader);
		pool.appendClassPath(cp );
		CtClass cl = null;
		try {
			cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
			String className  = cl.getName();
			if(filter.filterOut(className) || cl.isInterface()){
				return null;
			}
			
			CtBehavior[] methods = cl.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (filterMethod(methods[i])) {
					doMethod(cl,methods[i]);
				}
			}
			transformed = cl.toBytecode();
		} catch (Exception e) {
			Log.log(""+e.getMessage());
		} finally {
			if (cl != null) {
				cl.detach();
			}
		}
		return transformed;
	}

	/**
	 * ONLY the non-empty method and public method will be instrumented.
	 * @param method
	 * @return
	 */
	private boolean filterMethod(CtBehavior method) {
		return method.isEmpty() == false;
	}

	private void doMethod(final CtClass cl, final CtBehavior method) throws NotFoundException,
			CannotCompileException {
		CtBehavior m = method;
		
		m.insertBefore("com.test.instrument.Stats.push(\""+cl.getName()+"\",\""+m.getName()+"\",Thread.currentThread().getId());");
		m.insertAfter("com.test.instrument.Stats.pop(Thread.currentThread().getId());");
		CtClass e = cl.getClassPool().get("java.lang.Exception");
		m.addCatch("{com.test.instrument.Stats.pop(Thread.currentThread().getId());throw $e;}", e);
	}
}
