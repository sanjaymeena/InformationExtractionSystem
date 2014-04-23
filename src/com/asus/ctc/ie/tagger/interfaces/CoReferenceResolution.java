package com.asus.ctc.ie.tagger.interfaces;

import java.util.List;

import com.asus.ctc.ie.datastructures.SentenceNode;
import com.asus.ctc.ie.datastructures.TextNode;

import edu.stanford.nlp.trees.Tree;

public interface CoReferenceResolution {

    public void resolveCoreferences(TextNode textNode) ;
    public void resolveCoreference(List<Tree> sentences);
    public List<SentenceNode> clarifyNounPhrases(List<SentenceNode> treeSet,
	    boolean clarifyPronouns, boolean clarifyNonPronouns);
}
