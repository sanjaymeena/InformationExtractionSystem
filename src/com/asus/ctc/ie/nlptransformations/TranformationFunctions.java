package com.asus.ctc.ie.nlptransformations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.TregexPatternFactory;


import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

/**
 * Public Class for Various NLP Transformations related to Sentence Simplifications;
 * 
 * @author Sanjay_Meena
 *
 */
public class TranformationFunctions {

TransformationUtilities utils;	
private TreeFactory factory;

public TranformationFunctions()
{
	utils=new TransformationUtilities();
	factory = new LabeledScoredTreeFactory();
}
	




/**
 * e.g., As John slept, I studied. ->  John slept.
 * 
 */
public void extractSubordinateClauses(Collection<SentenceNode> extracted, SentenceNode input) {
	Tree subord;
	String tregexOpStr;
	TregexPattern matchPattern;
	TregexMatcher matcher;

	tregexOpStr = " SBAR [ > VP < IN | > S|SINV ]  " + //not a complement
		" !< (IN < if|unless|that)" + //not a conditional antecedent
		" < (S=sub !< (VP < VBG)) "+//+ //not a participial phrase
		" >S|SINV|VP "; //not part of a noun phrase or PP (other methods for those)
		
	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
	matcher = matchPattern.matcher(input.getTree());
	while(matcher.find()){
		Tree newTree = factory.newTreeNode("ROOT", new ArrayList<Tree>());
		subord = matcher.getNode("sub");
		newTree.addChild(subord.deepCopy());

		AnalysisUtilities.getInstance().addPeriodIfNeeded(newTree);
		utils.addQuotationMarksIfNeeded(newTree);
		SentenceNode newTreeWithFeatures = input.deepCopy();
		newTreeWithFeatures.setTree(newTree);
		if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromFiniteClause", 1.0); //old feature name
		if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromSubordinateClause", 1.0);
		if(GlobalProperties.getDebug()) System.err.println("extractSubordinateClauses: "+newTree.toString());
		utils.addIfNovel(extracted, newTreeWithFeatures);
	}
}






 /**
  * 
  * 
  */public void extractComplementClauses(Collection<SentenceNode> extracted, SentenceNode input) {
 	Tree subord;
 	String tregexOpStr;
 	TregexPattern matchPattern;
 	TregexMatcher matcher;

 	//TODO should also address infinitive complements
 	tregexOpStr = "SBAR "+
 		" < (S=sub !< (VP < VBG)) "+//+ //not a participial phrase
 		" !> NP|PP "+ //not part of a noun phrase or PP (other methods for those)
 		" [ $- /^VB.*/=verb | >+(SBAR) (SBAR $- /^VB.*/=verb) ] "; //complement of a VP (follows the verb)

 	matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
 	matcher = matchPattern.matcher(input.getTree());
 	while(matcher.find()){
 		Tree newTree = factory.newTreeNode("ROOT", new ArrayList<Tree>());
 		subord = matcher.getNode("sub");
 		Tree verb = matcher.getNode("verb");
 		String verbLemma = AnalysisUtilities.getInstance().getLemma(AnalysisUtilities.getInstance().treeToString(verb), verb.label().value());
 		
 		if(!utils.verbImpliesComplement(verbLemma)){
 			continue;
 		}
 		newTree.addChild(subord.deepCopy());

 		AnalysisUtilities.getInstance().addPeriodIfNeeded(newTree);
 		utils.addQuotationMarksIfNeeded(newTree);
 		SentenceNode newTreeWithFeatures = input.deepCopy();
 		newTreeWithFeatures.setTree(newTree);
 		if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromFiniteClause", 1.0); //old feature name
 		if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromComplementClause", 1.0);
 		if(GlobalProperties.getDebug()) System.err.println("extractComplementClauses: "+newTree.toString());
 		utils.addIfNovel(extracted, newTreeWithFeatures);
 	}
 }

 




	
	/**
	 * e.g., John and James like Susan.  ->  John likes Susan.
	 * 
	 */
	public void extractConjoinedNPs(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		Tree conjoinedNode;  
		Tree parent;

		TregexMatcher matcher;
		SentenceNode newQuestion;

		
		//only extract conjoined NPs that are arguments or adjuncts of the main verb
		// in the tree, this means the closest S will be the one under the root
		tregexOpStr = "NP=parent < (CONJP|CC !< or|nor [ "
			+ " $+ /^(N.*|PRP|SBAR)$/=child  $-- /^(N.*|PRP|SBAR)$/ | " //there must be a noun on each side of the conjunction
			+ " $-- /^(N.*|PRP|SBAR)$/=child $+ /^(N.*|PRP|SBAR)$/ ] ) " //this avoids extracting from flat NPs such as "the smaller and darker form"
			+ " !>> (/.*/ $ (CC|CONJP !< or|nor)) "  //this cannot be nested within a larger conjunction or followed by a conjunction (we recur later to catch this) 
			+ " !$ (CC|CONJP !< or|nor)" 
			+ " !.. (CC|CONJP !< or|nor > NP|PP|S|SBAR|VP) !>> SBAR " 
		/*	to avoid splits like:
		 * eg  Taipei and Keelung together form the taipei area.
		 * -> Taipei together form the taipei area.
		 * -> Keelung together form the taipei area.
		 */
			+ " !$+ (ADVP < RB $+ VP)" 
		/*
		 * To avoid splits for sentences like: 
		 * two airports are Taipei Songshan and Taiwan Taoyuan .
		 * => Two airports are Taipei Songshan.
		 * => Two airports are Taiwan Taoyuan.
		 */	
			+ " !$- (/VB.*/ >> (VP $- (NP < CD)))" 
			
			/*
			 *Example EmailSentence:  It lies in the two relatively narrow valleys of the Keelung and Xindian rivers. 
			 *There are two levels of Maths and Physics.
			 *
			 *These sentences have a number, might be a good reason not to break them down.
			 */
		    +	"!>> (PP $- (NP < CD))";
			//+ " >> (ROOT !< (S <+(VP) (/^VB.*$/ < are|were|be|seem|appear))) " ; //don't break plural predicatePhrase nominatives (e.g., "John and Mary are two of my best friends.")";
			
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());
		List<Integer> nodeIndexes = new ArrayList<Integer>();
		List<Integer> parentIDs = new ArrayList<Integer>();

		
		while(matcher.find()){
			//store the parents' IDs (in the tree)
			parent = matcher.getNode("parent");
			parentIDs.add(parent.nodeNumber(input.getTree()));

			conjoinedNode = matcher.getNode("child");
			//store the conjoined nodes' index into their parent's list of children
			int idx = parent.objectIndexOf(conjoinedNode);
			if(!nodeIndexes.contains(idx)) nodeIndexes.add(idx);
		}
		
		//for each of the conjoined children,
		//create a new tree by removing all the nodes they are conjoined with
		Collections.sort(nodeIndexes);//sort, just to keep them in the original order
		for(int i=0; i<nodeIndexes.size(); i++){
			newQuestion = input.deepCopy();

			Tree t = newQuestion.getTree();
			parent = t.getNodeNumber(parentIDs.get(i));
			Tree gparent = parent.parent(t);
			conjoinedNode = parent.getChild(nodeIndexes.get(i));
			String siblingLabel;
			
			//Remove all the nodes that are conjoined
			//with the selected noun (or are conjunctions, commas).
			//These can have labels NP, NN, ..., PRP for pronouns, CC, "," for commas, ":" for semi-colons
			for(int j=0;j<parent.numChildren(); j++){
				if(parent.getChild(j) == conjoinedNode) continue;
				siblingLabel = parent.getChild(j).label().value();
				if(siblingLabel.matches("^[NCP,:S].*")){
					parent.removeChild(j);
					j--;
				}
			}

			//if there is an trivial unary "NP -> NP",
			//remove the parent and put the child in its place
			if(parent.numChildren() == 1 && parent.getChild(0).label().equals("NP")){
				int tmpIndex = gparent.objectIndexOf(parent);
				gparent.removeChild(tmpIndex);
				gparent.addChild(tmpIndex, parent.getChild(0));
			}

			utils.correctTense(conjoinedNode, gparent);
			utils.addQuotationMarksIfNeeded(newQuestion.getTree());

			if(GlobalProperties.getDebug()) System.err.println("extractConjoinedNPs: "+newQuestion.getTree().toString());
			if(GlobalProperties.getComputeFeatures()) newQuestion.setFeatureValue("extractedFromConjoinedPhrases", 1.0); //old feature name
			if(GlobalProperties.getComputeFeatures()) newQuestion.setFeatureValue("extractedFromConjoinedNPs", 1.0);
			extracted.add(newQuestion);
		}
	}
	
	
	
	/**
	 * e.g., John ran and Bill walked.  -> John ran. Bill walked.
	 * 
	 */
	public void extractConjoinedPhrases(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		Tree conjoinedNode;  

		TregexMatcher matcher;
		//Tree newTree = copy.getIntermediateTree();
		Tree newTree;
		int nodeindex;

		tregexOpStr = "__ " +
		" [ < (VP < (/VB.*/=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) "+ //get the first conjunction, to avoid spurious duplicate matches
		" | < (VP < (VP=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) "+ // verb phrases may be conjoined by commas and adverbs (e.g., "John ran, then walked.")
		" | < (S|SINV < (S|SINV=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) "+
		" | < (S|SINV < (S|SINV=child $ (/:/ < /;/ !$++ /:/))) "+
		//" | < (ADJP < (JJ|JJR|ADJP=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) " +
		//" | < (ADVP < (RB|RBR|ADVP=child $ RB|RBR|ADVP=child $ (CC|CONJP !< or|nor !$++ CC|CONJP)))  "+ 
		//" | < (PP < (PP=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) " +
		" | < (SBAR < (SBAR=child $ (CC|CONJP !< or|nor !$++ CC|CONJP))) ] "+
		" !$ (CC|CONJP !< or|nor)" + //this cannot be nested within a larger conjunction or followed by a conjunction (we recur later to catch this)
		" !< (CC|CONJP !< or|nor) " +
		" !.. (CC|CONJP !< or|nor > NP|PP|S|SBAR|VP) !>> SBAR"; 


		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());

		while(matcher.find()){
			conjoinedNode = matcher.getNode("child");
			nodeindex = conjoinedNode.nodeNumber(input.getTree());

			//make a copy of the input for this iteration
			newTree = input.getTree().deepCopy();	
			removeConjoinedSiblingsHelper(newTree, nodeindex);	
			
			//for conjoined main clauses, add punctuation if necessary
			AnalysisUtilities.getInstance().addPeriodIfNeeded(newTree);

			//make a new Question object and add it
			utils.addQuotationMarksIfNeeded(newTree);
			
			SentenceNode newTreeWithFeatures = input.deepCopy();
			newTreeWithFeatures.setTree(newTree);
			if(GlobalProperties.getDebug()) System.err.println("extractConjoinedPhrases: "+newTree.toString());
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromConjoinedPhrases", 1.0); //old feature name
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromConjoined", 1.0);
			utils.addIfNovel(extracted, newTreeWithFeatures);
		}
	}
	
	
	
	private void removeConjoinedSiblingsHelper(Tree copy, int childindex) {
		if(GlobalProperties.getDebug()) System.err.println("removeConjoinedSiblingsHelper: "+copy.toString());
		Tree child = copy.getNodeNumber(childindex);
		Tree parent = child.parent(copy);
		Tree gparent = parent.parent(copy);

		int parentIdx = gparent.objectIndexOf(parent);
		
		//By an annoying PTB convention, some verb phrase conjunctions 
		//can conjoin two verb preterminals under a VP,
		//rather than only allowing VP nodes to be conjoined.
		//e.g., John walked and played.
		//So, we add an extra VP node in between if necessary
		if(child.label().value().startsWith("VB")){
			gparent.removeChild(parentIdx);
			Tree newTree = factory.newTreeNode("VP", new ArrayList<Tree>());
			newTree.addChild(child);
			gparent.addChild(parentIdx, newTree);
		}else{
			gparent.setChild(parentIdx, child);
		}
	}
	
	
	/**
	 * 
	 * John studied, hoping to get a good grade. -> John hoped to get a good grade.
	 * 
	 * @param extracted
	 * @param input
	 */
	public void extractVerbParticipialModifiers(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;

		tregexOpStr = 
			"S=sub $- /,/ !< NP < (VP=participial < VBG=verb) " +
			" >+(VP) (S|SINV < NP=subj) " +
			" >> (ROOT <<# /VB.*/=tense) "; //tense determined by top-most verb
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());
		while(matcher.find()){
			String verbPOS = utils.findTense(matcher.getNode("tense"));
			Tree p = matcher.getNode("participial").deepCopy();
			Tree verb = matcher.getNode("verb");
			String verbLemma =  AnalysisUtilities.getInstance().getLemma(verb.getChild(0).label().value(), verb.label().value());
			String newVerb = AnalysisUtilities.getInstance().getSurfaceForm(verbLemma, verbPOS); 
			int verbIndex = p.objectIndexOf(verb);
			if(verbIndex>=0){
			p.removeChild(verbIndex);
			p.addChild(verbIndex, AnalysisUtilities.getInstance().readTreeFromString("("+verbPOS+" "+newVerb+")"));
			String treeStr = "(ROOT (S "+matcher.getNode("subj").toString()+" "+p.toString()+" (. .)))";
			Tree newTree = AnalysisUtilities.getInstance().readTreeFromString(treeStr);
			utils.correctTense(newTree.getChild(0).getChild(0), newTree.getChild(0));
			
			utils.addQuotationMarksIfNeeded(newTree);
			SentenceNode newTreeWithFeatures = input.deepCopy();
			newTreeWithFeatures.setTree(newTree);
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromParticipial", 1.0);
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromVerbParticipial", 1.0);
			if(GlobalProperties.getDebug()) System.err.println("extractVerbParticipialModifiers: "+newTree.toString());
			utils.addIfNovel(extracted, newTreeWithFeatures);
			}
		}
	}
	
	
	
	/**
	 * e.g., Lincoln, the 16th president, was tall. -> Lincoln was the 16th president.
	 * The meeting, in 1984, was important. -> The meeting was in 1984.
	 */
	public void extractAppositives(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		
		tregexOpStr = "NP < (NP=noun !$-- NP $+ (/,/ $++ NP|PP=appositive !$ CC|CONJP)) " +
				" >> (ROOT <<# /^VB.*/=mainverb) "; //extract the main verb to capture the verb tense
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());
		while(matcher.find()){
			Tree verbtree = matcher.getNode("mainverb");
			Tree nountree = matcher.getNode("noun").deepCopy();
			Tree appositivetree = matcher.getNode("appositive");

			utils.makeDeterminerDefinite(nountree);
			
			//if both are proper nouns, do not extract because this is not an appositive(e.g., "Pittsburgh, PA")
			/*if(nountree.headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder()).label().value().equals("NNP")
					&& appositivetree.headPreTerminal(AnalysisUtilities.getInstance().getHeadFinder()).label().value().equals("NNP"))
			{
				continue;
			}*/

			//make a new tree for a copula sentence with the noun and appositive
			String pos = verbtree.label().value();
			String copula;
			if(pos.equals("VBD")){
				if(utils.isPlural(nountree)){
					copula = "(VBD were)";
				}else{
					copula = "(VBD was)";
				}
			}else{
				if(utils.isPlural(nountree)){
					copula = "(VBD are)";
				}else{
					copula = "(VBD is)";
				}
			}
			Tree newTree = AnalysisUtilities.getInstance().readTreeFromString("(ROOT (S "+nountree+" (VP "+copula+" "+appositivetree+") (. .)))");		
			
			utils.addQuotationMarksIfNeeded(newTree);
			if(GlobalProperties.getDebug()) System.err.println("extractAppositives: "+ newTree.toString());
			SentenceNode newTreeWithFeatures = input.deepCopy();
			newTreeWithFeatures.setTree(newTree);
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromAppositive", 1.0);

			utils.addIfNovel(extracted, newTreeWithFeatures);

		}
	}
	
	
	
	

	/**
	 * e.g., John, who is a friend of mine, likes Susan. -> John is a friend of mine.
	 * 
	 */
	public void extractNonRestrictiveRelativeClauses(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		TregexMatcher matcherclause;

		tregexOpStr = "NP=np < (SBAR=sbar [ < (WHADVP=wherecomp < (WRB < where)) "
					+ " | < (WHNP !< /WP\\$/) "
					+ " | < (WHNP=possessive < /WP\\$/)"  //John, whose car was
					+ " | < (WHPP < IN|TO=preposition) ] $-- NP $- /,/ "
					+ " < S=relclause  !< WHADJP)";

		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());

		//iterate over all the relative clauses in the input
		//and create an output sentence for each one.
		while(matcher.find()){
			Tree missingArgumentTree = matcher.getNode("np");
			Tree relclause = matcher.getNode("relclause");
			if(missingArgumentTree == null || relclause == null) continue;
			missingArgumentTree = missingArgumentTree.deepCopy();
			relclause = relclause.deepCopy();	
			Tree possessive = matcher.getNode("possessive");
			Tree sbar = matcher.getNode("sbar").deepCopy();
			
			utils.makeDeterminerDefinite(missingArgumentTree);
			
			if(possessive != null){
				possessive = possessive.deepCopy();
				possessive.removeChild(0);
				String newTree = "(NP (NP "+missingArgumentTree.toString()+ " (POS 's))";
				for(int i=0; i<possessive.numChildren(); i++) newTree += possessive.getChild(i).toString() + " ";
				newTree += ")";
				missingArgumentTree = AnalysisUtilities.getInstance().readTreeFromString(newTree);
			}

			//remove the relative clause and the commas surrounding it from the missing argument tree
			for(int i=0; i<missingArgumentTree.numChildren(); i++){
				if(missingArgumentTree.getChild(i).equals(sbar)){
					//remove the relative clause
					missingArgumentTree.removeChild(i);
					//remove the comma after the relative clause
					if(i<missingArgumentTree.numChildren() && missingArgumentTree.getChild(i).label().value().equals(",")){
						missingArgumentTree.removeChild(i);
					}
					//remove the comma before the relative clause
					if(i>0 && missingArgumentTree.getChild(i-1).label().value().equals(",")){
						missingArgumentTree.removeChild(i-1);
						i--;
					}
					i--;
				}
			}
			
			//put the noun in the clause at the topmost place with an opening for a noun. 
			//Note that this may mess up if there are noun phrase adjuncts like "The man I met Tuesday".

			//specifically: 
			//the parent of the noun can be either a clause (S) as in "The man who met me"
			//or a verb phrase as in "The man who I met".
			//for verb phrases, add the noun to the end since it will be an object.
			//for clauses, add the noun to the beginning since it will be the subject.
			tregexOpStr = "S|VP=newparent !< NP < (VP=verb !< TO !$ TO)";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcherclause = matchPattern.matcher(relclause);
			boolean subjectMovement = true;
			if(!matcherclause.find()){
				tregexOpStr = "VP=newparent !< VP < /VB.*/=verb !>> (S !< NP) !<< (VP !< VP !< NP)";
				matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
				matcherclause = matchPattern.matcher(relclause);
				subjectMovement = false;
			}
			
			//reset (so the first match isn't skipped)
			matcherclause = matchPattern.matcher(relclause);
			
			if(matcherclause.find()){
				Tree newparenttree = matcherclause.getNode("newparent");
				Tree verbtree = matcherclause.getNode("verb");
				boolean ppRelativeClause = false;  
				
				if(matcher.getNode("wherecomp") != null){
					String tmp = "(PP (IN at) "+missingArgumentTree.toString()+")";  
					missingArgumentTree = AnalysisUtilities.getInstance().readTreeFromString(tmp);
					ppRelativeClause = true;
					subjectMovement = false;
				}else if(matcher.getNode("preposition") != null){
					String tmp = "(PP (IN "+AnalysisUtilities.getInstance().treeToString(matcher.getNode("preposition"))+") "+missingArgumentTree.toString()+")";  
					missingArgumentTree = AnalysisUtilities.getInstance().readTreeFromString(tmp);
					ppRelativeClause = true;
				}
				
				if(subjectMovement){	//subject
					newparenttree.addChild(newparenttree.objectIndexOf(verbtree), missingArgumentTree);
				}else{ // newparentlabel is VP	
					if(ppRelativeClause) newparenttree.addChild(newparenttree.numChildren(), missingArgumentTree);
					else newparenttree.addChild(newparenttree.objectIndexOf(verbtree)+1, missingArgumentTree);
				}

				
				//create a new tree with punctuation
				Tree newTree = factory.newTreeNode("ROOT", new ArrayList<Tree>());
				newTree.addChild(relclause);
				AnalysisUtilities.getInstance().addPeriodIfNeeded(newTree);

				if(GlobalProperties.getDebug()) System.err.println("extractRelativeClauses: "+ newTree.toString());
				utils.addQuotationMarksIfNeeded(newTree);
				SentenceNode newTreeWithFeatures = input.deepCopy();
				newTreeWithFeatures.setTree(newTree);
				if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromRelativeClause", 1.0);
				utils.addIfNovel(extracted, newTreeWithFeatures);
			}
		}
	}


	

	/**
	 * e.g., In January, John wore his winter coat. -> John wore his winter coat in January.
	 * 
	 * @param input
	 * @return
	 */
	public void moveLeadingPPsAndQuotes(SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		Tree mainvp = null;
		Tree subj = null;

		tregexOpStr = "ROOT < (S|SINV=mainclause < (NP|SBAR=subj !$++ /,/) < VP=mainvp "
			+ " [ < (PP=modifier < NP) " //must be a PP with an NP object
			+ "| < (S=modifier < SBAR|NP <<# VB|VBD|VBP|VBZ) ] ) "; //OR: a quote, which is an S clause with a subject and finite main verb
		//the modifiers to move must be immediately followed by commas
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());

		List<Tree> modifiers = new ArrayList<Tree>();
		while(matcher.find()){
			if(mainvp == null){
				mainvp = matcher.getNode("mainvp").deepCopy();
				subj = matcher.getNode("subj").deepCopy();
			}
			Tree mainclause = matcher.getNode("mainclause");
			Tree modifier = matcher.getNode("modifier").deepCopy();
			int idx = mainclause.objectIndexOf(modifier);
			if(modifiers.contains(modifier)) continue; //just in case the tregex expression catches duplicates
			//add commas and quotation marks if they appeared in the original
			if(idx > 0 && mainclause.getChild(idx-1).label().value().equals("``")){
				modifiers.add(AnalysisUtilities.getInstance().readTreeFromString("(, ,)"));
				modifiers.add(AnalysisUtilities.getInstance().readTreeFromString("(`` ``)"));
				Tree sbar = factory.newTreeNode("SBAR", new ArrayList<Tree>());
				sbar.addChild(modifier);
				modifiers.add(sbar);
				modifiers.add(AnalysisUtilities.getInstance().readTreeFromString("('' '')"));
			}else{
				modifiers.add(modifier);
			}			
		}
		
		if(mainvp != null){ //any matches?
			for(Tree m: modifiers){
				mainvp.addChild(m);
			}
			
			Tree newTree = factory.newTreeNode("ROOT", new ArrayList<Tree>());
			Tree clause = factory.newTreeNode("S", new ArrayList<Tree>());
			newTree.addChild(clause);
			clause.addChild(subj);
			clause.addChild(mainvp);
			
			AnalysisUtilities.getInstance().addPeriodIfNeeded(newTree);
			utils.addQuotationMarksIfNeeded(newTree);
			if(GlobalProperties.getDebug()) System.err.println("moveLeadingModifiers: "+ newTree.toString());
			input.setTree(newTree);
			if(GlobalProperties.getComputeFeatures()) input.setFeatureValue("movedLeadingPPs", 1.0);
		}
	
	}

	
	/**
	 * e.g., John, hoping to get a good grade, studied. -> John hoped to get a good grade.
	 *   Walking to the store, John saw Susan -> John was walking to the store.
	 *   
	 *   NOTE: This method produces false positives for sentences like, 
	 *   			"Broadly speaking, the project was successful."
	 *   		where the participial phrase does not modify the subject.
	 *   
	 * @param extracted
	 * @param input
	 */
	public void extractNounParticipialModifiers(Collection<SentenceNode> extracted, SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;

		tregexOpStr = "ROOT < (S "
			+ " [ << (NP < (NP=subj  $++ (/,/ $+ (VP=modifier <# VBN|VBG|VP=tense )))) " 	//modifiers that appear after nouns
			+ " | < (S !< NP|SBAR < (VP=modifier <# VBN|VBG|VP=tense) $+ (/,/ $+ NP=subj)) " 	//modifiers before the subject. e.g., Founded by John, the company...
			+ " | < (SBAR < (S !< NP|SBAR < (VP=modifier <# VBN|VBG=tense)) $+ (/,/ $+ NP=subj)) " //e.g., While walking to the store, John saw Susan.
			+ " | < (PP=modifier !< NP <# VBG=tense $+ (/,/ $+ NP=subj)) ] ) " // e.g., Walking to the store, John saw Susan.
			+ " <<# /^VB.*$/=maintense ";	//tense determined by top-most verb
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());
		while(matcher.find()){
			Tree nountree = matcher.getNode("subj").deepCopy();
			Tree vptree = matcher.getNode("modifier");
			Tree verb = matcher.getNode("tense");
			utils.makeDeterminerDefinite(nountree);
			
			if(vptree.label().value().equals("PP")) vptree.label().setValue("VP");
			String verbPOS = utils.findTense(matcher.getNode("maintense"));
			if(vptree == null || nountree == null) return;
			
			String newTreeStr;
			if(verb.label().value().equals("VBG")){
				//for present partcipials, change the tense to the tense of the main verb
				//e.g., walking to the store -> walked to the store
				String verbLemma =  AnalysisUtilities.getInstance().getLemma(verb.getChild(0).label().value(), verb.label().value());
				String newVerb = AnalysisUtilities.getInstance().getSurfaceForm(verbLemma, verbPOS); 
				int verbIndex = vptree.objectIndexOf(verb);
				vptree = vptree.deepCopy();
				vptree.removeChild(verbIndex);
				vptree.addChild(verbIndex, AnalysisUtilities.getInstance().readTreeFromString("("+verbPOS+" "+newVerb+")"));
				newTreeStr = "(ROOT (S "+matcher.getNode("subj").toString()+" "+vptree.toString()+" (. .)))";
			}else{ 
				//for past participials, add a copula
				//e.g., John, exhausted, -> John was exhausted
				//(or for conjunctions, just add the copula---kind of a hack to make the moby dick sentence work out)
				String auxiliary;
				if(verbPOS.equals("VBP") || verbPOS.equals("VBD")){
					if(utils.isPlural(nountree)) auxiliary = "(VBD were)";
					else auxiliary = "(VBD was)";
				}else{
					if(utils.isPlural(nountree)) auxiliary = "(VB are)";
					else auxiliary = "(VBZ is)";
				}
			
				newTreeStr = "(ROOT (S "+nountree+" (VP "+auxiliary+" "+vptree+") (. .)))";
			}
			
			Tree newTree = AnalysisUtilities.getInstance().readTreeFromString(newTreeStr);
			utils.correctTense(newTree.getChild(0).getChild(0), newTree.getChild(0));
			utils.addQuotationMarksIfNeeded(newTree);
			
			if(GlobalProperties.getDebug()) System.err.println("extractNounParticipialModifiers: "+ newTree.toString());
			SentenceNode newTreeWithFeatures = input.deepCopy();
			newTreeWithFeatures.setTree(newTree);
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromParticipial", 1.0); //old feature name
			if(GlobalProperties.getComputeFeatures()) newTreeWithFeatures.setFeatureValue("extractedFromNounParticipial", 1.0);
			extracted.add(newTreeWithFeatures);
		}
		
		
	}
	
	
	
	
	/**
	 * 
	 * e.g., John, the painter, knew Susan.  -> John knew Susan.
	 * 
	 * @param q
	 * @return whether or not a change was made
	 */
	public boolean removeAppositives(SentenceNode q) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;

		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "NP=parent < (NP=child $++ (/,/ $++ NP|PP=appositive) !$-- /,/) !< CC|CONJP";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(q.getTree()).find()){
		
			ps.add(Tsurgeon.parseOperation("move child $+ parent"));
			ps.add(Tsurgeon.parseOperation("prune parent"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
	
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			utils.addQuotationMarksIfNeeded(q.getTree());
	
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedAppositives", 1.0);
			return true;
		}else{
			return false;
		}
	}
	
	
	
	/**
	 * 
	 * e.g., John studied, hoping to get a good grade. -> John studied.
	 * 
	 * @param input
	 * @return whether or not a change was made
	 */
	public  boolean removeVerbalModifiersAfterCommas(SentenceNode q) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops;
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;

		ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

		tregexOpStr = "ROOT=root << (VP !< VP < (/,/=comma $+ /[^`].*/=modifier))";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		
		//remove modifiers
		ps = new ArrayList<TsurgeonPattern>();
		if(matchPattern.matcher(q.getTree()).find()){
			ps.add(Tsurgeon.parseOperation("prune modifier"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
	
			//now remove the comma
			ops.clear();
			ps.clear();
			tregexOpStr = "ROOT=root << (VP !< VP < /,/=comma)";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			ps.add(Tsurgeon.parseOperation("prune comma"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			utils.addQuotationMarksIfNeeded(q.getTree());

			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedVerbalModifiersAfterCommas", 1.0);
			return true;
		}else{
			return false;
		}
	}
	
	
	
	
	
	/**
	 * 
	 * e.g., However, John did not study. -> John did not study.
	 * 
	 * @param q
	 * @return
	 */
	public boolean removeClauseLevelModifiers(SentenceNode q) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops;
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;

		boolean modified = false;
		
		//remove subordinate clauses and various phrases
		//leave conditional antecedents (i.e., with "if" or "unless" as complementizers.  punt on "even if")
		tregexOpStr = "ROOT=root < (S=mainclause < (/SBAR|ADVP|ADJP|CC|PP|S|NP/=fronted !< (IN < if|unless) !$ ``  $++ NP=subject))";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		TregexMatcher matcher = matchPattern.matcher(q.getTree());
		if(matcher.find()){
			ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();		
			ps = new ArrayList<TsurgeonPattern>();
			tregexOpStr = "ROOT=root < (S=mainclause < (/[,:]/=comma $ (/SBAR|ADVP|ADJP|CC|PP|S|NP/=fronted !< (IN < if|unless) $++ NP=subject)))";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			ps.add(Tsurgeon.parseOperation("prune comma"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());

			ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();		
			ps = new ArrayList<TsurgeonPattern>();
			tregexOpStr = "ROOT=root < (S=mainclause < (/SBAR|ADVP|ADJP|CC|PP|S|NP/=fronted !< (IN < if|unless) $++ NP=subject))";
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			ps.add(Tsurgeon.parseOperation("prune fronted"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());

			utils.addQuotationMarksIfNeeded(q.getTree());
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedClauseLevelModifiers", 1.0);
			modified = true;
		}
		
		return modified;
	}
	
	
	
	/**
	 * e.g., John, who hoped to get a good grade, studied. -> John studied.
	 *   
	 */
	public boolean removeNonRestrRelClausesAndParticipials(SentenceNode q) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;
		
		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "NP < (VP|SBAR=mod $- /,/=punc !$+ /,/ !$ CC|CONJP)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		boolean modified = false;
		if(matchPattern.matcher(q.getTree()).find()){
			ps.add(Tsurgeon.parseOperation("prune punc"));
			ps.add(Tsurgeon.parseOperation("prune mod"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedNonRestrRelClausesAndParticipials", 1.0);
			modified = true;
		}
		
		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "NP < (VP|SBAR=mod $- /,/=punc $+ /,/=punc2 !$ CC|CONJP)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(q.getTree()).find()){
			ps.add(Tsurgeon.parseOperation("prune punc"));
			ps.add(Tsurgeon.parseOperation("prune mod"));
			ps.add(Tsurgeon.parseOperation("prune punc2"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedNonRestrRelClausesAndParticipials", 1.0);
			modified = true;
		}
		
		return modified;	
	}
	
	
	
	
	/**
	 * 
	 * e.g., John Smith (1931-1992) was a fireman. -> John Smith was a Fireman.
	 * 
	 * @return whether or not a change was made
	 */
	public boolean removeParentheticals(SentenceNode q) {
		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		List<TsurgeonPattern> ps;
		boolean res = false;

		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "__=parenthetical [ $- /-LRB-/=leadingpunc $+ /-RRB-/=trailingpunc " +
				" | $+ /,/=leadingpunc $- /,/=trailingpunc !$ CC|CONJP "+
				" | $+ (/:/=leadingpunc < --) $- (/:/=trailingpunc < /--/) ]";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(q.getTree()).find()){
			ps.add(Tsurgeon.parseOperation("prune leadingpunc"));
			ps.add(Tsurgeon.parseOperation("prune parenthetical"));
			ps.add(Tsurgeon.parseOperation("prune trailingpunc"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			
			if(res)	utils.addQuotationMarksIfNeeded(q.getTree());
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedParentheticals", 1.0);
			res = true;
		}
		
		ps = new ArrayList<TsurgeonPattern>();
		tregexOpStr = "PRN=parenthetical";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		if(matchPattern.matcher(q.getTree()).find()){
			ps.add(Tsurgeon.parseOperation("prune parenthetical"));
			p = Tsurgeon.collectOperations(ps);
			ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
			Tsurgeon.processPatternsOnTree(ops, q.getTree());
			if(GlobalProperties.getComputeFeatures()) q.setFeatureValue("removedParentheticals", 1.0);
			res = true;
		}
		
		return res;
	}
	
	
	
	
}
