

package com.asus.ctc.ie.utilities;

import edu.stanford.nlp.trees.Tree;

/**
 * This class is to represent the parse tree of a sentence received from stanford parser
 * 
 * 
 * @author Sanjay_Meena
 * @see  AnalysisUtilities
 */
public class ParseResult {
	/**
	 * 
	 */
	public boolean success;
	/**
	 * 
	 */
	public Tree parse;
	/**
	 * 
	 */
	public double score;
	/**
	 * @param s
	 * @param p
	 * @param sc
	 */
	public ParseResult(boolean s, Tree p, double sc) { success=s; parse=p; score=sc; }
}
