package org.gnucash.read.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPackage extends TestCase
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static Test suite() throws Exception
  {
    TestSuite suite = new TestSuite();
    
    suite.addTest(org.gnucash.read.impl.TestGnucashFileImpl.suite());
    
    suite.addTest(org.gnucash.read.impl.TestGnucashAccountImpl.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashCustomerImpl.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashVendorImpl.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashTransactionImpl.suite());
    
    suite.addTest(org.gnucash.read.impl.spec.TestPackage.suite());

    return suite;
  }
}
