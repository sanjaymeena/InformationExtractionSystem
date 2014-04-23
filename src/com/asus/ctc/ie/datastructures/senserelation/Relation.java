package com.asus.ctc.ie.datastructures.senserelation;

import com.asus.ctc.ie.datastructures.interfaces.RelationExtraction;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Sanjay_Meena
 */
public class Relation implements RelationExtraction{
    String predicate;
    String object;
    String subject;
    Tree predicateTree;

    /**
     * 
     */
    public Relation() {
        this.predicate = "";
        this.object = "";
        this.subject = "";

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    @Override
    public String toString() {
        String print = "";
        if (!predicate.equals("") | !subject.equals("") | !object.equals("")) {

            if (!predicate.equals("")) {
                print += predicate.trim();
            }
            print += "(";
            if (!subject.equals("")) {
                print += subject.trim();
            }
            print += ",";
            if (!object.equals("")) {
                print += object.trim();
            }
            print += ")";

        }
        return print;
    }

    /**
     * @return the predicatePhrase
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * @param predicatePhrase
     *            the predicatePhrase to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * @param object
     *            the object to set
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the predicateTree
     */
    public Tree getPredicateTree() {
        return predicateTree;
    }

    /**
     * @param predicateTree the predicateTree to set
     */
    public void setPredicateTree(Tree predicateTree) {
        this.predicateTree = predicateTree;
    }

}
