/**
 * 
 */
package com.asus.ctc.ie.tagger.interfaces;

import java.util.List;

import com.asus.ctc.ie.datastructures.TextNode;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Interface For tagging information to Text
 * 
 * @author Sanjay_Meena
 *
 */
public interface SyntacticParsing {
    public void generateSyntacticParseTrees(TextNode textNode) ;
    public List<Tree> generateSyntacticParseTrees(String input) ;
    public List<List<TypedDependency>> generateStanfordDependencies(String input);
}
