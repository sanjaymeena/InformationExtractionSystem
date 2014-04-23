package com.asus.ctc.ie.utilities.servers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.utilities.sigmakb.SimpleDOMParser;
import com.asus.ctc.ie.utilities.sigmakb.SimpleElement;





import edu.cmu.ark.DiscriminativeTagger;
import edu.cmu.ark.LabeledSentence;
import edu.cmu.ark.SuperSenseFeatureExtractor;

/**
 * @author Sanjay_Meena
 *
 */
public class SuperSenseTaggerServer {

    private static String CONFIG_FILE = GlobalProperties.getPrefixPath()+File.separator+ "resources"+ File.separator+"core_ie_resources"+File.separator+"ie_data"+File.separator+ "supersense_tagger_configuration.xml";
    private static HashMap<String, String> preferences = new HashMap<String, String>();

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
    public static void SuperSenseTaggerServer()
    {
        int port = 5555;
        String modelFile = null;
        String propertiesFile = "tagger.properties";
        boolean debug = false;
        
        
        readconfig(CONFIG_FILE);
        
        
        port=Integer.parseInt(preferences.get("port"));
        modelFile=GlobalProperties.getPrefixPath()+File.separator+preferences.get("model");
        propertiesFile = GlobalProperties.getPrefixPath()+File.separator+preferences.get("properties");
        debug= Boolean.parseBoolean(preferences.get("debug"));
        if(modelFile == null){
            System.err.println("need to specify --model");
            System.exit(0);
        }
        
        System.out.println("-port " + port + "  -model "  +modelFile + "  -propertiesFile " + propertiesFile );
        DiscriminativeTagger.loadProperties(propertiesFile);
        DiscriminativeTagger tagger = DiscriminativeTagger.loadModel(modelFile);
        
        // declare a server socket and a client socket for the server
        // declare an input and an output stream
        ServerSocket server = null;
        BufferedReader br;
        PrintWriter outputWriter;
        Socket clientSocket = null;
        try {
            server = new ServerSocket(port);
        }
        catch (IOException e) {
            System.err.println(e);
        } 

        // Create a socket object from the ServerSocket to listen and accept 
        // connections.
        // Open input and output streams

        while (true) {
            System.err.println("Waiting for Connection on Port: "+port);
            try {
                clientSocket = server.accept();
                System.err.println("Connection Accepted From: "+clientSocket.getInetAddress());
                br = new BufferedReader(new InputStreamReader(new DataInputStream(clientSocket.getInputStream())));
                outputWriter = new PrintWriter(new PrintStream(clientSocket.getOutputStream()));


                LabeledSentence inputSentence = new LabeledSentence();
                String buf = br.readLine();
                if(debug) System.err.println("received: " + buf);
                String [] parts;
                List<LabeledSentence> sentences = new ArrayList<LabeledSentence>();
                do{
                    
                    if(buf.length()==0){
                        if(inputSentence.length()>0){
                            sentences.add(inputSentence);
                            inputSentence = new LabeledSentence();                              
                        }
                    }else{
                        parts = buf.split("\\t");
                        if(parts.length == 2){//word and POS                
                            inputSentence.addToken(parts[0], SuperSenseFeatureExtractor.getInstance().getStem(parts[0], parts[1]), parts[1], "0");//TODO
                        }else if(parts.length == 3){//word,stem, POS
                            inputSentence.addToken(parts[0], parts[1], parts[2], "0");
                        }
                    }
                    
                    buf = br.readLine();
                    if(debug) System.err.println("recv:\t" + buf);
                }while(br.ready());
                
                if(inputSentence.length()>0){
                    sentences.add(inputSentence);
                    inputSentence = new LabeledSentence();                              
                }
                                
                //PROCESS
                try{
                    String output = "";
                    for(LabeledSentence sent: sentences){   
                        tagger.findBestLabelSequenceViterbi(sent, tagger.getWeights());
                        output += sent.taggedString()+"\n";
                    }
                    outputWriter.print(output);
                    if(debug) System.err.println("sent:\t" + output);
                }catch(Exception e){
                    outputWriter.println("");
                    e.printStackTrace();
                }
                
                outputWriter.flush();
                outputWriter.close();

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        
        
        
        
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        SuperSenseTaggerServer();
    }

}
