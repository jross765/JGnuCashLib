package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import org.gnucash.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.basetypes.simple.GCshID;
import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashEmployee;
import org.gnucash.read.IllegalTransactionSplitActionException;
import org.gnucash.read.TaxTableNotFoundException;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.read.impl.spec.GnucashEmployeeVoucherEntryImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.write.spec.GnucashWritableEmployeeVoucher;
import org.gnucash.write.spec.GnucashWritableEmployeeVoucherEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableEmployeeVoucherImpl extends GnucashWritableGenerInvoiceImpl 
                                                implements GnucashWritableEmployeeVoucher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableEmployeeVoucherImpl.class);

    /**
     * Create an editable invoice facading an existing JWSDP-peer.
     *
     * @param jwsdpPeer the JWSDP-object we are facading.
     * @param file      the file to register under
     * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice,
     *      GnucashFile)
     */
    @SuppressWarnings("exports")
    public GnucashWritableEmployeeVoucherImpl(
	    final GncV2.GncBook.GncGncInvoice jwsdpPeer, 
	    final GnucashFile file) {
	super(jwsdpPeer, file);
    }

    /**
     * @param file the file we are associated with.
     * @throws WrongOwnerTypeException 
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws IllegalTransactionSplitActionException 
     */
    public GnucashWritableEmployeeVoucherImpl(
	    final GnucashWritableFileImpl file, 
	    final String number,
	    final GnucashEmployee empl, 
	    final GnucashAccountImpl expensesAcct,
	    final GnucashAccountImpl payableAcct,
	    final LocalDate openedDate,
	    final LocalDate postDate,
	    final LocalDate dueDate) throws WrongOwnerTypeException, InvalidCmdtyCurrTypeException, IllegalTransactionSplitActionException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	super(createEmployeeVoucher_int(file, 
		                   number, empl,
		                   false, // <-- caution!
		                   expensesAcct, payableAcct,
		                   openedDate, postDate, dueDate), 
	      file);
    }

    /**
     * @param file the file we are associated with.
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException 
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public GnucashWritableEmployeeVoucherImpl(final GnucashWritableGenerInvoiceImpl invc)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	super(invc.getJwsdpPeer(), invc.getFile());

	// No, we cannot check that first, because the super() method
	// always has to be called first.
	if ( invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT) != GnucashGenerInvoice.TYPE_VENDOR )
	    throw new WrongInvoiceTypeException();

	// Caution: In the following two loops, we may *not* iterate directly over
	// invc.getGenerEntries(), because else, we will produce a ConcurrentModificationException.
	// (It only works if the invoice has one single entry.)
	// Hence the indirection via the redundant "entries" hash set.
	Collection<GnucashGenerInvoiceEntry> entries = new HashSet<GnucashGenerInvoiceEntry>();
	for ( GnucashGenerInvoiceEntry entry : invc.getGenerEntries() ) {
	    entries.add(entry);
	}
	for ( GnucashGenerInvoiceEntry entry : entries ) {
	    addEntry(new GnucashWritableEmployeeVoucherEntryImpl(entry));
	}

	// Caution: Indirection via a redundant "trxs" hash set. 
	// Same reason as above.
	Collection<GnucashTransaction> trxs = new HashSet<GnucashTransaction>();
	for ( GnucashTransaction trx : invc.getPayingTransactions() ) {
	    trxs.add(trx);
	}
	for ( GnucashTransaction trx : trxs ) {
	    for (GnucashTransactionSplit splt : trx.getSplits()) {
		GCshID lot = splt.getLotID();
		if (lot != null) {
		    for (GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices()) {
			GCshID lotID = invc1.getLotID();
			if (lotID != null && lotID.equals(lot)) {
			    // Check if it's a payment transaction.
			    // If so, add it to the invoice's list of payment transactions.
			    if (splt.getAction().equals(GnucashTransactionSplit.Action.PAYMENT.getLocaleString())) {
				addPayingTransaction(splt);
			    }
			} // if lotID
		    } // for invc
		} // if lot
	    } // for splt
	} // for trx
    }

    // ---------------------------------------------------------------

    /**
     * The gnucash-file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    protected GnucashWritableFileImpl getWritingFile() {
	return (GnucashWritableFileImpl) getFile();
    }

    /**
     * support for firing PropertyChangeEvents. (gets initialized only if we really
     * have listeners)
     */
    private volatile PropertyChangeSupport myPropertyChange = null;

    /**
     * Returned value may be null if we never had listeners.
     *
     * @return Our support for firing PropertyChangeEvents
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
	return myPropertyChange;
    }

    /**
     * Add a PropertyChangeListener to the listener list. The listener is registered
     * for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property. The listener will be
     * invoked only when a call on firePropertyChange names that specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The PropertyChangeListener to be removed
     */
    public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(propertyName, listener);
	}
    }

    /**
     * Remove a PropertyChangeListener from the listener list. This removes a
     * PropertyChangeListener that was registered for all properties.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(listener);
	}
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void setEmployee(GnucashEmployee empl) throws WrongInvoiceTypeException {
	// ::TODO
	GnucashEmployee oldEmpl = getEmployee();
	if (oldEmpl == empl) {
	    return; // nothing has changed
	}

	getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(empl.getId().toString());
	getWritingFile().setModified(true);

	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("employee", oldEmpl, empl);
	}
    }

    // -----------------------------------------------------------

    /**
     * create and add a new entry.
     * 
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public GnucashWritableEmployeeVoucherEntry createEntry(
	    final GnucashAccount acct,
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	GnucashWritableEmployeeVoucherEntry entry = createEmplVchEntry(acct, 
		                                                       singleUnitPrice, quantity);
	return entry;
    }

    /**
     * create and add a new entry.<br/>
     * The entry will use the accounts of the SKR03.
     * 
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public GnucashWritableEmployeeVoucherEntry createEntry(
	    final GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final String taxTabName) throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	GnucashWritableEmployeeVoucherEntry entry = createEmplVchEntry(acct, 
		                                                       singleUnitPrice, quantity, 
		                                                       taxTabName);
	return entry;
    }

    /**
     * create and add a new entry.<br/>
     *
     * @return an entry using the given Tax-Table
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public GnucashWritableEmployeeVoucherEntry createEntry(
	    final GnucashAccount acct,
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable taxTab) throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	GnucashWritableEmployeeVoucherEntry entry = createEmplVchEntry(acct, 
		                                                       singleUnitPrice, quantity, 
		                                                       taxTab);
	return entry;
    }

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
     */
    protected void removeEntry(final GnucashWritableEmployeeVoucherEntryImpl impl)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

	removeVoucherEntry(impl);
    }

    /**
     * Called by
     * ${@link GnucashWritableEmployeeVoucherEntryImpl#createEmplVoucherEntry_int(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
     *
     * @param entry the entry to add to our internal list of employee-bill-entries
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    protected void addEntry(final GnucashWritableEmployeeVoucherEntryImpl entry)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

	addVoucherEntry(entry);
    }

    protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	subtractVoucherEntry(entry);
    }

    /**
     * @return the ID of the Account to transfer the money from
     * @throws WrongInvoiceTypeException
     */
    private GCshID getAccountIDToTransferMoneyFrom(final GnucashEmployeeVoucherEntryImpl entry)
	    throws WrongInvoiceTypeException {
	return getVoucherPostAccountID(entry);
    }

    @Override
    protected GCshID getInvcPostAccountID(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException {
	throw new WrongInvoiceTypeException();
    }

    @Override
    protected GCshID getJobPostAccountID(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException {
	throw new WrongInvoiceTypeException();
    }

    /**
     * Throw an IllegalStateException if we are not modifiable.
     *
     * @see #isModifiable()
     */
    protected void attemptChange() {
	if (!isModifiable()) {
	    throw new IllegalStateException(
		    "this employee bill is NOT changeable because there are already payment for it made!");
	}
    }

    /**
     * @see GnucashWritableGenerInvoice#getWritableGenerEntryById(java.lang.String)
     */
    public GnucashWritableEmployeeVoucherEntry getWritableEntryById(final GCshID id) {
	return new GnucashWritableEmployeeVoucherEntryImpl(getGenerEntryById(id));
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getEmployeeId() {
	return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashEmployee getEmployee() {
	return getFile().getEmployeeByID(getEmployeeId());
    }

    // ---------------------------------------------------------------

    @Override
    public void post(final GnucashAccount expensesAcct, 
	             final GnucashAccount payablAcct, 
	             final LocalDate postDate, 
	             final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException, InvalidCmdtyCurrTypeException, IllegalTransactionSplitActionException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	postEmployeeVoucher(
		getFile(), 
		this, getEmployee(), 
		expensesAcct, payablAcct, 
		postDate, dueDate);
    }

}