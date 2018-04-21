import java.io.*;
import java.util.Random;

/** GenAES - main program for random AES triples generator
  
    @author Lawrie Brown <Lawrie.Brown@adfa.edu.au>      Feb 2002
 */
public class GenAES {

    /** AES Cipher object used for calculations. */
    private AES myAES;

    /** usage description for command-line invocation of the AES calculator. */
    private static final String usage = "Usage:\n" +
	"GenAES [n]\n" +
	"	- generate 1 [n] random AES triples\n";

    //......................................................................
    /** ideacalc program main routine.
	@param args command line arguments
     */
    public static void main (String[] args) {
    
	byte[]  key = new byte[AES.BLOCK_SIZE];	/* 128-bit AES key */
	byte[]  data = new byte[AES.BLOCK_SIZE]; /* 128-bit AES data */
	byte[]  result;				/* 128-bit AES result */
	AES	testAES;			/* AES cipher object */
	Random	gen;				/* random number generator */
	int	numTriples = 1;			/* no triples to generate */
    
	/* check command-line args for number of triples desired */
	if (args.length > 0) {
	    numTriples = Integer.parseInt(args[0]);
	}

	/* create a new random generator object, seeded with current time */
	gen = new Random(System.currentTimeMillis());

	/* display initial comment */
	System.out.println("# Random AES (key,plain,cipher) triples");

	/* loop for desired number of triples */
	for (int i = 0; i < numTriples; i++) {
	    /* load arrays with random key and data values for this triple */
	    gen.nextBytes(key);
	    gen.nextBytes(data);

	    /* now create AES cipher object and encrypt data to get cipher */
	    testAES = new AES();
	    testAES.setKey(key);
	    result = testAES.encrypt(data);

	    /* and display the resulting triple */
	    System.out.println( Util.toHEX1(key) + " " +
		Util.toHEX1(data)  + " " + Util.toHEX1(result) );
        }
    }
}

