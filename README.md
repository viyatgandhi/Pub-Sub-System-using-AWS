# Pub-Sub-System-using-AWS

A Publisher Subscriber System is implemented using AWS.

A Pub-Sub system is implemented on local machine where client and worker both run on same machine and uses a in-memeory blocking queue to access the task/jobs.

Second Pub-Sub system uses AWS like SQS for sending and receving tasks, while it also uses DynamoDB to keep track of the task ids for duplicate execution of tasks. And EC2 are used as client and worker.

Worker threads run on different EC2 instances, and it can also run in multi-threaded environment.
