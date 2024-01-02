package org.gnucash.api.write.impl.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.gnucash.api.ConstTest;
import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.GnucashCustomer;
import org.gnucash.api.read.GnucashTransaction;
import org.gnucash.api.read.aux.GCshBillTerms;
import org.gnucash.api.read.aux.GCshTaxTable;
import org.gnucash.api.read.aux.GCshTaxTableEntry;
import org.gnucash.api.read.impl.GnucashCustomerImpl;
import org.gnucash.api.read.impl.GnucashFileImpl;
import org.gnucash.api.read.impl.TestGnucashCustomerImpl;
import org.gnucash.api.read.impl.aux.GCshFileStats;
import org.gnucash.api.read.impl.aux.TestGCshBillTermsImpl;
import org.gnucash.api.read.impl.aux.TestGCshTaxTableImpl;
import org.gnucash.api.read.spec.GnucashCustomerInvoice;
import org.gnucash.api.write.GnucashWritableCustomer;
import org.gnucash.api.write.GnucashWritableTransaction;
import org.gnucash.api.write.aux.GCshWritableTaxTable;
import org.gnucash.api.write.impl.GnucashWritableFileImpl;
import org.gnucash.api.write.spec.GnucashWritableCustomerInvoice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestGCshWritableTaxTableImpl
{
    private static final GCshID TAXTABLE_DE_1_1_ID = TestGCshTaxTableImpl.TAXTABLE_DE_1_1_ID;
    private static final GCshID TAXTABLE_DE_1_2_ID = TestGCshTaxTableImpl.TAXTABLE_DE_1_2_ID;
    private static final GCshID TAXTABLE_DE_2_ID   = TestGCshTaxTableImpl.TAXTABLE_DE_2_ID;
      
    public  static final GCshID TAXTABLE_FR_1_ID   = TestGCshTaxTableImpl.TAXTABLE_FR_1_ID;
    private static final GCshID TAXTABLE_FR_2_ID   = TestGCshTaxTableImpl.TAXTABLE_FR_2_ID;
      
    public  static final GCshID TAXTABLE_UK_1_ID   = TestGCshTaxTableImpl.TAXTABLE_UK_1_ID;
    private static final GCshID TAXTABLE_UK_2_ID   = TestGCshTaxTableImpl.TAXTABLE_UK_1_ID;
    
    private static final GCshID TAX_ACCT_ID        = TestGCshTaxTableImpl.TAX_ACCT_ID;

    // -----------------------------------------------------------------

    private GnucashWritableFileImpl gcshInFile = null;
    private GnucashFileImpl         gcshOutFile = null;

    private GCshFileStats           gcshInFileStats = null;
    private GCshFileStats           gcshOutFileStats = null;
    
    // https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
    @SuppressWarnings("exports")
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshWritableTaxTableImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshInFileStream = null;
    try 
    {
      gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash in-file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------
  // PART 1: Read existing objects as modifiable ones
  //         (and see whether they are fully symmetrical to their read-only
  //         counterparts)
  // -----------------------------------------------------------------
  // Cf. TestGCshTaxTableImpl.testxyz
  // 
  // Check whether the GCshWritableTaxTable objects returned by 
  // GnucashWritableFileImpl.getWritableTaxTableByID() are actually 
  // complete (as complete as returned be GnucashFileImpl.getTaxTableByID().
  
  @Test
  public void test01() throws Exception
  {
      Collection<GCshWritableTaxTable> taxTableList = gcshInFile.getWritableTaxTables();
      
      assertEquals(7, taxTableList.size());

      // ::TODO: Sort array for predictability
//      Object[] taxTableArr = taxTableList.toArray();
//      
//      assertEquals(TAXTABLE_UK_2_ID,   ((GCshWritableTaxTable) taxTableArr[0]).getID());
//      assertEquals(TAXTABLE_DE_1_2_ID, ((GCshWritableTaxTable) taxTableArr[1]).getID());
//      assertEquals(TAXTABLE_UK_1_ID,   ((GCshWritableTaxTable) taxTableArr[2]).getID());
//      assertEquals(TAXTABLE_DE_1_1_ID, ((GCshWritableTaxTable) taxTableArr[3]).getID());
//      assertEquals(TAXTABLE_DE_2_ID,   ((GCshWritableTaxTable) taxTableArr[4]).getID());
//      assertEquals(TAXTABLE_FR_1_ID,   ((GCshWritableTaxTable) taxTableArr[5]).getID());
//      assertEquals(TAXTABLE_FR_2_ID,   ((GCshWritableTaxTable) taxTableArr[6]).getID());
  }

  @Test
  public void test02_1_1() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByID(TAXTABLE_DE_1_1_ID);
      
      assertEquals(TAXTABLE_DE_1_1_ID, taxTab.getID());
      assertEquals("DE_USt_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test02_1_2() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByName("DE_USt_Std");
      
      assertEquals(TAXTABLE_DE_1_1_ID, taxTab.getID());
      assertEquals("DE_USt_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test02_2_1() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByID(TAXTABLE_DE_1_2_ID);
      
      assertEquals(TAXTABLE_DE_1_2_ID, taxTab.getID());
      assertEquals("USt_Std", taxTab.getName()); // sic, old name w/o prefix "DE_"
      assertEquals(TAXTABLE_DE_1_1_ID, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test02_2_2() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByName("USt_Std");
      
      assertEquals(TAXTABLE_DE_1_2_ID, taxTab.getID());
      assertEquals("USt_Std", taxTab.getName()); // sic, old name w/o prefix "DE_"
      assertEquals(TAXTABLE_DE_1_1_ID, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test03_1() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByID(TAXTABLE_DE_2_ID);
      
      assertEquals(TAXTABLE_DE_2_ID, taxTab.getID());
      assertEquals("DE_USt_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(7.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test03_2() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByName("DE_USt_red");
      
      assertEquals(TAXTABLE_DE_2_ID, taxTab.getID());
      assertEquals("DE_USt_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(7.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test04_1() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByID(TAXTABLE_FR_1_ID);
      
      assertEquals(TAXTABLE_FR_1_ID, taxTab.getID());
      assertEquals("FR_TVA_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(20.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test04_2() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByName("FR_TVA_Std");
      
      assertEquals(TAXTABLE_FR_1_ID, taxTab.getID());
      assertEquals("FR_TVA_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(20.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test05_1() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByID(TAXTABLE_FR_2_ID);
      
      assertEquals(TAXTABLE_FR_2_ID, taxTab.getID());
      assertEquals("FR_TVA_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(10.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test05_2() throws Exception
  {
      GCshWritableTaxTable taxTab = gcshInFile.getWritableTaxTableByName("FR_TVA_red");
      
      assertEquals(TAXTABLE_FR_2_ID, taxTab.getID());
      assertEquals("FR_TVA_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(10.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.Type.PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }
  
  // -----------------------------------------------------------------
  // PART 2: Modify existing objects
  // -----------------------------------------------------------------
  // Check whether the GnucashWritableCustomer objects returned by 
  // can actually be modified -- both in memory and persisted in file.
  
  // ::TODO
  
  // -----------------------------------------------------------------
  // PART 3: Create new objects
  // -----------------------------------------------------------------
  
  // ------------------------------
  // PART 3.1: High-Level
  // ------------------------------
  
  // ::TODO
  
  // ------------------------------
  // PART 3.2: Low-Level
  // ------------------------------
  
//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }

}
