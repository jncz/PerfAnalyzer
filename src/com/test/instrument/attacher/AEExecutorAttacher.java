package com.test.instrument.attacher;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import com.test.instrument.util.Log;

public class AEExecutorAttacher implements Attacher {

	private boolean reged = false;

	@Override
	public String attachPoint() {
		return "com.spss.ae.restapis.RestApiModule";
	}

	@Override
	public void attach(ClassPool pool, CtClass cl) {
		if(reged  || !cl.getName().equals(attachPoint())){
			return;
		}
		Log.info("AE attache before");
		pool.importPackage("com.spss.ae.rest.executor.DefaultRequestExecutor");
		pool.importPackage("com.spss.ae.http.HTTPMethod");
		pool.importPackage("com.spss.ae.rest.DispatchRule");
		pool.importPackage("com.spss.ae.http.HTTPRequest");
		pool.importPackage("com.spss.ae.http.HTTPResponse");
		pool.importPackage("com.spss.ae.rest.executor.RequestExecutor");
		pool.importPackage("com.spss.ae.http.response.JsonResponse");
		pool.importPackage("com.spss.ae.http.response.ResponseOK");
		pool.importPackage("com.spss.utilities.path.Path");
		pool.importPackage("com.spss.ae.http.requeststate.RequestState");
		pool.importPackage("com.test.instrument.FileOp");
		pool.importPackage("java.io.File");
		pool.importPackage("com.ibm.json.java.JSONObject");
		pool.importPackage("com.ibm.json.java.JSONArray");
		CtClass cl2 = pool.makeClass("com.spss.rest.executors.GetJSONCallTree");
		try {
//			cl2.setSuperclass(pool.get("com.spss.nextgen.rest.AbstractExecutor"));
			cl2.setInterfaces(pool.get(new String[]{"com.spss.ae.rest.executor.DefaultRequestExecutor"}));
			String mstr1 = "public HTTPResponse execute() {" +
			"	System.out.println(\"JSON Call\");" +
			"	HTTPRequest request = RequestState.get().getValue();" +
			"	Path path = request.getUriAsPath();" +
			"	java.util.List parts = path.getParts();" +
			"	String result = FileOp.process(parts);" +
			"	JsonResponse res = new com.spss.ae.http.response.JsonResponse(result);" +
			"	return res;" +
			"}";
			CtMethod m1 = CtNewMethod.make(mstr1 , cl2);
			cl2.addMethod(m1);
			String mstr2 = "public DispatchRule[] getDispatchRules() {" +
			"return new DispatchRule[] {" +
			"		DispatchRule.rule(\"/ae/jsoncalltree\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ae/jsoncalltree/?\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ae/jsoncalltree/archive\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ae/jsoncalltree/archive/rebuild\", HTTPMethod.GET)," +
			"		DispatchRule.rule(\"/ae/jsoncalltree/stats/?\", HTTPMethod.GET)" +
			"	};" +
			"}";
			CtMethod m2 = CtNewMethod.make(mstr2, cl2);
			cl2.addMethod(m2);

			CtConstructor constuctor = CtNewConstructor.make("public GetJSONCallTree(){}", cl2);
			cl2.addConstructor(constuctor);
			
			CtMethod[] methods = cl.getMethods();
			
//			cl2.toClass(cl.getClassPool().getClassLoader().getSystemClassLoader());
//			cl2.toClass(cl.getClassPool().getClassLoader().getParent());
//			cl2.toClass(cl.getClassPool().getClassLoader());
			//cl2.
//			pool.insertClassPath(new ClassClassPath(cl2.toClass()));
			pool.appendClassPath(new ClassClassPath(cl2.toClass()));
			pool.importPackage("com.spss.rest.executors.GetJSONCallTree");
			
			
			for(CtMethod method:methods){
				if(method.getName().equals("getRequestExecutors")){
					method.insertBefore("allExecutors.add(new com.spss.rest.executors.GetJSONCallTree());");
					reged = true;
					Log.info("AE attached");
				}
			}
		}catch(Exception e){
			Log.info(e.getMessage());
		}
	}

}
