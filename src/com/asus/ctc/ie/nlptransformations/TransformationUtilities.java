package com.asus.ctc.ie.nlptransformations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.TregexPatternFactory;


import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

/**
 * Utility functions required for sentence simplification
 * 
 * @author Sanjay_Meena
 *
 */
public class TransformationUtilities {
	
	private Set<String> verbsThatImplyComplements = null; //not yet fully implemented, can be ignored
	
	
	/**
	 * Add quotation marks if required to the tree.
	 * @param input
	 */
	public void addQuotationMarksIfNeeded(Tree input){
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;

		tregexOpStr = "__=parent < (/`/ !.. /'/)";

		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input);

		if(matcher.find()){
			TsurgeonPattern p;
			List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
			List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

			ps.add(Tsurgeon.parseOperation("insert ('' '') >-1 parent"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, input);
		}
	}
		
	/**
	 * Find Tense Information
	 * @param node
	 * @return
	 */
	public String findTense(Tree node) {
		if(node.label().equals("MD")){
			if(AnalysisUtilities.getInstance().treeToString(node).matches("^(would|could)$")){
				return "VBD";
			}
		}
		return node.label().value();
	}
	
	
	/**
	 * e.g., John and Mary like Bill.  -> John LIKES Bill.  Mary LIKES Bill.
	 * John and I like Bill -> John LIKES Bill.  I LIKE Bill.
	 * John and I are old. -> I IS old. John IS old.
	 */
	public void correctTense(Tree subject, Tree clause) {
		int tmpIndex;
		//correct verb tense when modifying subjects
		for(Tree uncle: clause.getChildrenAsList()){
			String newVerbPOS = null;
			Tree verbPreterminal = null;
			boolean needToModifyVerb = false;
			//if the node is a subject (i.e., its uncle is a VP), then check
			//to see if its tense needs to be changed
			String headPOS = subject.headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder()).label().value();
			if(uncle.label().value().equals("VP") && !headPOS.endsWith("S")){
				verbPreterminal = uncle.headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder());
				//original main verb was plural but the conjoined subject word is singular
				//e.g., John (and Mary) like Bill.  -> John like Bill.
				if((verbPreterminal.label().value().equals("VB") || verbPreterminal.label().value().equals("VBP"))){ //the parser confuses VBP with VB
					if(AnalysisUtilities.getInstance().treeToString(subject).equals("I") || AnalysisUtilities.getInstance().treeToString(subject).equals("you")){
						newVerbPOS = "VBP";
					}else{
						newVerbPOS = "VBZ";
					}
					needToModifyVerb = true;
				}else if(verbPreterminal.label().value().equals("VBD")){
					newVerbPOS = "VBD";
					needToModifyVerb = true;
				}
			}
			//if needed, change the tense of the verb
			if(needToModifyVerb){
				String verbLemma = AnalysisUtilities.getInstance().getLemma(verbPreterminal.getChild(0).label().value(), verbPreterminal.label().value());
				String newVerb;
				//special cases
				if(verbLemma.equals("be") && newVerbPOS.equals("VBD")){
					if(subject.label().value().endsWith("S")) newVerb = "were";
					else  newVerb = "was";
				}else if(verbLemma.equals("be") && AnalysisUtilities.getInstance().treeToString(subject).equals("I") && newVerbPOS.equals("VBP")){
					newVerb = "am";
				}else{ //default
					newVerb = AnalysisUtilities.getInstance().getSurfaceForm(verbLemma, newVerbPOS);
				}
				tmpIndex = verbPreterminal.parent(uncle).objectIndexOf(verbPreterminal);
				Tree verbParent = verbPreterminal.parent(uncle);
				verbParent.removeChild(tmpIndex);
				verbParent.addChild(tmpIndex, AnalysisUtilities.getInstance().readTreeFromString("("+newVerbPOS+" "+newVerb+")"));
			}					
		}
	}
	
	
	
	
	/**
	 * Identifies whether the given verb implies its complement
	 * e.g., true for "forget", false for "believe"
	 * 
	 * @param verbLemma
	 * @return
	 */
	public boolean verbImpliesComplement(String verbLemma) {
		if(verbsThatImplyComplements == null){
			verbsThatImplyComplements = new HashSet<String>();
			verbsThatImplyComplements.add("know");
			verbsThatImplyComplements.add("forget");
			verbsThatImplyComplements.add("discover");
			//TODO (this isn't used in the system, but extensions are possible)
		}
	
		return verbsThatImplyComplements.contains(verbLemma);
	
	}
	
	
	
	
	/**
	 * Method to add a new Question object q to a given set
	 * if the collection treeSet does not already include q.
	 * 
	 * @param treeSet
	 * @param q
	 */
	public void addIfNovel(Collection<SentenceNode> treeSet, SentenceNode q) {
		boolean exists = false;
		for(SentenceNode old: treeSet){
			if(old.getTree().equals(q.getTree())){
				exists = true;
				break;
			}
		}
		if(!exists){
			treeSet.add(q);
		}
	}
	
	
	
	
	
	/**
	 * Convert a non-definite determiner to "the".
	 * Used when extracting from noun modifiers such as relative clauses.
	 * E.g., "A tall man, who was named Bob, entered the store." 
	 * -> "A tall man was named Bob."
	 * -> "THE tall man was named Bob."
	 *  
	 * @param np
	 */
	public void makeDeterminerDefinite(Tree np) {
		String tregexOpStr = "NP !> __ <+(NP) (DT=det !< the)";
		TregexPattern matchPattern = TregexPatternFactory.getPattern(tregexOpStr);

		TsurgeonPattern p;
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

		ps.add(Tsurgeon.parseOperation("replace det (DT the)"));
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, np);
	}



	


	public boolean isPlural(Tree nountree){
		String headTerminalLabel = nountree.headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder()).label().value();
		return (headTerminalLabel.equals("NNS") || headTerminalLabel.equals("NPS"));
	}
	
}
