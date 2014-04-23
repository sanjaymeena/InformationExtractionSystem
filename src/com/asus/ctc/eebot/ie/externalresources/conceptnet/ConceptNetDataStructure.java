package com.asus.ctc.eebot.ie.externalresources.conceptnet;

import java.io.Serializable;
import java.util.List;

import com.asus.ctc.eebot.ie.externalresources.conceptnet.model.ConceptNetEdge;

/**
 * @author Sanjay_Meena
 */
public class ConceptNetDataStructure implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6943995331451383830L;
	Long numFound;
	List<ConceptNetEdge> edges;
	String maxScore;
	String conceptDescription;

	/**
	 * @return
	 */
	public String getConceptDescription() {
		return conceptDescription;
	}

	/**
	 * @param conceptDescription
	 */
	public void setConceptDescription(String conceptDescription) {
		this.conceptDescription = conceptDescription;
	}

	/**
	 * @return the maxScore
	 */
	public String getMaxScore() {
		return maxScore;
	}

	/**
	 * @param maxScore
	 *            the maxScore to set
	 */
	public void setMaxScore(String maxScore) {
		this.maxScore = maxScore;
	}

	/**
	 * @return the edges
	 */
	public List<ConceptNetEdge> getEdges() {
		return edges;
	}

	/**
	 * @param edges
	 *            the edges to set
	 */
	public void setEdges(List<ConceptNetEdge> edges) {
		this.edges = edges;
	}

	/**
	 * @return the numFound
	 */
	public Long getNumFound() {
		return numFound;
	}

	/**
	 * @param numFound
	 *            the numFound to set
	 */
	public void setNumFound(Long numFound) {
		this.numFound = numFound;
	}

}
