package com.asus.ctc.ie.datastructures.senserelation;

import java.io.Serializable;

/**
 * @author Sanjay_Meena
 *
 */
public class SuperSenseWord implements Serializable {
   
            /**
	 * 
	 */
	private static final long serialVersionUID = -5379821275908250546L;
			String token;
            String sstag;
            String posTag;
            String lemma;

            public String getLemma() {
				return lemma;
			}

			public void setLemma(String lemma) {
				this.lemma = lemma;
			}

			SuperSenseWord() {
                token = "";
                sstag = "";
                posTag = "";

            }

            /**
             * @param t
             * @param tag
             * @param pos
             */
            public SuperSenseWord(String token, String tag, String pos) {
                this.token = token;
                sstag = tag;
                posTag = pos;

            }

            public SuperSenseWord(String token, String tag, String pos,
					String stem) {
				// TODO Auto-generated constructor stub
            	this.token = token;
                sstag = tag;
                posTag = pos;
                lemma=stem;
            
            }

			/**
             * @return the token
             */
            public String getToken() {
                return token;
            }

            /**
             * @param token the token to set
             */
            public void setToken(String token) {
                this.token = token;
            }

            /**
             * @return the sstag
             */
            public String getSstag() {
                return sstag;
            }

            /**
             * @param sstag the sstag to set
             */
            public void setSstag(String sstag) {
                this.sstag = sstag;
            }

            /**
             * @return the posTag
             */
            public String getPosTag() {
                return posTag;
            }

            /**
             * @param posTag the posTag to set
             */
            public void setPosTag(String posTag) {
                this.posTag = posTag;
            }

            @Override
            public String toString() {
            // TODO Auto-generated method stub


                String temp = token + "/" + posTag + "/" + sstag+"/"+lemma;
                return temp;
                
            }
        
}
