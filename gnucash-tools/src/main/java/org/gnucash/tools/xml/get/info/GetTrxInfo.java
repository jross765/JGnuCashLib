package org.gnucash.tools.xml.get.info;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetTrxInfo extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GetTrxInfo.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String  gcshFileName = null;
  private static GCshID  trxID = null;
  
  private static boolean showSplits = false;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetTrxInfo tool = new GetTrxInfo ();
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
    // trxID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFile = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("file")
      .withDescription("GnuCash file")
      .withLongOpt("gnucash-file")
      .create("f");
      
    Option optTrxID = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("UUID")
      .withDescription("Transaction-ID")
      .withLongOpt("transaction-id")
      .create("trx");
    
    // The convenient ones
    Option optShowSplt = OptionBuilder
      .withDescription("Show splits")
      .withLongOpt("show-splits")
      .create("ssplt");
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optTrxID);
    options.addOption(optShowSplt);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashFileImpl gcshFile = new GnuCashFileImpl(new File(gcshFileName));
    
    GnuCashTransaction trx = gcshFile.getTransactionByID(trxID);
    
    try
    {
      System.out.println("ID:              " + trx.getID());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("Number:          " + trx.getNumber());
    }
    catch ( Exception exc )
    {
      System.out.println("Number:          " + "ERROR");
    }
    
    try
    {
      System.out.println("toString:        " + trx.toString());
    }
    catch ( Exception exc )
    {
      System.out.println("toString:        " + "ERROR");
    }
    
    try
    {
      System.out.println("Balance:         " + trx.getBalanceFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Balance:         " + "ERROR");
    }
    
    try
    {
      System.out.println("Cmdty/Curr:      '" + trx.getCmdtyCurrID() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Cmdty/Curr:      " + "ERROR");
    }
        
    try
    {
      System.out.println("Description:     '" + trx.getDescription() + "'");
    }
    catch ( Exception exc )
    {
      System.out.println("Description:     " + "ERROR");
    }

    // ---
        
    if ( showSplits )
      showSplits(trx);
  }

  // -----------------------------------------------------------------

  private void showSplits(GnuCashTransaction trx)
  {
    System.out.println("");
    System.out.println("Splits:");
    
    for ( GnuCashTransactionSplit splt : trx.getSplits() )
    {
      System.out.println(" - " + splt.toString());
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

    // <gnucash-file>
    try
    {
      gcshFileName = cmdLine.getOptionValue("gnucash-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-file>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("GnuCash file: '" + gcshFileName + "'");
    
    // <transaction-id>
    try
    {
      trxID = new GCshID( cmdLine.getOptionValue("transaction-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <transaction-id>");
      throw new InvalidCommandLineArgsException();
    }
    
    if ( ! scriptMode )
      System.err.println("Transaction ID: '" + trxID + "'");

    // <show-splits>
    if ( cmdLine.hasOption("show-splits"))
    {
      showSplits = true;
    }
    else
    {
      showSplits = false;
    }
    
    if ( ! scriptMode )
      System.err.println("Show splits: " + showSplits);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetTrxInfo", options );
  }
}
