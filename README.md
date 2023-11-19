# Elevator Simulation: Lifts.java
Elevator simulator written in java using a Doubly Linked List or Array List. The characteristics of this program can be manipulated by using a Properties File. The program can also accept a second argument, "verbose", which outputs extended information regarding the simulation. 

# final variables

  M = 5: Maximum distance elevator moves per tick. 
  
  PF = 3: Probability factor. Persons' destination floor has a 2/3 probability of being 1, if they are not currently on floor 1. 

 # global variables

  prob: the "passengers" value, given by properties file or default 

  capacity: capacity of people in elevator, given by properties file or default 

  duration: amount of ticks, given by properties file or default 

  elevators: number of elevators, given by properties file or default 

  floors: number of floors, given by properties file or default 

  maxTime: maximum time from arrival to destination 

  minTime: minimum time from arrival to destination

  pID: a Person's ID, can ve viewed in verbose mode. 

  tick: time metric 

  totalTime: total time simulation ran 

  visitors: total amount of visitors

  reachedDest: the amount of persons who reached their destinaton 

  up = 0: int variable that represents up 

  down = 1: int variable that represents down 

  # Main
