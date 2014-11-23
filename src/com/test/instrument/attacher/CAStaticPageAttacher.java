package com.test.instrument.attacher;

import com.test.instrument.util.Log;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class CAStaticPageAttacher implements Attacher {

	private boolean perfPageReged = false;

	@Override
	public String attachPoint() {
		return "com.spss.ca.jetty.JettyResourceRegistratModule";
	}

	@Override
	public void attach(ClassPool pool, CtClass cl) {
		if(perfPageReged || !cl.getName().equals(attachPoint())){
			return;
		}
		try {
			CtMethod[] ms = cl.getMethods();
			for (CtMethod m : ms) {
				if (m.getName().equals("startJetty")) {
					m.instrument(new ExprEditor() {
						public void edit(MethodCall mc)
								throws CannotCompileException {
							if (mc.getClassName().equals(
									"org.eclipse.jetty.server.Server")
									&& mc.getMethodName().equals("setHandler")) {
								mc.replace("String root = System.getProperty(\"agenthome\");"
										+ "org.eclipse.jetty.server.handler.ResourceHandler perf = create(root+\"/webclient\", false, null); "
										+ "org.eclipse.jetty.server.handler.ContextHandler perfCtx = createContextHandler(\"/perf\", root+\"/webclient\", perf);"
										+ "contexts.addHandler(perfCtx);"
										+ "$_ = $proceed($$);");
							}
						}
					});
					perfPageReged = true;
					Log.info("CAP attached");
				}
			}
		} catch (Exception e) {
			Log.info(e.getMessage());
		}

	}

}
