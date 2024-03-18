package org.gnucash.api.read.spec;

import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.numbers.FixedPointNumber;

import javax.security.auth.login.AccountNotFoundException;

import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;

/**
 * One entry (line item) of a {@link GnuCashVendorBill}
 * 
 *  @see GnuCashCustomerInvoiceEntry
 *  @see GnuCashEmployeeVoucherEntry
 *  @see GnuCashJobInvoiceEntry
 *  @see GnuCashGenerInvoiceEntry
 */
public interface GnuCashVendorBillEntry extends GnuCashGenerInvoiceEntry 
{
  GCshID getBillID();

  GnuCashVendorBill getBill();
  
  // -----------------------------------------------------------------

  GCshID getAccountID() throws AccountNotFoundException;

  GnuCashAccount getAccount() throws AccountNotFoundException;
  
  // -----------------------------------------------------------------

  FixedPointNumber getPrice();

  String getPriceFormatted();
  
}
