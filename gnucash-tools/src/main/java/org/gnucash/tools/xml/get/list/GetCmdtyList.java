package org.gnucash.tools.xml.get.list;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.read.GnuCashCommodity;
import org.gnucash.api.read.impl.GnuCashFileImpl;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class GetCmdtyList extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(GetCmdtyList.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String               gcshFileName = null;
  private static Helper.CmdtyListMode mode         = null; 
  private static String               isin         = null;
  private static String               name         = null;
  
  private static boolean scriptMode = false; // ::TODO

  public static void main( String[] args )
  {
    try
    {
      GetCmdtyList tool = new GetCmdtyList ();
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
    // cmdtyID = UUID.randomUUID();

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
      
    Option optMode = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("Mode")
      .withDescription("Mode")
      .withLongOpt("mode")
      .create("m");
    	    	      
//    Option optType = OptionBuilder
//      .hasArg()
//      .withArgName("type")
//      .withDescription("Commodity type")
//      .withLongOpt("type")
//      .create("t");
      
    Option optISIN = OptionBuilder
      .hasArg()
      .withArgName("isin")
      .withDescription("ISIN")
      .withLongOpt("isin")
      .create("is");
    	    	      
    Option optName = OptionBuilder
      .hasArg()
      .withArgName("name")
      .withDescription("Account name (part of)")
      .withLongOpt("name")
      .create("n");
    	      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFile);
    options.addOption(optMode);
    // options.addOption(optType);
    options.addOption(optISIN);
    options.addOption(optName);
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
    
    Collection<GnuCashCommodity> cmdtyList = null; 
    if ( mode == Helper.CmdtyListMode.ALL )
        cmdtyList = gcshFile.getCommodities();
//    else if ( mode == Helper.CmdtyListMode.TYPE )
//    	cmdtyList = gcshFile.getCommoditiesByType(type);
    else if ( mode == Helper.CmdtyListMode.ISIN ) {
    	GnuCashCommodity sec = gcshFile.getCommodityByXCode(isin);
    	cmdtyList = new ArrayList<GnuCashCommodity>();
    	cmdtyList.add( sec );
    }
    else if ( mode == Helper.CmdtyListMode.NAME )
    	cmdtyList = gcshFile.getCommoditiesByName(name, true);

    if ( cmdtyList.size() == 0 ) 
    {
    	System.err.println("Found no commodity with that type.");
    	throw new NoEntryFoundException();
    }

    System.err.println("Found " + cmdtyList.size() + " commodity/ies.");
    for ( GnuCashCommodity cmdty : cmdtyList )
    {
    	System.out.println(cmdty.toString());	
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
      System.err.println("GnuCash file:      '" + gcshFileName + "'");
    
    // <mode>
    try
    {
      mode = Helper.CmdtyListMode.valueOf(cmdLine.getOptionValue("mode"));
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <mode>");
      throw new InvalidCommandLineArgsException();
    }

    // <type>
//    if ( cmdLine.hasOption( "type" ) )
//    {
//    	if ( mode != Helper.CmdtyListMode.TYPE )
//    	{
//            System.err.println("Error: <type> must only be set with <mode> = '" + Helper.CmdtyListMode.TYPE + "'");
//            throw new InvalidCommandLineArgsException();
//    	}
//    	
//        try
//        {
//        	type = GnuCashCommodity.Type.valueOf(cmdLine.getOptionValue("type"));
//        }
//        catch ( Exception exc )
//        {
//        	System.err.println("Could not parse <type>");
//        	throw new InvalidCommandLineArgsException();
//        }
//    }
//    else
//    {
//    	if ( mode == Helper.CmdtyListMode.TYPE )
//    	{
//            System.err.println("Error: <type> must be set with <mode> = '" + Helper.CmdtyListMode.TYPE + "'");
//            throw new InvalidCommandLineArgsException();
//    	}
//    	
//    	type = null;
//    }
//    
//    if ( ! scriptMode )
//      System.err.println("Type:              " + type);

    // <isin>
    if ( cmdLine.hasOption( "isin" ) )
    {
    	if ( mode != Helper.CmdtyListMode.ISIN )
    	{
            System.err.println("Error: <isin> must only be set with <mode> = '" + Helper.CmdtyListMode.ISIN + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
        try
        {
        	isin = cmdLine.getOptionValue("isin");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <isin>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == Helper.CmdtyListMode.ISIN )
    	{
            System.err.println("Error: <isin> must be set with <mode> = '" + Helper.CmdtyListMode.ISIN + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	isin = null;
    }
    
    if ( ! scriptMode )
      System.err.println("ISIN:              " + isin);
    
    // <name>
    if ( cmdLine.hasOption( "name" ) )
    {
    	if ( mode != Helper.CmdtyListMode.NAME )
    	{
            System.err.println("Error: <name> must only be set with <mode> = '" + Helper.CmdtyListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
        try
        {
        	name = cmdLine.getOptionValue("name");
        }
        catch ( Exception exc )
        {
        	System.err.println("Could not parse <name>");
        	throw new InvalidCommandLineArgsException();
        }
    }
    else
    {
    	if ( mode == Helper.CmdtyListMode.NAME )
    	{
            System.err.println("Error: <name> must be set with <mode> = '" + Helper.CmdtyListMode.NAME + "'");
            throw new InvalidCommandLineArgsException();
    	}
    	
    	name = null;
    }
    
    if ( ! scriptMode )
      System.err.println("Name:              " + name);
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "GetCmdtyList", options );
    
    System.out.println("");
    System.out.println("Valid values for <mode>:");
    for ( Helper.CmdtyListMode elt : Helper.CmdtyListMode.values() )
      System.out.println(" - " + elt);

//    System.out.println("");
//    System.out.println("Valid values for <type>:");
//    for ( GnuCashAccount.Type elt : GnuCashAccount.Type.values() )
//      System.out.println(" - " + elt);
  }
}
