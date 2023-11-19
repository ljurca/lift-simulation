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

 # Elevator private methods 

accept(): A new passenger is added to the elevator 

add(): Method is only called when elevator is a linked list. Add() adds a passenger to the linked list. 

display(): displays information if program is running in verbose mode.

firstPassenger(): Returns first person in elevator at given moment. 

getPassenger(): Gets passenger into elevator. The passenger's destination floor must be in the same direction as the elevator. 
