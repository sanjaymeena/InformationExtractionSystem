package com.asus.ctc.ie.tagger;



import com.asus.ctc.ie.utilities.TregexPatternFactory;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

/**
 * This class keeps track of of various heuristic metrics. 
 * <p>
 * <ul>
 * <li> Number of noun phrases </li>
 * <li> Number of Proper Nouns </li>
 * <li> Number of vague Noun phrases </li>
 * 
 * 
 * </ul>
 * </p>
 * @author Sanjay_Meena
 *
 */
public class SpecificityAnalyzer {




	private SpecificityAnalyzer(){
		numNPs = 0;
		numNPsWithProperNouns = 0;
		numVagueNPs = 0;
		numReferenceWords = 0;
	}
	
	
	/**
	 * @return instance
	 */
	public static SpecificityAnalyzer getInstance(){
		if(instance == null){
			instance = new SpecificityAnalyzer();
		}
		return instance;
	}
	
	
	/**
	 * @param t
	 */
	public void analyze(Tree t){
		numVagueNPs = 0;
		numNPs = 0;
		numNPsWithProperNouns = 0;
		numPronouns = 0;
		numReferenceWords = 0;
		
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		
		for(Tree leaf:t.getLeaves()){
			if(leaf.label().value().matches("^(another|other)$")){
				numReferenceWords++;
			}
		}

		
		tregexOpStr = "NP [ !> NP | < POS ]";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(t);
		while(matcher.find()){
			numNPs++;
		}
		
		
		tregexOpStr = "NP  < /^(PRP|PRP\\$|DT|JJ)$/  !<< NN|NNS|NNP|NNPS";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(t);
		while(matcher.find()){
			numPronouns++;
		}
		
		
		
		tregexOpStr = "NP !<< PP|SBAR|ADVP "
				+ "!<< (CD|NNS < /^\\d\\d\\d\\ds?$/) " // years are specific. e.g., "1984", "the 1980s"
				+ "!<< (NP < POS) " //possessed entities are specific. e.g., John's book
				+ "!< NNP|NNPS " //proper nouns are specific
				+ "[ !> NP | < POS ]"; //possessor NPs may be vague.  They are directly under an NP, unlike other mentions.  e.g., The man's book. 
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(t);
		while(matcher.find()){
			numVagueNPs++;
		}
		
		tregexOpStr = "NP < (NNP|NNPS !$,, NNP|NNPS) !> NP";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(t);
		while(matcher.find()){
			numNPsWithProperNouns++;
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	/**
	 * @return int
	 */
	public int getNumNPs() {
		return numNPs;
	}


	/**
	 * @return int
	 */
	public int getNumNPsWithProperNouns() {
		return numNPsWithProperNouns;
	}


	/**
	 * @return int
	 */
	public int getNumVagueNPs() {
		return numVagueNPs;
	}


	
	/**
	 * @return int
	 */
	public int getNumPronouns() {
		return numPronouns;
	}
	
	/**
	 * @return int
	 */
	public int getNumReferenceWords() {
		return numReferenceWords;
	}
	
	private int numVagueNPs;
	private int numNPs;
	private int numNPsWithProperNouns;
	private int numPronouns;
	private int numReferenceWords;

	private static SpecificityAnalyzer instance;




}
