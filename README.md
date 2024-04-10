# Concurrent Operating System Processing Simulation
A simulation of an operating system that starts, schedules and terminates processes using a priority scheduling method, implemented in Java. This implementation avoids synchronized keywords and classes and uses Reentrant Locks and Conditions.

## Classes
OS_sim_interface.java
> An interface for the OS.java class that outlines the public methods.  

OS.java
> The main implementation of the scheduling algorithm. Overrides start, schedule and terminate methods from the interface and contains some private helper methods.

Test.java
> Custom tests made to ensure the scheduling algorithm works as intended and is thread-safe. Uses the Thread class in Java.

Main.java
> The main method that runs the tests.
