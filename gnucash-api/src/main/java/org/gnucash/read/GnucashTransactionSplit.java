/**
 * GnucashTransactionSplit.java
 * License: GPLv3 or later
 * Created on 05.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *
 * -----------------------------------------------------------
 * major Changes:
 *  05.05.2005 - initial version
 * ...
 *
 */
package org.gnucash.read;



import java.util.Collection;
import java.util.Locale;

import org.gnucash.numbers.FixedPointNumber;



/**
 * created: 05.05.2005 <br/>
 * This denotes a single addition or removal of some
 * value from one account in a transaction made up of
 * multiple such splits.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashTransactionSplit extends Comparable<GnucashTransactionSplit> {

  // ::MAGIC
  public final String ACTION_INVOICE = "Rechnung";
  public final String ACTION_BILL    = "Lieferantenrechnung";
  public final String ACTION_PAYMENT = "Zahlung";
  public final String ACTION_BUY     = "Kauf";
  public final String ACTION_SELL    = "Verkauf";
  
  // -----------------------------------------------------------------

    /**
     *
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    String getId();

    /**
     *
     * @return the id of the account we transfer from/to.
     */
    String getAccountID();

    /**
     * This may be null if an account-id is specified in
     * the gnucash-file that does not belong to an account.
     * @return the account of the account we transfer from/to.
     */
    GnucashAccount getAccount();

    /**
     * @return the transaction this is a split of.
     */
    GnucashTransaction getTransaction();


    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    FixedPointNumber getValue();

    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    String getValueFormatted();
    /**
     * The value is in the currency of the transaction!
     * @param locale the locale to use
     * @return the value-transfer this represents
     */
    String getValueFormatted(Locale locale);
    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    String getValueFormattedForHTML();
    /**
     * The value is in the currency of the transaction!
     * @param locale the locale to use
     * @return the value-transfer this represents
     */
    String getValueFormattedForHTML(Locale locale);

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     */
    FixedPointNumber getAccountBalance();

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     */
    String getAccountBalanceFormatted();

    /**
     * @see GnucashAccount#getBalanceFormated()
     */
    String getAccountBalanceFormatted(Locale locale);

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     */
    FixedPointNumber getQuantity();

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     */
    String getQuantityFormatted();

    /**
     * The quantity is in the currency of the account!
     * @param locale the locale to use
     * @return the number of items added to the account
     */
    String getQuantityFormatted(Locale locale);

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     */
    String getQuantityFormattedForHTML();

    /**
     * The quantity is in the currency of the account!
     * @param locale the locale to use
     * @return the number of items added to the account
     */
    String getQuantityFormattedForHTML(Locale locale);

    /**
     * @return the user-defined description for this object
     *         (may contain multiple lines and non-ascii-characters)
     */
    String getDescription();

    public String getLotID();

      /**
     * Get the type of association this split has with
     * an invoice's lot.
     * @return null, or one of the ACTION_xyz values defined
     */
    String getSplitAction();

    /**
     * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
     */
    Collection<String> getUserDefinedAttributeKeys();

    /**
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    String getUserDefinedAttribute(final String name);

}
