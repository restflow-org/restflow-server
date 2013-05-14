package org.restflow;

import static java.util.Arrays.asList;

import java.util.Collection;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.restflow.server.ServerOptions;
import org.restflow.util.ClassPathHacker;


public class RestFlowServer {
	
	public static void main(String[] args) throws Exception {
		
		OptionParser parser = createOptionParser();

		OptionSet options = parser.parse(args);

		if (options.has("h")) {
			parser.printHelpOn(System.out);
			return;
		}
				
		if (options.has("cp")) {
			Collection<?> fileNames = options.valuesOf("cp");
			for(Object file : fileNames ) {
			  ClassPathHacker.addFile((String)file);
			}
		}
		
		ServerOptions.handleServerOptions(args, options);
	}
	
	private static OptionParser createOptionParser() {
		
		OptionParser parser = null;
		
		try {
			parser = new OptionParser() {
			{
				acceptsAll(asList("h", "?"), "show help");
				acceptsAll(asList("s", "server"), "start as server")
						.withOptionalArg().describedAs("listen port")
						.ofType(Integer.class).defaultsTo(0);
				acceptsAll(asList("c", "client"), "start as client")
						.withOptionalArg().describedAs("remote port")
						.ofType(Integer.class).defaultsTo(0);
				acceptsAll(asList("server-loop"),
						"number of clients to be served before exit.")
						.withRequiredArg().describedAs("num")
						.ofType(Integer.class);
				acceptsAll(asList("server-name"), "server to connect to")
						.withOptionalArg().describedAs("hostname")
						.ofType(String.class).defaultsTo("localhost");
				acceptsAll(asList("server-stop"), "stop RestFlow server");
				acceptsAll(asList("server-restart"), "restart RestFlow server");
				acceptsAll(asList("server-restart-name"), "restart RestFlow server")
						.withOptionalArg().describedAs("RestFlow executable").defaultsTo("RestFlow")
				        .ofType(String.class);
				acceptsAll(
						asList("server-secret"),
						"Simple challenge-response secret. If no secret is specified, a random number is used.")
						.withOptionalArg().describedAs("secret")
						.ofType(Integer.class);
				acceptsAll(asList("server-idle-timeout"),
						"The server will terminate itself if not used by a client for this time span.")
						.withOptionalArg().describedAs("seconds")
						.ofType(Integer.class).defaultsTo(60 * 60);
				acceptsAll(asList("server-shell"), "format for environmental variables")
				        .withOptionalArg().describedAs("BASH|TCSH|WIN_CMD").defaultsTo("BASH")
				        .ofType(String.class);
				acceptsAll(asList("cp"), "add to classpath")
				        .withRequiredArg().describedAs("jar|directory")
				        .ofType(String.class);
				}
			};
			
		} catch (OptionException e) {
			System.err.print("Option contains illegal character: ");
			System.err.println(e.getLocalizedMessage());
			System.exit(0);
		}
			
		return parser;
	}
}
