/**
 * 
 */
package com.asus.ctc.ie.nlptransformations;


import com.asus.ctc.ie.config.GlobalProperties;
import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.utilities.TregexPatternFactory;


import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/**
 * 
 * Class which provides various checks before NLP transformations happens. 
 * E.g. Check whether to break conjunction or not. 
 * 
 * @author Sanjay_Meena
 *
 */
public class TransformationsChecks {

	
	
	/**
	 * This is a simple hack to avoid bad output for a few special cases.
	 * Specifically, we want to avoid extracting 
	 * from phrases with "according" and "including",
	 * which syntactically look like participial phrases.
	 *    
	 */
	public boolean mainVerbOK(SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		
		//avoid extracting sentences from "...according to X..."
		tregexOpStr = "ROOT <+(VP|S) (/VB.*/ < /(accord.*|includ.*)/)";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());		
		boolean res = !matcher.find();
		
		
		return res;
	}
	
	/**
	 * A simple hack to avoid bad output due to syntactic parsing errors.
	 * We check for various rare nonterminal labels
	 * to skip over parses that are likely to be bad.
	 * 
	 */
	public boolean uglyParse(Tree t) {
		if(TregexPatternFactory.getPattern("UCP|FRAG|X|NAC").matcher(t).find()){
			if(GlobalProperties.getDebug()) System.err.println("Ugly parse");
		}
		return false;
	}
	
	
	
	public boolean hasBreakableConjunction(SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		//Consider some exceptions which should be added in the results
		if(hasConjoinedNpsExceptions(input)) {
			return false;
		}

		//conjoined VPs, clauses, etc.
		tregexOpStr = "CONJP|CC !< either|or|neither|nor > S|SBAR|VP"
			+ " [ $ SBAR|S | !>> SBAR ] "; //we can break conjoined SBARs, but not anything else under an SBAR node
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());		
		if(matcher.find()){
			return true;
		}
		
		//clauses conjoined by semi-colons
		tregexOpStr = " S < (S=child $ (/:/ < /;/) !$++ (/:/ < /;/) ) ";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());		
		if(matcher.find()){
			return true;
		}
		
		if(NLPTransformations.breakNPs){ 
			tregexOpStr = "CONJP|CC !< either|or|neither|nor > NP !>> SBAR "
				+ " !> (NP < (/^(N.*|SBAR|PRP)$/ !$ /^(N.*|SBAR|PRP)$/))";
			//the latter part is to address special cases of flat NPs in treebank:
			//we allow NPs like "(NP (JJ eastern) (CC and) (JJ western) (NNS coasts))" 
			//because we can't easily split them
			
			matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
			matcher = matchPattern.matcher(input.getTree());		
			if(matcher.find()){
				return true;
			}
		}
		
		return false;
	}
	

	
	/**
	 * Returns whether the input sentence has a subject and a finite main verb.
	 * If it does not, then we do not want to add it to the output. 
	 * 
	 * @param input
	 * @return
	 */
	public boolean hasSubjectAndFiniteMainVerb(SentenceNode input) {
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		tregexOpStr = "ROOT " + //main clause dominates...
				" <+(S) NP|SBAR  <+(VP|S) VB|VBD|VBP|VBZ  !<+(VP) TO"; //AND also dominates a finite, non-participle verb
				//allowing VBN would allow participial phrases like "founded by Bill Gates"
		
		//" [ < /^(PRP|N.*|SBAR|PP)$/ " + //either PRP for pronoun, N for NP|NN|NNS...
		//" | < (S < (VP < TO|VBG)) ] " + // or a non-finite verb phrase (e.g., "walking")
		
		
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());		
		boolean res = matcher.find();
		return res;
	}
	
	
	
	
	/**
	 * Sentences from which conjunctions should not be extracted.
	 */
	private boolean hasConjoinedNpsExceptions(SentenceNode input) {
		
		String tregexOpStr;
		TregexPattern matchPattern;
		TregexMatcher matcher;

		
		tregexOpStr = "NP=parent < (CONJP|CC !< or|nor [ "
			+ " $+ /^(N.*|PRP|SBAR)$/=child $-- /^(N.*|PRP|SBAR)$/ | " //there must be a noun on each side of the conjunction
			+ " $-- /^(N.*|PRP|SBAR)$/=child $+ /^(N.*|PRP|SBAR)$/ ] ) " //this avoids extracting from flat NPs such as "the smaller and darker form"
			+ " !>> (/.*/ $ (CC|CONJP !< or|nor)) "  //this cannot be nested within a larger conjunction or followed by a conjunction (we recur later to catch this) 
			+ " !$ (CC|CONJP !< or|nor)" 
			+ " !.. (CC|CONJP !< or|nor > NP|PP|S|SBAR|VP) !>> SBAR " +
			"   $+ (ADVP < RB $+ VP)  | $- (/VB.*/ >> (VP $- (NP < CD)))" 
			+"  | >> (PP $- (NP < CD))";
			//+ " >> (ROOT !< (S <+(VP) (/^VB.*$/ < are|were|be|seem|appear))) " ; //don't break plural predicatePhrase nominatives (e.g., "John and Mary are two of my best friends.")
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(input.getTree());
		
		
		if(matcher.find()){
			return true;
		}
		return false;
	}
	
	
	

}
