
import java.util.Arrays;

public class Task implements Runnable {

	private int id;
	private String[] args;
	private int status;

	private void init(int id, String[] args) {
		this.id = id;
		this.args = args;
	}

	public Task(int id, String[] args) {
		init(id, args);
		status = -1;
	}

	public Task(String msg) {
		String split[] = msg.split(" ");
		int id = Integer.parseInt(split[0]);
		String[] args = Arrays.copyOfRange(split, 1, split.length);
		init(id, args);
	}

	public int getId() {
		return id;
	}

	public String[] getArgs() {
		return args;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Integer.toString(id));
		for (String arg : args) {
			sb.append(" " + arg);
		}
		return sb.toString();
	}

	@Override
	public void run() {
		try {
			//System.out.print(" Inside Run of Task");
			if ("sleep".equals(args[0])) {
				Thread.sleep(Integer.parseInt(args[1]));
				status = 0;
			} else {
				// System.out.print("Inside else task");
				status = 1;
			}
		} catch (Exception e) {
			// System.out.print("Inside exception");
			status = 1;
		}
	}
}
