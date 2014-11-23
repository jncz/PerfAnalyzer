The application contains two parts, 1 is java agent, it is used for collecting the data from the system.
the other one is used for displaying the data in the web page.

1.Java Agent
	How to start the agent,
	1. -javaagent:d:\agent.jar
	2. -Dconfig=configpath
2.Displaying app
	How to start the displaying app
	1. start jetty/liberty/tomcat?
	2. deploy perf.war to the app container
	3. modify the config file in the perf.war/WEB-INF/classes, change the path to the data folder