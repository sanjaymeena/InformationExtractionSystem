package testarea;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.asus.ctc.ie.utilities.sigmakb.SimpleDOMParser;
import com.asus.ctc.ie.utilities.sigmakb.SimpleElement;



import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

/**
 * @author Sanjay_Meena
 *
 */
public class StanfordParserServerWithPOS {
    
    private static HashMap<String, String> preferences = new HashMap<String, String>();
    private static String CONFIG_FILE = "resources"+File.separator+ "core_ie_resources"+File.separator+"ie_data"+File.separator+ "stanford_parser_configuration.xml";
    static MaxentTagger tagger;
    /**
     * @param config
     */
    private static void readconfig(String config) {
        SimpleElement configuration = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(config));
            SimpleDOMParser sdp = new SimpleDOMParser();
            configuration = sdp.parse(br);

            if (configuration == null)
                throw new Exception("Error reading configuration file");
            // System.out.println( "configuration == " + configuration );
            preferencesFromXML(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preferencesFromXML(SimpleElement configuration) {

        for (int i = 0; i < configuration.getChildElements().size(); i++) {
            SimpleElement element = configuration.getChildElements().get(i);
            if (element.getTagName().equals("preference")) {
                String name = element.getAttribute("name");
                String value = element.getAttribute("value");
                preferences.put(name, value);
            }
        }
    }

    /**
     * 
     */
    public static void parserServer() {

        // INITIALIZE PARSER
        String serializedInputFileOrUrl = null;
        int port = 5556;
        int maxLength = 40;
     // variables needed to process the files to be parse
        String sentenceDelimiter = null;
        String model = "resources" + File.separator + "core_ie_resources"
		+ File.separator + "ie_data" + File.separator + "models"
		+ File.separator + "english-left3words-distsim.tagger";

	//String filePath = "resources/testdata/Articles/test##test.txt";
	 tagger = new MaxentTagger(model);
	
	
    
    
    
    
        
        
        readconfig(CONFIG_FILE);

        // variables needed to process the files to be parse
        port = Integer.parseInt(preferences.get("port"));
        maxLength = Integer.parseInt(preferences.get("maxLength"));
        serializedInputFileOrUrl = preferences.get("grammar");
        sentenceDelimiter = preferences.get("sentence");

        System.err.println("maxlength = " + maxLength);
        System.err.println("port = " + port);

        LexicalizedParser lp = null;
        // so we load a serialized parser
        String[] options = {
                            "-maxLength", Integer.toString(maxLength), "-outputFormat", "oneline" };
        
        Options op=new Options();
        op.setOptions(options);
        if (serializedInputFileOrUrl == null) {
            System.err.println("No grammar specified, exiting...");
            System.exit(0);
        }
        try {
            //lp = LexicalizedParser.loadModel(serializedInputFileOrUrl);
           // lp.setOptionFlags(options);
            lp =LexicalizedParser.loadModel(serializedInputFileOrUrl,op);
        } catch (IllegalArgumentException e) {
            System.err.println("Error loading parser, exiting...");
            System.exit(0);
        }
        // lp.setMaxLength(maxLength);

        // lp.setOptionFlags("-outputFormat",
        // "oneline","-maxLength",Integer.toString(maxLength));

        // declare a server socket and a client socket for the server
        // declare an input and an output stream
        ServerSocket parseServer = null;
        BufferedReader br;
        PrintWriter outputWriter;
        Socket clientSocket = null;
        try {
            parseServer = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e);
        }

        // Create a socket object from the ServerSocket to listen and accept
        // connections.
        // Open input and output streams

        while (true) {
            System.err.println("Waiting for Connection on Port: " + port);
            try {
                clientSocket = parseServer.accept();
                System.err.println("Connection Accepted From: " + clientSocket.getInetAddress());
                br = new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
                outputWriter = new PrintWriter(new PrintStream(clientSocket.getOutputStream()));

                String doc = "";

                do {
                    doc += br.readLine();
                } while (br.ready());
                System.err.println("received: " + doc);

                // PARSE
                try {
                    /**
                     * New Stanford parser 2012-03-02 , using apply is enough to
                     * get best scored tree.
                     */

                    
                    Reader sr=new StringReader(doc);
    		List<List<HasWord>> sentences = MaxentTagger
    			.tokenizeText(sr);
    		 ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentences.get(0));
    		
    		
                    
                    
                    
                    
                    // OUTPUT RESULT
                    Tree bestParse = lp.parse(tSentence);
                    TreePrint tp = lp.getTreePrint();
                    tp.printTree(bestParse, outputWriter);
                    // outputWriter.println(lp.getPCFGScore());
                    // String output = bestParse.toString();
                    // outputWriter.println(output);
                    // System.err.println("sent: " + output);

                    // int k = 5;
                    System.err.println("best factored parse:\n" + bestParse.toString());
                    System.err.println("k-best PCFG parses:");
                    // List<ScoredObject<Tree>> kbest =
                    // lp.getKBestPCFGParses(k);
                    // for(int i=0; i<kbest.size(); i++){
                    // System.err.println(kbest.get(i).object().toString());
                    // }

                } catch (Exception e) {
                    outputWriter.println("(ROOT (. .))");
                    outputWriter.println("-999999999.0");
                    e.printStackTrace();
                }

                outputWriter.flush();
                outputWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        parserServer();

    }

}
