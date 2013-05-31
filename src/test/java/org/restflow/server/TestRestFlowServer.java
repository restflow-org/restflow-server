package org.restflow.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.restflow.TestRestFlow;
import org.restflow.util.PortableIO;
import org.springframework.util.Assert;

public class TestRestFlowServer extends TestRestFlow {

	public static String RestFlowServerInvocationCommand = "java -classpath target/classes" +
			  System.getProperty("path.separator") + "target/test-classes" +
			  System.getProperty("path.separator") + 
			  "target/dependency/* org.restflow.RestFlowServer";
	
	static public class Server {
		
		int port = 0;
		int magic = 0;
		public int getPort() { return port; }
		public int getMagic() { return magic; }
		Process p;
		
		public Server(String[] cmd) throws IOException {
			System.out.println("Executing: " + Arrays.toString(cmd));
			p = Runtime.getRuntime().exec(cmd);
			init(p);
		}
	
		public Server(String cmd) throws IOException {
			System.out.println("Executing: " + cmd);
			p = Runtime.getRuntime().exec(cmd);
			init(p);
		}
		
		void init(Process p) throws IOException {
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Assert.notNull(in, "Errors during executing the process");
			
			String line;
			do {
				line = in.readLine();
				Assert.notNull(line, "Cannot read output from process");
				if (line.matches("export RESTFLOW_SERVER_PORT=.*")) {
					String s = line.replaceFirst("export RESTFLOW_SERVER_PORT=", "");
					port = Integer.parseInt(s);
				}
				if (line.matches("export RESTFLOW_SECRET=.*")) {
					String s = line.replaceFirst("export RESTFLOW_SECRET=", "");
					magic = Integer.parseInt(s);
				}
			} while (! line.equals("DONE"));
			
			assertTrue(port != 0);
			assertTrue(magic != 0);
			
		}
		
		public void stop() throws IOException, InterruptedException {
			verifyRunRegexp(RestFlowServerInvocationCommand + " -c " + port + " --server-secret " + magic +
					" --server-stop", 
					"",
					"",
					"");
			PortableIO.StreamSink stdOut = new PortableIO.StreamSink(p.getInputStream());
			stdOut.run();
			
			PortableIO.StreamSink stdErr = new PortableIO.StreamSink(p.getErrorStream());
			stdErr.run();
			
			System.out.println("SERVER-OUT:" + EOL + stdOut.toString());
			System.out.println("SERVER-Err:" + EOL + stdOut.toString());
			
			p.getOutputStream().close();
		}
	}

		
	public void testServerShellsAndWatchDog() throws IOException, InterruptedException {
		verifyRunRegexp(RestFlowServerInvocationCommand + " -s --server-idle-timeout 1",
				  "",
				  ".*export RESTFLOW_SERVER_PORT=.*DONE.*Server stopped due to watch-dog timeout.*",
				  "");

		verifyRunRegexp(RestFlowServerInvocationCommand + " -s --server-idle-timeout 1 --server-shell TCSH",
				  "",
				  ".*setenv RESTFLOW_SERVER_PORT .*DONE.*Server stopped due to watch-dog timeout.*",
				  "");

		verifyRunRegexp(RestFlowServerInvocationCommand + " -s --server-idle-timeout 1 --server-shell WIN_CMD",
				  "",
				  ".*SET RESTFLOW_SERVER_PORT=.*DONE.*Server stopped due to watch-dog timeout.*",
				  "");
	}
	
	public void testStartStopServer() throws IOException, InterruptedException {
		// Test if start-stop gets the same port, ie if the first start releases the port correctly
		int port;
		{ Server s = new Server(RestFlowServerInvocationCommand + " -s --server-secret");
		  port = s.getPort();
		  System.out.println(s.getPort());
		  s.stop();
		}
		{
		  Server s = new Server(RestFlowServerInvocationCommand + " -s --server-secret");
		  assertEquals(port, s.getPort());
		  System.out.println(s.getPort());
		  s.stop();
		}
	}

	public void testServerExecution() throws IOException, InterruptedException {
		Server s = new Server(RestFlowServerInvocationCommand + " -s --server-secret");
		
		String saveExec = RestFlowServerInvocationCommand;
		RestFlowServerInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		
		{
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
			testClassPath();
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			testHelloWorldInputFromStdin();
			testHelp_OutputDetails();
			testWorkflowInputs();
			testTrace();
			testWrongOption();
		}
		
		RestFlowServerInvocationCommand = saveExec;
		s.stop();
	}
	
	public void testServerRegression() throws IOException, InterruptedException {
		Server s = new Server(RestFlowServerInvocationCommand + " -s --server-secret");
		
		String saveExec = RestFlowServerInvocationCommand;
		RestFlowServerInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		testClassPath();
		for(int i = 0; i < 10; ++i) {
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			testHelloWorldInputFromStdin();
			testHelp_OutputDetails();
			testWorkflowInputs();
			testTrace();
			testWrongOption();
		}
		
		RestFlowServerInvocationCommand = saveExec;
		s.stop();
	}

	public void testServerRestartExecution() throws IOException, InterruptedException {
		String[] cmd_exec = RestFlowServerInvocationCommand.replaceAll("([^\\\\]) ", "$1__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__").split("__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__");
		ArrayList<String> serverCommandLine = new ArrayList<String>();
		serverCommandLine.addAll(java.util.Arrays.asList(cmd_exec));
		serverCommandLine.addAll(java.util.Arrays.asList(
				"-s", "--server-secret", "--server-restart-name"));
		serverCommandLine.add(RestFlowServerInvocationCommand);

		String[] asAStringArray = new String[0];
		Server s = new Server(serverCommandLine.toArray(asAStringArray));

		String saveExec = RestFlowServerInvocationCommand;
		RestFlowServerInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		for(int i = 0; i < 4; ++i) {
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
			testClassPath();
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			verifyRunRegexp(RestFlowServerInvocationCommand + " --server-restart",
					"",
					".*Starting new Restflow server.*",
					"");
			Thread.sleep(300);
		}
		
		RestFlowServerInvocationCommand = saveExec;
		s.stop();
	}
	
	public void testHelp_OutputDetails() throws IOException, InterruptedException {
		verifyRunExact(RestFlowServerInvocationCommand + " -h",
			"", 
				"Option                                  Description                            " + EOL +
				"------                                  -----------                            " + EOL +
				"-?, -h                                  show help                              " + EOL +
				"-c, --client [Integer: remote port]     start as client (default: 0)           " + EOL +
				"--cp <jar|directory>                    add to classpath                       " + EOL +
				"-s, --server [Integer: listen port]     start as server (default: 0)           " + EOL +
				"--server-idle-timeout [Integer:         The server will terminate itself if    " + EOL +
				"  seconds]                                not used by a client for this time   " + EOL +
				"                                          span. (default: 3600)                " + EOL +
				"--server-loop <Integer: num>            number of clients to be served before  " + EOL +
				"                                          exit.                                " + EOL +
				"--server-name [hostname]                server to connect to (default:         " + EOL +
				"                                          localhost)                           " + EOL +
				"--server-restart                        restart RestFlow server                " + EOL +
				"--server-restart-name [RestFlow         restart RestFlow server (default:      " + EOL +
				"  executable]                             RestFlow)                            " + EOL +
				"--server-secret [Integer: secret]       Simple challenge-response secret. If   " + EOL +
				"                                          no secret is specified, a random     " + EOL +
				"                                          number is used.                      " + EOL +
				"--server-shell [BASH|TCSH|WIN_CMD]      format for environmental variables     " + EOL +
				"                                          (default: BASH)                      " + EOL +
				"--server-stop                           stop RestFlow server                   " + EOL,
			"");
	}
}
