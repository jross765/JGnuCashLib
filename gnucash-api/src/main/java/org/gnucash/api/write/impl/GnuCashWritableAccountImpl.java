package org.gnucash.api.write.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.gnucash.api.Const;
import org.gnucash.api.generated.GncAccount;
import org.gnucash.api.generated.ObjectFactory;
import org.gnucash.api.generated.Slot;
import org.gnucash.api.generated.SlotValue;
import org.gnucash.api.generated.SlotsType;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.impl.GnuCashAccountImpl;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.api.read.impl.hlp.SlotListDoesNotContainKeyException;
import org.gnucash.api.write.GnuCashWritableAccount;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.api.write.GnuCashWritableTransactionSplit;
import org.gnucash.api.write.impl.hlp.GnuCashWritableObjectImpl;
import org.gnucash.api.write.impl.hlp.HasWritableUserDefinedAttributesImpl;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.base.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.base.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.base.basetypes.simple.GCshID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.UnknownAccountTypeException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of GnuCashAccountImpl to allow read-write access instead of
 * read-only access.
 */
public class GnuCashWritableAccountImpl extends GnuCashAccountImpl 
                                        implements GnuCashWritableAccount 
{
    /**
     * Our logger for debug- and error-output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GnuCashWritableAccountImpl.class);

    // ---------------------------------------------------------------

    /**
     * Our helper to implement the GnuCashWritableObject-interface.
     */
    private final GnuCashWritableObjectImpl helper = new GnuCashWritableObjectImpl(getWritableGnuCashFile(), this);
    
    /**
     * Used by ${@link #getBalance()} to cache the result.
     */
    private FixedPointNumber myBalanceCached = null;

    /**
     * Used by ${@link #getBalance()} to cache the result.
     */
    private PropertyChangeListener myBalanceCachedInvalidator = null;
    
    // ---------------------------------------------------------------

    /**
     * @param jwsdpPeer 
     * @param file 
     * @see GnuCashAccountImpl#GnuCashAccountImpl(GncAccount, GnuCashFile)
     */
    @SuppressWarnings("exports")
    public GnuCashWritableAccountImpl(final GncAccount jwsdpPeer, final GnuCashFileImpl file) {
	super(jwsdpPeer, file);
    }

    /**
     * @param file 
     * @see GnuCashAccountImpl#GnuCashAccountImpl(GncAccount, GnuCashFile) )
     */
    public GnuCashWritableAccountImpl(final GnuCashWritableFileImpl file) {
	super(createAccount_int(file, GCshID.getNew()), file);
    }

    public GnuCashWritableAccountImpl(
    		final GnuCashAccountImpl acct, 
    		final boolean addSplits) {
	super(acct.getJwsdpPeer(), acct.getGnuCashFile());

	if (addSplits) {
	    for ( GnuCashTransactionSplit splt : ((GnuCashFileImpl) acct.getGnuCashFile()).getTransactionSplits_readAfresh() ) {
		if ( ! acct.isRootAccount() && 
			 splt.getAccountID().equals(acct.getID()) ) {
		    super.addTransactionSplit(splt);
		    // NO:
//			    addTransactionSplit(new GnuCashTransactionSplitImpl(splt.getJwsdpPeer(), splt.getTransaction(), 
//	                            false, false));
		}
	    }
	}
    }

    // ---------------------------------------------------------------

    /**
     * @param file
     * @return
     */
    private static GncAccount createAccount_int(
    		final GnuCashWritableFileImpl file, 
    		final GCshID newID) {
    	if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}

	ObjectFactory factory = file.getObjectFactory();

	GncAccount jwsdpAcct = file.createGncAccountType();
	// left unset account.setActCode();
	jwsdpAcct.setActCommodityScu(100); // x,yz
	jwsdpAcct.setActDescription("no description yet");
	// left unset account.setActLots();
	jwsdpAcct.setActName("UNNAMED");
	// left unset account.setActNonStandardScu();
	// left unset account.setActParent())
	jwsdpAcct.setActType(GnuCashAccount.Type.BANK.toString());

	jwsdpAcct.setVersion(Const.XML_FORMAT_VERSION);

	{
	    GncAccount.ActCommodity currency = factory.createGncAccountActCommodity();
	    currency.setCmdtyId(file.getDefaultCurrencyID());
	    currency.setCmdtySpace(GCshCmdtyCurrNameSpace.CURRENCY);
	    jwsdpAcct.setActCommodity(currency);
	}

	{
	    GncAccount.ActId guid = factory.createGncAccountActId();
	    guid.setType(Const.XML_DATA_TYPE_GUID);
	    guid.setValue(newID.toString());
	    jwsdpAcct.setActId(guid);
	}

	{
	    SlotsType slots = factory.createSlotsType();
	    jwsdpAcct.setActSlots(slots);
	}

	{
	    Slot slot = factory.createSlot();
	    slot.setSlotKey(Const.SLOT_KEY_ACCT_PLACEHOLDER);
	    SlotValue slottype = factory.createSlotValue();
	    slottype.setType(Const.XML_DATA_TYPE_STRING);
	    slottype.getContent().add("false");
	    slot.setSlotValue(slottype);
	    jwsdpAcct.getActSlots().getSlot().add(slot);
	}

	{
	    Slot slot = factory.createSlot();
	    slot.setSlotKey(Const.SLOT_KEY_ACCT_NOTES);
	    SlotValue slottype = factory.createSlotValue();
	    slottype.setType(Const.XML_DATA_TYPE_STRING);
	    slottype.getContent().add("");
	    slot.setSlotValue(slottype);
	    jwsdpAcct.getActSlots().getSlot().add(slot);
	}

	file.getRootElement().getGncBook().getBookElements().add(jwsdpAcct);
	file.setModified(true);

	LOGGER.debug("createAccount_int: Created new account (core): " + jwsdpAcct.getActId().getValue());

	return jwsdpAcct;
    }

    /**
     * Remove this account from the sytem.<br/>
     * Throws IllegalStateException if this account has splits or childres.
     */
    public void remove() {
	if ( getTransactionSplits().size() > 0 ) {
	    throw new IllegalStateException("Cannot remove account while it contains transaction-splits!");
	}
	
	if ( this.getChildren().size() > 0 ) {
	    throw new IllegalStateException("Cannot remove account while it contains child-accounts!");
	}

	getWritableGnuCashFile().getRootElement().getGncBook().getBookElements().remove(getJwsdpPeer());
	getWritableGnuCashFile().removeAccount(this);
    }

    // ---------------------------------------------------------------

    /**
     * The GnuCash file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public GnuCashWritableFileImpl getWritableGnuCashFile() {
	return (GnuCashWritableFileImpl) super.getGnuCashFile();
    }

    /**
     * The GnuCash file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    @Override
    public GnuCashWritableFileImpl getGnuCashFile() {
	return (GnuCashWritableFileImpl) super.getGnuCashFile();
    }

	// ---------------------------------------------------------------

    /**
     * @see GnuCashAccount#addTransactionSplit(GnuCashTransactionSplit)
     */
    @Override
    public void addTransactionSplit(final GnuCashTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}
		
		if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
			 getGnuCashFile().getRootAccountID().equals(getID())) {
			LOGGER.error("Setting name is forbidden for root and top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
		}
		
		super.addTransactionSplit(splt);

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, getTransactionSplits());
		}
    }

    /**
     * @param splt the split to remove
     */
    protected void removeTransactionSplit(final GnuCashWritableTransactionSplit splt) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}
		
		if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
			 getGnuCashFile().getRootAccountID().equals(getID())) {
			LOGGER.error("Setting name is forbidden for root and top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
		}
			
		List<GnuCashTransactionSplit> transactionSplits = getTransactionSplits();
		transactionSplits.remove(splt);

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, transactionSplits);
		}
    }

    // ---------------------------------------------------------------

    /**
     * @see GnuCashWritableAccount#setName(java.lang.String)
     */
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("null name given!");
    	}

    	if ( name.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty name given!");
    	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
			 getGnuCashFile().getRootAccountID().equals(getID())) {
			LOGGER.error("Setting name is forbidden for root and top-level accounts");
			throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
		}
			
    	String oldName = getName();
    	if (oldName == name) {
    		return; // nothing has changed
    	}
    	
    	this.getJwsdpPeer().setActName(name);
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if (propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("name", oldName, name);
    	}
    }

    /**
     * @see GnuCashWritableAccount#setAccountCode(java.lang.String)
     */
    public void setAccountCode(final String code) {
    	if ( code == null ) {
    		throw new IllegalArgumentException("null code given!");
    	}

    	if ( code.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty code given!");
    	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
    		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
		
    	String oldCode = getCode();
    	if (oldCode == code) {
    		return; // nothing has changed
    	}
    	
    	this.getJwsdpPeer().setActCode(code);
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if (propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("code", oldCode, code);
    	}
    }

    /**
     * @param currNameSpace the new namespace
     * @see {@link GnuCashAccount#getCurrencyNameSpace()}
     */
    private void setCmdtyCurrNameSpace(final String currNameSpace) {
    	if ( currNameSpace == null ) {
    		throw new IllegalArgumentException("null currencyNameSpace given!");
    	}

    	if ( currNameSpace.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty currencyNameSpace given!");
    	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
    		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
		
    	String oldCurrNameSpace = getCmdtyCurrID().getNameSpace();
    	if (oldCurrNameSpace == currNameSpace) {
    		return; // nothing has changed
    	}
    	
    	this.getJwsdpPeer().getActCommodity().setCmdtySpace(currNameSpace);
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if ( propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("currencyNameSpace", oldCurrNameSpace, currNameSpace);
    	}
    }

    public void setCmdtyCurrID(final GCshCmdtyCurrID cmdtyCurrID) {
    	if ( cmdtyCurrID == null ) {
    		throw new IllegalArgumentException("null commodity/currency ID given!");
    	}

    	if ( ! cmdtyCurrID.isSet() ) {
    		throw new IllegalArgumentException("unset commodity/currency ID given!");
    	}

//    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
//			 getGnuCashFile().getRootAccountID().equals(getID())) {
//			LOGGER.error("Setting name is forbidden for root and top-level accounts");
//			throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
//		}
			
    	setCmdtyCurrNameSpace(cmdtyCurrID.getNameSpace());
    	setCmdtyCurrCode(cmdtyCurrID.getCode());
    }

    /**
     * @param currID the new currency
     * @see #setCurrencyNameSpace(String)
     * @see {@link GnuCashAccount#getCurrencyID()}
     */
    private void setCmdtyCurrCode(final String currID) {
    	if ( currID == null ) {
    		throw new IllegalArgumentException("null currencyID given!");
    	}

    	if ( currID.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty currencyID given!");
    	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
    		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
		
    	String oldCurrencyId = getCmdtyCurrID().getCode();
    	if (oldCurrencyId == currID) {
    		return; // nothing has changed
    	}
    	
    	this.getJwsdpPeer().getActCommodity().setCmdtyId(currID);
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if (propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("currencyID", oldCurrencyId, currID);
    	}
    }

    /**
     * set getWritableFile().setModified(true).
     */
    protected void setIsModified() {
    	GnuCashWritableFile writableFile = getWritableGnuCashFile();
    	writableFile.setModified(true);
    }

    /**
     * @see GnuCashWritableAccount#setName(java.lang.String)
     */
    public void setDescription(final String descr) {
    	if ( descr == null ) {
    		throw new IllegalArgumentException("null description given!");
    	}

    	// Caution: empty string allowed here
    	//	if ( descr.trim().length() == 0 ) {
    	//	    throw new IllegalArgumentException("empty description given!");
    	//	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
    		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
		
    	String oldDescr = getDescription();
    	if (oldDescr == descr) {
    		return; // nothing has changed
    	}
    	
    	getJwsdpPeer().setActDescription(descr);
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if (propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("description", oldDescr, descr);
    	}
    }

    public void setType(final Type type) {
    	if ( type == null ) {
    		throw new IllegalArgumentException("null type given!");
    	}

    	if ( type == Type.ROOT ) {
    		throw new UnsupportedOperationException("no account's type may be set to '" + Type.ROOT.toString() + "'");
    	}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
       		 getGnuCashFile().getRootAccountID().equals(getID())) {
       		LOGGER.error("Setting name is forbidden for root and top-level accounts");
       		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
       	}
   		
    	Type oldType = getType();
    	if (oldType == type) {
    		return; // nothing has changed
    	}
    	
		// ::CHECK
		// Not sure whether we should allow this action at all...
		// It does happen that you set an account's type wrong by accident,
		// and it should be possibly to correct that.
		// It does not seem prudent to change an account's type when
		// there are already transactions pointing to/from it.
		if ( getTransactionSplits().size() > 0 ) {
	    	LOGGER.error("Changing account type is forbidden for accounts that already contain transactions: " + getID());
			throw new UnsupportedOperationException("Changing account type is forbidden for accounts that already contain transactions: " + getID());
		}

		getJwsdpPeer().setActType(type.toString());
    	setIsModified();
    	
    	// <<insert code to react further to this change here
    	PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
    	if (propertyChangeFirer != null) {
    		propertyChangeFirer.firePropertyChange("type", oldType, type);
    	}
    }

    /**
     * @see GnuCashWritableAccount#setParentAccount(GnuCashAccount)
     */
    public void setParentAccountID(final GCshID prntAcctID) {
    	if (prntAcctID == null) {
    		setParentAccount(null);
    		return;
    	} 

		if ( ! prntAcctID.isSet() ) {
			throw new IllegalArgumentException("unset account ID given!");
		}

		// check if new parent is a child-account recursively
		GnuCashAccount prntAcct = getGnuCashFile().getAccountByID(prntAcctID);
		if ( isChildAccountRecursive(prntAcct) ) {
			throw new IllegalArgumentException("An account may not be set as its own (grand-)parent");
		}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
       		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
      		
    	GnuCashAccount oldPrntAcctID = null;
    	GncAccount.ActParent jwsdpPrntID = getJwsdpPeer().getActParent();
    	if (jwsdpPrntID == null) {
    	    jwsdpPrntID = ((GnuCashWritableFileImpl) getWritableGnuCashFile()).getObjectFactory()
    	    		.createGncAccountActParent();
    	    jwsdpPrntID.setType(Const.XML_DATA_TYPE_GUID);
    	    jwsdpPrntID.setValue(prntAcctID.toString());
    	    getJwsdpPeer().setActParent(jwsdpPrntID);

    	} else {
    	    oldPrntAcctID = getParentAccount();
    	    jwsdpPrntID.setValue(prntAcctID.toString());
    	}
		setIsModified();

		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = helper.getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("parentAccount", oldPrntAcctID, prntAcctID);
		}
    }

    /**
     * @see GnuCashWritableAccount#setParentAccount(GnuCashAccount)
     */
    public void setParentAccount(final GnuCashAccount prntAcct) {
    	if (prntAcct == null) {
    		this.getJwsdpPeer().setActParent(null);
    		return;
    	}

    	if (prntAcct == this) {
    		throw new IllegalArgumentException("I cannot be my own parent!");
    	}

		setParentAccountID(prntAcct.getID());
    }

    // ---------------------------------------------------------------

    /**
     * same as getBalance(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * This implementation caches the result.<br/>
     * We assume that time does never move backwards
     *
     * @see #getBalance(LocalDate)
     */
    @Override
    public FixedPointNumber getBalance() {

	if ( myBalanceCached != null ) {
	    return myBalanceCached;
	}

	List<GnuCashTransactionSplit> after = new ArrayList<GnuCashTransactionSplit>();
	FixedPointNumber balance = getBalance(LocalDate.now(), after);
	
	if ( after.isEmpty() ) {
	    myBalanceCached = balance;

	    // add a listener to keep the cache up to date
	    if ( myBalanceCachedInvalidator != null ) {
		myBalanceCachedInvalidator = new PropertyChangeListener() {
		    private final Collection<GnuCashTransactionSplit> splitsWeAreAddedTo = new HashSet<GnuCashTransactionSplit>();

		    public void propertyChange(final PropertyChangeEvent evt) {
			myBalanceCached = null;

			// we don't handle the case of removing an account
			// because that happens seldomly enough

			if ( evt.getPropertyName().equals("account") && 
			     evt.getSource() instanceof GnuCashWritableTransactionSplit ) {
			    GnuCashWritableTransactionSplit splitw = (GnuCashWritableTransactionSplit) evt.getSource();
			    if (splitw.getAccount() != GnuCashWritableAccountImpl.this) {
				helper.removePropertyChangeListener("account", this);
				helper.removePropertyChangeListener("quantity", this);
				helper.removePropertyChangeListener("datePosted", this);
				splitsWeAreAddedTo.remove(splitw);
			    }

			}
			
			if ( evt.getPropertyName().equals("transactionSplits") ) {
				List<GnuCashTransactionSplit> splits = (List<GnuCashTransactionSplit>) evt.getNewValue();
			    for ( GnuCashTransactionSplit split : splits ) {
				if ( ! (split instanceof GnuCashWritableTransactionSplit) || 
				     splitsWeAreAddedTo.contains(split)) {
				    continue;
				}
				GnuCashWritableTransactionSplit splitw = (GnuCashWritableTransactionSplit) split;
				helper.addPropertyChangeListener("account", this);
				helper.addPropertyChangeListener("quantity", this);
				helper.addPropertyChangeListener("datePosted", this);
				splitsWeAreAddedTo.add(splitw);
			    }
			}
		    }
		};
		
		helper.addPropertyChangeListener("currencyID", myBalanceCachedInvalidator);
		helper.addPropertyChangeListener("currencyNameSpace", myBalanceCachedInvalidator);
		helper.addPropertyChangeListener("transactionSplits", myBalanceCachedInvalidator);
	    }
	}

	return balance;
    }

    /**
     * Get the sum of all transaction-splits affecting this account in the given
     * time-frame.
     *
     * @param from when to start, inclusive
     * @param to   when to stop, exlusive.
     * @return the sum of all transaction-splits affecting this account in the given
     *         time-frame.
     */
    public FixedPointNumber getBalanceChange(final LocalDate from, final LocalDate to) {
	FixedPointNumber retval = new FixedPointNumber();

	for ( GnuCashTransactionSplit splt : getTransactionSplits() ) {
	    LocalDateTime whenHappened = splt.getTransaction().getDatePosted().toLocalDateTime();
	    
	    if ( ! whenHappened.isBefore(to.atStartOfDay()) ) {
		continue;
	    }
	    
	    if ( whenHappened.isBefore(from.atStartOfDay()) ) {
		continue;
	    }
	    
	    retval = retval.add(splt.getQuantity());
	}

	return retval;
    }

    // ---------------------------------------------------------------

	@Override
	public void addUserDefinedAttribute(final String type, final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

		if ( value == null ) {
			throw new IllegalArgumentException("null value given");
		}
		
		if ( value.isEmpty() ) {
			throw new IllegalArgumentException("empty value given");
		}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
       		 getGnuCashFile().getRootAccountID().equals(getID())) {
    		LOGGER.error("Setting name is forbidden for root and top-level accounts");
    		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
    	}
      		
		if ( jwsdpPeer.getActSlots() == null ) {
			ObjectFactory fact = getGnuCashFile().getObjectFactory();
			SlotsType newSlotsType = fact.createSlotsType();
			jwsdpPeer.setActSlots(newSlotsType);
		}
		
		HasWritableUserDefinedAttributesImpl
			.addUserDefinedAttributeCore(jwsdpPeer.getActSlots(), 
										 getWritableGnuCashFile(), 
										 type, name, value);	
	}

	@Override
	public void removeUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
       		 getGnuCashFile().getRootAccountID().equals(getID())) {
       		LOGGER.error("Setting name is forbidden for root and top-level accounts");
       		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
       	}
         		
		if ( jwsdpPeer.getActSlots() == null ) {
			throw new SlotListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.removeUserDefinedAttributeCore(jwsdpPeer.getActSlots(), 
										 	getWritableGnuCashFile(), 
										 	name);	
	}

	@Override
	public void setUserDefinedAttribute(final String name, final String value) {
		if ( name == null ) {
			throw new IllegalArgumentException("null name given");
		}
		
		if ( name.isEmpty() ) {
			throw new IllegalArgumentException("empty name given");
		}

    	if ( getGnuCashFile().getTopAccountIDs().contains(getID()) ||
       		 getGnuCashFile().getRootAccountID().equals(getID())) {
       		LOGGER.error("Setting name is forbidden for root and top-level accounts");
       		throw new UnsupportedOperationException("Setting name is forbidden for root and top-level accounts");
       	}
         		
		if ( jwsdpPeer.getActSlots() == null ) {
			throw new SlotListDoesNotContainKeyException();
		}
		
		HasWritableUserDefinedAttributesImpl
			.setUserDefinedAttributeCore(jwsdpPeer.getActSlots(), 
										 getWritableGnuCashFile(), 
										 name, value);	
	}

	public void clean() {
		LOGGER.debug("clean: [account-id=" + getID() + "]");
		HasWritableUserDefinedAttributesImpl.cleanSlots(getJwsdpPeer().getActSlots());
	}

    // ---------------------------------------------------------------

    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("GnuCashWritableAccountImpl [");

	buffer.append("id=");
	buffer.append(getID());

	buffer.append(", code='");
	buffer.append(getCode() + "'");

	buffer.append(", type=");
	try {
	    buffer.append(getType());
	} catch (UnknownAccountTypeException e) {
	    buffer.append("ERROR");
	}

	buffer.append(", qualif-name='");
	buffer.append(getQualifiedName() + "'");

	buffer.append(", commodity/currency='");
	try {
	    buffer.append(getCmdtyCurrID() + "'");
	} catch (InvalidCmdtyCurrTypeException e) {
	    buffer.append("ERROR");
	}

	buffer.append("]");

	return buffer.toString();
    }

}
