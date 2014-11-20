package com.test.instrument;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

import com.test.instrument.attacher.AEExecutorAttacher;
import com.test.instrument.attacher.Attacher;
import com.test.instrument.attacher.CAExecutorAttacher;
import com.test.instrument.attacher.CAStaticPageAttacher;
import com.test.instrument.filter.Filter;
import com.test.instrument.util.Config;
/**
 * 
 * @author pli
 *
 */
public class CallTreePerfMonXformer implements ClassFileTransformer {
	private static Filter filter;
	private static final List<Attacher> attachers = new ArrayList<Attacher>();
	static{
		filter = new Filter(Config.getExcludes(),Config.getIncludes());
		
		attachers.add(new CAExecutorAttacher());
		attachers.add(new CAStaticPageAttacher());
		attachers.add(new AEExecutorAttacher());
	}
	public byte[] transform(ClassLoader loader, String classNameInternalForm,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] transformed = null;
		if(filter.filterOut(classNameInternalForm)){
			return null;
		}
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = null;
		try {
			cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
			String className  = cl.getName();
			if(filter.filterOut(className) || cl.isInterface() || cl.isEnum()){
				return null;
			}
			
			attachAttacher(pool,cl);
			
			CtBehavior[] methods = cl.getDeclaredMethods();
			//log(cl);
			for (int i = 0; i < methods.length; i++) {
				if (filterMethod(methods[i])) {
					doMethod(cl,methods[i]);
				}
			}
			transformed = cl.toBytecode();
			//writeToDisk(cl.getName(),transformed);
		} catch (Exception e) {
			Log.log(""+e.getMessage());
		} finally {
			if (cl != null) {
				cl.detach();
			}
		}
		return transformed;
	}

	private void attachAttacher(ClassPool pool, CtClass cl) {
		for(Attacher att : attachers){
			att.attach(pool, cl);
		}
	}

//	private void writeToDisk(String name, byte[] transformed) throws IOException {
//		File f = new File("d:/test/"+name+".class");
//		FileOutputStream os = new FileOutputStream(f);
//		os.write(transformed);
//		os.close();
//	}

//	private void log(CtClass cl) {
//		Log.info("class: "+cl.getName());
//		if(cl.getName().equals("com.spss.ca.service.impl.ProjectServiceImpl")){
//			CtBehavior[] methods = cl.getDeclaredMethods();
//			for(CtBehavior c:methods){
//				Log.info("Method: "+c.getLongName());
//			}
//		}
//	}

//	private void regPerfPage(ClassPool pool, CtClass cl) throws CannotCompileException {
//		if(cl.getName().equals("com.spss.ca.jetty.JettyResourceRegistratModule")){
//			CtMethod[] ms = cl.getMethods();
//			for(CtMethod m:ms){
//				if(m.getName().equals("startJetty")){
//					m.instrument(new ExprEditor(){
//						public void edit(MethodCall mc) throws CannotCompileException {
//							if(mc.getClassName().equals("org.eclipse.jetty.server.Server") && mc.getMethodName().equals("setHandler")){
//								mc.replace("String root = System.getProperty(\"agenthome\");" +
//										"org.eclipse.jetty.server.handler.ResourceHandler perf = create(root+\"/webclient\", false, null); " +
//										"org.eclipse.jetty.server.handler.ContextHandler perfCtx = createContextHandler(\"/perf\", root+\"/webclient\", perf);" +
//										"contexts.addHandler(perfCtx);" +
//										"$_ = $proceed($$);");
//							}
//						}
//					});
//					perfPageReged = true;
//				}
//			}
//		}
//	}

//	private void regCallTreeExecutor(ClassPool pool,CtClass cl) {
//		if(cl.getName().equals("com.spss.nextgen.rest.ExecutorFinder")){
//			pool.importPackage("com.spss.ae.http.HTTPMethod");
//			pool.importPackage("com.spss.ae.rest.DispatchRule");
//			pool.importPackage("com.spss.ae.http.HTTPResponse");
//			pool.importPackage("com.spss.ae.rest.executor.RequestExecutor");
//			pool.importPackage("com.spss.ae.http.response.JsonResponse");
//			pool.importPackage("com.spss.ae.http.response.ResponseOK");
//			pool.importPackage("com.spss.utilities.path.Path");
//			pool.importPackage("com.test.instrument.FileOp");
//			pool.importPackage("java.io.File");
//			pool.importPackage("com.ibm.json.java.JSONObject");
//			pool.importPackage("com.ibm.json.java.JSONArray");
//			CtClass cl2 = pool.makeClass("com.spss.rest.executors.GetJSONCallTree");
//			try {
//				cl2.setSuperclass(pool.get("com.spss.nextgen.rest.AbstractExecutor"));
//				String mstr1 = "public HTTPResponse CAExecute() {" +
//				"	System.out.println(\"JSON Call\");" +
//				"	Path path = getPath();" +
//				"	java.util.List parts = path.getParts();" +
//				"	String result = FileOp.process(parts);" +
//				"	JsonResponse res = new com.spss.ae.http.response.JsonResponse(result);" +
//				"	return res;" +
//				"}";
//				CtMethod m1 = CtNewMethod.make(mstr1 , cl2);
//				cl2.addMethod(m1);
//				String mstr2 = "public DispatchRule[] getDispatchRules() {" +
//				"return new DispatchRule[] {" +
//				"		DispatchRule.rule(\"/ca/jsoncalltree\", HTTPMethod.GET)," +
//				"		DispatchRule.rule(\"/ca/jsoncalltree/?\", HTTPMethod.GET)," +
//				"		DispatchRule.rule(\"/ca/jsoncalltree/archive\", HTTPMethod.GET)," +
//				"		DispatchRule.rule(\"/ca/jsoncalltree/archive/rebuild\", HTTPMethod.GET)," +
//				"		DispatchRule.rule(\"/ca/jsoncalltree/stats/?\", HTTPMethod.GET)" +
//				"	};" +
//				"}";
//				CtMethod m2 = CtNewMethod.make(mstr2, cl2);
//				cl2.addMethod(m2);
//
//				CtConstructor constuctor = CtNewConstructor.make("public GetJSONCallTree(){}", cl2);
//				cl2.addConstructor(constuctor);
//				
//				CtMethod[] methods = cl.getMethods();
//				
//				cl2.toClass(cl.getClassPool().getClassLoader());
//				pool.importPackage("com.spss.rest.executors.GetJSONCallTree");
//				
//				
//				for(CtMethod method:methods){
//					if(method.getName().equals("registerExecutors")){
//						method.insertAfter("RequestExecutor[] myexecutors = new RequestExecutor[] {new com.spss.rest.executors.GetJSONCallTree()};dispatcher.addExecutors(myexecutors);");
//						executorReged = true;
//					}
//				}
//			} catch (CannotCompileException e) {
//				e.printStackTrace();
//			} catch (NotFoundException e) {
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				e.printStackTrace();
//			}
//		}
//	}

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
		
//		method.instrument(new ExprEditor() {
//			public void edit(MethodCall m) throws CannotCompileException {
//				if(!filter.filterOut(m.getClassName())){
//					String methodStr = "{ " +
//					"com.test.instrument.Stats.push(\""+m.getClassName()+"\",\""+m.getMethodName()+"\",\"\"+System.nanoTime());" +
//					"long stime = System.currentTimeMillis(); " +
//					"try{" +
//					"	$_ = $proceed($$); " +
//					"}finally{" +
//					"	com.test.instrument.Stats.pop();" +
//					"}" +
//					"}";
//					m.replace(methodStr);
//				}
//			}
//		});
	}
}
