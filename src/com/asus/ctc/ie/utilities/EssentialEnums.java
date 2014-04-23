/**
 * 
 */
package com.asus.ctc.ie.utilities;

import java.io.Serializable;

/**
 * @author Sanjay_Meena
 * 
 */
public class EssentialEnums implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8634641583691417939L;

	/**
	 * The Named Entity Recognition (NER) tags
	 */
	public enum NERNounTags {
		/**
         *  
         */
		person(1),
		/**
         * 
         */
		location(2),
		/**
         * 
         */
		animal(3),
		/**
         * 
         */
		object(4),
		/**
         * 
         */
		group(5),
		/**
         * 
         */
		act(6),
		/**
         * 
         */
		event(7),
		/**
         * 
         */
		communication(8),
		/**
         * 
         */
		process(9),
		/**
         * 
         */
		time(10),
		/**
         * 
         */
		artifact(11),
		/**
         * 
         */
		food(12),
		/**
         * 
         */
		/**
         * 
         */
		other(13),

		/**
         * 
         */
		substance(14),
		/**
         * 
         */
		body(15),

		/**
         * 
         */
		cognition(16),
		/**
         * 
         */
		Tops(17),
		/**
         * 
         */
		attribute(18),
		/**
         * 
         */
		motive(19),
		/**
         * 
         */
		phenomenon(20),
		/**
         * 
         */
		plant(21),
		/**
         * 
         */
		possession(22),
		/**
         * 
         */
		relation(23),
		/**
         * 
         */
		shape(24),
		/**
         * 
         */

		state(25),
		/**
         * 
         */
		quantity(26),
		/**
         * 
         */
		feeling(27), 
		
		date(28), duration(29);

		public int id;

		NERNounTags(int id) {
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
	 * The verb tags
	 * 
	 * @author Sanjay_Meena
	 * 
	 */
	public enum NERVerbType {

		/**
         * 
         */
		body(1),
		/**
         * 
         */
		change(2),
		/**
         * 
         */
		cognition(3),
		/**
         * 
         */
		communication(4),
		/**
         * 
         */
		competition(5),
		/**
         * 
         */
		consumption(6),
		/**
         * 
         */
		contact(7),
		/**
         * 
         */
		creation(8),
		/**
         * 
         */
		emotion(9),
		/**
         * 
         */
		motion(10),
		/**
         * 
         */
		perception(11),
		/**
         * 
         */
		possession(12),
		/**
         * 
         */
		social(13),
		/**
         * 
         */
		stative(14),
		/**
         * 
         */
		weather(15);

		private final int id;

		NERVerbType(int id) {
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
	 * Text Input Types. The most common ones are article, story.
	 */

	public enum textType {
		/**
         * 
         */
		article(1),
		/**
         * 
         */
		story(2),
		/**
         * 
         */
		miscellaneous(3);

		private final int id;

		textType(int id) {
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
	 * Text Input Types. The most common ones are article, story.
	 */

	public enum ApplicationCommands {
		/**
         * 
         */
		add(1),
		/**
         * 
         */
		edit(2),
		/**
         * 
         */
		delete(3),
		/**
         * 
         */
		retrieve(4);

		private final int id;

		ApplicationCommands(int id) {
			this.id = id;
		}

	}

	/**
	 * Text Input Types. The most common ones are article, story.
	 */

	public enum ApplicationType {
		/**
         * 
         */
		InformationExtractionSystem(1),
		/**
         * 
         */
		Drpedia(2),
		/**
         * 
         */
		miscellaneous(3);

		private final int id;

		ApplicationType(int id) {
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
	 * @author Sanjay_Meena
	 * 
	 */
	public class paragraphRange {
		int paragraph_begin;
		int paragraph_end;

		paragraphRange(int begin, int end) {
			paragraph_begin = begin;
			paragraph_end = end;
		}

		paragraphRange() {
			paragraph_begin = -1;
			paragraph_end = -1;
		}
	}

	/**
	 * Text Input Types. The most common ones are article, story.
	 */

	public enum questionType {
		/**
         * 
         */
		who(1),
		/**
         * 
         */
		when(2),
		/**
         * 
         */
		where(3),
		/**
         * 
         */
		what(4),
		/**
         * 
         */
		howmuch(5),
		/**
         * 
         */
		polar(6),
		/**
         * 
         */
		whose(7),
		/**
         * 
         */
		howmany(8),
		/**
         * 
         */
		how(9),
		/**
         * 
         */
		why(10),
		/**
         * 
         */
		conceptDescription(11);

		private final int id;

		questionType(int id) {
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
	 * @author Sanjay_Meena
	 */
	public enum questionLevel {

		/**
		 * This questions are directly created from sentences in the article.
		 * E.g. Taipei is capital of Taiwan. Question: Is Taipei capital of
		 * Taiwan?
		 */
		level_1(1),
		/**
         * 
         */
		level_2(2);

		private final int id;

		questionLevel(int id) {
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
	 * @author Sanjay_Meena
	 */
	public enum fetchQuestionsByKeyword {

		/**
		 * Get questions where any of the keywords are mentioned.
		 */
		questionswithAnyKeyword(1),
		/**
		 * Get questions where all the keywords are mentioned.
		 */
		questionsWithAllKeyword(2);
		private final int id;

		fetchQuestionsByKeyword(int id) {
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
	 * @author Sanjay_Meena
	 */
	public enum queryType {

		/**
		 * Get the query to get all mentioned concepts in the article/story
		 */
		getAllConceptsQuery(1),
		/**
		 * Get questions where all the keywords are mentioned.
		 */
		getQuestionsQuery(2),

		/**
         * 
         */
		getQuestionsOnKeywords(3),

		/**
		 * Get the query to get all mentioned concepts in the article/story
		 */
		getAllConceptsQueryForFacts(4),
		/**
		 * Get the query to get all mentioned concepts in the article/story
		 */
		getQuestionsQueryForFacts(5);
		private final int id;

		queryType(int id) {
			this.id = id;
		}

		/**
		 * @return id
		 */
		public int getValue() {
			return id;
		}
	}

}
