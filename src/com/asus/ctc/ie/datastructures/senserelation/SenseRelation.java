package com.asus.ctc.ie.datastructures.senserelation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.interfaces.RelationExtraction;



/**
 * @author Sanjay_Meena
 */
public class SenseRelation implements Serializable,RelationExtraction {

    // For Predicate

    /**
	 * 
	 */
	private static final long serialVersionUID = 8816042242325921176L;

	/**
     * 
     */
    public SenseRelation() {
        // TODO Auto-generated constructor stub
        predicatePhrase = "";
        subjectPhrase = "";
        objectPhrase = "";

        predicateType = "";
        subjectType = "";
        objecttType = "";
        aliases = "";

        verbIndex = -1;

        conceptsInSubject = new ArrayList<Concept>();
        conceptsInObject = new ArrayList<Concept>();

        stanfordSupersenseSentenceInfo = new ArrayList<SuperSenseWord>();

        subjectSenseList = new ArrayList<SuperSenseWord>();
        objectSenseList = new ArrayList<SuperSenseWord>();
        predicateSenseList = new ArrayList<SuperSenseWord>();

    }

    int verbIndex;
    int subjectPhraseIndex;
    int objectPhraseIndex;

    String predicateType;
    String subjectType;
    String objecttType;
    String aliases;

    List<Concept> conceptsInSubject;
    List<Concept> conceptsInObject;

    String subjectPhrase;
    String objectPhrase;
    String predicatePhrase;

    List<SuperSenseWord> stanfordSupersenseSentenceInfo;

    List<SuperSenseWord> subjectSenseList;
    List<SuperSenseWord> objectSenseList;
    List<SuperSenseWord> predicateSenseList;

    /**
     * @param concept
     * @return if concept was found in subject phrase
     */
    public boolean ifConceptInSubject(Concept concept) {
        boolean conceptInsubject = false;

        if (concept != null) {

            for (int i = 0; i < conceptsInSubject.size(); i++) {
                Concept c = conceptsInSubject.get(i);

                if (c.getConcept().trim().equals(concept.getConcept().trim()) && (c.getTag().id == concept.getTag().id)) {
                    conceptInsubject = true;
                }

            }

        }

        return conceptInsubject;

    }

    /**
     * @return the predicateType
     */
    public String getPredicateType() {
        return predicateType;
    }

    /**
     * @param predicateType
     *            the predicateType to set
     */
    public void setPredicateType(String predicateType) {
        this.predicateType = predicateType;
    }

    /**
     * @return the subjectType
     */
    public String getSubjectType() {
        return subjectType;
    }

    /**
     * @param subjectType
     *            the subjectType to set
     */
    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    /**
     * @return the objecttType
     */
    public String getObjecttType() {
        return objecttType;
    }

    /**
     * @param objecttType
     *            the objecttType to set
     */
    public void setObjecttType(String objecttType) {
        this.objecttType = objecttType;
    }

    /**
     * @return the aliases
     */
    public String getAliases() {
        return aliases;
    }

    /**
     * @param aliases
     *            the aliases to set
     */
    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    /**
     * @param conceptsInSubjectPhrase
     *            the conceptsInSubject to set
     */
    public void setConceptsInSubject(List<Concept> conceptsInSubjectPhrase) {
        this.conceptsInSubject = conceptsInSubjectPhrase;
    }

    /**
     * @return the conceptsInObject
     */
    public List<Concept> getConceptsInObject() {
        return conceptsInObject;
    }

    /**
     * @param conceptsInObjectPhrase
     *            the conceptsInObject to set
     */
    public void setConceptsInObject(List<Concept> conceptsInObjectPhrase) {
        this.conceptsInObject = conceptsInObjectPhrase;
    }

    /**
     * @return the conceptsInSubject
     */
    public List<Concept> getConceptsInSubject() {
        return conceptsInSubject;
    }

    /**
     * @return the verbIndex
     */
    public int getVerbIndex() {
        return verbIndex;
    }

    /**
     * @param verbIndex
     *            the verbIndex to set
     */
    public void setVerbIndex(int verbIndex) {
        this.verbIndex = verbIndex;
    }

    /**
     * @return the subjectPhraseIndex
     */
    public int getSubjectPhraseIndex() {
        return subjectPhraseIndex;
    }

    /**
     * @param subjectPhraseIndex
     *            the subjectPhraseIndex to set
     */
    public void setSubjectPhraseIndex(int subjectPhraseIndex) {
        this.subjectPhraseIndex = subjectPhraseIndex;
    }

    /**
     * @return the objectPhraseIndex
     */
    public int getObjectPhraseIndex() {
        return objectPhraseIndex;
    }

    /**
     * @param objectPhraseIndex
     *            the objectPhraseIndex to set
     */
    public void setObjectPhraseIndex(int objectPhraseIndex) {
        this.objectPhraseIndex = objectPhraseIndex;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
       StringBuffer display=new StringBuffer();
        if (!(this.getPredicatePhrase()).isEmpty()) {

		display.append( "\n" + "Relation Extraction: " + "\n");
		display.append( "Subject: " + this.getSubjectPhrase()
				+ "  ##Entities in Subject: "
				+ this.getConceptsInSubject().toString() + "\n");
		display.append( "Predicate: " + this.getPredicatePhrase()
				+ "  ##type: " + this.getPredicateType()
				+ " ##Sense list: "
				+ this.getPredicateSenseList().toString() + "\n");
		display.append( "Object: " + this.getObjectPhrase()
				+ "  ##Entities in Object: "
				+ this.getConceptsInObject().toString() + "\n");
	}
        
        // str+="Subject: "+subjectPhrase +" senseInfo: "+su

        return display.toString();
    }

    /**
     * @return the subjectPhrase
     */
    public String getSubjectPhrase() {
        return subjectPhrase;
    }

    /**
     * @param subjectPhrase
     *            the subjectPhrase to set
     */
    public void setSubjectPhrase(String subjectPhrase) {
        this.subjectPhrase = subjectPhrase;
    }

    /**
     * @return the objectPhrase
     */
    public String getObjectPhrase() {
        return objectPhrase;
    }

    /**
     * @param objectPhrase
     *            the objectPhrase to set
     */
    public void setObjectPhrase(String objectPhrase) {
        this.objectPhrase = objectPhrase;
    }

    /**
     * @return the stanfordSupersenseSentenceInfo
     */
    public List<SuperSenseWord> getStanfordSupersenseSentenceInfo() {
        return stanfordSupersenseSentenceInfo;
    }

    /**
     * @param stanfordSupersenseSentenceInfo
     *            the stanfordSupersenseSentenceInfo to set
     */
    public void setStanfordSupersenseSentenceInfo(List<SuperSenseWord> stanfordSupersenseSentenceInfo) {
        this.stanfordSupersenseSentenceInfo = stanfordSupersenseSentenceInfo;
    }

    /**
     * @return the subjectSenseList
     */
    public List<SuperSenseWord> getSubjectSenseList() {
        return subjectSenseList;
    }

    /**
     * @param subjectSenseList
     *            the subjectSenseList to set
     */
    public void setSubjectSenseList(List<SuperSenseWord> subjectSenseList) {
        this.subjectSenseList = subjectSenseList;
    }

    /**
     * @return the objectSenseList
     */
    public List<SuperSenseWord> getObjectSenseList() {
        return objectSenseList;
    }

    /**
     * @param objectSenseList
     *            the objectSenseList to set
     */
    public void setObjectSenseList(List<SuperSenseWord> objectSenseList) {
        this.objectSenseList = objectSenseList;
    }

    /**
     * @return the predicateSenseList
     */
    public List<SuperSenseWord> getPredicateSenseList() {
        return predicateSenseList;
    }

    /**
     * @param predicateSenseList
     *            the predicateSenseList to set
     */
    public void setPredicateSenseList(List<SuperSenseWord> predicateSenseList) {
        this.predicateSenseList = predicateSenseList;
    }

    /**
     * @return the predicatePhrase
     */
    public String getPredicatePhrase() {
        return predicatePhrase;
    }

    /**
     * @param predicatePhrase
     *            the predicatePhrase to set
     */
    public void setPredicatePhrase(String predicatePhrase) {
        this.predicatePhrase = predicatePhrase;
    }

}
