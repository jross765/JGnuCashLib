package org.gnucash.api.read.spec;

import java.util.Collection;

import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.read.GnucashCustomer;
import org.gnucash.api.read.GnucashGenerInvoice;
import org.gnucash.api.read.GnucashGenerJob;
import org.gnucash.api.read.GnucashVendor;
import org.gnucash.api.read.aux.GCshOwner;

/**
 * This class represents an invoice that is sent to a customer
 * so (s)he knows what to pay you. <br>
 * <br>
 * Note: The correct business term is "invoice" (as opposed to "bill"), 
 * as used in the GnuCash documentation. However, on a technical level, both 
 * customer invoices and vendor bills are referred to as "GncInvoice" objects.
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the Invoice was
 * created and secondarily on the date it should be paid.
 *
 * @see GnucashGenerJob
 * @see GnucashCustomer
 */
public interface GnucashJobInvoice extends GnucashGenerInvoice {

    /**
     * @return ID of customer this invoice/bill has been sent to.
     * 
     * Note that a job may lead to multiple o no invoices.
     * (e.g. a monthly payment for a long lasting contract.)
     * @return the ID of the job this invoice is for.
     */
    GCshID getJobId();

    GCshOwner.Type getJobType();

    // ----------------------------

    /**
     * @return ID of customer this invoice has been sent to.
     */
    GCshID getCustomerId() throws WrongInvoiceTypeException;

    /**
     * @return ID of vendor this bill has been sent from.
     */
    GCshID getVendorId() throws WrongInvoiceTypeException;
    
    // ----------------------------

    /**
     * @return the job this invoice is for
     */
    GnucashGenerJob getGenerJob();
	
    /**
     * @return Job of customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomerJob getCustJob() throws WrongInvoiceTypeException;
	
    /**
     * @return Job of vendor this bill has been sent from.
     * @throws WrongInvoiceTypeException 
     */
    GnucashVendorJob getVendJob() throws WrongInvoiceTypeException;
	
    // ----------------------------

    /**
     * @return Customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomer getCustomer() throws WrongInvoiceTypeException;
	
    /**
     * @return Vendor this bill has been sent from.
     * @throws WrongInvoiceTypeException 
     */
    GnucashVendor getVendor() throws WrongInvoiceTypeException;
	
    // ---------------------------------------------------------------

    GnucashJobInvoiceEntry getEntryById(GCshID id) throws WrongInvoiceTypeException;

    Collection<GnucashJobInvoiceEntry> getEntries() throws WrongInvoiceTypeException;

    void addEntry(final GnucashJobInvoiceEntry entry);
    
}