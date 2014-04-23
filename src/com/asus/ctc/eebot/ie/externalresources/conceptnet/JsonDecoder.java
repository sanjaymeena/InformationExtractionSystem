/**
 * 
 */
package com.asus.ctc.eebot.ie.externalresources.conceptnet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.asus.ctc.eebot.ie.externalresources.conceptnet.EssentialEnums.conceptNetApiVars;
import com.asus.ctc.eebot.ie.externalresources.conceptnet.EssentialEnums.conceptNetEdgeVars;
import com.asus.ctc.eebot.ie.externalresources.conceptnet.EssentialEnums.conceptNetRelations;
import com.asus.ctc.eebot.ie.externalresources.conceptnet.model.ConceptNetEdge;



/**
 * @author Sanjay_Meena
 */
public class JsonDecoder {

	HashMap<String, conceptNetEdgeVars> conceptVarEdgeMap;
	HashMap<String, conceptNetApiVars> conceptVarMaps;
	HashMap<String, List<String>> conceptnetRelationMap;
	JSONParser parser;

	/**
     * 
     */
	public JsonDecoder() {

		parser = new JSONParser();

		conceptVarEdgeMap = new HashMap<String, conceptNetEdgeVars>();
		conceptVarMaps = new HashMap<String, conceptNetApiVars>();

		initializeConceptNetVarMaps();
		initializeconceptnetRelationMap();
	}

	private void initializeconceptnetRelationMap() {
		// TODO Auto-generated method stub
		conceptnetRelationMap = new HashMap<String, List<String>>();

		List<String> ISASentenceList = new LinkedList<String>();
		List<String> UsedForSentenceList = new LinkedList<String>();
		List<String> HasASentenceList = new LinkedList<String>();
		List<String> CapableOfSentenceList = new LinkedList<String>();
		List<String> DesiresSentenceList = new LinkedList<String>();
		List<String> CreatedBySentenceList = new LinkedList<String>();
		List<String> PartOfSentenceList = new LinkedList<String>();
		List<String> CausesSentenceList = new LinkedList<String>();
		List<String> HasFirstSubeventSentenceList = new LinkedList<String>();
		List<String> AtLocationSentenceList = new LinkedList<String>();
		List<String> HasPropertySentenceList = new LinkedList<String>();
		List<String> LocatedNearSentenceList = new LinkedList<String>();
		List<String> DefinedAsSentenceList = new LinkedList<String>();
		List<String> SymbolOfSentenceList = new LinkedList<String>();
		List<String> ReceivesActionSentenceList = new LinkedList<String>();
		List<String> MotivatedByGoalSentenceList = new LinkedList<String>();
		List<String> CausesDesireSentenceList = new LinkedList<String>();
		List<String> MadeOfSentenceList = new LinkedList<String>();
		List<String> HasSubeventSentenceList = new LinkedList<String>();
		List<String> HasPrerequisiteSentenceList = new LinkedList<String>();
		List<String> HasLastSubeventSentenceList = new LinkedList<String>();
		List<String> NotIsASentenceList = new LinkedList<String>();
		List<String> DerivedFromSentenceList = new LinkedList<String>();
		List<String> RelatedToSentenceList = new LinkedList<String>();
		List<String> MemberOfSentenceList = new LinkedList<String>();
		List<String> ConceptuallyRelatedToSentenceList = new LinkedList<String>();
		List<String> TranslationOfSentenceList = new LinkedList<String>();
		List<String> HasContextSentenceList = new LinkedList<String>();

		ISASentenceList.add("is a");

		UsedForSentenceList.add("is used for");
		HasASentenceList.add("has");

		CapableOfSentenceList.add("is capable of");
		DesiresSentenceList.add("want to");
		CreatedBySentenceList.add("is created by");
		PartOfSentenceList.add("is part of");
		CausesSentenceList.add("causes");

		AtLocationSentenceList.add("can be in");
		HasPropertySentenceList.add("is");
		LocatedNearSentenceList.add("is located near");
		DefinedAsSentenceList.add("is defined as");
		SymbolOfSentenceList.add("is defined as");
		ReceivesActionSentenceList.add("can be");

		MadeOfSentenceList.add("is made of");
		NotIsASentenceList.add("is not");

		DerivedFromSentenceList.add("is derived from");
		RelatedToSentenceList.add("is related to");

		MemberOfSentenceList.add("is member of");
		ConceptuallyRelatedToSentenceList.add("is conceptually related to");
		TranslationOfSentenceList.add("is translation of");
		
		HasContextSentenceList.add("can have context");
		
		
		
		conceptnetRelationMap.put(conceptNetRelations.IsA.name(),
				ISASentenceList);
		conceptnetRelationMap.put(conceptNetRelations.UsedFor.name(),
				UsedForSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasA.name(),
				HasASentenceList);
		conceptnetRelationMap.put(conceptNetRelations.CapableOf.name(),
				CapableOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.Desires.name(),
				DesiresSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.CreatedBy.name(),
				CreatedBySentenceList);
		conceptnetRelationMap.put(conceptNetRelations.PartOf.name(),
				PartOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.Causes.name(),
				CausesSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasFirstSubevent.name(),
				HasFirstSubeventSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.AtLocation.name(),
				AtLocationSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasProperty.name(),
				HasPropertySentenceList);
		conceptnetRelationMap.put(conceptNetRelations.LocatedNear.name(),
				LocatedNearSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.DefinedAs.name(),
				DefinedAsSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.SymbolOf.name(),
				SymbolOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.ReceivesAction.name(),
				ReceivesActionSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasPrerequisite.name(),
				HasPrerequisiteSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.MotivatedByGoal.name(),
				MotivatedByGoalSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.CausesDesire.name(),
				CausesDesireSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.MadeOf.name(),
				MadeOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasSubevent.name(),
				HasSubeventSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasLastSubevent.name(),
				HasLastSubeventSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.NotIsA.name(),
				NotIsASentenceList);
		conceptnetRelationMap.put(conceptNetRelations.DerivedFrom.name(),
				DerivedFromSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.RelatedTo.name(),
				RelatedToSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.MemberOf.name(),
				MemberOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.TranslationOf.name(),
				TranslationOfSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.ConceptuallyRelatedTo.name(),
				ConceptuallyRelatedToSentenceList);
		conceptnetRelationMap.put(conceptNetRelations.HasContext.name(),
				HasContextSentenceList);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param jsonString
	 * @return
	 */
	public ConceptNetDataStructure decodeJsonParametersforConceptNetResult(
			String jsonString) {

		
		
		Object jsonMap = null;
		try {
			jsonMap = parser.parse(jsonString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map resultMap = new LinkedHashMap();
		resultMap = (Map) jsonMap;

		/**
		 * Total results;
		 */
		long numFound = (Long) resultMap.get(conceptNetApiVars.numFound.name());

		
	
		
		/**
		 * Lets create conceptNet datastructure
		 */
		ConceptNetDataStructure cds = new ConceptNetDataStructure();
		
		
		
		/**
		 * Let's deal with edges now
		 */
		if (numFound > 0) {
			Object edgeObj = resultMap.get(conceptNetApiVars.edges.name());
			cds = createConceptNetEdges(edgeObj);
		}

		
		
		cds.setNumFound(numFound);
		
		return cds;

	}

	/**
	 * This code extracts the edges from json;
	 * 
	 * @param edges
	 * @return
	 */
	private ConceptNetDataStructure createConceptNetEdges(Object edges) {
		ConceptNetDataStructure cds = new ConceptNetDataStructure();
		List<ConceptNetEdge> edgeList = new LinkedList<ConceptNetEdge>();
		String conceptDescription="";
		
		
		JSONArray edgeJsonArray = (JSONArray) edges;

		
		
		for (int i = 0; i < edgeJsonArray.size(); i++) {
			JSONObject jobj = (JSONObject) edgeJsonArray.get(i);

			String start = (String) jobj.get("start");
			String rel = (String) jobj.get("rel");
			String end = (String) jobj.get("end");

			if (start.contains("/c/en/")) {
				start = start.replace("/c/en/", "");
			}
			if (end.contains("/c/en/")) {
				end = end.replace("/c/en/", "");
			}
			if (rel.contains("/r/")) {
				rel = rel.replace("/r/", "");
			}

			ConceptNetEdge edge = new ConceptNetEdge();
			edge.setStart(start);
			edge.setRel(rel);
			edge.setEnd(end);

			createEdgeNLG(edge);

			edgeList.add(edge);
			conceptDescription+=edge.getNlg()+". ";
			// System.out.println(start + " " + rel + " "+ end);

		}
		
		cds.setEdges(edgeList);
		cds.setConceptDescription(conceptDescription);
		return cds;

	}

	private void createEdgeNLG(ConceptNetEdge edge) {
		String relation = "";
		List<String> relationlist = conceptnetRelationMap.get(edge.getRel());
		if (relationlist != null && relationlist.size() > 0) {
			relation = conceptnetRelationMap.get(edge.getRel()).get(0);
		} else {
			relation = edge.getRel();
		}

		/**
		 * for start and end lemmas. let's strip of the /n part
		 */

		String start = edge.getStart();
		String end = edge.getEnd();
		
		
		int idx=start.indexOf("/n");
		if(idx>0){
			start=start.substring(0,idx);
		}
		
		
		idx=end.indexOf("/n");
		
		if (idx>0) {
			end = end.substring(0,idx);
		}

		start=start.replaceAll("_", " ");
		end=end.replaceAll("_", " ");
		
		String nlg = "";

		nlg = start + " " + relation + " " + end;
		edge.setNlg(nlg);
	}

	void initializeConceptNetVarMaps() {
		initializeConceptNetVarMaps1();
		initializeConceptNetVarMaps2();
	}

	private void initializeConceptNetVarMaps2() {

		conceptVarMaps.put("edges", conceptNetApiVars.edges);
		conceptVarMaps.put("maxScore", conceptNetApiVars.maxScore);
		conceptVarMaps.put("numFound", conceptNetApiVars.numFound);

	}

	private void initializeConceptNetVarMaps1() {

		conceptVarEdgeMap.put("endLemmas", conceptNetEdgeVars.endLemmas);
		conceptVarEdgeMap.put("rel", conceptNetEdgeVars.rel);
		conceptVarEdgeMap.put("end", conceptNetEdgeVars.end);
		conceptVarEdgeMap.put("features", conceptNetEdgeVars.features);
		conceptVarEdgeMap.put("license", conceptNetEdgeVars.license);
		conceptVarEdgeMap.put("sources", conceptNetEdgeVars.sources);
		conceptVarEdgeMap.put("startLemmas", conceptNetEdgeVars.startLemmas);
		conceptVarEdgeMap.put("text", conceptNetEdgeVars.text);
		conceptVarEdgeMap.put("uri", conceptNetEdgeVars.uri);
		conceptVarEdgeMap.put("weight", conceptNetEdgeVars.weight);
		conceptVarEdgeMap.put("dataset", conceptNetEdgeVars.dataset);
		conceptVarEdgeMap.put("start", conceptNetEdgeVars.start);
		conceptVarEdgeMap.put("score", conceptNetEdgeVars.score);
		conceptVarEdgeMap.put("context", conceptNetEdgeVars.context);
		conceptVarEdgeMap.put("nodes", conceptNetEdgeVars.nodes);
		conceptVarEdgeMap.put("id", conceptNetEdgeVars.id);
		conceptVarEdgeMap.put("surfaceText", conceptNetEdgeVars.surfaceText);

	}

}
