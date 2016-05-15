package pa3Worker;

/*
 *  main class for worker
 *  input : input sqs queue name
 *  result queue is hard coded to result_task_sqs
 */

public class worker {

	private static String queueName;
	private static int numOfThreads;

	public static void main(String[] args) {

		commandLineInterfaceWorker cmd = new commandLineInterfaceWorker(args);

		queueName = cmd.getOptionValue("s");
		numOfThreads = Integer.parseInt(cmd.getOptionValue("t"));

		System.out.println("Input task SQS queue is: " + queueName);
		System.out.println("Number of threads are: " + numOfThreads);

		// start the number of threads as given by user for sqs worker
		for (int i = 0; i < numOfThreads; i++) {
			sqsWorker worker = new sqsWorker(queueName);
			// start the sqs worker thread
			worker.start();
		}

	}

}
