package com.asus.ctc.ie.utilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class to read a file with proper encoding.
 * 
 * @author Sanjay_Meena
 *
 */
public class ReadWriteTextFileWithEncoding {

	/**
	 * @param aArgs
	 * @throws IOException
	 */
	public static void main(String... aArgs) throws IOException {
	   // String fileName = aArgs[0];
	   // String encoding = aArgs[1];
	    ReadWriteTextFileWithEncoding test = new ReadWriteTextFileWithEncoding(
	      "read.txt", "UTF-8"
	    );
	   // test.write();
	    test.read();
	  }
	  
	  /** Constructor. 
	 * 
	 * @param aFileName 
	 * @param aEncoding */
	  public ReadWriteTextFileWithEncoding(String aFileName, String aEncoding){
	    fEncoding = aEncoding;
	    fFileName = aFileName;
	  }
	  
	  
	  
	  /** Read the contents of the given file. 
	 * 
	 * @return file content
	 * @throws IOException */
	  public String read() throws IOException {
	    log("Reading from file....");
	    StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(new FileInputStream(fFileName), fEncoding);
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    log("Text read in: " + text);
	    return text.toString();
	  }
	  
	  // PRIVATE 
	  private final String fFileName;
	  private final String fEncoding;
	//  private final String FIXED_TEXT = "But soft! what code in yonder program breaks?";
	  final static String ENCODING = "UTF_8";
	  private void log(String aMessage){
	    System.out.println(aMessage);
	  }
	  public void write(String content) throws IOException {
		    
		  File file=new File(fFileName);
		  
		// if file doesnt exists, then create it
					if (!file.exists()) {
						file.createNewFile();
					}
		  
		  
		    
		    FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		    }
		  }
