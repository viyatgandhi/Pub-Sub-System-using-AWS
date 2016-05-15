package pa3Worker;

/*
 * used for getting args using apache cli for client
 */

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class commandLineInterfaceWorker {

	private Options options = new Options();
	private CommandLine cmd = null;

	public commandLineInterfaceWorker(String[] args) {

		CommandLineParser parser = new DefaultParser();

		Option queueName = Option.builder("s").required(true).longOpt("queueName").hasArg().numberOfArgs(1).argName("s")
				.desc("pass Queue Name for worker").build();

		Option worker = Option.builder("t").required(true).longOpt("numOfWorkerThreads").hasArg().numberOfArgs(1).argName("t")
				.desc("pass number of threads for this worker node").build();

		options.addOption(queueName);
		options.addOption(worker);

		// if no args than print help and exit
		if (args.length == 0) {
			help();
		}

		// parse the args 
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			// e.printStackTrace();
			System.out.println("Parsing failed: " + e.getMessage());
			help();
			System.exit(0);
		}

	}

	// for any exception print help 
	public void help() {

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("worker", options);
		System.exit(0);
	}

	// get the option value
	public String getOptionValue(String option) {
		String value = cmd.getOptionValue(option);
		return value;

	}

}
