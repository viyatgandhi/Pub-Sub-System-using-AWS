package pa3Client;

/*
 *  dynamoDB Table class
 */

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/*
 * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/AppendixSampleDataCodeJava.html
 * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaDocumentAPITablesExample.html
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/dynamodbv2/util/TableUtils.html
 * https://java.awsblog.com/blog/tag/DynamoDB
 * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/JavaDocumentAPICRUDExample.html
 * 
 */

public class dynamoDBTable {

	private static AmazonDynamoDBClient dynamoDB;
	private static String tableName = "dup_task_PA3";
	static AWSCredentials credentials = null;

	public static void init() {

		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
			dynamoDB = new AmazonDynamoDBClient(credentials);
			Region region = Region.getRegion(Regions.US_EAST_1);
			dynamoDB.setRegion(region);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void createTable() {
		
		// intit for setting all the parameters
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			// create table request with 25L capacity initially 
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
					.withKeySchema(new KeySchemaElement().withAttributeName("task_id").withKeyType(KeyType.HASH))
					.withAttributeDefinitions(new AttributeDefinition().withAttributeName("task_id")
							.withAttributeType(ScalarAttributeType.N))
					.withProvisionedThroughput(
							new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(25L));

			// dynamoDB.createTable(createTableRequest);
			
			// using latest TableUtils API for table creation for dynamoDB
			if (TableUtils.createTableIfNotExists(dynamoDB, createTableRequest)) {
				try {
					System.out.println("Waiting for table to become active");
					TableUtils.waitUntilActive(dynamoDB, tableName);
					System.out.println("Table created: " + tableName);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Table is already active: " + tableName);
			}
			
			// getting the status of the table
			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
			TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();

			System.out.println("Table status is: " + tableDescription.getTableStatus());

		} catch (AmazonClientException e) {
			e.printStackTrace();
		}
	}

	public static boolean addTask(int i) {

		// while adding set task status as -1
		int status = -1;

		try {

			// System.out.println("adding to DB: "+i);

			Map<String, AttributeValue> task = new HashMap<String, AttributeValue>();
			task.put("task_id", new AttributeValue().withN(Integer.toString(i)));
			task.put("task_status", new AttributeValue().withN(Integer.toString(status)));

			Map<String, ExpectedAttributeValue> ev = new HashMap<String, ExpectedAttributeValue>();
			ev.put("task_id", new ExpectedAttributeValue(false));
			
			// using put item request API to add the task to the DB
			// conditional expression is used if id is already inserted than it will throw exception 
			PutItemRequest putItemRequest = new PutItemRequest().withTableName(tableName).withItem(task)
					.withExpected(ev);

			try {
				dynamoDB.putItem(putItemRequest);
				// System.out.println("added");
				return true;
			} catch (ConditionalCheckFailedException e) {
				// System.out.println("in error");
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void updateTask(int i, int status) {

		try {

			Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
			key.put("task_id", new AttributeValue().withN(Integer.toString(i)));
			
			// update status using update item request API according to status 0 or 1
			UpdateItemRequest updateItemRequest = new UpdateItemRequest().withTableName(tableName).withKey(key)
					.addAttributeUpdatesEntry("task_status",
							new AttributeValueUpdate().withValue(new AttributeValue().withN(Integer.toString(status)))
									.withAction(AttributeAction.PUT));

			try {
				dynamoDB.updateItem(updateItemRequest);
			} catch (ConditionalCheckFailedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
