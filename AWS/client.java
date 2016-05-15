package pa3Client;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * References
 * http://codereview.stackexchange.com/questions/68154/producer-consumer-program
 * http://stackoverflow.com/questions/22162738/how-to-stop-consumer-thread-based-on-producer-thread-state
 * http://www.drdobbs.com/parallel/java-concurrency-queue-processing-part-2/232900063
 * http://stackoverflow.com/questions/8974638/blocking-queue-and-multi-threaded-consumer-how-to-know-when-to-stop
 * https://examples.javacodegeeks.com/core-java/util/concurrent/java-blockingqueue-example/
 * http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
 */

public class client {

	private static BlockingQueue<Task> inputQ;
	private static BlockingQueue<Task> resultQ;

	private static String workerType;
	private static String workloadFile;
	private static int numOfThreads;
	private static int numOfWorkers;

	public static void main(String[] args) {
		
		// interceptArgs(args);
		// String workloadFile = "C:\\Users\\viyat\\Desktop\\PA3\\input.txt";
		
		commandLineInterfaceClient cmd = new commandLineInterfaceClient(args);
		
		workerType = cmd.getOptionValue("s");
		workloadFile= cmd.getOptionValue("w");
		
		if(workerType.equals("LOCAL")){
			System.out.println("worker type is: "+workerType);
			System.out.println("work load file is: "+workloadFile);
			numOfThreads= Integer.parseInt(cmd.getOptionValue("t"));
			System.out.println("Number of threads are: "+numOfThreads);
		} else if (workerType.equals("REMOTE")){
			System.out.println("worker type is: "+workerType);
			System.out.println("work load file is: "+workloadFile);
			numOfWorkers= Integer.parseInt(cmd.getOptionValue("t"));
			System.out.println("Number of remote workers are: "+numOfWorkers);
		}

		switch(workerType){
		case "LOCAL":

			inputQ = new LinkedBlockingQueue<Task>();
			resultQ = new LinkedBlockingQueue<Task>();
			// start client with all required parameter
			//localClient client = new localClient(inputQ,workloadFile,resultQ,numOfThreads);
			// start the client thread
			//new Thread(client).start();

			// start the number of threads as given by user
			for (int i = 0; i < numOfThreads; i++) {
				//localWorker worker = new localWorker(inputQ, resultQ);
				// start the worker thread
				//worker.start();
			}

			break;

		case "REMOTE":

			 inputQ = new LinkedBlockingQueue<Task>();

			 // start the SQS client with input queue provided and workload file
			 sqsClient remoteClient = new sqsClient(inputQ,workloadFile);
			 new Thread(remoteClient).start();

			 break;

		}

	}

	private static void interceptArgs(String[] args) {

		if (args.length < 6) {
			System.out.println("Please provide proper input with -s -w and -t attributes");
			System.out.println("-s with LOCAL or REMOTE");
			System.out.println("-w with workloadfile contaning sleep tasks");
			System.out.println("-t with number of worker thread");
			System.exit(1);
		}

		if (!("-s".equals(args[0]) && "-w".equals(args[2]) && "-t".equals(args[4]))) {
			System.out.println("Please provide proper input with -s -w and -t attributes");
			System.out.println("For e.g. client -s LOCAL -w input.txt -t 3");
			System.out.println("OR client -s REMOTE -w input.txt -t 3");
			System.out.println("-s with LOCAL or REMOTE");
			System.out.println("-w with workloadfile path contaning sleep tasks");
			System.out.println("-t with number of worker thread");
			System.exit(1);
		} else if ("-s".equals(args[0]) && "-w".equals(args[2]) && "-t".equals(args[4])) {

			if (!("LOCAL".equals(args[1]) || "REMOTE".equals(args[1]))) {
				System.out.println("Please provide proper input with -s attribute : LOCAL or REMOTE");
				System.out.println("For e.g. client -s LOCAL -w input.txt -t 3");
				System.out.println("OR client -s REMOTE -w input.txt -t 3");
				System.out.println("-s with LOCAL or REMOTE");
				System.out.println("-w with workloadfile path contaning sleep tasks");
				System.out.println("-t with number of worker thread");
				System.exit(1);
			} else {
				workerType = args[1];
				if (workerType.equals("LOCAL")) {
					workloadFile = args[3];
					numOfThreads = Integer.parseInt(args[5]);

					System.out.println("Worker Type is: " + workerType);
					System.out.println("Workload File path is: " + workloadFile);
					System.out.println("Number of threads for local worker is: " + numOfThreads);

				} else if (workerType.equals("REMOTE")) {
					workloadFile = args[3];
					numOfWorkers = Integer.parseInt(args[5]);

					System.out.println("Worker Type is: " + workerType);
					System.out.println("Workload File path is: " + workloadFile);
					System.out.println("Number of remote workers are: " + numOfWorkers);
				}
			}
		} else {
			System.out.println("Something is still not right, please provide proper input");
			System.out.println("Please provide proper input with -s -w and -t attributes");
			System.out.println("-s with LOCAL or REMOTE");
			System.out.println("-w with workloadfile path contaning sleep tasks");
			System.out.println("-t with number of worker thread");
			System.exit(1);
		}

	}

}
