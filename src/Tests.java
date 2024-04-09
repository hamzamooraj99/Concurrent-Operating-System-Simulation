import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Tests {

	public void ur1_example_test(){
		System.out.println("\n\n\n*********** UR1 *************");
		
		//Instantiate OS simulation for single processor
		OS os =  new OS();	
		os.set_number_of_processors(1);		
		boolean successful_test = true;
		for (int expected_pid=0; expected_pid<4; expected_pid++) {
			int pid = os.reg(1);
			System.out.println("pid returned = " + pid + ", pid expected = "  + expected_pid);
			if (pid != expected_pid) successful_test = false;
		}
		if (successful_test) System.out.println("\nUR1: Passed");
		else System.out.println("\nUR1: Fail!");
	}	
	
	
	int  test_timeout = 300; //Timeout 	
	
	//Declare global list of events for ease of access (yup, lazy, not best practice)
	ConcurrentLinkedQueue<String> events;		
	
	//Declare sleep with local exception handling:
	void sleep (int delay) {try {Thread.sleep(delay);} catch (InterruptedException e) {e.printStackTrace();} }
	
	//Define process simulation threads:
	class ProcessSimThread2 extends Thread {
		int pid = -1;
		int start_session_length=0;
		OS os;
		ProcessSimThread2(OS os){this.os = os;} //Constructor stores reference to os for use in run()
		
		public void run(){

			os.start(pid); 
			events.add("pid="+pid+", session=0");
			try {Thread.sleep(start_session_length);} catch (InterruptedException e) {e.printStackTrace();}
			
			os.schedule(pid);
			events.add("pid="+pid+", session=1");
			
			os.schedule(pid);
			events.add("pid="+pid+", session=2");
			
			os.terminate(pid);
			//events.add("pid="+pid+", session=3"); //could test this return as well ....
		};	
	};	
	
	public void ur2_test(){
		/*********************
		 * 
		 * UR2 - Single Process, Single Processor, Single Priority Queue
		 * 
		 * Creates a single process p0
		 * 
		 * p0's start session is set at 100mS
		 * 
		 * The scheduling must be only p0
		 * 		
		 ***********************/	
		
		System.out.println("\n\n\n*********** UR2 *************");		
		events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for single processor
		OS os =  new OS();	
		os.set_number_of_processors(1);
		int priority1 = 1;
		
		//Create one process simulation thread:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 0; p0.pid = pid0;
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = { "pid=0, session=0", "pid=0, session=1", "pid=0, session=2" };
		
		//Delay to allow p0 to terminate
		sleep(test_timeout);
	

		System.out.println("\nUR2 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR2 PASSED";
		if (events.size() == expected.length) {
			 Iterator <String> iterator = events.iterator(); 
			 int index=0;
			 while (iterator.hasNext()) {
				 String event = iterator.next();
				 if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				 else {
					 test_status = "UR2 FAILED - NO MARKS";	
					 System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				 }
				 index++;
			 }
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR2 FAILED - NO MARKS";			
		}		

		System.out.println("\n" + test_status);	
	}
	
	public void ur3_example_test(){
		/*********************
		 * 
		 * UR3 - Multiple Processes, Single Processor, Single Priority Queue
		 * 
		 * Creates three processes p0-p2
		 * 
		 * p0's start session is set at 150mS 
		 * p1's start is delayed by 50 to ensure that p1 has gained control of (is scheduled onto) the 'single processor'
		 * p2's start is delayed by 100ms to ensure that p1 is before it in the ready queue
		 * 
		 *  Hence scheduling must be p0, p1, p2 repeated for FIFO queue		 #
		 * 		
		 ***********************/	
		
		System.out.println("\n\n\n*********** UR3 *************");		
		events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for single processor
		OS os =  new OS();	
		os.set_number_of_processors(1);
		int priority1 = 1;
		
		//Create three process simulation threads:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 150; p0.pid = pid0;
		
		int pid1 = os.reg(priority1); 
		ProcessSimThread2 p1 = new ProcessSimThread2(os); 
		p1.start_session_length = 0; p1.pid = pid1;
		
		int pid2 = os.reg(priority1); 				
		ProcessSimThread2 p2 = new ProcessSimThread2(os); 
		p2.start_session_length = 0; p2.pid = pid2;		
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();
		sleep(50); // Start p2 one third way through p0's start session
		p1.start();
		sleep(50); // Start p3 two thirds way through p0's start session
		p2.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = { 
				"pid=0, session=0", "pid=1, session=0", "pid=2, session=0", 
				"pid=0, session=1", "pid=1, session=1", "pid=2, session=1", 
				"pid=0, session=2", "pid=1, session=2", "pid=2, session=2"};
		
		//Delay to allow p0-p2 to terminate
		sleep(test_timeout);
	

		System.out.println("\nUR3 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR3 PASSED";
		if (events.size() == expected.length) {
			 Iterator <String> iterator = events.iterator(); 
			 int index=0;
			 while (iterator.hasNext()) {
				 String event = iterator.next();
				 if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				 else {
					 test_status = "UR3 FAILED - NO MARKS";	
					 System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				 }
				 index++;
			 }
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR3 FAILED - NO MARKS";
			System.out.println("DEBUGGING");
			Iterator <String> iterator = events.iterator();
			while(iterator.hasNext()) {
				String event = iterator.next();
				System.out.println("actual event = " + event);
			}
		}		

		System.out.println("\n" + test_status);	
	}

	public void ur4_example_test(){
		
		/*********************
		 * 
		 * UR4  - Multiple Processes, Two Processors, Single Priority Queue
		 * 
		 * Creates three processes p0-p2
		 * 
		 * p0's start session is set at 250mS to ensure that it will grab and keep control of the first available processor
		 * p1's start session is set at 50mS to ensure that p1 has gained control the second processor before p2 starts
		 * p2's start is delayed by 25ms to ensure that it is on the ready queue before when p1 finishes its start session
		 * 
		 * 
		 * 		

		EXPECTED ORDERING OF SESSION ENDS:
			pid=0, session=0
			pid=2, session=1
			pid=1, session=2		
			pid=1, session=1
			pid=2, session=1
			pid=1, session=2
			pid=2, session=2
			pid=0, session=1
			pid=0, session=2
			
		*/
		
		System.out.println("\n\n\n*********** UR4 *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events
		
		//Instantiate OS simulation for two processors
		OS os =  new OS();	
		os.set_number_of_processors(2);
		int priority1 = 1;
		
		//Create two process simulation threads:

		int pid0 = os.reg(priority1); 
		ProcessSimThread2 p0 = new ProcessSimThread2(os); 
		p0.start_session_length = 250; p0.pid = pid0; //p0 grabs first processor and keeps it for 250ms
		
		int pid1 = os.reg(priority1); 
		ProcessSimThread2 p1 = new ProcessSimThread2(os); 
		p1.start_session_length = 50;  p1.pid = pid1; //p1 grabs 2nd processor and keeps it for 50ms
		
		int pid2 = os.reg(priority1); 			
		ProcessSimThread2 p2 = new ProcessSimThread2(os); 
		p2.start_session_length = 0; p2.pid = pid2;	//p2 tries to get processor straight away but has to wait for p1 os.schedule call
		
		//Start the treads making sure that p0 will get to its first os.start()
		p0.start();
		sleep(20);
		p1.start();
		sleep(25); //make sure that p1 has grabbed a processor before starting p2
		p2.start();

		//Give time for all the process threads to complete:
		sleep(test_timeout);

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 1 & 2 in this example)
		String[] expected = { "pid=0, session=0", "pid=1, session=0", "pid=2, session=0", "pid=1, session=1", "pid=2, session=1", "pid=1, session=2", "pid=2, session=2", "pid=0, session=1", "pid=0, session=2"};
		
		System.out.println("\nUR4 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR4 PASSED";
		if (events.size() == expected.length) {
			 Iterator <String> iterator = events.iterator(); 
			 int index=0;
			 while (iterator.hasNext()) {
				 String event = iterator.next();
				 if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				 else {
					 test_status = "UR3 FAILED - NO MARKS";	
					 System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				 }
				 index++;
			 }
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR4 FAILED - NO MARKS";			
		}		

		System.out.println("\n" + test_status);	
	}
	
	public void ur5_test(){
		/*********************
		 * 
		 * UR5  - Multiple Processes, Single Processor, Multiple Priority Queues
		 * 
		 * Creates four processes p0-p3
		 * 
		 * UR5 - Multiple Processes, Single Processor, Multiple Priority Queues
		 * As UR3 except that multiple priorities are used in calls to os.reg(priority). 
		 * Argument priority=1 is the highest priority, priority=2 is the next highest etc. 
		 * Thus, higher values of this argument indicate lower priority processes.  
		 * Hence, if p0 and p1 call os.reg(priority=10), and p2 calls os.reg(priority=20), then 
		 * p0 and p1 will be repeatedly scheduled in FIFO order until both call OS.terminate, 
		 * at which point p2 will be scheduled in.
		 * 
		 * 
		 * 		
		*/
		System.out.println("\n\n\n*********** UR5 *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events

		//Instantiate OS simulation for single processor
		OS os =  new OS();
		os.set_number_of_processors(1);

		//Create four process simulation threads with different priorities:

		int pid0 = os.reg(1);
		ProcessSimThread2 p0 = new ProcessSimThread2(os);
		p0.start_session_length = 200; p0.pid = pid0;

		int pid1 = os.reg(1);
		ProcessSimThread2 p1 = new ProcessSimThread2(os);
		p1.start_session_length = 0; p1.pid = pid1;

		int pid2 = os.reg(2);
		ProcessSimThread2 p2 = new ProcessSimThread2(os);
		p2.start_session_length = 0; p2.pid = pid2;

		int pid3 = os.reg(3);
		ProcessSimThread2 p3 = new ProcessSimThread2(os);
		p3.start_session_length = 0; p3.pid = pid3;

		//Start the threads making sure that p0 and p1 will get to their first os.start()
		p0.start();
		sleep(50);
		p1.start();
		sleep(50); // Start p2 one third way through p0's start session
		p2.start();
		sleep(50); // Start p3 two thirds way through p0's start session
		p3.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = {
				"pid=0, session=0", "pid=1, session=0", 
				"pid=0, session=1", "pid=1, session=1",
				"pid=0, session=2", "pid=1, session=2",
				"pid=2, session=0", "pid=2, session=1", "pid=2, session=2", 
				"pid=3, session=0", "pid=3, session=1",	"pid=3, session=2"};

		//Delay to allow p0-p3 to terminate
		sleep(test_timeout);

		System.out.println("\nUR5 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR5 PASSED";
		if (events.size() == expected.length) {
			Iterator <String> iterator = events.iterator();
			int index=0;
			while (iterator.hasNext()) {
				String event = iterator.next();
				if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				else {
					test_status = "UR5 FAILED - NO MARKS";
					System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				}
				index++;
			}
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR5 FAILED - NO MARKS";
			System.out.println("DEBUGGING");
			Iterator <String> iterator = events.iterator();
			while(iterator.hasNext()) {
				String event = iterator.next();
				System.out.println("actual event = " + event);
			}
		}

		System.out.println("\n" + test_status);
		
	}

	public void ur5_mixed_test(){
		/*********************
		 * 
		 * UR5  - Multiple Processes, Single Processor, Multiple Priority Queues
		 * 
		 * Creates four processes p0-p3
		 * 
		 * UR5 - Multiple Processes, Single Processor, Multiple Priority Queues
		 * As UR3 except that multiple priorities are used in calls to os.reg(priority). 
		 * Argument priority=1 is the highest priority, priority=2 is the next highest etc. 
		 * Thus, higher values of this argument indicate lower priority processes.  
		 * Hence, if p0 and p1 call os.reg(priority=10), and p2 calls os.reg(priority=20), then 
		 * p0 and p1 will be repeatedly scheduled in FIFO order until both call OS.terminate, 
		 * at which point p2 will be scheduled in.
		 * 
		 * This test follows UR5's parameters, but with a different ordering of process starts. Lower priority processes are started first!
		 * 		
		*/
		System.out.println("\n\n\n*********** UR5(MIXED) *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events

		//Instantiate OS simulation for single processor
		OS os =  new OS();
		os.set_number_of_processors(1);

		//Create four process simulation threads with different priorities:

		int pid0 = os.reg(1);
		ProcessSimThread2 p0 = new ProcessSimThread2(os);
		p0.start_session_length = 0; p0.pid = pid0;

		int pid1 = os.reg(1);
		ProcessSimThread2 p1 = new ProcessSimThread2(os);
		p1.start_session_length = 0; p1.pid = pid1;

		int pid2 = os.reg(2);
		ProcessSimThread2 p2 = new ProcessSimThread2(os);
		p2.start_session_length = 250; p2.pid = pid2;

		int pid3 = os.reg(3);
		ProcessSimThread2 p3 = new ProcessSimThread2(os);
		p3.start_session_length = 0; p3.pid = pid3;

		//Start the threads making sure that p0 and p1 will get to their first os.start()
		p2.start();
		sleep(50);
		p1.start();
		sleep(50); // Start p2 one third way through p0's start session
		p3.start();
		sleep(50); // Start p3 two thirds way through p0's start session
		p0.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = {
				"pid=2, session=0",
				"pid=1, session=0", "pid=0, session=0", 
				"pid=1, session=1", "pid=0, session=1",
				"pid=1, session=2", "pid=0, session=2",
				"pid=2, session=1", "pid=2, session=2", 
				"pid=3, session=0", "pid=3, session=1",	"pid=3, session=2"};

		//Delay to allow p0-p3 to terminate
		sleep(test_timeout);

		System.out.println("\nUR5(MIXED) - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR5(MIXED) PASSED";
		if (events.size() == expected.length) {
			Iterator <String> iterator = events.iterator();
			int index=0;
			while (iterator.hasNext()) {
				String event = iterator.next();
				if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				else {
					test_status = "UR5(MIXED) FAILED - NO MARKS";
					System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				}
				index++;
			}
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR5(MIXED) FAILED - NO MARKS";
			System.out.println("DEBUGGING");
			Iterator <String> iterator = events.iterator();
			while(iterator.hasNext()) {
				String event = iterator.next();
				System.out.println("actual event = " + event);
			}
		}

		System.out.println("\n" + test_status);
		
	}

	public void ur6_test(){
		/*********************
		 * 
		 * UR6  - Multiple Processes, Multiple Processors, Multiple Priority Queues
		 * 
		 * Creates four processes p0-p3
		 * 
		 * Combines requirements of UR4 and UR5.
		 * 		
		*/
		System.out.println("\n\n\n*********** UR6 *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events

		//Instantiate OS simulation for single processor
		OS os =  new OS();
		os.set_number_of_processors(2);

		//Create four process simulation threads with different priorities:

		int pid0 = os.reg(1);
		ProcessSimThread2 p0 = new ProcessSimThread2(os);
		p0.start_session_length = 250; p0.pid = pid0;

		int pid1 = os.reg(1);
		ProcessSimThread2 p1 = new ProcessSimThread2(os);
		p1.start_session_length = 175; p1.pid = pid1;

		int pid2 = os.reg(2);
		ProcessSimThread2 p2 = new ProcessSimThread2(os);
		p2.start_session_length = 0; p2.pid = pid2;

		int pid3 = os.reg(2);
		ProcessSimThread2 p3 = new ProcessSimThread2(os);
		p3.start_session_length = 0; p3.pid = pid3;

		//Start the threads making sure that p0 and p1 will get to their first os.start()
		p0.start();
		sleep(50);
		p1.start();
		sleep(50); // Start p2 one third way through p1's start session
		p2.start();
		sleep(50); // Start p3 two thirds way through p1's start session
		p3.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = {
				"pid=0, session=0",
				"pid=1, session=0", "pid=1, session=1", "pid=1, session=2",
				"pid=2, session=0", "pid=3, session=0",
				"pid=2, session=1", "pid=3, session=1",
				"pid=2, session=2", "pid=3, session=2",
				"pid=0, session=1", "pid=0, session=2",};

		//Delay to allow p0-p3 to terminate
		sleep(test_timeout);

		System.out.println("\nUR6 - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR6 PASSED";
		if (events.size() == expected.length) {
			Iterator <String> iterator = events.iterator();
			int index=0;
			while (iterator.hasNext()) {
				String event = iterator.next();
				if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				else {
					test_status = "UR6 FAILED - NO MARKS";
					System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				}
				index++;
			}
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR6 FAILED - NO MARKS";
			System.out.println("DEBUGGING");
			Iterator <String> iterator = events.iterator();
			while(iterator.hasNext()) {
				String event = iterator.next();
				System.out.println("actual event = " + event);
			}
		}

		System.out.println("\n" + test_status);
		
	}

	public void ur6_mixed_test(){
		/*********************
		 * 
		 * UR6  - Multiple Processes, Multiple Processors, Multiple Priority Queues
		 * 
		 * Creates four processes p0-p3
		 * 
		 * Combines requirements of UR4 and UR5.
		 * 
		 * This test follows UR6's parameters, but with both processors executing at the same time.
		 * 
		 * This test starts 0, then puts it to sleep for 100mS.
		 * It then starts 1 after 50mS and then puts it to sleep for 75mS.
		 * Then 2 enters after 50mS but is held. Once 2 enters, 0 resumes its session and terminates.
		 * 2 is signalled after 0 terminates and completes its entire session using 0's old processor 
		 * Once 2 terminates, 1 resumes its session and terminates.
		 * By this point, it has been 50mS, and 3 starts its session and terminates without any need for swapping
		 * 		
		*/
		System.out.println("\n\n\n*********** UR6(MIXED) *************");
		events = new ConcurrentLinkedQueue<String>(); //List of process events

		//Instantiate OS simulation for single processor
		OS os =  new OS();
		os.set_number_of_processors(2);

		//Create four process simulation threads with different priorities:

		int pid0 = os.reg(1);
		ProcessSimThread2 p0 = new ProcessSimThread2(os);
		p0.start_session_length = 100; p0.pid = pid0;

		int pid1 = os.reg(1);
		ProcessSimThread2 p1 = new ProcessSimThread2(os);
		p1.start_session_length = 75; p1.pid = pid1;

		int pid2 = os.reg(2);
		ProcessSimThread2 p2 = new ProcessSimThread2(os);
		p2.start_session_length = 0; p2.pid = pid2;

		int pid3 = os.reg(2);
		ProcessSimThread2 p3 = new ProcessSimThread2(os);
		p3.start_session_length = 0; p3.pid = pid3;

		//Start the threads making sure that p0 and p1 will get to their first os.start()
		p0.start();
		sleep(50);
		p1.start();
		sleep(50); // Start p2 one third way through p1's start session
		p2.start();
		sleep(50); // Start p3 two thirds way through p1's start session
		p3.start();

		//Created EXPECTED ORDERING OF SESSION EVENTS (will test for sessions 0, 1 & 2 in this example)
		String[] expected = {
				"pid=0, session=0", "pid=1, session=0", 
				"pid=0, session=1", "pid=0, session=2",
				"pid=2, session=0", "pid=2, session=1", "pid=2, session=2", 
				"pid=1, session=1", "pid=1, session=2",
				"pid=3, session=0", "pid=3, session=1", "pid=3, session=2"};

		//Delay to allow p0-p3 to terminate
		sleep(test_timeout);

		System.out.println("\nUR6(MIXED) - NOW CHECKING");
		//Check expected events against actual:
		String test_status = "UR6(MIXED) PASSED";
		if (events.size() == expected.length) {
			Iterator <String> iterator = events.iterator();
			int index=0;
			while (iterator.hasNext()) {
				String event = iterator.next();
				if (event.equals(expected[index])) System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- MATCH");
				else {
					test_status = "UR6(MIXED) FAILED - NO MARKS";
					System.out.println("Expected event = "+ expected[index] + ", actual event = " + event + " --- ERROR");
				}
				index++;
			}
		} else {
			System.out.println("Number of events expected = " + expected.length + ", number of events reported = " + events.size());
			test_status = "UR6 FAILED(MIXED) - NO MARKS";
			System.out.println("DEBUGGING");
			Iterator <String> iterator = events.iterator();
			while(iterator.hasNext()) {
				String event = iterator.next();
				System.out.println("actual event = " + event);
			}
		}

		System.out.println("\n" + test_status);
		
	}

}


