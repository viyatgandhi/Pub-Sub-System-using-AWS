
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class localClient extends Thread {

	protected BlockingQueue<Task> inputQ;
	protected BlockingQueue<Task> resultQ;
	protected String workloadFile;
	protected int numOfThreads;

	public localClient(BlockingQueue<Task> queue, String workloadfile, BlockingQueue<Task> result, int threads) {
		this.inputQ = queue;
		this.workloadFile = workloadfile;
		this.resultQ = result;
		this.numOfThreads = threads;
	}

	public void run() {

		BufferedReader br = null;
		System.out.println(Thread.currentThread().getName() + " client started");
		
		long startTime, finishTime, elapsedTime;
		
		startTime = System.currentTimeMillis();

		try {

			String line;
			String[] args;

			br = new BufferedReader(new FileReader(workloadFile));
			int i = 1;

			// adding tasks to the input queue
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				args = line.split(" ");
				Task task = new Task(i, args);
				inputQ.add(task);
				i++;
			}

			// creating poison pill
			String[] end = new String[1];
			end[0] = "exit -1";

			// adding poison pill for each thread
			for (int j = 0; j < numOfThreads; j++) {
				inputQ.add(new Task(-1, end));
			}

			System.out.println("-----------Tasks added in Q are: " + --i);


			// checking result queue
			int total_tasks_done = 0;
			int pass_tasks_done = 0;
			int fail_tasks_done = 0;

			System.out.println("Polling resultQ from Client......");

			while (total_tasks_done < i) {
				try {
					Task t = (Task) resultQ.take();
					//System.out.println("Producer task id is: " + t.getId() +" result is: " + t.getResult());
					if (t.getStatus() == 0) {
						pass_tasks_done++;
						total_tasks_done++;
					} else if (t.getStatus()==1) {
						fail_tasks_done++;
						total_tasks_done++;
					}

					if ( total_tasks_done % 50000 == 0 ){
						System.out.println("Completed tasks recevied by client are: " +total_tasks_done);
						System.out.println("Total tasks left in input queue are: " +inputQ.size());
					}


				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.out.println("task done are "+total_tasks_done);

			}
			
			finishTime = System.currentTimeMillis();
			elapsedTime = finishTime - startTime;
			float elapsedTimeSeconds = (float) (elapsedTime / 1000.0);

			if (total_tasks_done == i) {
				System.out.println("All tasks are completed by workers");
				System.out.println("Total Task done are: " + total_tasks_done);
				System.out.println("Passed task are: " + pass_tasks_done);
				System.out.println("Failed task are: " + fail_tasks_done);
				System.out.println("Total time taken is: "+ elapsedTimeSeconds + " seconds");
				this.shutdown();
			} else {
				System.out.println("Total Task Done by worker are: " + total_tasks_done);
				System.out.println("Pass task are: " + pass_tasks_done);
				System.out.println("Failed task are: " + fail_tasks_done);
				System.out.println("Total time taken is: " + elapsedTimeSeconds + " seconds");
				this.shutdown();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void shutdown() {
		System.out.println(Thread.currentThread().getName() + " - Client Stopped");
		interrupt();
	}
}
