# Pub-Sub-System-using-AWS

A Publisher Subscriber System is implemented using AWS.

A Pub-Sub system is implemented on local machine where client and worker both run on same machine and uses a in-memeory blocking queue to access the task/jobs.

Second Pub-Sub system uses AWS like SQS for sending and receving tasks, while it also uses DynamoDB to keep track of the task ids for duplicate execution of tasks. And EC2 are used as client and worker.

Worker threads run on different EC2 instances, and it can also run in multi-threaded environment. 

How to run code for AWS code ?

How to start SQS client?
SQS client takes two arguments (-t argument is optional here but we can provide it is just used for printing the scenerio being tested)

-s,--workerType <s> REMOTE
-w,--workerloadFile <w> pass workload file .txt with sleep task
-t,--numOfWorkerThreads <t> pass number of remote workers (optional for remote)

Here input queue name is --> input_task_sqs
While result queue name is --> result_task_sqs
dynamoDB table is --> dup_task_PA3

e.g.
java -jar client.jar -s REMOTE -t 1 -w input.txt

How to start SQS worker?
SQS worker takes two arguments as below, pass the input queue name and number of threads for the current node
-s,--queueName <s> pass Queue Name for worker
-t,--numOfWorkerThreads <t> pass number of threads for this worker node
Result queue name is --> result_task_sqs
DynamoDB table is --> dup_task_PA3

e.g.
java -jar worker.jar -s input_taks_sqs -t 1

How to generate workload file?
use taskgen.sh to generate the workload file. Edit the sleep task value and for loop value for generating required input.txt workload file.
