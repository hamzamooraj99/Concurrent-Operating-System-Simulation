# Concurrent Operating System Processing Simulation
A simulation of an operating system that starts, schedules and terminates processes using a priority scheduling method, implemented in Java. This implementation avoids synchronized keywords and classes and uses Reentrant Locks and Conditions.

## Classes
OS_sim_interface.java
> An interface for the OS.java class that outlines the public methods.  

OS.java
> The main implementation of the scheduling algorithm. Overrides start, schedule and terminate methods from the interface and contains some private helper methods.

Test.java
> Custom tests made to ensure the scheduling algorithm works as intended and is thread-safe. Uses the Thread class in Java. All the tests ensure the program satisfies all User Requirements.

Main.java
> The main method that runs the tests.

## User Requirements
UR1. Process IDs and Priorities 
> This UR ensures that the process' IDs and priorities are correctly assigned before the algorithm begins scheduling.

UR2. Single Process, Single Processor, Single Priority Level  
> This UR ensures that the scheduling algorithm can handle an OS simulation that needs to execute a single process using only one processor.

UR3. Multiple Processes, Single Processor, Single Priority Level
> This UR ensures that the scheduling algorithm can handle an OS simulation that needs to execute multiple processes of the same priority using only one processor.

UR4. Multiple Processes, Two Processors, Single Priority Level
> This UR ensures that the scheduling algorithm can handle an OS simulation that needs to execute multiple processes of the same priority using two processors simultaneously.

UR5. Multiple Processes, Single Processor, Multiple Priority Levels
> This UR ensures that the scheduling algorithm can handle an OS simulation that needs to execute multiple processes of different priorities using only one processor.

UR6. Multiple Processes, Two Processors, Multiple Priority Levels
> This UR ensures that the scheduling algorithm can handle an OS simulation that needs to execute multiple processes of different priorities using only two processors simultaneously.


