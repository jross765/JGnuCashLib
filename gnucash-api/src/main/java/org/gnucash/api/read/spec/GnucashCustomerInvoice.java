package org.gnucash.api.read.spec;

import java.util.Collection;

import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.read.GnucashCustomer;
import org.gnucash.api.read.GnucashGenerInvoice;
import org.gnucash.api.read.GnucashGenerJob;

/**
 * This class represents an invoice that is sent to a customer
 * so (s)he knows what to pay you. <br>
 * <br>
 * Note: The correct business term is "invoice" (as opposed to "bill"), 
 * as used in the GnuCash documentation. However, on a technical level, both 
 * customer invoices and vendor bills are referred to as "GncInvoice" objects.
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the 
 * invoice was created and secondarily on the date it should be paid.
 *
 * @see GnucashGenerJob
 * @see GnucashCustomer
 */
public interface GnucashCustomerInvoice extends GnucashGenerInvoice {

    /**
     * @return ID of customer this invoice has been sent to.
     */
    GCshID getCustomerId();

    /**
     * @return Customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomer getCustomer() throws WrongInvoiceTypeException;
	
    // ---------------------------------------------------------------

    GnucashCustomerInvoiceEntry getEntryById(GCshID id) throws WrongInvoiceTypeException;

    Collection<GnucashCustomerInvoiceEntry> getEntries() throws WrongInvoiceTypeException;

    void addEntry(final GnucashCustomerInvoiceEntry entry);
    
}