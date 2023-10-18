package org.example.gnucash.write;

import java.io.File;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.write.GnucashWritableCustomer;
import org.gnucash.write.impl.GnucashWritableFileImpl;

public class GenCmdty {
    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName  = "example_in.gnucash";
    private static String gcshOutFileName = "example_out.gnucash";
    private static String name            = "Nanosoft Pears Corp.";
    private static String isin            = "Nanosoft Pears Corp.";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GenCmdty tool = new GenCmdty();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashWritableFileImpl gcshFile = new GnucashWritableFileImpl(new File(gcshInFileName));

	GnucashWritableCustomer cust = gcshFile.createWritableCustomer();
	cust.setNumber(GnucashCustomer.getNewNumber(cust));
	cust.setName(name);
	System.err.println("Customer: " + cust.getNumber() + " (" + cust.getName() + ")");

	gcshFile.writeFile(new File(gcshOutFileName));

	System.out.println(cust.getId());
    }
}