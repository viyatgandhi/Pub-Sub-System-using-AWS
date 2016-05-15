package pa3Client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/*
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonSimpleQueueService/SimpleQueueServiceSample.java
 * https://www.javacodegeeks.com/2013/06/working-with-amazon-simple-queue-service-using-java.html
 * http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSGettingStartedGuide/SendMessage.html
 * http://www.programcreek.com/java-api-examples/index.php?api=com.amazonaws.services.sqs.model.GetQueueAttributesRequest
 */

public class sqsClient extends Thread {

	protected BlockingQueue<Task> inputQ;
	protected String workloadFile;
	protected String inputSQS = "input_task_sqs";
	private static AmazonSQS inputsqs;
	private static AmazonSQS resultsqs;
	protected String resultSQS = "result_task_sqs";
	static AWSCredentials credentials = null;

	public sqsClient(BlockingQueue<Task> queue, String workloadfile) {
		this.inputQ = queue;
		this.workloadFile = workloadfile;
	}

	public void run() {

		long startTime, finishTime, elapsedTime;

		BufferedReader br = null;
		System.out.println(Thread.currentThread().getName() + " remote client started");

		startTime = System.currentTimeMillis();

		try {

			String line;
			String[] args;

			br = new BufferedReader(new FileReader(workloadFile));
			int i = 1;

			// adding tasks to the input queue
			while ((line = br.readLine()) != null) {
				args = line.split(" ");
				Task task = new Task(i, args);
				inputQ.add(task);
				i++;
			}

			// create table in dynamoDB
			dynamoDBTable.createTable();

			Region region = Region.getRegion(Regions.US_EAST_1);

			// setting credentials for input sqs
			credentials = new ProfileCredentialsProvider("default").getCredentials();
			inputsqs = new AmazonSQSClient(credentials);
			inputsqs.setRegion(region);

			CreateQueueResult createInputQueueResult = inputsqs.createQueue(inputSQS);
			String inputSQSURL = createInputQueueResult.getQueueUrl();
			System.out.println("Input task SQS URL is: " + inputSQSURL);

			// add tasks to SQS queue
			Task t = null;
			while (!inputQ.isEmpty()) {
				try {
					t = inputQ.take();
					inputsqs.sendMessage(inputSQSURL, t.toString());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Task added to queue are: " + --i);

			System.out.println("Now polling for results in resultQ...");

			// checking result queue
			int total_tasks_done = 0;
			int pass_tasks_done = 0;
			int fail_tasks_done = 0;

			/*
			 * GetQueueAttributesResult result =
			 * resultsqs.getQueueAttributes(new GetQueueAttributesRequest()
			 * .withQueueUrl(resultSQSURL).withAttributeNames(
			 * "ApproximateNumberOfMessages"));
			 * 
			 * while (total_tasks_done < i) { total_tasks_done =
			 * Integer.valueOf(result.getAttributes().get(
			 * "ApproximateNumberOfMessages")); System.out.println(
			 * "Total task received are:" + total_tasks_done); try {
			 * Thread.sleep(1000); } catch (InterruptedException e) {
			 * e.printStackTrace(); } }
			 */

			// total_tasks_done = 0;

			// setting credentials for result sqs
			credentials = new ProfileCredentialsProvider("default").getCredentials();
			resultsqs = new AmazonSQSClient(credentials);
			resultsqs.setRegion(region);

			CreateQueueResult createResultQueueResult = resultsqs.createQueue(resultSQS);
			String resultSQSURL = createResultQueueResult.getQueueUrl();
			System.out.println("Result task SQS URL is: " + resultSQSURL);

			
			// polling result queue for answers
			Task tR;

			while (total_tasks_done < i) {

				try {

					ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(resultSQSURL);
					int waitTimeSeconds = 1;
					int maxNumberOfMessages = 10;
					request.withWaitTimeSeconds(waitTimeSeconds).setMaxNumberOfMessages(maxNumberOfMessages);

					List<Message> messages = resultsqs.receiveMessage(request).getMessages();

					if (!messages.isEmpty()) {
						for (Message msg : messages) {
							resultsqs.deleteMessage(new DeleteMessageRequest(resultSQSURL, msg.getReceiptHandle()));
							String resultMsg = messages.get(0).getBody();
							tR = new Task(resultMsg);
							int status = tR.getStatus();
							if (status == 0) {
								pass_tasks_done++;
							} else {
								fail_tasks_done++;
							}
							total_tasks_done++;
						}
					}

					if (total_tasks_done % 1000 == 0) {
						System.out.println("Total tasks received are: " + total_tasks_done);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			finishTime = System.currentTimeMillis();
			elapsedTime = finishTime - startTime;
			float elapsedTimeSeconds = (float) (elapsedTime / 1000.0);

			System.out.println("All tasks completed: " + total_tasks_done);
			System.out.println("Total time taken is: " + elapsedTimeSeconds + " seconds");
			System.out.println("Passed tasks are: " + pass_tasks_done);
			System.out.println("Failed tasks are: " + fail_tasks_done);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
