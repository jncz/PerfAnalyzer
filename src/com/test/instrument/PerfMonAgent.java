package com.test.instrument;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;

import com.test.instrument.util.Util;

public class PerfMonAgent {
    private static final String AGENT_HOME = "agenthome";
	static private Instrumentation inst = null;
    /**
     * This method is called before the application�s main-method is called,
     * when this agent is specified to the Java VM.
     **/
    public static void premain(String agentArgs, Instrumentation _inst) {
    	systemEnvCheck();
        Log.info("PerfMonAgent.premain() was called.");
        // Initialize the static variables we use to track information.
        inst = _inst;
        // Set up the class-file transformer.
        ClassFileTransformer trans = new CallTreePerfMonXformer();
        Log.info("Adding a PerfMonXformer instance to the JVM.");
        inst.addTransformer(trans);
    }
	private static void systemEnvCheck() {
		String agenthome = System.getProperty(AGENT_HOME);
		if(Util.isEmptyString(agenthome)){
			Log.error("agenthome should be set in your evironment, or you -Dagenthome for your jvm parameter");
			System.exit(1);
		}
	}
}