package org.example.gnucash.write;

import java.io.File;
import java.time.LocalDateTime;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.OwnerNotFoundException;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableCustomerJob;
import org.gnucash.write.spec.GnucashWritableVendorJob;

public class GenJob {
    enum JobType {
	CUSTOMER, 
	VENDOR
    }

    // -----------------------------------------------------------------

    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName  = "example_in.gnucash";
    private static String gcshOutFileName = "example_out.gnucash";
    private static JobType type           = JobType.CUSTOMER;
    private static String custID          = "1d2081e8a10e4d5e9312d9fff17d470d";
    private static String vendID          = "bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7";
    private static String number          = "1234";
    private static String name            = "Jobbo McJob";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GenJob tool = new GenJob();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashWritableFileImpl gcshFile = new GnucashWritableFileImpl(new File(gcshInFileName));

	GnucashGenerJob job = null;
	if (type == JobType.CUSTOMER)
	    job = doCustomer(gcshFile);
	else if (type == JobType.VENDOR)
	    job = doVendor(gcshFile);

	System.out.println("Job to write: " + job.toString());
	gcshFile.writeFile(new File(gcshOutFileName));
	System.out.println("OK");
    }

    // -----------------------------------------------------------------

    private GnucashWritableCustomerJob doCustomer(GnucashWritableFileImpl gcshFile) 
	    throws OwnerNotFoundException {
	GnucashCustomer cust = null;
	try {
	    cust = gcshFile.getCustomerByID(custID);
	    System.err.println("Customer: " + cust.getNumber() + " (" + cust.getName() + ")");
	} catch (Exception exc) {
	    System.err.println("Error: No customer with ID '" + custID + "' found");
	    throw new OwnerNotFoundException();
	}

	GnucashWritableCustomerJob job = gcshFile.createWritableCustomerJob(cust, number, name);
	job.setName("Generated by GenJob " + LocalDateTime.now().toString());

	return job;
    }

    private GnucashWritableVendorJob doVendor(GnucashWritableFileImpl gcshFile)
	    throws OwnerNotFoundException, WrongInvoiceTypeException, TaxTableNotFoundException {
	GnucashVendor vend = null;
	try {
	    vend = gcshFile.getVendorByID(vendID);
	    System.err.println("Vendor: " + vend.getNumber() + " (" + vend.getName() + ")");
	} catch (Exception exc) {
	    System.err.println("Error: No vendor with ID '" + vendID + "' found");
	    throw new OwnerNotFoundException();
	}

	GnucashWritableVendorJob job = gcshFile.createWritableVendorJob(vend, number, name);
	job.setName("Generated by GenJob " + LocalDateTime.now().toString());

	return job;
    }
}
