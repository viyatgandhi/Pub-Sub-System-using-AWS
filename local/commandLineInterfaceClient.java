
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/*
 * References
 * https://commons.apache.org/proper/commons-cli/usage.html
 * https://commons.apache.org/proper/commons-cli/javadocs/api-release/index.html
 * https://commons.apache.org/proper/commons-cli/download_cli.cgi
 * http://www.thinkplexx.com/blog/simple-apache-commons-cli-example-java-command-line-arguments-parsing
 * https://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/Option.Builder.html
 * http://www.programcreek.com/java-api-examples/org.apache.commons.cli.CommandLineParser
 */

public class commandLineInterfaceClient {

	private Options options = new Options();
	private CommandLine cmd = null;

	public commandLineInterfaceClient(String[] args) {

		CommandLineParser parser = new DefaultParser();

		Option workerType = Option.builder("s").required(true).longOpt("workerType").hasArg().numberOfArgs(1)
				.argName("s").desc("pass Queue Name for LOCAL or pass REMOTE for SQS worker").build();

		Option workloadFile = Option.builder("w").required(true).longOpt("workerloadFile").hasArg().numberOfArgs(1)
				.argName("w").desc("pass workload file .txt with sleep task").build();

		Option worker = Option.builder("t").longOpt("numOfWorkerThreads").hasArg().numberOfArgs(1).argName("t")
				.desc("pass number of threads for LOCAL or pass number of remote workers for REMOTE").build();

		options.addOption(workerType);
		options.addOption(workloadFile);
		options.addOption(worker);

		if (args.length == 0) {
			help();
		}

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			// e.printStackTrace();
			System.out.println("Parsing failed: " + e.getMessage());
			help();
			System.exit(0);
		}

	}

	public void help() {

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("client", options);
		System.exit(0);
	}

	public String getOptionValue(String option) {

		String value = cmd.getOptionValue(option);

		return value;

	}

}
