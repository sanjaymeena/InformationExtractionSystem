package com.asus.ctc.eebot.ie.externalresources.conceptnet.model;

/**
 * @author Sanjay_Meena
 */
public class ConceptNetEdge {

    String rel;
    String end;
    String start;
    String features;
    String license;
    String sources;
    String startLemmas;
    String text;
    String uri;
    String weight;
    String dataset;
   
    String score;
    String context;
    String timestamp;
    String nodes;
    String id;

    
    String nlg;
    
    /**
     * 
     */
    public ConceptNetEdge() {

        rel="";
        end="";
        start="";
        nlg="";
    }

    /**
     * @return the rel
     */
    public String getRel() {
        return rel;
    }

    /**
     * @param rel the rel to set
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * @return the features
     */
    public String getFeatures() {
        return features;
    }

    /**
     * @param features the features to set
     */
    public void setFeatures(String features) {
        this.features = features;
    }

    /**
     * @return the license
     */
    public String getLicense() {
        return license;
    }

    /**
     * @param license the license to set
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * @return the sources
     */
    public String getSources() {
        return sources;
    }

    /**
     * @param sources the sources to set
     */
    public void setSources(String sources) {
        this.sources = sources;
    }

    /**
     * @return the startLemmas
     */
    public String getStartLemmas() {
        return startLemmas;
    }

    /**
     * @param startLemmas the startLemmas to set
     */
    public void setStartLemmas(String startLemmas) {
        this.startLemmas = startLemmas;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the weight
     */
    public String getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * @return the dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return the score
     */
    public String getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the nodes
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

  
    @Override
    public String toString() {

      //  String string= start + " " +rel +" " + end;
        return nlg;
    }

    /**
     * @return the nlg
     */
    public String getNlg() {
        return nlg;
    }

    /**
     * @param nlg the nlg to set
     */
    public void setNlg(String nlg) {
        this.nlg = nlg;
    }

}