package org.gnucash.api.read.aux;

import org.gnucash.base.basetypes.simple.GCshID;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

import org.gnucash.api.read.GnuCashAccount;

public interface GCshTaxTableEntry {

    /**
     * @see ${@link #getType()}
     */
    public enum Type {
	VALUE,
	PERCENT
    }
    
    // ---------------------------------------------------------------

    /**
     * usually ${@link GCshTaxTableEntry#TYPE_PERCENT}.
     * @see ${@link #getAmount())
     */
    Type getType();

    /**
     * @return Returns the accountID.
     * @see ${@link #myAccountID}
     */
    GCshID getAccountID();

    /**
     * @return Returns the account.
     * @see ${@link #myAccount}
     */
    GnuCashAccount getAccount();
    
    /**
     * @return the amount the tax is ("16" for "16%")
     * @see ${@link #getType()}
     */
    FixedPointNumber getAmount();

}
