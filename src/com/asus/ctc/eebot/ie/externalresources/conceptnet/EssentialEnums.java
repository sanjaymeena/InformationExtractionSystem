package com.asus.ctc.eebot.ie.externalresources.conceptnet;

import java.io.Serializable;

/**
 * @author Sanjay_Meena
 *
 */
public class EssentialEnums implements Serializable{
    
    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6657572137106444167L;

	/**
     *  type of relations present in ConceptNEt
     */
    
    public enum conceptNetRelations {
       
        IsA(1),
        UsedFor(2),
        HasA(3),
        CapableOf(4),
        Desires(5),
        CreatedBy(6),
        PartOf(7),
        Causes(8),
        HasFirstSubevent(9),
        AtLocation(10),
        HasProperty(11),
        LocatedNear(12),
        DefinedAs(13),
        SymbolOf(14),
        ReceivesAction(15),
        HasPrerequisite(16),
        MotivatedByGoal(17),
        CausesDesire(18),
        MadeOf(19),
        HasSubevent(20),
        HasLastSubevent(21),
        NotIsA(22),
        DerivedFrom(23),
        RelatedTo(24),
        MemberOf(25),
        TranslationOf(26),
        ConceptuallyRelatedTo(27),
        HasContext(28);

        private final int id;

        conceptNetRelations(int id) {
            this.id = id;
        }

        /**
         * @return id
         */
        public int getValue() {
            return id;
        }
    }
    
    
    /**
     *  type of call to make to concept web api
     */
    
    public enum callConceptAPI {
        /**
         * 
         */
        findConceptInfo(1),
        /**
         * 
         */
        findAssociationBetweenTwoConcepts(2);
       

        private final int id;

        callConceptAPI(int id) {
            this.id = id;
        }

        /**
         * @return id
         */
        public int getValue() {
            return id;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    /**
     *  variables present in a json concept query response
     */
    
    public enum conceptNetApiVars {
        /**
         * 
         */
        numFound(1),
        /**
         * 
         */
        edges(2),
        /**
         * 
         */
        maxScore(3);

        private final int id;

        conceptNetApiVars(int id) {
            this.id = id;
        }

        /**
         * @return id
         */
        public int getValue() {
            return id;
        }
    }
    
    /**
     *  variables present in a concept Edge
     */
    
    public enum conceptNetEdgeVars {
      /**
     * 
     */
        endLemmas(17),
        /**
         * 
         */
        rel(1),
        /**
         * 
         */
        end(2),
        /**
         * 
         */
        features(3),
        /**
         * 
         */
        license(4),
        /**
         * 
         */
        sources(5),
        /**
         * 
         */
        startLemmas(6),
        /**
         * 
         */
        text(7),
        /**
         * 
         */
        uri(8),
        /**
         * 
         */
        weight(9),
        /**
         * 
         */
        dataset(10),
        /**
         * 
         */
        start(11),
        /**
         * 
         */
        score(12),
        /**
         * 
         */
        context(13),
        /**
         * 
         */
        timestamp(14),
        /**
         * 
         */
        nodes(15),
        /**
         * 
         */
        id(16),
        /**
         * 
         */
        surfaceText(18);

        private final int idx;

        conceptNetEdgeVars(int idx) {
            this.idx = idx;
        }

        /**
         * @return id
         */
        public int getValue() {
            return idx;
        }
    }
    
}
