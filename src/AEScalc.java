import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.io.*;

/**
 *  AEScalc - main program for AES calculator applet.
 *
 *  @author Lawrie Brown <Lawrie.Brown@adfa.edu.au>
 *  @version 1.0, 8 Feb 2005
 *  @see <a href="http://www.unsw.adfa.edu.au/~lpb/">Lawrie Brown</a>
 */
public class AEScalc extends Applet 
			implements ActionListener, ItemListener {

    /** AES Cipher object used for calculations. */
    private AES myAES;

    /** GUI Components for the applet. */
    private Label	lTitle;
    private Label	lData;
    private Label	lKey;
    private Label	lResult;
    private Label	lTrace;
    private TextField	tData;
    private TextField	tKey;
    private TextField	tResult;
    private Button	bEncrypt;
    private Button	bDecrypt;
    private Button	bAbout;
    private Button	bQuit;

    /** checkboxes to specify desired level of trace information. */
    private CheckboxGroup	cbTrace;
    private Checkbox		cTrace0, cTrace1, cTrace2, cTrace3, cTrace4, cTrace5;

    /** area to display  diagnostic trace information. */
    private TextArea	taTrace;

    /** level of trace information desired. */
    private int traceLev = 2;

    /** Flag saying whether we're running as application or applet. */
    boolean		application = false;

    /** Usage and copyright message - displayed when applet first starts. */
    private static final String about = "AES Calculator Applet v2.0 Feb 2005.\n\n" +
    	"The AES Calculator applet is used to encrypt or decrypt test data values\n" +
	"using AES block cipher.  It takes a 128-bit (32 hex digit) data value and a\n" +
	"128/192/256-bit (32/48/64 hex digit) key.  It can optionally provide a trace\n" +
	"of the calculations performed, with varying degrees of detail.\n\n" +
	"AEScalc was written and is Copyright 2005 by Lawrie Brown.\n" +
	"Permission is granted to copy, distribute, and use this applet\n" +
	"provided the author is acknowledged and this copyright notice remains intact.\n\n" +
	"See http://www.unsw.adfa.edu.au/~lpb/ for authors website.\n";

    /** Init method creates the GUI. */
    public void init() {
	// build the GUI
	setLayout(new BorderLayout());

	// nice plain white background
	setBackground(Color.white);

	// title
	Font fTitle = new Font("Times", Font.BOLD, 24);
	lTitle = new Label("AES Block Cipher Calculator");
	lTitle.setAlignment(Label.CENTER);
	lTitle.setFont(fTitle);
	lTitle.setForeground(Color.blue);
	add("North", lTitle);
	
	// various inputs and controls
	Panel pControls = new Panel();
	GridBagLayout gbl = new GridBagLayout();
	pControls.setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.anchor = GridBagConstraints.WEST;
	Font fLabels = new Font("Courier", Font.BOLD, 14);

	lData  = new Label("Input Data (in hex)");
	lData.setAlignment(Label.LEFT);
	lData.setFont(fLabels);
	lData.setForeground(Color.blue);
	gbc.gridwidth = GridBagConstraints.RELATIVE;
	gbl.setConstraints(lData, gbc);
	pControls.add(lData);

	tData  = new TextField(2*AES.BLOCK_SIZE);
	tData.setText("");
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(tData, gbc);
	pControls.add(tData);

	lKey   = new Label("AES Key (in hex)");
	lKey.setAlignment(Label.LEFT);
	lKey.setFont(fLabels);
	lKey.setForeground(Color.blue);
	gbc.gridwidth = 1;
	gbl.setConstraints(lKey, gbc);
	pControls.add(lKey);

	tKey    = new TextField(2*AES.KEY_LENGTH);
	tKey.setText("");
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(tKey, gbc);
	pControls.add(tKey);

	lResult = new Label("Result displayed here.");
	lResult.setAlignment(Label.LEFT);
	lResult.setFont(fLabels);
	lResult.setForeground(Color.blue);
	gbc.gridwidth = 1;
	gbl.setConstraints(lResult, gbc);
	pControls.add(lResult);

	tResult = new TextField(2*AES.BLOCK_SIZE);
	tResult.setEditable(false);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(tResult, gbc);
	pControls.add(tResult);

	// buttons to request various actions
	Panel pButtons = new Panel();
	bEncrypt = new Button("Encrypt");
	bEncrypt.setForeground(Color.blue);
	pButtons.add(bEncrypt);
	bEncrypt.addActionListener(this);
	bDecrypt = new Button("Decrypt");
	bDecrypt.setForeground(Color.blue);
	pButtons.add(bDecrypt);
	bDecrypt.addActionListener(this);
	bAbout = new Button("About");
	bAbout.setForeground(Color.blue);
	pButtons.add(bAbout);
	bAbout.addActionListener(this);
	bQuit = new Button("Quit");
	bQuit.setForeground(Color.blue);
	if (application) {	// only place Quit button if an application
	    pButtons.add(bQuit);
	    bQuit.addActionListener(this);
	}
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(pButtons, gbc);
	pControls.add(pButtons);

	// and place panels into main GUI
	add("Center", pControls);

	// lastly build the trace info text area
	Panel pTrace = new Panel();
	pTrace.setLayout(new BorderLayout());
	lTrace  = new Label("Trace of AES Calculations or Errors");
	lTrace.setAlignment(Label.CENTER);
	lTrace.setFont(fLabels);
	lTrace.setForeground(Color.blue);
	pTrace.add("North", lTrace);

	cbTrace = new CheckboxGroup();
	Panel pCB = new Panel();
	Label lCB = new Label("Trace Level: ");
	lCB.setFont(fLabels);
	lCB.setForeground(Color.blue);
	pCB.add(lCB);
	cTrace0 = new Checkbox("0: none", cbTrace, (traceLev == 0 ? true : false));
	pCB.add(cTrace0);
	cTrace0.addItemListener(this);
	cTrace1 = new Checkbox("1: calls", cbTrace, (traceLev == 1 ? true : false));
	if (traceLev >= 1)	pCB.add(cTrace1);
	cTrace1.addItemListener(this);
	cTrace2 = new Checkbox("2: +rounds", cbTrace, (traceLev == 2 ? true : false));
	if (traceLev >= 2)	pCB.add(cTrace2);
	cTrace2.addItemListener(this);
	cTrace3 = new Checkbox("3: +steps", cbTrace, (traceLev == 3 ? true : false));
	if (traceLev >= 3)	 pCB.add(cTrace3);
	cTrace3.addItemListener(this);
	cTrace4 = new Checkbox("4: +subkeys", cbTrace, (traceLev == 4 ? true : false));
	if (traceLev >= 4)	 pCB.add(cTrace4);
	cTrace4.addItemListener(this);
	cTrace5 = new Checkbox("5: +static", cbTrace, (traceLev >= 5 ? true : false));
	if (traceLev >= 5)	 pCB.add(cTrace5);
	cTrace5.addItemListener(this);
	pTrace.add("Center", pCB);

	Font fTrace = new Font("Courier", Font.PLAIN, 12);
	taTrace = new TextArea(about, 20, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
	taTrace.setEditable(false);
	taTrace.setFont(fTrace);
	taTrace.setBackground(Color.white);
	pTrace.add("South", taTrace);

	add("South", pTrace);
    
        // create the AES cipher object to use
        myAES = new AES();
	// and set level of trace info 
	myAES.traceLevel = traceLev;
    }
    
    /**
     *  respond to Action Event: pressing one of the buttons.
     */
    public void actionPerformed (ActionEvent e) {
	byte[]  data;				/* 128-bit AES data */
	byte[]  key;				/* 128/192/256-bit AES key */
	byte[]  result;				/* 128-bit AES result */
	String	hexdata;			/* hex string to use as data */
	String	hexkey;				/* hex string to use as key */
	String	info = "";			/* trace info to display */
	Object	source = e.getSource();		/* identify action source */

	if (source == bEncrypt) {		// want to encrypt the data
	    // now extract key and data
	    hexkey  = tKey.getText();
	    int keylen = hexkey.length();
	    if (! ((keylen == 32) || (keylen == 48) || (keylen == 64)) ||
	        (!Util.isHex(hexkey))) {
		lResult.setForeground(Color.red);
		lResult.setText("Error in Key!!!");
		taTrace.setForeground(Color.red);
		taTrace.setText("AES key [" + hexkey +
		    "] must be 32 or 48 or 64 hex digits long.");
		tResult.setText("");
		return;
	    }
	    key = Util.hex2byte(hexkey);

	    hexdata = tData.getText();
	    if ((hexdata.length() != (2*AES.BLOCK_SIZE)) ||
	        (!Util.isHex(hexdata))) {
		lResult.setForeground(Color.red);
		lResult.setText("Error in Data!!!");
		taTrace.setForeground(Color.red);
		taTrace.setText("AES data [" + hexdata +
		    "] must be strictly " + (2*AES.BLOCK_SIZE) +
                    " hex digits long.");
		tResult.setText("");
		return;
	    }
	    data = Util.hex2byte(hexdata);
    
	    myAES.setKey(key);
	    if (traceLev>0) info += myAES.traceInfo;
    
	    result = myAES.encrypt(data);
	    if (traceLev>0) info += myAES.traceInfo;

	    lResult.setForeground(Color.blue);
	    lResult.setText("Encrypted value is:");
	    tResult.setText(Util.toHEX1(result));
	    taTrace.setForeground(Color.black);
	    taTrace.setText(info);

	} else if (source == bDecrypt) {	// want to decrypt the data
	    // now extract key and data
	    hexkey  = tKey.getText();
	    int keylen = hexkey.length();
	    if (! ((keylen == 32) || (keylen == 48) || (keylen == 64)) ||
	        (!Util.isHex(hexkey))) {
		lResult.setForeground(Color.red);
		lResult.setText("Error in Key!!!");
		taTrace.setForeground(Color.red);
		taTrace.setText("AES key [" + hexkey +
		    "] must be 32 or 48 or 64 hex digits long.");
		tResult.setText("");
		return;
	    }
	    key = Util.hex2byte(hexkey);

	    hexdata = tData.getText();
	    if ((hexdata.length() != (2*AES.BLOCK_SIZE)) ||
	        (!Util.isHex(hexdata))) {
		lResult.setForeground(Color.red);
		lResult.setText("Error in Data!!!");
		taTrace.setForeground(Color.red);
		taTrace.setText("AES data [" + hexdata +
		    "] must be strictly " + (2*AES.BLOCK_SIZE) +
                    " hex digits long.");
		tResult.setText("");
		return;
	    }
	    data = Util.hex2byte(hexdata);
    
	    myAES.setKey(key);
	    if (traceLev>0) info += myAES.traceInfo;
    
	    result = myAES.decrypt(data);
	    if (traceLev>0) info += myAES.traceInfo;

	    lResult.setForeground(Color.blue);
	    lResult.setText("Decrypted value is:");
	    tResult.setText(Util.toHEX1(result));
	    taTrace.setForeground(Color.black);
	    taTrace.setText(info);

	} else if (source == bAbout) {
	    taTrace.setForeground(Color.black);	// display about message
	    taTrace.setText(about);
	} else if ((source == bQuit) && application) {
	    System.exit(0);	// can only quit applications
	}
    }
    
    
    /**
     *  respond to ItemEvent: pressing one of the checkboxes.
     */
    public void itemStateChanged (ItemEvent e) {
	Object	source = e.getSource();		/* identify action source */

	if (source == cTrace0) traceLev = 0;
	else if (source == cTrace1) traceLev = 1;
	else if (source == cTrace2) traceLev = 2;
	else if (source == cTrace3) traceLev = 3;
	else if (source == cTrace4) traceLev = 4;
	else if (source == cTrace5) traceLev = 5;
	
  	myAES.traceLevel = traceLev;

	taTrace.setForeground(Color.black);
	taTrace.setText("AES trace level set to " + traceLev);
    }


    /** usage description for command-line invocation of the AES calculator. */
    private static final String usage = "Usage:\n" +
	"AEScalc [-tlevel]\n" +
	"	- run AES calculator as GUI applet (with specified trace-level)\n" +
	"AEScalc [-e|-d] [-tlevel] hexkey hexdata\n" +
	"	- AES en/decrypt data using key (both in hex)\n" +
	"	- with trace details at specified level (0-5)\n";

    //......................................................................
    /** AEScalc program main routine.
	@param args command line arguments
     */
    public static void main (String[] args) {
    
	byte[]  data;				/* 64-bit AES data */
	byte[]  key;				/* 128-bit AES key */
	byte[]  result;				/* 64-bit AES result */
	String	hexdata;			/* hex string to use as data */
	String	hexkey;				/* hex string to use as key */
	boolean	encrypting = true;		/* encrypt/decrypt flag */
	int	trace = -1;			/* level to trace at */
	int	curr = 0;			/* current argument index */
	int	argc = args.length;		/* total number of arguments */
    
	// parse command-line flags
	while ((argc > 0) && (args[curr].startsWith("-"))) {
	    if (args[curr].equals("-e"))
	        encrypting = true;
	    else if (args[curr].equals("-d"))
	        encrypting = false;
	    else if (args[curr].startsWith("-t"))
	        trace = Integer.parseInt(args[curr].substring(2));
	    else {
	        System.err.println("Unknown flag: "+args[curr]+"\n"+usage+"\n"+about);
	        System.exit(1);
	    }
	    curr++;
	    argc--;
	}

	// if no other args run as GUI
	if (argc == 0) {
	    // GUI interface - setup some System properties (esp for optimum MacOSX use)
	    System.setProperty("apple.laf.useScreenMenuBar", "true");      // use Mac-style menus
	    // create frame to display graphics in
	    Frame	fr = new Frame ("AES Block Cipher Calculator");
	    // create a new instance of this class to run
	    AEScalc	app = new AEScalc();
	    app.application = true; // running as application
	    if (trace > 0) app.traceLev = trace; // set desired trace level
	    // and set the applet running displayed in the frame
	    app.init();             // run init method
	    fr.add( "Center", app); // insert into frame
	    fr.setSize(800,1000);    // specify size of frame
	    fr.pack();              // pack components in
	    fr.show();              // display on screen
	    app.start();            // then run applet code

	// check still have 2 more values for key & data
	} else if (argc < 2) {
		System.err.println(usage);
		System.exit(1);

	// run in command-line mode
	} else {
	    // now extract key and data
	    hexkey  = args[curr++];
	    int keylen = hexkey.length();
	    if (! ((keylen == 32) || (keylen == 48) || (keylen == 64)) ||
	        (!Util.isHex(hexkey))) {
		System.err.println("AES key [" + hexkey +
		    "] must be 32 or 48 or 64 hex digits long." + usage);
		System.exit(1);
	    }
	    key = Util.hex2byte(hexkey);

	    hexdata = args[curr++];
	    if ((hexdata.length() != (2*AES.BLOCK_SIZE)) ||
	        (!Util.isHex(hexdata))) {
		System.err.println("AES data [" + hexdata +
		    "] must be strictly " + (2*AES.BLOCK_SIZE) +
                    " hex digits long.\n" + usage);
		System.exit(1);
	    }
	    data = Util.hex2byte(hexdata);

	    // now do actual en/decryption and display trace data
	    // icreate AES cipher object
	    AES testAES = new AES();

	    // set level of trace info 
	    if (trace > 0) testAES.traceLevel = trace; // set desired trace level

	    // trace static tables if requested
	    if (trace>4)	testAES.trace_static();

	    testAES.setKey(key);
	    if (trace>0) System.out.print(testAES.traceInfo);

	    if (encrypting) {
		result = testAES.encrypt(data);
		if (trace>0) System.out.print(testAES.traceInfo);
		System.out.println(Util.toHEX1(result));
	    } else {
		result = testAES.decrypt(data);
		if (trace>0) System.out.print(testAES.traceInfo);
		System.out.println(Util.toHEX1(result));
	    }
	}
    }
}
