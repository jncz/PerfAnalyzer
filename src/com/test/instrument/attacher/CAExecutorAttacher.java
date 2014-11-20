package com.test.instrument.attacher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import com.test.instrument.Log;

public class CAExecutorAttacher implements Attacher {

	private boolean executorReged = false;

	@Override
	public String attachPoint() {
		return "com.spss.nextgen.rest.ExecutorFinder";
	}

	@Override
	public void attach(ClassPool pool, CtClass cl) {
		if(executorReged || !cl.getName().equals(attachPoint())){
			return;
		}

		pool.importPackage("com.spss.ae.http.HTTPMethod");
		pool.importPackage("com.spss.ae.rest.DispatchRule");
		pool.importPackage("com.spss.ae.http.HTTPResponse");
		pool.importPackage("com.spss.ae.rest.executor.RequestExecutor");
		pool.importPackage("com.spss.ae.http.response.JsonResponse");
		pool.importPackage("com.spss.ae.http.response.ResponseOK");
		pool.importPackage("com.spss.utilities.path.Path");
		pool.importPackage("com.test.instrument.FileOp");
		pool.importPackage("java.io.File");
		pool.importPackage("com.ibm.json.java.JSONObject");
		pool.importPackage("com.ibm.json.java.JSONArray");
		CtClass cl2 = pool.makeClass("com.spss.rest.executors.GetJSONCallTree");
		try {
			cl2.setSuperclass(pool.get("com.spss.nextgen.rest.AbstractExecutor"));
			String mstr1 = "public HTTPResponse CAExecute() {" +
			"	System.out.println(\"JSON Call\");" +
			"	Path path = getPath();" +
			"	java.util.List parts = path.getParts();" +
			"	String result = FileOp.process(parts);" +
			"	JsonResponse res = new com.spss.ae.http.response.JsonResponse(result);" +
			"	return res;" +
			"}";
			CtMethod m1 = CtNewMethod.make(mstr1 , cl2);
			cl2.addMethod(m1);
			String mstr2 = "public DispatchRule[] getDispatchRules() {" +
			"return new DispatchRule[] {" +
			"		DispatchRule.rule(\"/ca/jsoncalltree\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ca/jsoncalltree/?\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ca/jsoncalltree/archive\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ca/jsoncalltree/archive/rebuild\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ca/jsoncalltree/stats/?\", HTTPMethod.GET)" +
			"	};" +
			"}";
			CtMethod m2 = CtNewMethod.make(mstr2, cl2);
			cl2.addMethod(m2);

			CtConstructor constuctor = CtNewConstructor.make("public GetJSONCallTree(){}", cl2);
			cl2.addConstructor(constuctor);
			
			CtMethod[] methods = cl.getMethods();
			
			cl2.toClass(cl.getClassPool().getClassLoader());
			pool.importPackage("com.spss.rest.executors.GetJSONCallTree");
			
			
			for(CtMethod method:methods){
				if(method.getName().equals("registerExecutors")){
					method.insertAfter("RequestExecutor[] myexecutors = new RequestExecutor[] {new com.spss.rest.executors.GetJSONCallTree()};dispatcher.addExecutors(myexecutors);");
					executorReged = true;
				}
			}
		}catch(Exception e){
			Log.info(e.getMessage());
		}
		
	}

	

}
