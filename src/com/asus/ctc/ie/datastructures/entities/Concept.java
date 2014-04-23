package com.asus.ctc.ie.datastructures.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.asus.ctc.eebot.ie.externalresources.conceptnet.ConceptNetDataStructure;
import com.asus.ctc.ie.datastructures.interfaces.Entity;
import com.asus.ctc.ie.utilities.EssentialEnums.NERNounTags;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Sanjay_Meena
 */
public class Concept implements Serializable, Entity{

    /**
	 * 
	 */
	private static final long serialVersionUID = -5869026119914497893L;
	private String concept;
    private Tree concepTree;
    private String concepTreeString;
   
    private List<Integer> sentenceList;
    
    private String modifier;
   
    private NERNounTags tag;
    
    private ConceptNetDataStructure commonSense;
    
    
    
    
    
    /**
 * 
 */
    public Concept() {
        setSentenceList(new ArrayList<Integer>());
        
        setModifier("");
       
    
    }

    /**
     * @return concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * @param concept
     */
    public void setConcept(String concept) {
        this.concept = concept;
    }

    /**
     * @return conceptTree
     */
    public Tree getConcepTree() {
        return concepTree;
    }

    /**
     * @param concepTree
     */
    public void setConcepTree(Tree concepTree) {
        this.concepTree = concepTree;
    }

    

    @Override
    public String toString() {
        String str="";
        if(!(modifier.isEmpty())){
            str+=  modifier+ " ";    
        }
        if(concept!=null && tag!=null) str+=concept ;
        return str;
    }

    /**
     * @return list
     */
    public List<Integer> getSentenceList() {
        return sentenceList;
    }

    /**
     * @param sentenceList
     */
    public void setSentenceList(List<Integer> sentenceList) {
        this.sentenceList = sentenceList;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj != null) {
            Concept c = (Concept) obj;

            if (tag != null && c.tag != null && concepTree != null && c.concepTree != null) {

                if ((this.concept.toLowerCase().equals(c.concept.toLowerCase()))
                    && (this.tag.toString().toLowerCase().equals(c.tag.toString().toLowerCase())) && (this.concepTree.toString().toLowerCase().equals(c.concepTree.toString().toLowerCase()))) {
                    return true;
                }
            } else {
                if (this.concept.trim().toLowerCase().equals(c.concept.trim().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 1;

        if (concepTree != null && tag != null) {
            hash = hash * 31 + concepTree.hashCode();
            hash = hash * 31 + tag.hashCode();
        }

        else {
            hash = hash * 31 + concept.trim().toLowerCase().hashCode();
        }

        return hash;

    }


   

    /**
     * To check if concept is empty or not.
     * @return
     */
    public boolean isEmpty()
    {
        if(concept.equals("") && concept.length()==0)
            return true;
        else return false;
        
    }

   

  

    /**
     * @return the tag
     */
    public NERNounTags getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(NERNounTags tag) {
        this.tag = tag;
    }

    /**
     * @return the concepTreeString
     */
    public String getConcepTreeString() {
        return concepTreeString;
    }

    /**
     * @param concepTreeString the concepTreeString to set
     */
    public void setConcepTreeString(String concepTreeString) {
        this.concepTreeString = concepTreeString;
    }

    /**
     * @return the modifier
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * @param modifier the modifier to set
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

   

    /**
     * @return the commonSense
     */
    public ConceptNetDataStructure getCommonSense() {
        return commonSense;
    }

    /**
     * @param commonSense the commonSense to set
     */
    public void setCommonSense(ConceptNetDataStructure commonSense) {
        this.commonSense = commonSense;
    }

    
}
