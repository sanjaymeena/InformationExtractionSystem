package com.asus.ctc.ie.relationextraction;

import java.util.List;

import com.asus.ctc.ie.datastructures.entities.Concept;
import com.asus.ctc.ie.datastructures.senserelation.SuperSenseWord;

;

public class REUtilities {
	static REUtilities instance;

	/**
	 * @return instance of InformationEngineUtilities
	 */
	public static REUtilities getInstance() {
		if (instance == null) {
			instance = new REUtilities();
		}
		return instance;
	}

	/**
	 * Create a string from the SuperSenseWord List.
	 * 
	 * @param list
	 * @return str
	 */
	public String createStringFromStanfordSuperSenseWordList(
			List<SuperSenseWord> list) {
		String str = "";

		for (int i = 0; i < list.size(); i++) {
			SuperSenseWord ssw = list.get(i);
			String token = ssw.getToken().toLowerCase();

			str += token + " ";
		}

		return str;
	}
	public boolean findConceptInSupersenseWordList(Concept concept,
			List<SuperSenseWord> list, int verbIndex, int phraseType) {
		boolean conceptinPhrase = false;
		String SubjectString = "";

		if (phraseType == 1)

		{
			for (int i = 0; i < verbIndex; i++) {
				SuperSenseWord ssw = list.get(i);
				String token = ssw.getToken().toLowerCase();

				SubjectString += token + " ";
			}

			String c = concept.getConcept().toLowerCase();

			if (SubjectString.indexOf(c) >= 0) {
				conceptinPhrase = true;
			}

		}

		else {

			for (int i = verbIndex + 1; i < list.size(); i++) {
				SuperSenseWord ssw = list.get(i);
				String token = ssw.getToken().toLowerCase();

				SubjectString += token + " ";
			}

			String c = concept.getConcept().toLowerCase();

			if (SubjectString.indexOf(c) >= 0) {
				conceptinPhrase = true;
			}

		}

		return conceptinPhrase;

	}
}
