
import java.util.concurrent.BlockingQueue;

public class localWorker extends Thread {

	protected BlockingQueue<Task> inputQ;
	protected BlockingQueue<Task> resultQ;
	protected boolean shutdown = false;

	public localWorker(BlockingQueue<Task> queue, BlockingQueue<Task> result) {
		this.inputQ = queue;
		this.resultQ = result;
	}


	public void run() {

		try {

			System.out.println(Thread.currentThread().getName() + " worker started");
			Task t = null;



			while (!shutdown) {

				t = inputQ.take();

				// if poison pill found kill the thread
				if (t.getId() == -1) {
					this.kill();
					break;
				}
				//System.out.println(Thread.currentThread().getName() + " consumer task id running is: " + t.getId());
				// String[] line = t.getArgs();
				// System.out.print(" Task command: "+line[0]);
				// System.out.print(" till: "+line[1]);
				// System.out.print(" Completed or not: "+t.getStatus());

				// run the task
				t.run();

				// System.out.print(" NOW Status: "+t.getStatus()+"\n");

				// add the task in result queue with modified status
				resultQ.add(t);


			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void kill() {
		shutdown = true;
		System.out.println(Thread.currentThread().getName() + " worker stopped");
		interrupt();
	}
}
