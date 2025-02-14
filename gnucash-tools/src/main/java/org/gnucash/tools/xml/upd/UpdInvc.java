package org.gnucash.tools.xml.upd;

import java.io.File;
import java.time.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.InvoiceNotFoundException;
import org.gnucash.api.write.GnuCashWritableGenerInvoice;
import org.gnucash.api.write.GnuCashWritableGenerInvoiceEntry;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.AccountNotFoundException;
import xyz.schnorxoborx.base.beanbase.WrongAccountTypeException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;
import xyz.schnorxoborx.base.dateutils.LocalDateHelpers;

public class UpdInvc extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdInvc.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String           gcshInFileName = null;
  private static String           gcshOutFileName = null;
  private static GCshID           invcID = null;
  private static GCshID           incExpAcctID = null;
  private static GCshID           recvblPayblAcctID = null;
  private static String           number = null;
  private static String           descr = null;
  private static LocalDate        dateOpen = null;

  private static GnuCashWritableGenerInvoice  invcGener = null;
  private static GnuCashAccount               incExpAcct = null;
  private static GnuCashAccount               recvblPayblAcct = null;

  public static void main( String[] args )
  {
    try
    {
      UpdInvc tool = new UpdInvc ();
      tool.execute(args);
    }
    catch (CouldNotExecuteException exc) 
    {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void init() throws Exception
  {
    // invcID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFileIn = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("file")
      .withDescription("GnuCash file (in)")
      .withLongOpt("gnucash-in-file")
      .create("if");
          
    Option optFileOut = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("file")
      .withDescription("GnuCash file (out)")
      .withLongOpt("gnucash-out-file")
      .create("of");
      
    Option optID = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("UUID")
      .withDescription("Invoice ID")
      .withLongOpt("invoice-id")
      .create("id");
            
    Option optIncExpAcctID = OptionBuilder
      .hasArg()
      .withArgName("UUID")
      .withDescription("Income/expense account ID")
      .withLongOpt("income-expense-account-id")
      .create("ieacct");
            
    Option optRecvblPayblAcctID = OptionBuilder
      .hasArg()
      .withArgName("UUID")
      .withDescription("Receivable/payable account ID")
      .withLongOpt("receivable-payable-account-id")
      .create("rpacct");
            
    Option optNumber = OptionBuilder
      .hasArg()
      .withArgName("number")
      .withDescription("Invoice number")
      .withLongOpt("number")
      .create("no");
    
    Option optDescr = OptionBuilder
      .hasArg()
      .withArgName("descr")
      .withDescription("Invoice description")
      .withLongOpt("description")
      .create("desc");
    
    Option optOpenDate = OptionBuilder
      .hasArg()
      .withArgName("date")
      .withDescription("Date opened")
      .withLongOpt("opened-date")
      .create("odat");

    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optIncExpAcctID);
    options.addOption(optRecvblPayblAcctID);
    options.addOption(optNumber);
    options.addOption(optDescr);
    options.addOption(optOpenDate);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));

    try 
    {
      invcGener = gcshFile.getWritableGenerInvoiceByID(invcID);
      System.err.println("Invoice before update: " + invcGener.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate invoice with ID '" + invcID + "'");
      throw new InvoiceNotFoundException();
    }
    
    doChanges(gcshFile);
    System.err.println("Invoice after update: " + invcGener.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( incExpAcctID != null )
    {
      try 
      {
        incExpAcct = gcshFile.getAccountByID(incExpAcctID);
        System.err.println("Income/expense account:     " + incExpAcct.getCode() + " (" + incExpAcct.getQualifiedName() + ")");
        
        if ( incExpAcct.getType() != GnuCashAccount.Type.INCOME && 
             incExpAcct.getType() != GnuCashAccount.Type.EXPENSE )
          throw new WrongAccountTypeException();
        
        // ::TODO
        // invcGener.setXYZ
      }
      catch ( Exception exc )
      {
        System.err.println("Error: Could not instantiate account with ID '" + incExpAcctID + "'");
        throw new AccountNotFoundException();
      }
    }

    if ( recvblPayblAcct != null )
    {
      try 
      {
        recvblPayblAcct = gcshFile.getAccountByID(recvblPayblAcctID);
        System.err.println("Receivable/payable account: " + recvblPayblAcct.getCode() + " (" + recvblPayblAcct.getQualifiedName() + ")");

        if ( recvblPayblAcct.getType() != GnuCashAccount.Type.RECEIVABLE && 
             recvblPayblAcct.getType() != GnuCashAccount.Type.PAYABLE )
         throw new WrongAccountTypeException();
       
        // ::TODO
        // invcGener.setXYZ
      }
      catch ( Exception exc )
      {
        System.err.println("Error: Could not instantiate account with ID '" + recvblPayblAcctID + "'");
        throw new AccountNotFoundException();
      }
    }

    if ( number != null )
    {
      System.err.println("Setting number");
      invcGener.setNumber(number);
    }

    if ( descr != null )
    {
      System.err.println("Setting description");
      invcGener.setDescription(descr);
    }

    if ( dateOpen != null )
    {
      System.err.println("Setting opened date: invoice itself");
      invcGener.setDateOpened(dateOpen);

      System.err.println("Setting opened date: entries");
      for ( GnuCashGenerInvoiceEntry entr : invcGener.getGenerEntries() )
      {
        GnuCashWritableGenerInvoiceEntry writEntr = invcGener.getWritableGenerEntryByID(entr.getID());
        writEntr.setDate(dateOpen);
      }
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args) throws InvalidCommandLineArgsException
  {
    CommandLineParser parser = new GnuParser();
    CommandLine cmdLine = null;
    try
    {
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exc)
    {
      System.err.println("Parsing options failed. Reason: " + exc.getMessage());
    }

    // ---

    // <gnucash-in-file>
    try
    {
      gcshInFileName = cmdLine.getOptionValue("gnucash-in-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-in-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (in): '" + gcshInFileName + "'");
    
    // <gnucash-out-file>
    try
    {
      gcshOutFileName = cmdLine.getOptionValue("gnucash-out-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-out-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");
    
    // <invoice-id>
    try
    {
      invcID = new GCshID( cmdLine.getOptionValue("invoice-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <invoice-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Invoice ID: " + invcID);

    // <income-expense-account-id>
    if ( cmdLine.hasOption("income-expense-account-id") ) 
    {
      try
      {
        incExpAcctID = new GCshID( cmdLine.getOptionValue("income-expense-account-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <income-expense-account-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Income/expense account ID: '" + incExpAcctID + "'");

    // <receivable-payable-account-id>
    if ( cmdLine.hasOption("receivable-payable-account-id") ) 
    {
      try
      {
        recvblPayblAcctID = new GCshID( cmdLine.getOptionValue("receivable-payable-account-id") );
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <receivable-payable-account-id>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Receivable/payable account ID: '" + recvblPayblAcctID + "'");

    // <number>
    if ( cmdLine.hasOption("number") ) 
    {
      try
      {
        number = cmdLine.getOptionValue("number");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <number>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Number: '" + number + "'");

    // <description>
    if ( cmdLine.hasOption("description") ) 
    {
      try
      {
        descr = cmdLine.getOptionValue("description");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Description: '" + descr + "'");

    // <opened-date>
    if ( cmdLine.hasOption("opened-date") ) 
    {
      try
      {
        dateOpen = LocalDateHelpers.parseLocalDate(cmdLine.getOptionValue("opened-date"));
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <opened-date>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Opened date: " + dateOpen);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "UpdInvc", options );
  }
}
