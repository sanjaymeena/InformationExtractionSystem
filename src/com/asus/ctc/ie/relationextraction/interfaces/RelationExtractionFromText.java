/**
 * 
 */
package com.asus.ctc.ie.relationextraction.interfaces;

import com.asus.ctc.ie.datastructures.TextNode;

/**
 * This module can extract relations from text or Text node
 * 
 * @author Sanjay_Meena
 *
 */
public interface RelationExtractionFromText {

 public void extractRelations(TextNode textNode); 
 public void extractRelations(String document);   
}
