import java.io.*;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList ;

public class Lifts {
    private static final int M=5;	// max distance elevator moves per tick
    private static final int PF=3;	// prob (PF-1)/PF that person on floor != 1
    //  chooses floor 1 as next destination floor


    /*------------------------------------------------------------*/
    /* global variables					      */
    /*------------------------------------------------------------*/

    static Boolean linked;
    static int capacity,duration,elevators,floors,maxt,mint,pid,
            tick,tott,v0,v1;
    static double prob;			// "passengers" in file

    public static void main( String[] args ) throws IOException {

        /*------------------------------------------------------------*/
        /* local variables					      */
        /*------------------------------------------------------------*/

        String [] defvalue={"linked","32","0.03","1","10","500"};
        String [] key={"structures","floors","passengers","elevators",
                "elevatorCapacity","duration"};
        String [] value=new String[defvalue.length];

        /*------------------------------------------------------------*/
        /* checking possible input file				      */
        /*------------------------------------------------------------*/

        if ( args.length == 0 ) {
            System.arraycopy( defvalue,0,value,0,defvalue.length );
        } else {
            getvalues( args[0],key,value,defvalue );
        }
        System.out.println( "-- default values --\n" );
        for ( int i = 0;  i < defvalue.length;  i++ ) {
            System.out.println( value[i] );
        }
        linked = (value[0].equals( "linked" ));
        // linked = false;
        capacity = Integer.parseInt( value[4] );
        duration = Integer.parseInt( value[5] );
        elevators = Integer.parseInt( value[3] );
        floors = Integer.parseInt( value[1] );
        prob = Double.parseDouble( value[2] );
        System.out.println( "\n-- actual values --\n" );
        System.out.println( "structures       = "+"\""+value[0]+"\"" );
        System.out.println( "floors           = "+floors );
        System.out.println( "passengers       = "+prob );
        System.out.println( "elevators        = "+elevators );
        System.out.println( "elevatorCapacity = "+capacity );
        System.out.println( "duration         = "+duration );

        /*------------------------------------------------------------*/
        /* initializing stuff					      */
        /*------------------------------------------------------------*/

        duration = 500;				// actually used here
        elevators = 4;				// "
        floors = 32;				// "
        prob = 0.03125;				// "  1/32, exact


        Elevator [] elevatora=new Elevator [elevators];
        Floor [] floora=new Floor [floors+1];
        Boolean [] pushup=new Boolean [floors+1];
        Boolean [] pushdown=new Boolean [floors+1];
        Integer [] wait=new Integer [floors+1];
        for ( int e = 0;  e < elevators;  e++ ) {
            elevatora[e] = new Elevator();
        }
        for ( int f = 0;  f <= floors;  f++ ) {
            floora[f] = new Floor();
        }
        for ( int f = 1;  f <= floors;  f++ ) {
            pushup[f] = pushdown[f] = false;// call elevator buttons
            wait[f] = 0;
        }
        mint = Integer.MAX_VALUE;		// for min
        maxt = tott = 0;			// for mean, max
        pid = v0 = v1 = 0;	// people id's; visitors; who reach dest

        /*------------------------------------------------------------*/
        /* the actual work					      */
        /*------------------------------------------------------------*/

        long t = System.currentTimeMillis();
        for ( tick = 1;  tick <= duration;  tick++ ) {
            arrivals( floora,wait,pushup,pushdown );
            departures( floora,wait,pushup,pushdown,elevatora );
        }
        t = System.currentTimeMillis()-t;

        /*------------------------------------------------------------*/
        /* the results						      */
        /*------------------------------------------------------------*/

        int r=0,w=0;				// riders, waiters

        for ( int f = 1;  f <= floors;  f++ ) {
            if ( 0 < wait[f] ) {
                w += wait[f];
                System.out.format( "%4d-th floor:",f );
                floora[f].display();
            }
        }
        for ( int e = 0;  e < elevators;  e++ ) {
            Elevator car = elevatora[e];
            if ( 0 < car.Load() ) {
                r += car.Load();
                System.out.format( "%4d-th elevator:",e );
                car.display();
            }
        }
        System.out.format( "%4d visitors\n",v0 );
        System.out.format( "%4d to destination\n",v1 );
        System.out.format( "%4d waiting on floors\n",w );
        System.out.format( "%4d  riding in elevator(s)\n",r );
        System.out.format( "%4d minimum time from arrival to "+
                "destination\n",mint );
        System.out.format( "%7.2f mean time from arrival to "+
                "destination\n",(double)tott/(double)v1 );
        System.out.format( "%4d maximum time from arrival to "+
                "destination\n",maxt );
        System.out.format( "%4d milliseconds to do simulation\n",t );
    }

    private static void arrivals( Floor [] floora,Integer [] wait,
                                  Boolean [] b0,Boolean [] b1 ) {

        for ( int f = 1;  f <= floors;  f++ ) {
            System.out.format( "-- arrivals: f =%2d --\n",f );
            Floor floor = floora[f];
            int n = npersons();	// usually 0
            System.out.format( "-- arrivals: f =%3d, n =%2d --\n",f,n );
            v0 += n;
            wait[f] += n;
            for ( int i = n;  0 < i--; ) {	// n times
                Person p = new Person( f,b0,b1 );
                System.out.format( "-- tick%4d - no."+
                        "%4d enters on floor%3d, dest =%"+
                        "3d\n",tick,p.Id(),f,p.Dest() );
                floor.append( p );
            }
            if ( n != 0 ) {
                floor.ckbuttons( f,b0,b1 );
            }
        }
    }

    private static void departures( Floor [] floora,Integer [] wait,
                                    Boolean [] b0,Boolean [] b1,Elevator [] elevatora ) {

        for ( int e = 0;  e < elevators;  e++ ) {
            Elevator car = elevatora[e];
            int curr = car.Curr();
            if ( 0 < car.Load() ) {
                car.unload( curr );
            }
            int dir = (car.Prev() < curr ? 0 : 1); // 0 up 1 down
            if ( wait[curr] != 0 ) {
                car.getPassengers( floora[curr],wait,curr,
                        b0,b1,e,dir );
            }
            car.toNextFloor( curr,dir,b0,b1 );
        }
    }

    private static int destfloor( int floor ) {
        Random rand=new Random();

        if ( floor == 1 ) {
            return( (random( rand ) % (floors-1))+2 );
            // 2 <= ? <= floors
        }
        if ( (random( rand ) % PF) != 0 ) {
            return( 1 );	// to floor 1	// probability of this
            //  equals (PF-1)/PF
        }
        int r = (random( rand ) % (floors-2))+2; // 2 <= r < floors
        return( (r < floor ? r : r+1) );     // 2 <= ? != 1 <= floors
    }

    private static void getvalues( String fname,String [] key,String [] v,
                                   String [] dv ) {

        try {
            FileReader reader = new FileReader( fname );
            Properties p = new Properties();
            p.load( reader );
            for ( int i = 0;  i < key.length;  i++ ) {
                v[i] = p.getProperty( key[i] );
                if ( v[i] == null ) {
                    v[i] = dv[i];
                }
            }
        } catch ( Exception e ) {
            System.out.println( "** bad properties file? **" );
            System.exit( 0 );
        }
    }

    private static int max( int a,int b ) {

        return( (a <= b ? b : a) );
    }

    private static int min( int a,int b ) {

        return( (a <= b ? a : b) );
    }

    private static int npersons() {

        return( (prob < new Random().nextDouble() ? 0 : 1) );
    }

    /*--------------------------------------------------------------------*/
    /* Declare: Random rand=new Random(); in calling function.	      */
    /* Returns randomly generated int in [0,Integer.MAX_VALUE].	      */
    /*--------------------------------------------------------------------*/

    private static int random( Random rand ) {

        int r = rand.nextInt();
        if ( r == Integer.MIN_VALUE ) {
            r = Integer.MAX_VALUE;
        }
        if ( r < 0 ) {
            r = -r;
        }
        return( r );
    }

    private static class Elevator {

        /*--------------------------------------------------------------------*/
        /* private data members - Elevator				      */
        /*--------------------------------------------------------------------*/

        private Passenger head,tail;	// an Elevator is a doubly linked list
        private ArrayList<Passenger> elist;	// or an ArrayList
        private int curr,load,prev;

        /*--------------------------------------------------------------------*/
        /* constructor - Elevator					      */
        /*--------------------------------------------------------------------*/

        Elevator() {

            head = tail = null;
            this.elist = new ArrayList<Passenger>();
            load = prev = 0;			// must go up from 1
            curr = 1;
        }

        /*--------------------------------------------------------------------*/
        /* accessors/mutators - Elevator				      */
        /*--------------------------------------------------------------------*/

        private int Curr() {
            return( curr );
        }

        private int Load() {
            return( load );
        }

        private int Prev() {
            return( prev );
        }

        private void setCurr( int f ) {
            curr = f;
        }

        private void setPrev( int f ) {
            prev = f;
        }

        /*--------------------------------------------------------------------*/
        /* private methods - Elevator					      */
        /*--------------------------------------------------------------------*/

        private void accept( Passenger p ) {

            if ( linked ) {
                if ( head == null ) {
                    head = p;
                    p.blink = null;
                } else {
                    tail.flink = p;
                    p.blink = tail;
                }
                tail = p;
            } else {
                elist.add( p );
            }
            load++;
        }

        private void display() {

            int i = 0;
            Passenger p = (linked ? head : elist.get( i++ ));
            while ( p != null ) {
                System.out.print( " ["+p.id+","+p.arr+","+p.dest+"]" );
                p = nextPassenger( p,i++ );
            }
            System.out.println();
        }

        private void getPassengers( Floor floor,Integer [] wait,int curr,
                                    Boolean [] b0,Boolean [] b1,int e,int dir ) {

            int i = 0;
            Person p = (linked ? floor.Head() : floor.Get( i++ ));
            while ( p != null ) {
                if ( capacity <= load ) {
                    break;
                }
                int dirp = (curr < p.Dest() ? 0 : 1);
                if ( dirp == dir ) {
                    accept( new Passenger( p.Id(),p.Arr(),
                            p.Dest() ) );
                    System.out.format( "-- tick%4d - no.%4d enters"+
                            " elevator%3d:",tick,p.Id(),e );
                    display();
                    floor.goodbye( p );
                    wait[curr]--;
                }
                p = floor.nextPerson( p,i++ );
            }
            floor.ckbuttons( curr,b0,b1 );			// check buttons
        }

        private int goodbye( Passenger p ) {

            if ( linked ) {
                if ( p == head ) {
                    head = p.flink;
                } else {
                    p.blink.flink = p.flink;
                }
                if ( p == tail ) {
                    tail = p.blink;
                } else {
                    p.flink.blink = p.blink;
                }
            } else {
                elist.remove( p );
            }
            System.out.format( "-- tick%4d - no.%4d exits  on floor%3d\n",
                    tick,p.Id(),p.Dest() );
            --load;
            v1++;
            return( tick-p.Arr() );
        }

        private Passenger nextPassenger( Passenger p,int i ) {

            if ( linked ) {
                return( p.flink );
            } else {
                return (i < elist.size() ? elist.get(i) : null) ;
            }
        }



        private void toNextFloor( int curr,int dir,Boolean [] b0,
                                  Boolean [] b1 ) {
            int i,j,k;

            if ( load == 0 ) {				// empty car
                for ( i = curr;  i < floors-1; ) {
                    if ( b0[++i] ) {
                        i = min( i,curr+M );
                        setPrev( i-1 );
                        setCurr( i );		// go to i
                        return;
                    }
                }			// nobody above wants up
                for ( i = curr;  i < floors; ) {
                    if ( b1[++i] ) {
                        i = min( i,curr+M );
                        setPrev( i+1 );
                        setCurr( i );		// go to i
                        return;
                    }
                }			// nobody above wants down
                for ( j = curr;  2 < j; ) {
                    if ( b1[--j] ) {
                        j = max( j,curr-M );
                        setPrev( j+1 );
                        setCurr( j );		// go to j
                        return;
                    }
                }			// nobody below wants down
                for ( j = curr;  2 <= j; ) {
                    if ( b0[--j] ) {
                        j = max( j,curr-M );
                        setPrev( j-1 );
                        setCurr( j );		// go to j
                        return;
                    }
                }			// nobody below wants up
                return;
            }
            if ( dir == 0 ) {			// going up
                i = floors;
                k = 0;
                Passenger p = (linked ? head : elist.get( k++ ));
                while ( p != null ) {
                    int d = p.Dest();
                    if ( d <= i ) {
                        i = d;
                    }
                    p = nextPassenger( p,k++ );
                }
                for ( j = curr;  j < floors; ) {
                    if ( b0[++j] ) {	// waiter going up?
                        break;
                    }
                }
                if ( j < i ) {
                    i = j;
                }
                i = min( i,curr+M );
                setPrev( i-1 );
                setCurr( i );
            } else {				// going down
                i = 1;				// bottom floor
                k = 0;
                Passenger p = (linked ? head : elist.get( k++ ));
                while ( p != null ) {
                    int d = p.Dest();
                    if ( i <= d ) {
                        i = d;
                    }
                    p = nextPassenger( p,k++ );
                }
                for ( j = curr;  1 < j; ) {
                    if ( b1[--j] ) {
                        break;
                    }
                }
                if ( j < i ) {
                    j = i;
                }
                j = max( j,curr-M );
                setPrev( j+1 );
                setCurr( j );
            }
            return;
        }

        private void unload( int floor ) {

            System.out.println( "-- unloading --" );
            int i = 0;
            Passenger p = (linked ? head : elist.get( i++ ));
            while ( p != null ) {
                if ( p.Dest() == floor ) {
                    int t = goodbye( p );
                    if ( t < mint ) {
                        mint = t;	// new min time
                    }
                    tott += t;		// sum of times
                    if ( maxt < t ) {
                        maxt = t;	// new max time
                    }
                }
                p = nextPassenger( p,i++ );
            }
            System.out.println( "-- unloaded --" );
        }
    }

    private static class Floor {

        /*--------------------------------------------------------------------*/
        /* private data members - Floor					      */
        /*--------------------------------------------------------------------*/

        private Person head,tail;	// a Floor is a doubly linked list
        private ArrayList<Person> flist; // or not

        /*--------------------------------------------------------------------*/
        /* constructor - Floor						      */
        /*--------------------------------------------------------------------*/

        Floor() {

            head = tail = null;	// empty Floor
            this.flist = new ArrayList<Person>();
        }

        /*--------------------------------------------------------------------*/
        /* accessor - Floor						      */
        /*--------------------------------------------------------------------*/

        private ArrayList<Person> Flist() {
            return( flist );
        }

        private Person Get( int i ) {
            return( flist.get( i ) );
        }

        private Person Head() {
            return( head );
        }

        /*--------------------------------------------------------------------*/
        /* private methods - Floor					      */
        /*--------------------------------------------------------------------*/

        private void append( Person p ) {

            if ( linked ) {
                if ( head == null ) {
                    head = p;
                    p.blink = null;
                } else {
                    tail.flink = p;
                    p.blink = tail;
                }
                p.flink = null;
                tail = p;
            } else {
                flist.add( p );
            }
        }

        private void ckbuttons( int curr,Boolean [] b0,Boolean [] b1 ) {
            int i;

            if(!linked && flist.size() == 0){
                return ;
            }
            b0[curr] = b1[curr] = false;
            i = 0;
            Person p = (linked ? head : flist.get( i++ ));
            while ( p != null ) {
                if ( curr < p.Dest() ) {
                    b0[curr] = true;
                    break;
                }
                p = nextPerson( p,i++ );
            }
            i = 0;
            p = (linked ? head : flist.get( i++ ));
            while ( p != null ) {
                if ( p.Dest() < curr ) {
                    b1[curr] = true;
                    break;
                }
                p = nextPerson( p,i++ );
            }
        }

        private void display() {
            if ((head == null) || (!linked && flist.size() == 0)){
                return ;
            }
            for ( Person p = head;  p != null;  p = p.flink ) {
                System.out.format( " (%d,%d,%d)",p.Id(),p.Arr(),
                        p.Dest() );
            }
            System.out.println();
        }

        private void goodbye( Person p ) {

            if ( linked ) {
                if ( p == head ) {
                    head = p.flink;
                } else {
                    p.blink.flink = p.flink;
                }
                if ( p == tail ) {
                    tail = p.blink;
                } else {
                    p.flink.blink = p.blink;
                }
            } else {
                flist.remove( p );
            }
        }

        private Person nextPerson( Person p,int i ) {

            if ( linked ) {
                return( p.flink );
            } else {
                return (i < flist.size() ? flist.get(i) : null) ;
            }
        }
    }

    private static class Passenger {

        /*--------------------------------------------------------------------*/
        /* private data members - Passenger				      */
        /*--------------------------------------------------------------------*/

        private Passenger blink;	// a Passenger is a node
        private int id;
        private int arr;
        private int dest;
        private Passenger flink;

        /*--------------------------------------------------------------------*/
        /* constructor - Passenger					      */
        /*--------------------------------------------------------------------*/

        Passenger( int id0,int arr0,int dest0 ) {

            id = id0;
            arr = arr0;
            dest = dest0;
        }

        /*--------------------------------------------------------------------*/
        /* accessors - Passenger					      */
        /*--------------------------------------------------------------------*/

        private int Arr() {
            return( arr );
        }

        private int Dest() {
            return( dest );
        }

        private int Id() {
            return( id );
        }
    }

    private static class Person {

        /*--------------------------------------------------------------------*/
        /* private data members - Person				      */
        /*--------------------------------------------------------------------*/

        private Person blink;		// if a Person is a node
        private int id;
        private int arr;
        private int dest;
        private Person flink;		// if a Person is a node

        /*--------------------------------------------------------------------*/
        /* constructor - Person						      */
        /*--------------------------------------------------------------------*/

        Person( int floor,Boolean [] b0,Boolean [] b1 ) {

            id = ++pid;
            arr = tick;
            dest = destfloor( floor );
        }

        /*--------------------------------------------------------------------*/
        /* accessors - Person						      */
        /*--------------------------------------------------------------------*/

        private int Arr() {
            return( arr );
        }

        private int Dest() {
            return( dest );
        }

        private Person Flink() {
            return flink;
        }

        private int Id() {
            return id;
        }
    }


}
