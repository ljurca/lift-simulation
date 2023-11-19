# Lifts Class
Elevator simulator written in java using a Doubly Linked List or Array List. The characteristics of this program can be manipulated by using a Properties File. The program can also accept a second argument, "verbose", which outputs extended information regarding the simulation. 

 # Main()

 The main function manages the properties of the simulation. If there is a properties file present, the key variables are changed. 
 
 New arrays are intialized, such as the array of elevators, the array of floors, and the array of people waiting on each floor. Additoinally, pushUp and pushDown is a boolean array that shows if someone "pressed up" or "pressed down" on each floor. Likewise, "wait" is an array that shows who is waiting on a given floor. 

 Main also prints out the required output, as well as the optional verbose output. 

 # Other methods

 arrivals(): A person is created. Simulates when a person arrives on a floor. They are added to the array "wait" array, as they are now waiting on a floor. 

 departures(): The elevator car unloads if people want to get off, and the car picks people up if they are going in the same direction. 

 getvalues(): Assists with reading the Properties File, and changing the key variables in the simulation. 

 npersons(): Creates a new person if a random double is less than the probablity given by the property file, or default value (0.03). 

 random(): Random number generator, ensures a positive random int is returned. 

 # Elevator Class

 An elevator is either a doubly linked list or an array list depending on implementation. The private data members are

 curr: current floor

 dir: direction of elevator 

 load: number of passenger inside

 # Elevator methods() 

accept(): A new passenger is added to the elevator. 

add(): Method is only called when elevator is a linked list. Add() adds a passenger to the linked list. 

display(): Displays information if program is running in verbose mode.

firstPassenger(): Returns first person in elevator at given moment. 

getPassenger(): Gets passenger into elevator. The passenger's destination floor must be in the same direction as the elevator. 

goNext(): Determine the direction of elevator.

goodbye(): Passenger exits elevator. Remove from elevator and subtract from the load in current elevator, and increment the people who have reached destination. 

nextPassenger(): Returns the next passenger in an elevator. 

remove(): Remove a passenger from a linked list. Only called when a linked list is used. 

toNextFloor(): Determines the next floor an elevator goes to. If there is nobody in the elevator, it checks the floors above and below. It checks to see who is closest: someone who below who wants to go up, someone below who wants to go down, someone below who wants to go up, and someone below who wants to go down.

If the elevator is not empty and it is going up, it determines which destination floor is closest and goes there. If the elevator is not empty and is going down, it does the same. It also checks to see who is waiting on the near floors, and which is floor is closer. 

unload(): Unloads the elevator if a passenger reaches their destination floor. 

updateTimes(): Updates the time variables. 

# Floor Class 

A floor is either a doubly linked list or an array list depending on implementation. 

# Floor Methods()

add(): Adds a person to a linked list. Only called when using linked list.

append(): Adds a person to a floor. 

ckButtons(): Updates the pushUp and pushDown array. This determines which floors the up and down buttons have been pressed.

display(): Displays information if program is running in verbose mode.

firstPerson(): Returns first person on floor at given moment. 

goodbye(): Person leaves the floor. 

nextPerson(): Return next person on the floor.

remove(): Removes person from a linked list. Only called when a linked list is used.

# Passenger Class

A passenger is a node. The private data members are

id: id number that can be viewed in verbose mode

arr: arrival time

dest: destination floor 

# Person Class

A person is a person. But a passenger is distinctly someone who is riding/rode the elevator. A person is a node. The private data members are 

id: id number that can be viewed in verbose mode

arr: arrival time (tick)

dest: destination floor

dir: direction they will go in 

# Person Method()

destFloor(): Determines a person's destination floor. If the floor the person is on is 1, a random floor is generated. It will be a floor from floor 2 - the top floor. Otherwise, the probability factor (3) is utilized. If a person is on a floor that is not 1, the liklihood of them going back to floor 1 is about 2/3 of the time (guess). If a random number % PF is not 0, we return 1. This will be the person's destination floor. If it is 0, a random floor is generated as their destination floor. 



