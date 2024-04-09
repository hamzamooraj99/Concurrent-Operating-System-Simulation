import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition; //Note that the 'notifyAll' method or similar polling mechanism MUST not be used

// IMPORTANT:
//
//'Thread safe' and 'synchronized' classes (e.g. those in java.util.concurrent) other than the two imported above MUST not be used.
//
//You MUST not use the keyword 'synchronized', or any other `thread safe` classes or mechanisms  
//or any delays or 'busy waiting' (spin lock) methods. Furthermore, you must not use any delays such as Thread.sleep().

//However, you may import non-tread safe classes e.g.:
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;


//Your OS class must handle exceptions locally i.e. it must not explicitly 'throw' exceptions 
//otherwise the compilation with the Test classes will fail!!!

public class OS implements OS_sim_interface {

	//Enumeration of process states - Used to allow process to pass start
	enum Status { WAITING, RUNNING, SUSPENDED, TERMINATED }
	//Private class for Processes - A Process object includes pID, condition, priority, processor, condition and status
	private class Process {
		private final int ID;
		private final int priority;
		private Processor processor;
		private Condition process;
		private Status status;

		//CONSTRUCTOR
		public Process(int pID, int priority) {
			this.ID = pID;
			this.priority = priority;
			this.processor = null;
			this.process = lock.newCondition();
			this.status = Status.WAITING;
		}

		//GETTERS
		//Get process ID
		public int getID() {
			return this.ID;
		}
		
		//Get process priority
		public int getPriority() {
			return this.priority;
		}
		
		//Get the processor processing the process
		public Processor getProcessor() {
			return this.processor;
		}
		
		//Get the process' status
		public Status getStatus() {
			return this.status;
		}

		//SETTERS
		//Set the process' status
		public void setStatus(Status s) {
			this.status = s;
		}

		//Set the processor processing the process
		public void setProcessor(Processor p) {
			this.processor = p;
		}

		//CONDITION METHODS - implemented to allow for ease of understanding
		public void holdProcess() {
			try {
				this.process.await();
			} catch (InterruptedException e) {
				System.err.println("Interrupted");
			}
		}
		public void startProcess() {
			this.process.signal();
		}
	}
	
	//Private class for Processors - A processor includes an ID, an availability flag, and a process that it is executing
	private class Processor {
		private final int processorID;
		private boolean isAvailable;
		private Process executing;
		
		private Processor(int ID) {
			this.processorID = ID;
			this.isAvailable = true;
		}
		
		//GETTERS
		@SuppressWarnings("unused")
		//Get processor ID
		private int getID() {
			return this.processorID;
		}

		@SuppressWarnings("unused")
		//Get the process being executed by processor
		private Process getProcessing() {
			return this.executing;
		}

		//Get the processor availability
		private boolean isAvailable() {
			return this.isAvailable;
		}
		
		//SETTERS
		//Set the processor to available and reset the executing process
		private void setAvailable(Process process) {
			this.isAvailable = true;
			this.executing = null;
			process.setProcessor(null);
		}

		//Set the processor to unavailable, set executing process, and assign the processor to the process
		private void setUnavailable(Process p, Processor proc) {
			this.isAvailable = false;
			this.executing = p;
			p.setProcessor(proc);
		}
	}
	//Instance Variables
	private ReentrantLock lock;
	private TreeMap<Integer, LinkedList<Process>> readyQueues; //TreeMap for a priority and the associated ready queue (linked list) - Used TreeMap in case order of HashMap is not maintained
	private HashMap<Integer, Process> generalProcessList; //HashMap for every process created using reg() in the OS - Purpose is to allow direct accessing for other methods. 
	private ArrayList<Processor> processors; //ArrayList for each processor object.
	private int nextPID = 0; //Process ID counter
	
	private ArrayList<Boolean> processorList = new ArrayList<Boolean>();

	//Constructor
	public OS() {
		this.lock = new ReentrantLock();
		this.readyQueues = new TreeMap<>();
		this.generalProcessList = new HashMap<>();
		processors = new ArrayList<>();
	}
	

	@Override
	public void set_number_of_processors(int nProcessors) {
		this.processors = new ArrayList<>();
		for(int i=0 ; i<nProcessors ; i++) {
			processors.add(new Processor(i));
			processorList.add(true);
		}
	}

	@Override
	public int reg(int priority) {
		lock.lock();
		try {
			//Defining values for process - ID, Condition, Priority
			int pID = this.nextPID;
			//Creating Process object
			Process process = new Process(pID, priority);
			
			if(!this.readyQueues.containsKey(priority))
				this.readyQueues.put(priority, new LinkedList<Process>());
			
			//Add process to generalProcessList
			this.generalProcessList.put(pID, process);
						
			//Increment variable for next process
			this.nextPID++;
			
			return pID;
		} finally {
			lock.unlock();
		}
	}


//=================================================	HELPER METHODS	================================================
	//Method to check the availability of a processor: Returns the processor if available, else returns null
	private Processor availableProcessor() {
		Processor processor = null;
		for (Processor p : this.processors) {
			if (p.isAvailable()) {
				processor = p;
				break;
			} 
		}
		return(processor);
	}


	//Private class to store information about the existence of a higher or lower process
	private class PriorityProcessInfo {
		private boolean processExists;
		private LinkedList<Process> queue;

		public PriorityProcessInfo(boolean processExists, LinkedList<Process> queue) {
			this.processExists = processExists;
			this.queue = queue;
		}

		//GETTERS
		//Does the process exist
		private boolean getProcessExists() {
			return this.processExists;
		}
		//Get the list the process is stored in.
		private LinkedList<Process> getQueue() {
			return this.queue;
		}
	}
	
	//Method to check if a process of higher priority exists
	private PriorityProcessInfo higherPriorityProcessExists(Process process) {
		int priority = process.getPriority();
		boolean higherPriorityProcessExists = false;
		LinkedList<Process> higherPriorityQueue = new LinkedList<>();
		for(int i=1 ; i<=priority ; i++) {
			if(this.readyQueues.containsKey(i) && !this.readyQueues.get(i).isEmpty()) {
				higherPriorityProcessExists = true;
				higherPriorityQueue = this.readyQueues.get(i);
				break;
			}
		}
		return(new PriorityProcessInfo(higherPriorityProcessExists, higherPriorityQueue));
	}

	//Method to check if a process of lower priority exists
	private PriorityProcessInfo lowerPriorityProcessExists(Process process) {
		ArrayList<Integer> keys = new ArrayList<>(this.readyQueues.keySet());
		int processPriority = process.getPriority();
		boolean lowerPriorityProcessExists = false;
		LinkedList<Process> lowerPriorityQueue = new LinkedList<>();
		if(processPriority != keys.get(keys.size()-1)) {
			int index = keys.indexOf(processPriority) + 1;
			int lowerPriority = keys.get(index);
			lowerPriorityQueue = this.readyQueues.get(lowerPriority);
			if(!lowerPriorityQueue.isEmpty()) {
				lowerPriorityProcessExists = true;
			} 
		} 
		return(new PriorityProcessInfo(lowerPriorityProcessExists, lowerPriorityQueue));
	}

//==================================================================================================================
	


	@Override
	public void start(int ID) {
		lock.lock();
		try {
			//Retrieve process from ID
			Process process = this.generalProcessList.get(ID);
			
			//Check for available processor
			Processor availableProcessor = availableProcessor();

			while(availableProcessor == null) {
				this.readyQueues.get(process.getPriority()).addLast(process);
				System.out.println("HOLDING " + process.getID()); //DEBUGGING Purposes
				process.holdProcess(); //No Processor available

				//Conclusion Case: Uses Process' status to break while loop and complete start()
				if(process.getStatus() == Status.RUNNING)
					return;
			}
			
			availableProcessor.setUnavailable(process, availableProcessor); 
			process.setStatus(Status.RUNNING); 
		} finally {
			lock.unlock();
			System.out.flush(); //DEBUGGING Purposes
		}
	}



	@Override
	public void schedule(int ID) {
	    lock.lock();
	    try {
	        //Retrieve process from ID
	    	Process process = this.generalProcessList.get(ID);
	    	int processPriority = process.getPriority();
	    	
			//Check for higher priority process
			PriorityProcessInfo ppInfo = higherPriorityProcessExists(process);

			//For when there is a higher priority process
			if(ppInfo.getProcessExists()) {
				//Suspend current process @process
				Processor processor = process.getProcessor();
				this.readyQueues.get(processPriority).addLast(process);
				processor.setAvailable(process);

				//Start next process @nextProcess
				Process nextProcess = ppInfo.getQueue().removeFirst();
				processor.setUnavailable(nextProcess, processor);
				nextProcess.setStatus(Status.RUNNING);
				nextProcess.startProcess();

				process.setStatus(Status.SUSPENDED);
				while (process.getStatus() == Status.SUSPENDED) {
					process.holdProcess();
				}
				
			}
		} finally {
	        lock.unlock();
	    }   
	}


	@Override
	public void terminate(int ID) {
		lock.lock();
		try {
			//Get process and processor
			Process process = this.generalProcessList.get(ID);
			Processor processor = process.getProcessor();
			
			//Remove from general process list
			this.generalProcessList.remove(process.getID());

			//Release processor 
			processor.setAvailable(process);

			//Check if process is in a Queue (It shouldn't be - This is a failsafe)
			LinkedList<Process> queue = this.readyQueues.get(process.getPriority());
			if(queue.contains(process))
				queue.remove(process);

			//Check for next highest priority process
			PriorityProcessInfo hppInfo = higherPriorityProcessExists(process);
			if(hppInfo.getProcessExists()) {
				process.setStatus(Status.TERMINATED);
				Process nextProcess = hppInfo.getQueue().removeFirst();
				processor.setUnavailable(nextProcess, processor);
				nextProcess.setStatus(Status.RUNNING);
				nextProcess.startProcess();
				return;
			} 
			
			//If there is no higher priority process, check for lower priority process
			PriorityProcessInfo lppInfo = lowerPriorityProcessExists(process);
			if(lppInfo.getProcessExists()) {
				process.setStatus(Status.TERMINATED);
				Process nextProcess = lppInfo.getQueue().removeFirst();
				processor.setUnavailable(nextProcess, processor);
				nextProcess.setStatus(Status.RUNNING);
				nextProcess.startProcess();
				return;
			}

			//If there is neither a higher nor lower priority process, this is the last lowest priority process
			process.setStatus(Status.TERMINATED);
		} finally {
			lock.unlock();
		}
	}
			
}
