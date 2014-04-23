package com.asus.ctc.ie.utilities.servers;

import java.io.*;
import java.net.*;
import java.util.*;

import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.utilities.AnalysisUtilities;



import edu.cmu.ark.DiscriminativeTagger;
import edu.cmu.ark.LabeledSentence;
import edu.stanford.nlp.trees.Tree;
import edu.cmu.ark.*;

/**
 * Wrapper class for the Supersense tagger server
 * @author Sanjay_Meena
 *
 */
public class SuperSenseWrapper { 
    
    String modelPath;
    String propertiesFilePath;
	private SuperSenseWrapper(){
		sst = null;
		//String str=GlobalProperties.prefixPath+GlobalProperties.getProperties(1).getProperty("propertiesFilePath");
		DiscriminativeTagger.loadProperties(GlobalProperties.getProperties(1).getProperty("propertiesFilePath", 
			"resources" + File.separator + "core_ie_resources"+File.separator+"ie_data"+File.separator+"ie_properties.properties"));
		
	}
	
	
	/**
	 * @return instance
	 */
	public static SuperSenseWrapper getInstance(){
		if(instance == null){
			instance = new SuperSenseWrapper();
		}
		return instance;
	}
	
	
	
	private LabeledSentence generateSupersenseTaggingInput(Tree sentence){
		LabeledSentence res = new LabeledSentence();
		List<Tree> leaves = sentence.getLeaves();
		
		for(int i=0;i<leaves.size();i++){
			String word = leaves.get(i).label().value();
			Tree preterm = leaves.get(i).parent(sentence);
			String pos = preterm.label().value();
			String stem = AnalysisUtilities.getInstance().getLemma(word, pos);
			res.addToken(word, stem, pos, "0");
		}
		
		return res;
	}

	
	/**
	 * @param sentence
	 * @return list
	 */
	public List<String> annotateMostFrequentSenses(Tree sentence) {
		int numleaves = sentence.getLeaves().size();
		if(numleaves <= 1){
			return new ArrayList<String>();
		}
		LabeledSentence labeled = generateSupersenseTaggingInput(sentence);
		labeled.setMostFrequentSenses(SuperSenseFeatureExtractor.getInstance().extractFirstSensePredictedLabels(labeled));
		return labeled.getMostFrequentSenses();
	}
	
	/**
	 * @param sentence
	 * @return list
	 */
	public List<String> annotateSentenceWithSupersenses(Tree sentence) {
		List<String> result = new ArrayList<String>();
		
		int numleaves = sentence.getLeaves().size();
		if(numleaves <= 1){
			return result;
		}
		LabeledSentence labeled = generateSupersenseTaggingInput(sentence);
		
		//see if a NER socket server is available
        int port = new Integer(GlobalProperties.getProperties(1).getProperty("supersenseServerPort","5555"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String inputStr = "";
			for(int i=0;i<labeled.length(); i++){
				String token = labeled.getTokens().get(i);
				String stem = labeled.getStems().get(i);
				String pos = labeled.getPOS().get(i);
				inputStr += token+"\t"+stem+"\t"+pos+"\n";
			}
			pw.println(inputStr);
			pw.flush(); //flush to complete the transmission

			while((line = br.readLine())!= null){
				String [] parts = line.split("\\t");
				if(parts.length==1) continue;
				result.add(parts[2]);
			}
			br.close();
			pw.close();
			client.close();
			
		} catch (Exception ex) {
			if(GlobalProperties.getDebug()) System.err.println("Could not connect to SST server.");
		}
		
		//if socket server not available, then use a local NER object
		if(result.size() == 0){
			try {
				if(sst == null){
					sst = DiscriminativeTagger.loadModel(GlobalProperties.getProperties(1).getProperty("supersenseModelFile", "config"+File.separator+"supersenseModelAllSemcor.ser.gz"));
				}
				sst.findBestLabelSequenceViterbi(labeled, sst.getWeights());
				for(String pred: labeled.getPredictions()){
					result.add(pred);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		//add a bunch of blanks if necessary
		while(result.size() < numleaves) result.add("0");
		
		if(GlobalProperties.getDebug()) System.err.println("annotateSentenceSST: "+result);
		return result;
	}

	
	
	private DiscriminativeTagger sst;
	private static SuperSenseWrapper instance;




}



