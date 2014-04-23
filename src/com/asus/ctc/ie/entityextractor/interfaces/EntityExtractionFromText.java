/**
 * 
 */
package com.asus.ctc.ie.entityextractor.interfaces;

import com.asus.ctc.ie.datastructures.TextNode;

/**
 * Interface to extract entites from text;
 * 
 * @author Sanjay_Meena
 * 
 */
public interface EntityExtractionFromText {
    public void extractEntitiesFromText(TextNode textNode);
    public void extractEntitiesFromText(String document);
}
