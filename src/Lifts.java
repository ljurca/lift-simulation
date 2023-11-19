
/* ------------------------------------------------------------------------ */
/* LIFTS.JAVA : elevator simulation using ArrayLists and DoublyLinkedLists. */
/* Add a properties file arg[0] to change building properties.              */
/* Type "verbose" arg[1] to run in verbose mode                             */
/* ------------------------------------------------------------------------ */

import java.io.*;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList ;
import java.lang.Math ;

public class Lifts {
    private static final int M = 5;    // max distance elevator moves per tick
    private static final int PF = 3;    // prob (PF-1)/PF that person on floor != 1
                                        // chooses floor 1 as next destination floor

    /*------------------------------------------------------------*/
    /* global variables
    /*------------------------------------------------------------*/

    static double prob;            // "passengers" in file
    static int capacity, duration, elevators, floors, maxTime, minTime, pID,
            tick, totalTime, visitors, reachedDest, up, down;
    static Boolean linked, verbose;

    public static void main(String[] args) throws IOException {

        /*------------------------------------------------------------*/
        /* local variables
        /*------------------------------------------------------------*/

        String[] defvalue = {"linked", "32", "0.03", "1", "10", "500"};
        String[] key = {"structures", "floors", "passengers", "elevators",
                "elevatorCapacity", "duration"};
        String[] value = new String[defvalue.length];

        /*------------------------------------------------------------*/
        /* checking possible input file
        /*------------------------------------------------------------*/

        if (args.length != 0) {
            getvalues(args[0], key, value, defvalue);
        } else {
            System.arraycopy(defvalue, 0, value, 0, defvalue.length);
        }

        linked = (value[0].equals("linked"));
        capacity = Integer.parseInt(value[4]);
        duration = Integer.parseInt(value[5]);
        elevators = Integer.parseInt(value[3]);
        floors = Integer.parseInt(value[1]);
        prob = Double.parseDouble(value[2]);

        /*------------------------------------------------------------*/
        /* verbose mode
        /*------------------------------------------------------------*/
        verbose = false ;
        if (args.length > 1){
            if (args[1].equals("verbose")){
                verbose = true ;
                System.out.println("\n ** RUNNING IN VERBOSE MODE ** ") ;
            }
        }

        if (verbose){
            System.out.println( "\n-- actual values --\n" );
            System.out.println( "structures       = "+"\""+value[0]+"\"" );
            System.out.println( "floors           = "+floors );
            System.out.println( "passengers       = "+prob );
            System.out.println( "elevators        = "+elevators );
            System.out.println( "elevatorCapacity = "+capacity );
            System.out.println( "duration         = "+duration + "\n");
        }

        /*------------------------------------------------------------*/
        /* initializing
        /*------------------------------------------------------------*/

        up = 0;
        down = 1;
        Elevator[] elevatorArr = new Elevator[elevators];
        Floor[] floorArr = new Floor[floors + 1];
        Boolean[] pushUp = new Boolean[floors + 1];
        Boolean[] pushDown = new Boolean[floors + 1];
        Integer[] wait = new Integer[floors + 1];

        for (int e = 0; e < elevators; e++) {
            elevatorArr[e] = new Elevator(); // arr of elevators
        }
        for (int f = 1; f <= floors; f++) {
            floorArr[f] = new Floor();        // arr of floors
            pushUp[f] = pushDown[f] = false; // no elevator buttons called yet, no people waiting
            wait[f] = 0;
        }

        minTime = Integer.MAX_VALUE;        // min, max
        maxTime = totalTime = 0;
        pID = visitors = reachedDest = 0;    // total visitors & visitors who reach destination

        /*------------------------------------------------------------*/
        /* ticks and movement
        /*------------------------------------------------------------*/

        long t = System.currentTimeMillis();
        for (tick = 1; tick <= duration; tick++) {
            arrivals(floorArr, wait, pushUp, pushDown);
            departures(floorArr, wait, pushUp, pushDown, elevatorArr);
        }
        t = System.currentTimeMillis() - t;

        /*------------------------------------------------------------*/
        /* results
        /*------------------------------------------------------------*/

        int riders = 0, waiters = 0;

        for (int f = 1; f <= floors; f++) {
            if (0 < wait[f]) {
                waiters += wait[f];
            }
        }
        for (int e = 0; e < elevators; e++) {
            Elevator car = elevatorArr[e];
            if (0 < car.Load()) {
                riders += car.Load();
            }
        }

        if (verbose){
            System.out.println("\n" + visitors + " total visitors.");
            System.out.println(reachedDest + " passengers got to their destination.");
            System.out.println(waiters + " persons waiting on floors.");
            System.out.println(riders + " passengers riding in elevator(s).");
            System.out.println("-----------------------------" );
            System.out.println( t + " milliseconds to perform simulation." );
        }

        if (minTime < Integer.MAX_VALUE) {
            System.out.format("%4d minimum time from arrival to destination. \n", minTime);
        } else {
            System.out.format("0 minimum time from arrival to destination.\n");
        }

        if (reachedDest > 0) {
            double mean = (double) totalTime / (double) reachedDest;
            System.out.format("%7.2f average time from arrival to destination. \n", mean);
        } else {
            System.out.format(0 + " mean time from arrival to destination. \n");
        }

        if (maxTime > Integer.MIN_VALUE){
            System.out.format("%4d maximum time from arrival to destination. \n", maxTime);
        } else{
            System.out.format(0 + " maximum time from arrival to destination. \n");
        }
    }

    /* ARRIVALS(): A new Person is created on a Floor. */
    private static void arrivals(Floor[] floorArr, Integer[] wait,
                                 Boolean[] pushUp, Boolean[] pushDown) {

        for (int f = 1; f <= floors; f++) {
            Floor floor = floorArr[f];
            int n = npersons();    // usually 0
            visitors += n;
            wait[f] += n;

            if (verbose){
                System.out.format( "-- arrivals: f =%3d, n =%2d --\n",f,n );
            }

            for (int i = n; 0 < i--; ) {    // n times
                Person p = new Person(f);

                if (verbose){
                    System.out.format( "-- tick%4d - no."+
                            "%4d enters on floor%3d, dest =%"+
                            "3d\n",tick,p.Id(),f,p.Dest() );
                }

                floor.append(p);
            }
            if (0 < n) {
                floor.ckButtons(f, pushUp, pushDown);
            }
        }
    }

    /* DEPARTURES(): unload passenger if they want to get off &
     get people if they are going in same direction as elevator (and not
     at capacity) */
    private static void departures(Floor[] floorArr, Integer[] wait,
                                   Boolean[] pushUp, Boolean[] pushDown, Elevator[] elevatorArr) {

        for (int e = 0; e < elevators; e++) {
            Elevator car = elevatorArr[e];
            int curr = car.Curr();
            if (0 < car.Load()) {
                car.unload(curr);
            }
            int dir = car.Dir(); // 0 up 1 down, direction
            if (wait[curr] != 0) {
                car.getPassengers(floorArr[curr], wait, curr,
                        pushUp, pushDown, e,dir);
            }
            car.toNextFloor(curr, dir, pushUp, pushDown);
        }
    }


    /* GETVALUES(): used to get values from Property File (if present) */
    private static void getvalues(String fname, String[] key, String[] v,
                                  String[] dv) {

        try {
            FileReader reader = new FileReader(fname);
            Properties p = new Properties();
            p.load(reader);
            for (int i = 0; i < key.length; i++) {
                v[i] = p.getProperty(key[i]);
                if (v[i] == null) {
                    v[i] = dv[i];
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: BAD PROPERTIES FILE.");
            System.exit(0);
        }
    }

    /* NPERSONS(): number of Persons (0 or 1) on a Floor at specified tick */
    private static int npersons() {

        return ((new Random().nextDouble() > prob ? 0 : 1)); // if 0, no new person
    }

    /* RANDOM(): Random number generator */
    private static int random(Random rand) {

        int r = rand.nextInt();
        if (r == Integer.MIN_VALUE) {
            r = Integer.MAX_VALUE;
        }
        if (r < 0) {
            r = -r;
        }
        return (r);
    }

    private static class Elevator {

        /*--------------------------------------------------------------------*/
        /* private data members: Elevator
        /*--------------------------------------------------------------------*/

        private Passenger head, tail;    // an Elevator is a doubly linked list
        private ArrayList<Passenger> eList;    // or an ArrayList
        private int curr, dir, load; // current floor, direction of elevator,
                                    // num of passengers inside

        /*--------------------------------------------------------------------*/
        /* constructor: Elevator
        /*--------------------------------------------------------------------*/

        Elevator() {

            if (linked) {
                head = tail = null;
            } else {
                eList = new ArrayList<Passenger>();
            }
            curr = 1; // 1st floor
            dir = up; // up = 0
            load = 0;
        }

        /*--------------------------------------------------------------------*/
        /* accessors/mutators: Elevator
        /*--------------------------------------------------------------------*/

        private int Curr() {
            return (curr);
        }

        private int Load() {
            return (load);
        }

        private int Dir() {
            return (dir); // direction: up or down
        }

        private void setCurr(int floor) { // sets specific floor
            curr = floor;
        }

        private void setDir(int direction) { // sets current direction
            dir = direction;
        }

        /*--------------------------------------------------------------------*/
        /* private methods: Elevator
        /*--------------------------------------------------------------------*/

        /* ACCEPT(): admits a passenger in the elevator */
        private void accept(Passenger p) {

            if (linked) {
                add(p);
            } else {
                eList.add(p);
            }
            load++;
        }

        /* ADD(): adds passenger to linked list */
        private void add(Passenger p) { // flink = forwards link ; blink = backwards link
            // list is empty
            if (tail == null) {
                head = p;
                head.blink = null;
            }
            // add new node to the end
            else {
                p.blink = tail;
                tail.flink = p;
            }
            tail = p;
            tail.flink = null;
        }

        /* DISPLAY(): displays information in verbose mode */
        private void display() {

            int i = 0;
            Passenger p = firstPassenger() ;
            while ( p != null ) {
                if (verbose){
                    System.out.print( " ["+p.id+","+p.arr+","+p.dest+"]" ) ;
                    System.out.println();
                }
                p = nextPassenger( p, ++i );
            }
        }

        /* FIRSTPASSENGER(): returns first Passenger in Passenger List at given moment */
        private Passenger firstPassenger() {
            return (linked ? head : eList.get(0));
        }

        /* GETPASSENGER(): get Passengers into elevator */
        private void getPassengers(Floor floor, Integer[] wait, int curr,
                                   Boolean[] pushUp, Boolean[] pushDown, int e, int dir) {

            int i = 0;
            Person p = floor.firstPerson();
            while (p != null) {
                if (capacity <= load) {
                    break;
                }
                if (p.Dir() == dir) { // dest must be in the same dir as elevator
                    accept(new Passenger(p.Id(), p.Arr(),
                            p.Dest()));
                    floor.goodbye(p);
                    wait[curr]--;
                }
                p = floor.nextPerson(p, ++i);
            }
            if (wait[curr] == 0) { // no one waiting on given floor
                pushUp[curr] = pushDown[curr] = false;
            } else {
                floor.ckButtons(curr, pushUp, pushDown);
            }
        }

        /* GONEXT(): sets floor and direction for next tick */
        private void goNext(int destFloor, int dir) {
            int topFloor = floors; // # of floors = top floor

            setCurr(destFloor); // next floor
            if (destFloor == 1) {
                setDir(up); // must go up
                return;
            }
            if (destFloor == topFloor) {
                setDir(down); // must go down
                return;
            }
            setDir(dir);
        }

        /* GOODBYE(): passenger leaving elevator, arrived at desired floor */
        private int goodbye(Passenger p) {

            if (linked) {
                remove(p);
            } else {
                eList.remove(p);
            }
            if (verbose){
                System.out.format( "-- tick%4d - no.%4d exits  on floor%3d\n",
                        tick,p.Id(),p.Dest() );
            }
            --load;
            reachedDest++;
            return (tick - p.Arr());
        }

        /* NEXTPASSENGER(): return next Passenger in Passenger list (elevator) */
        private Passenger nextPassenger(Passenger p, int i) {
            if (linked) {
                return (p.flink); // forward link
            } else {
                return ( ((i < eList.size()) ? eList.get(i) : null) ); // ensures no index out of bounds
            }
        }

        /* REMOVE(): remove passenger from linked list */
        private void remove(Passenger p) { // flink = forwards link ; blink = backwards link
            if (p == head) {
                head = p.flink;
            } else {
                p.blink.flink = p.flink;
            }
            if (p == tail) {
                tail = p.blink;
            } else {
                p.flink.blink = p.blink;
            }

        }

        /* TONEXTFLOOR(): determines next floor an elevator goes to */
        private void toNextFloor(int curr, int dir, Boolean[] pushUp,
                                 Boolean[] pushDown) {
            int i, j;

            if (load == 0) {                // empty car
                for (i = curr; i < floors - 1; ) {
                    if (pushUp[++i]) { // someone above want to go up
                        goNext(Math.min(i, curr + M), up);
                        return;
                    }
                }
                for (i = curr; i < floors; ) {
                    if (pushDown[++i]) { // someone above want to go down
                        goNext(Math.min(i, curr + M), down);
                        return;
                    }
                }
                for (j = curr; 2 < j; ) {
                    if (pushDown[--j]) { // someone below want to go down
                        goNext(Math.max(j, curr - M), down);
                        return;
                    }
                }
                for (j = curr; 2 <= j; ) {
                    if (pushUp[--j]) { // someone below want to go up
                        goNext(Math.max(j, curr - M), up);
                        return;
                    }
                }
                return;   // nobody below wants up, don't move
            }

            if (dir == up) { // not empty, going up
                i = floors;
                j = 0;
                Passenger p = firstPassenger();
                while (p != null) {
                    int d = p.Dest();
                    if (d < i) {
                        // i = the closest above floor which is passenger destination floor
                        i = d;
                    }
                    p = nextPassenger(p, ++j);
                }
                for (j = curr; j < floors; ) {
                    if (pushUp[++j]) {
                        // j =  the closest floor above where an up button has been pushed
                        break;
                    }
                }
                if (j < i) {
                    // which floor is closer: destination or waiter floor?
                    // that will be next floor
                    i = j;
                }
                goNext((Math.min(i, curr + M)), up);

            } else {                // not empty, going down
                i = 1;
                j = 0;
                Passenger p = firstPassenger();
                while (p != null) {
                    int d = p.Dest();
                    if (i < d) {
                        // i = the closest below floor which is passenger destination floor
                        i = d;
                    }
                    p = nextPassenger(p, ++j);
                }
                for (j = curr; 1 < j; ) {
                    if (pushDown[--j]) {
                        // j = find the closest floor below where a down button has been pushed
                        break;
                    }
                }
                if (j < i) {
                    // which floor is closer: destination or waiter floor?
                    // that will be next floor
                    j = i;
                }
                goNext(Math.max(j, curr - M), down);
            }
        }

        /* UNLOAD(): unloads those who have reached dest floor */
        private void unload(int floor) {

            int i = 0;
            Passenger p = firstPassenger();
            while (p != null) {
                if (p.Dest() == floor) {
                    int t = goodbye(p); //passenger leaving elevator
                    updateTimes(t);
                }
                p = nextPassenger(p, ++i);
            }
        }

        /* UPDATETIMES(): update the time variables */
        private void updateTimes(int t) {
            if (t < minTime){
                minTime = t ;
            }
            totalTime += t ;
            if (maxTime < t){
                maxTime = t ;
            }
        }
    }

    private static class Floor {

        /*--------------------------------------------------------------------*/
        /* private data members: Floor
        /*--------------------------------------------------------------------*/

        private Person head, tail;    // a Floor is a doubly linked list
        private ArrayList<Person> fList; // or array list

        /*--------------------------------------------------------------------*/
        /* constructor: Floor
        /*--------------------------------------------------------------------*/

        Floor() {
            if (linked) {
                head = tail = null; // empty floor
            } else {
                fList = new ArrayList<Person>(); // empty arr list
            }
        }

        /*--------------------------------------------------------------------*/
        /* private methods: Floor
        /*--------------------------------------------------------------------*/

        /* ADD(): adds a new Person to linked list */
        private void add(Person p) {
            if (tail == null) {
                head = p;
                head.blink = null;
            } else {
                p.blink = tail;
                tail.flink = p;
            }
            tail = p;
            tail.flink = null;
        }

        /* APPEND(): adds a Person to a Floor */
        private void append(Person p) {

            if (linked) {
                add(p);
            } else {
                fList.add(p);
            }
        }

        /* CKBUTTONS(): updates pushUp & pushDown arr, to see who wants to
        * go up and go down on which floors */
        private void ckButtons(int curr, Boolean[] pushUp, Boolean[] pushDown) {
            int i;

            pushUp[curr] = pushDown[curr] = false;
            i = 0;
            Person p = firstPerson();
            while (p != null) {
                if (p.Dir() == up) {
                    pushUp[curr] = true;
                    break;
                }
                p = nextPerson(p, ++i);
            }
            i = 0;
            p = firstPerson();
            while (p != null) {
                if (p.Dir() == down) {
                    pushDown[curr] = true;
                    break;
                }
                p = nextPerson(p, ++i);
            }
        }

        /* DISPLAY(): Displays information in verbose mode */
        private void display() {
            if ((head == null) || (!linked && fList.isEmpty())){
                return ;
            }
            for ( Person p = head;  p != null;  p = p.flink ) {
                System.out.format( " (%d,%d,%d)",p.Id(),p.Arr(),
                        p.Dest() );
            }
            System.out.println();
        }

        /* FIRSTPERSON(): returns first Person in Floor List at given moment */
        private Person firstPerson() {
            return (linked ? head : fList.get(0));
        }

        /* GOODBYE(): Person leaves floor */
        private void goodbye(Person p) {
            if (linked) {
                remove(p);
            } else {
                fList.remove(p);
            }
        }

        /* NEXTPERSON(): return next Person in Floor List */
        private Person nextPerson(Person p, int i) {
            if (linked) {
                return (p.flink);
            } else {
                return (i < fList.size() ? fList.get(i) : null);
            }
        }

        /* REMOVE(): remove Person from Floor linked list */
        private void remove(Person p ){
            if (p == head){
                head = p.flink ;
            } else{
                p.blink.flink = p.flink ;
            }
            if (p == tail){
                tail = p.blink ;
            } else{
                p.flink.blink = p.blink ;
            }
        }
    }

    private static class Passenger {

        /*--------------------------------------------------------------------*/
        /* private data members: Passenger
        /*--------------------------------------------------------------------*/

        private Passenger blink;    // a Passenger is a node
        private int id;
        private int arr;
        private int dest;
        private Passenger flink;

        /*--------------------------------------------------------------------*/
        /* constructor: Passenger
        /*--------------------------------------------------------------------*/

        Passenger(int idNum, int arrivalTime, int destFloor) {

            if (linked) {
                blink = flink = null;
            }
            id = idNum;
            arr = arrivalTime;
            dest = destFloor;
        }

        /*--------------------------------------------------------------------*/
        /* accessors: Passenger
        /*--------------------------------------------------------------------*/

        private int Arr() {
            return (arr);
        }

        private int Dest() {
            return (dest);
        }

        private int Id() {
            return (id);
        }
    }

    private static class Person {

        /*--------------------------------------------------------------------*/
        /* private data members: Person
        /*--------------------------------------------------------------------*/

        private Person blink;        // if a Person is a node
        private int id;
        private int arr;
        private int dest;
        private int dir;
        private Person flink;        // if a Person is a node

        /*--------------------------------------------------------------------*/
        /* constructor: Person
        /*--------------------------------------------------------------------*/

        Person(int floor) {

            if (linked) {
                blink = flink = null;
            }

            id = ++pID;
            arr = tick;
            dest = destFloor(floor);
            dir = (floor < dest ? up : down);

        }
        /*--------------------------------------------------------------------*/
        /* accessors - Person
        /*--------------------------------------------------------------------*/

        private int Arr() {
            return (arr);
        }

        private int Dest() {
            return (dest);
        }

        private int Dir() {
            return dir;
        }

        private int Id() {
            return id;
        }
    }


    /*-------------------------------------------------------------------*/
    /* private method: Person
    /*-------------------------------------------------------------------*/

    /* DESTFLOOR(): determines destination floor. If not on floor 1, person has
    2/3 probability of going down to floor 1. */
    private static int destFloor(int floor) {
        Random rand = new Random();

        if (floor == 1) {
            return ((random(rand) % (floors - 1)) + 2);
            // 2 <= ? <= floors
        }
        if ((random(rand) % PF) != 0) {
            return (1);    // to floor 1	// probability of this
            //  equals (PF-1)/PF
        }
        int r = (random(rand) % (floors - 2)) + 2; // 2 <= r < floors
        return ((r < floor ? r : r + 1));     // 2 <= ? != 1 <= floors
    }
}
