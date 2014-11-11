package com.test.instrument;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class PerfMonXformer implements ClassFileTransformer {
	private static Filter filter;
	private static String include0;
	static{
		List<String> excludes = Excluder.init();
		List<String> includes = Includer.init();
		filter = new Filter(excludes,includes);
		include0 = System.getProperty("include0");
	}
	public byte[] transform(ClassLoader loader, String classNameInternalForm,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] transformed = null;
		if(filter.filterOut(classNameInternalForm)){
			return null;
		}
		Log.log("classNameInternalForm: "+classNameInternalForm);
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = null;
		try {
			cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
			String className  = cl.getName();
			if(filter.filterOut(className) || cl.isInterface() || cl.isEnum()){
				return null;
			}
			
			CtBehavior[] methods = cl.getDeclaredBehaviors();
			for (int i = 0; i < methods.length; i++) {
				if (filterMethod(methods[i])) {
					doMethod(methods[i]);
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

	private void doMethod(CtBehavior method) throws NotFoundException,
			CannotCompileException {

		method.instrument(new ExprEditor() {
			public void edit(MethodCall m) throws CannotCompileException {
				if(!filter.filterOut(m.getClassName())){
					String methodStr = "{ " +
					"long stime = System.currentTimeMillis(); " +
					"$_ = $proceed($$); " +
					"long etime = System.currentTimeMillis()-stime;";
					if(include0 != null && include0.trim().toLowerCase().equals("true")){
						methodStr += "System.out.println(\" "	+ m.getClassName()+ ","+ m.getMethodName()+ ", \"+etime);";
					}else{
						methodStr += "if(etime > 0){" +
						"	System.out.println(\" "	+ m.getClassName()+ ","+ m.getMethodName()+ ", \"+etime);" +
						"}";
					}
					
					methodStr += "}";
					m.replace(methodStr);
				}
			}
		});
	}
}
