package org.gnucash.api.write;

import java.time.LocalDate;

import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.IllegalTransactionSplitActionException;
import org.gnucash.api.read.TaxTableNotFoundException;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.api.write.hlp.GnuCashWritableGenerInvoiceEntry_Cust;
import org.gnucash.api.write.hlp.GnuCashWritableGenerInvoiceEntry_Empl;
import org.gnucash.api.write.hlp.GnuCashWritableGenerInvoiceEntry_Job;
import org.gnucash.api.write.hlp.GnuCashWritableGenerInvoiceEntry_Vend;
import org.gnucash.api.write.hlp.GnuCashWritableObject;
import org.gnucash.api.write.hlp.HasWritableUserDefinedAttributes;
import org.gnucash.base.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.base.numbers.FixedPointNumber;

/**
 * Invoice-Entry that can be modified.
 * 
 * @see GnuCashGenerInvoiceEntry
 */
public interface GnuCashWritableGenerInvoiceEntry extends GnuCashGenerInvoiceEntry,
                                                          GnuCashWritableGenerInvoiceEntry_Cust,
                                                          GnuCashWritableGenerInvoiceEntry_Vend,
                                                          GnuCashWritableGenerInvoiceEntry_Empl,
                                                          GnuCashWritableGenerInvoiceEntry_Job,
                                                          GnuCashWritableObject,
                                                          HasWritableUserDefinedAttributes
{

    /**
     * @see GnuCashGenerInvoiceEntry#getGenerInvoice() .
     */
    GnuCashWritableGenerInvoice getGenerInvoice();

    void setDate(LocalDate date);

    /**
     * Set the description-text.
     *
     * @param desc the new description
     */
    void setDescription(String desc);

    // ---------------------------------------------------------------

    void setAction(Action act);

    void setQuantity(String quantity)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException,
	    NumberFormatException, InvalidCmdtyCurrTypeException;

    void setQuantity(FixedPointNumber quantity)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException,
	    NumberFormatException, InvalidCmdtyCurrTypeException;

    void setQuantityFormatted(String n)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException,
	    NumberFormatException, InvalidCmdtyCurrTypeException;

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException
     * @throws NumberFormatException
     * @throws IllegalTransactionSplitActionException
     * 
     */
    void remove() throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException,
	    NumberFormatException, InvalidCmdtyCurrTypeException;

}