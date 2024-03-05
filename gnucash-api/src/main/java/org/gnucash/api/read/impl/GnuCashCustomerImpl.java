package org.gnucash.api.read.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.gnucash.api.generated.GncGncCustomer;
import org.gnucash.api.read.GnuCashCustomer;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.UnknownAccountTypeException;
import org.gnucash.api.read.aux.GCshAddress;
import org.gnucash.api.read.aux.GCshBillTerms;
import org.gnucash.api.read.aux.GCshTaxTable;
import org.gnucash.api.read.impl.aux.GCshAddressImpl;
import org.gnucash.api.read.impl.hlp.GnuCashObjectImpl;
import org.gnucash.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.gnucash.api.read.impl.spec.GnuCashCustomerJobImpl;
import org.gnucash.api.read.spec.GnuCashCustomerInvoice;
import org.gnucash.api.read.spec.GnuCashCustomerJob;
import org.gnucash.api.read.spec.GnuCashJobInvoice;
import org.gnucash.api.read.spec.SpecInvoiceCommon;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.numbers.FixedPointNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnuCashCustomerImpl extends GnuCashObjectImpl 
                                 implements GnuCashCustomer 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnuCashCustomerImpl.class);

    // ---------------------------------------------------------------

    /**
     * the JWSDP-object we are facading.
     */
    protected final GncGncCustomer jwsdpPeer;

    /**
     * The currencyFormat to use for default-formating.<br/>
     * Please access only using {@link #getCurrencyFormat()}.
     *
     * @see #getCurrencyFormat()
     */
    private NumberFormat currencyFormat = null;

    // ---------------------------------------------------------------

    /**
     * @param peer    the JWSDP-object we are facading.
     * @param gcshFile the file to register under
     */
    @SuppressWarnings("exports")
    public GnuCashCustomerImpl(final GncGncCustomer peer, final GnuCashFile gcshFile) {
	super(gcshFile);

//	if (peer.getCustSlots() == null) {
//	    peer.setCustSlots(getJwsdpPeer().getCustSlots());
//	}

	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncGncCustomer getJwsdpPeer() {
	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getID() {
	return new GCshID(jwsdpPeer.getCustGuid().getValue());
    }

    /**
     * {@inheritDoc}
     */
    public String getNumber() {
	return jwsdpPeer.getCustId();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
	return jwsdpPeer.getCustName();
    }

    /**
     * {@inheritDoc}
     */
    public GCshAddress getAddress() {
	return new GCshAddressImpl(jwsdpPeer.getCustAddr(), getGnuCashFile());
    }

    /**
     * {@inheritDoc}
     */
    public GCshAddress getShippingAddress() {
	return new GCshAddressImpl(jwsdpPeer.getCustShipaddr(), getGnuCashFile());
    }

    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getDiscount() {
	if ( jwsdpPeer.getCustDiscount() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getCustDiscount());
    }

    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getCredit() {
	if ( jwsdpPeer.getCustCredit() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getCustCredit());
    }

    /**
     * {@inheritDoc}
     */
    public String getNotes() {
	return jwsdpPeer.getCustNotes();
    }

    // ---------------------------------------------------------------

    /**
     * @return the currency-format to use if no locale is given.
     */
    protected NumberFormat getCurrencyFormat() {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

	return currencyFormat;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getTaxTableID() {
	GncGncCustomer.CustTaxtable custTaxtable = jwsdpPeer.getCustTaxtable();
	if (custTaxtable == null) {
	    return null;
	}

	return new GCshID( custTaxtable.getValue() );
    }

    /**
     * {@inheritDoc}
     */
    public GCshTaxTable getTaxTable() {
	GCshID id = getTaxTableID();
	if (id == null) {
	    return null;
	}
	return getGnuCashFile().getTaxTableByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getTermsID() {
	GncGncCustomer.CustTerms custTerms = jwsdpPeer.getCustTerms();
	if (custTerms == null) {
	    return null;
	}

	return new GCshID( custTerms.getValue() );
    }

    /**
     * {@inheritDoc}
     */
    public GCshBillTerms getTerms() {
	GCshID id = getTermsID();
	if (id == null) {
	    return null;
	}
	return getGnuCashFile().getBillTermsByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     *
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     *  
     */
    @Override
    public int getNofOpenInvoices() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnuCashFile().getUnpaidInvoicesForCustomer_direct(this).size();
    }

    // -------------------------------------

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     *  
     * @see #getIncomeGenerated_direct()
     * @see #getIncomeGenerated_viaAllJobs()
     */
    public FixedPointNumber getIncomeGenerated(GnuCashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	if ( readVar == GnuCashGenerInvoice.ReadVariant.DIRECT )
	    return getIncomeGenerated_direct();
	else if ( readVar == GnuCashGenerInvoice.ReadVariant.VIA_JOB )
	    return getIncomeGenerated_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     * 
     * @see #getIncomeGenerated_viaAllJobs()
     */
    public FixedPointNumber getIncomeGenerated_direct() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnuCashCustomerInvoice invcSpec : getPaidInvoices_direct()) {
//		    if ( invcGen.getType().equals(GnuCashGenerInvoice.TYPE_CUSTOMER) ) {
//		      GnuCashCustomerInvoice invcSpec = new GnuCashCustomerInvoiceImpl(invcGen); 
		GnuCashCustomer cust = invcSpec.getCustomer();
		if (cust.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) invcSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getIncomeGenerated_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     *  
     * @see #getIncomeGenerated_direct()
     */
    public FixedPointNumber getIncomeGenerated_viaAllJobs() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnuCashJobInvoice invcSpec : getPaidInvoices_viaAllJobs()) {
//		    if ( invcGen.getType().equals(GnuCashGenerInvoice.TYPE_CUSTOMER) ) {
//		      GnuCashCustomerInvoice invcSpec = new GnuCashCustomerInvoiceImpl(invcGen); 
		GnuCashCustomer cust = invcSpec.getCustomer();
		if (cust.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) invcSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getIncomeGenerated_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     *  
     * @see #getIncomeGenerated(org.gnucash.api.read.GnuCashGenerInvoice.ReadVariant)
     */
    public String getIncomeGeneratedFormatted(GnuCashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	return getCurrencyFormat().format(getIncomeGenerated(readVar));

    }

    /**
     * @param lcl the locale to format for
     * @return formatted according to the given locale's currency-format
     * @throws UnknownAccountTypeException 
     */
    public String getIncomeGeneratedFormatted(GnuCashGenerInvoice.ReadVariant readVar, final Locale lcl) throws UnknownAccountTypeException {
	return NumberFormat.getCurrencyInstance(lcl).format(getIncomeGenerated(readVar));
    }

    // -------------------------------------

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws UnknownAccountTypeException 
     * 
     * @see #getOutstandingValue_direct()
     * @see #getOutstandingValue_viaAllJobs()
     */
    public FixedPointNumber getOutstandingValue(GnuCashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	if ( readVar == GnuCashGenerInvoice.ReadVariant.DIRECT )
	    return getOutstandingValue_direct();
	else if ( readVar == GnuCashGenerInvoice.ReadVariant.VIA_JOB )
	    return getOutstandingValue_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue_viaAllJobs()
     */
    public FixedPointNumber getOutstandingValue_direct() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnuCashCustomerInvoice invcSpec : getUnpaidInvoices_direct()) {
//            if ( invcGen.getType().equals(GnuCashGenerInvoice.TYPE_CUSTOMER) ) {
//              GnuCashCustomerInvoice invcSpec = new GnuCashCustomerInvoiceImpl(invcGen); 
		GnuCashCustomer cust = invcSpec.getCustomer();
		if (cust.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) invcSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue_direct()
     */
    public FixedPointNumber getOutstandingValue_viaAllJobs() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnuCashJobInvoice invcSpec : getUnpaidInvoices_viaAllJobs()) {
//            if ( invcGen.getType().equals(GnuCashGenerInvoice.TYPE_CUSTOMER) ) {
//              GnuCashCustomerInvoice invcSpec = new GnuCashCustomerInvoiceImpl(invcGen); 
		GnuCashCustomer cust = invcSpec.getCustomer();
		if (cust.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) invcSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return Formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue(org.gnucash.api.read.GnuCashGenerInvoice.ReadVariant)
     */
    public String getOutstandingValueFormatted(GnuCashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	return getCurrencyFormat().format(getOutstandingValue(readVar));
    }

    /**
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue(org.gnucash.api.read.GnuCashGenerInvoice.ReadVariant)
     */
    public String getOutstandingValueFormatted(GnuCashGenerInvoice.ReadVariant readVar, final Locale lcl) throws UnknownAccountTypeException {
	return NumberFormat.getCurrencyInstance(lcl).format(getOutstandingValue(readVar));
    }

    // -----------------------------------------------------------------

    /**
     * @return the jobs that have this customer associated with them.
     * @throws WrongInvoiceTypeException
     */
    public List<GnuCashCustomerJob> getJobs() throws WrongInvoiceTypeException {

	List<GnuCashCustomerJob> retval = new ArrayList<GnuCashCustomerJob>();

	for ( GnuCashGenerJob jobGener : getGnuCashFile().getGenerJobs() ) {
	    if ( jobGener.getOwnerType() == GnuCashGenerJob.TYPE_CUSTOMER ) {
		GnuCashCustomerJob jobSpec = new GnuCashCustomerJobImpl(jobGener);
		if ( jobSpec.getCustomerID().equals(getID()) ) {
		    retval.add(jobSpec);
		}
	    }
	}

	return retval;
    }

    // -----------------------------------------------------------------

    @Override
    public List<GnuCashGenerInvoice> getInvoices() throws WrongInvoiceTypeException {
    	List<GnuCashGenerInvoice> retval = new ArrayList<GnuCashGenerInvoice>();

	for ( GnuCashCustomerInvoice invc : getGnuCashFile().getInvoicesForCustomer_direct(this) ) {
	    retval.add(invc);
	}
	
	for ( GnuCashJobInvoice invc : getGnuCashFile().getInvoicesForCustomer_viaAllJobs(this) ) {
	    retval.add(invc);
	}
	
	return retval;
    }

    @Override
    public List<GnuCashCustomerInvoice> getPaidInvoices_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnuCashFile().getPaidInvoicesForCustomer_direct(this);
    }

    @Override
    public List<GnuCashJobInvoice>      getPaidInvoices_viaAllJobs() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnuCashFile().getPaidInvoicesForCustomer_viaAllJobs(this);
    }

    @Override
    public List<GnuCashCustomerInvoice> getUnpaidInvoices_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnuCashFile().getUnpaidInvoicesForCustomer_direct(this);
    }

    @Override
    public List<GnuCashJobInvoice>      getUnpaidInvoices_viaAllJobs() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnuCashFile().getUnpaidInvoicesForCustomer_viaAllJobs(this);
    }

    // ------------------------------------------------------------

	@Override
	public String getUserDefinedAttribute(String name) {
		return HasUserDefinedAttributesImpl
					.getUserDefinedAttributeCore(jwsdpPeer.getCustSlots(), name);
	}

	@Override
	public List<String> getUserDefinedAttributeKeys() {
		return HasUserDefinedAttributesImpl
					.getUserDefinedAttributeKeysCore(jwsdpPeer.getCustSlots());
	}
    
    // ------------------------------------------------------------

    public static int getHighestNumber(GnuCashCustomer cust) {
	return ((GnuCashFileImpl) cust.getGnuCashFile()).getHighestCustomerNumber();
    }

    public static String getNewNumber(GnuCashCustomer cust) {
	return ((GnuCashFileImpl) cust.getGnuCashFile()).getNewCustomerNumber();
    }

    // -----------------------------------------------------------------

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("GnuCashCustomerImpl [");
	
	buffer.append("id=");
	buffer.append(getID());
	
	buffer.append(", number='");
	buffer.append(getNumber() + "'");
	
	buffer.append(", name='");
	buffer.append(getName() + "'");
	
	buffer.append("]");
	return buffer.toString();
    }

}