package com.asus.ctc.eebot.ie.externalresources.conceptnet.model;

/**
 * @author Sanjay_Meena
 *
 */
public class queryModel {
    static String baseURL = "http://conceptnet5.media.mit.edu/data/5.1";
    
    String concept;
    String language;
    
    
    
    queryModel(String concept, String language){
        
        
    }
    
private static String createURLforConcept(String concept, String language) {
        
        language="/"+ "en";
        String type="/"+ "c";
        String var="/" + concept;
        
        String url;
        url=baseURL+type+language+var;
        
        return url;
        
        
        
        

    }
}
