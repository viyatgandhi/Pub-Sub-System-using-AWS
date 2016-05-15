package pa3Worker;

/*
 * SQS worker thread - we need to pass input sqs queue name
 * result queue is hard-coded to result_task_sqs 
 * 
 */

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/*
 * http://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_ReceiveMessage.html
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonSimpleQueueService/SimpleQueueServiceSample.java
 * http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSGettingStartedGuide/ReceiveMessage.html
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/sqs/AmazonSQSClient.html
 */

public class sqsWorker extends Thread {

	protected String inputSQS;
	protected String resultSQS = "result_task_sqs";
	private static AmazonSQS sqs;
	// private static AmazonSQS resultsqs;
	static AWSCredentials credentials = null;
	// max idle time as 2 minutes
	private static int maxIdleTime = 300000;

	public sqsWorker(String queueName) {
		this.inputSQS = queueName;
	}

	public void run() {

		long idleStart = System.currentTimeMillis();
		Region region = Region.getRegion(Regions.US_EAST_1);

		credentials = new ProfileCredentialsProvider("default").getCredentials();
		sqs = new AmazonSQSClient(credentials);
		sqs.setRegion(region);

		System.out.println(Thread.currentThread().getName() + " remote worker thread started");

		// get tasks and result Queue URLs
		String inputSQSURL = sqs.createQueue(inputSQS).getQueueUrl();
		String resultSQSURL = sqs.createQueue(resultSQS).getQueueUrl();

		System.out.println("Input task SQS URL is: " + inputSQSURL);

		System.out.println("Result task SQS URL is: " + resultSQSURL);

		// checking DB table
		dynamoDBTable.createTable();

		System.out.println("Started polling input SQS queue...");
		System.out.println("And exeuting and sending back to resultQ...");

		Task t;

		while ((System.currentTimeMillis() - idleStart) < maxIdleTime) {

			try {

				ReceiveMessageRequest request = new ReceiveMessageRequest().withQueueUrl(inputSQSURL);
				int waitTimeSeconds = 1;
				int maxNumberOfMessages = 1;
				// setting message as 1 as only one message per thread
				request.withWaitTimeSeconds(waitTimeSeconds).setMaxNumberOfMessages(maxNumberOfMessages);

				List<Message> messages = sqs.receiveMessage(request).getMessages();

				for (Message message : messages) {

					if (!messages.isEmpty()) {

						String messageReceiptHandle = message.getReceiptHandle();
						String msg = message.getBody();
						t = new Task(msg);

						// if task added successfully than it is executed by
						// thread else prints error msg and proceed to next task
						// as task_id is the primary key for jobs dynamoDB will have unique values
						boolean runTask = dynamoDBTable.addTask(t.getId());

						if (runTask) {
							sqs.deleteMessage(new DeleteMessageRequest(inputSQSURL, messageReceiptHandle));
							t.run();
							// update the task status in DB
							dynamoDBTable.updateTask(t.getId(), t.getStatus());
							// add result back to result queue
							sqs.sendMessage(resultSQSURL, t.toString());
							idleStart = System.currentTimeMillis();
						} else {
							System.out.println("Task already picked up by other thread, task id: " + t.getId());
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
