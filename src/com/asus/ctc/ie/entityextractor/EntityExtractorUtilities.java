package com.asus.ctc.ie.entityextractor;

import java.util.ArrayList;
import java.util.List;


import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.utilities.AnalysisUtilities;
import com.asus.ctc.ie.utilities.EssentialEnums.NERNounTags;
import com.asus.ctc.ie.utilities.TregexPatternFactory;


import javatools.parsers.NounGroup;




import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexParseException;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

public class EntityExtractorUtilities {
	private static EntityExtractorUtilities instance;

	/**
	 * Return instance of this class
	 * 
	 * @return AnalysisUtilities
	 */
	public static EntityExtractorUtilities getInstance() {

		if (instance == null) {
			instance = new EntityExtractorUtilities();
		}
		return instance;
	}

	/**
	 * Get the Head word for a NP Phrase. E.g. African Animal Area : HeadWord:
	 * Area.
	 * 
	 * @param word
	 * @param parse
	 * @return
	 */
	public String NPTree(String word, Tree parse) {
		String head = null;

		String tregex1;
		TregexPattern matchPattern1;
		TregexMatcher matcher1;

		if (word.endsWith(".")) {
			word = word.substring(0, word.length() - 1);
		}
		tregex1 = "NP=parent < (NN|NNP|NNS|NX|NNPS=word < " + word + ")";
		matchPattern1 = TregexPatternFactory.getPattern(tregex1);

		try {
			matcher1 = matchPattern1.matcher(parse);

			if (matcher1.find()) {

				Tree parent = matcher1.getNode("parent");
				parent = checkConjunctionPatternCases(parent, word);

				String p = AnalysisUtilities.getInstance().treeToString(parent);
				NounGroup ng = new NounGroup(p);
				head = ng.head();
			}
		} catch (TregexParseException tpe) {
			tpe.printStackTrace();
		}
		return head;

	}

	/**
	 * Check for conjunction case for Concept Extraction ; Jack and Jill went up
	 * the hill. In Conjunction, we want each entity, and not the Whole noun
	 * phrase.
	 * 
	 * @param parentCopy
	 * @param word
	 * @return Tree
	 */
	public Tree checkConjunctionPatternCases(Tree parentCopy, String word) {

		String tregex2 = "NP < ((NN|NNP|NNS|NNPS=word < " + word
				+ " ) $+ (CONJP|CC) | $- (CONJP|CC ) | $+ /,/ | $- /,/)";
		TregexPattern matchPattern2;
		TregexMatcher matcher2;
		matchPattern2 = TregexPatternFactory.getPattern(tregex2);
		matcher2 = matchPattern2.matcher(parentCopy);

		/**
		 * Need to create a new phrase tree manually
		 */
		if (matcher2.find()) {
			Tree tempTree = matcher2.getNode("word");

			if (tempTree != null) {
				String t = tempTree.toString();
				String stringTree = "(NP " + t + ")";
				Tree newTree1 = AnalysisUtilities.getInstance()
						.readTreeFromString(stringTree);
				parentCopy = newTree1.deepCopy();
				// concept = newTree;
			}

		}

		return parentCopy;
	}

	/**
	 * Function to get the last index of the extracted Concept in the
	 * EmailSentence Tree. E.g. Asian Tropical Rainforest Area, Desert Animal
	 * Area, Australian Animal Area and African Animal Area are part of the
	 * Taiwan Zoo. C1: Asian Tropical Rainforest Area index=3 for last String
	 * Area
	 * 
	 * @param concept
	 * @param sentenceTagInfo
	 * @param currIndex
	 * @return int
	 */
	public int getLastIndexOfConcept(Tree concept, SentenceNode sentenceTagInfo) {
		Tree sentenceTree = sentenceTagInfo.getTree();
		// List<SuperSenseWord> superstanfordtree =
		// sentenceTagInfo.stanfordSupersenseTaggedSentence;
		int newIndex = 0;

		if (concept != null) {
			List<Tree> conceptLeaves = concept.getLeaves();
			List<Tree> parentLeaves = sentenceTree.getLeaves();

			int prevMatch = 0;

			for (int j = 0; j < conceptLeaves.size(); j++) {
				String conceptWord = AnalysisUtilities.getInstance().treeToString(conceptLeaves.get(j));
				for (int i = prevMatch; i < parentLeaves.size(); i++) {
					String sentWord = AnalysisUtilities.getInstance().treeToString(parentLeaves.get(i));

					if (conceptWord.equals(sentWord) && prevMatch <= i) {
						newIndex = i;
						prevMatch = i;
						break;
					}
				}

			}

		}
		return newIndex;
	}

	/**
	 * Given a tree, prune it.
	 * 
	 * @param parent
	 */
	public void genericPrune(Tree parent) {

		String tregex1;
		//String tregex2;

		TregexPattern matchPattern1;
		TregexMatcher matcher1;

		TsurgeonPattern p;
		List<TsurgeonPattern> ps;
		ArrayList<Pair<TregexPattern, TsurgeonPattern>> ops;

		tregex1 = "NP=parent < DT=det ";
		matchPattern1 = TregexPatternFactory.getPattern(tregex1);
		matcher1 = matchPattern1.matcher(parent);

		ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		ps = new ArrayList<TsurgeonPattern>();

		boolean detAdd = false;
		boolean prepAdd = false;

		if (matcher1.find()) {

			if (matcher1.getNode("det") != null && !detAdd) {
				detAdd = true;
				ps.add(Tsurgeon.parseOperation("prune det"));
			}

		}

		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern1, p));
		try {
			Tsurgeon.processPatternsOnTree(ops, parent);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		tregex1 = "NP=parent  </^PRP/=prep";
		matchPattern1 = TregexPatternFactory.getPattern(tregex1);
		matcher1 = matchPattern1.matcher(parent);
		ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		ps = new ArrayList<TsurgeonPattern>();
		if (matcher1.find()) {
			if (matcher1.getNode("prep") != null && !prepAdd) {
				prepAdd = true;
				ps.add(Tsurgeon.parseOperation("prune prep"));
			}

		}
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern, TsurgeonPattern>(matchPattern1, p));
		try {
			Tsurgeon.processPatternsOnTree(ops, parent);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
    /**
     * Given a String match the NER type
     * 
     * @param wordPhrase
     * @return NERNounTags
     */
    public NERNounTags mapNERTagType(String wordPhrase) {

        NERNounTags nt;

        if (wordPhrase.contains("noun.location")) {
            nt = NERNounTags.location;
        }

        else if (wordPhrase.contains("noun.person")||wordPhrase.contains("PERSON")) {
            nt = NERNounTags.person;
        }

        else if (wordPhrase.contains("noun.group")) {
            nt = NERNounTags.group;
        }

        else if (wordPhrase.contains("noun.act")) {
            nt = NERNounTags.act;
        }

        else if (wordPhrase.contains("noun.object")) {
            nt = NERNounTags.object;
        }

        else if (wordPhrase.contains("noun.process")) {
            nt = NERNounTags.process;
        }

        else if (wordPhrase.contains("noun.event")) {
            nt = NERNounTags.event;
        }

        else if (wordPhrase.contains("noun.communication")) {
            nt = NERNounTags.communication;
        }

        else if (wordPhrase.contains("noun.animal")) {
            nt = NERNounTags.animal;
        }

        else if (wordPhrase.contains("noun.substance")) {
            nt = NERNounTags.substance;
        } else if (wordPhrase.contains("noun.artifact")) {
            nt = NERNounTags.artifact;
        } else if (wordPhrase.contains("noun.body")) {
            nt = NERNounTags.body;
        }

        else if (wordPhrase.contains("noun.food")) {
            nt = NERNounTags.food;
        } else if (wordPhrase.contains("noun.other")) {
            nt = NERNounTags.other;
        }

        else if (wordPhrase.contains("noun.cognition")) {
            nt = NERNounTags.cognition;
        }

        else if (wordPhrase.contains("noun.Tops")) {
            nt = NERNounTags.Tops;
        }

        else if (wordPhrase.contains("noun.attribute")) {
            nt = NERNounTags.attribute;
        }

        else if (wordPhrase.contains("noun.motive")) {
            nt = NERNounTags.motive;
        }

        else if (wordPhrase.contains("noun.phenomenon")) {
            nt = NERNounTags.phenomenon;
        } else if (wordPhrase.contains("noun.possession")) {
            nt = NERNounTags.possession;
        }

        else if (wordPhrase.contains("noun.plant")) {
            nt = NERNounTags.plant;
        }

        else if (wordPhrase.contains("noun.relation")) {
            nt = NERNounTags.relation;
        }

        else if (wordPhrase.contains("noun.shape")) {
            nt = NERNounTags.shape;
        }

        else if (wordPhrase.contains("noun.state")) {
            nt = NERNounTags.state;
        } else if (wordPhrase.contains("noun.feeling")) {
            nt = NERNounTags.feeling;
        } else if (wordPhrase.contains("noun.quantity")) {
            nt = NERNounTags.quantity;
        }
        else if (wordPhrase.contains("noun.time")) {
            nt = NERNounTags.time;
        }
        else {
            nt = null;
        }

        return nt;
    }

}
