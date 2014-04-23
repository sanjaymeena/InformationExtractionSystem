package com.asus.ctc.ie.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * This class is to get the IE_properties like filepaths etc for the
 * IE System
 * 
 * @author Sanjay_Meena
 */
public class GlobalProperties {
    
    public static String prefixPath = "" ;
    public static final String resourcesPath = "resources/core_ie_resources/ie_data/" ;
   

	/**
     * File path for IEProperties Properties file
     */
    public static final String IEProperties = prefixPath + resourcesPath + "ie_properties.properties";
    
    /**
     * File path for FactualStatementExtractor Properties file
     */
    public static final String FactualStatementExtractor = prefixPath + resourcesPath+ "factual-statement-extractor.properties";

    /**
     * File path for QuestionAskerTest Properties file
     */
    public static final String IEProcessingConfiguration = prefixPath  + resourcesPath+ "ie_processing_configuration.properties";
    /**
     * File path for wordnet Properties file
     */
    public static final String wordnet_file_properties = prefixPath  + resourcesPath+ "wordnet" + File.separator + "file_properties.xml";
    /**
     * File path for verbConjugations Properties file
     */
    public static final String verbConjugations = prefixPath + resourcesPath+ "wordnet" + File.separator + "verbConjugations.txt";

    /**
     * File path for DRRecognizer (discourse relation recognizer) Properties file
     */
    public static final String drRecognizer_properties = prefixPath  + resourcesPath + "drrecognizer.properties";
    /**
     * This function sets the global IE_properties taking an identifier to load the
     * IE_properties from a file.
     * 
     * @param type
     *            the identity of the propertyfile
     * @return <code>Properties</code> java class representing persistent set of
     *         IE_properties
     */
    public static Properties getProperties(int type) {

        Properties property = null;
        switch (type) {
            case 1: // Global Properties
                if (IE_properties == null) {
                    String defaultPath = IEProperties;
                    loadProperties(1, defaultPath);
                }
                return IE_properties;
            case 2: // Properties for QuestionAskerTest
                if (ieRunTimeConfiguration == null) {
                    String defaultPath = IEProcessingConfiguration;
                    loadProperties(2, defaultPath);
                }
                return ieRunTimeConfiguration;
            case 3: // 
            	if (DR_properties == null) {
                    String defaultPath = drRecognizer_properties;
                    loadProperties(3, defaultPath);
                    System.out.println("GlobalProperties.getProperties() 3: "+ defaultPath);
                }
            	return DR_properties;
            default:
                break;
        }

        return property;

    }

    /**
     * This function will load the IE_properties from an given file with a given
     * path.
     * @param type
     *            the type of property to set:
     * @param propertiesFile
     *            The filepath of the file from which to set the property.
     */
    public static void loadProperties(int type, String propertiesFile) {
        if (!(new File(propertiesFile).exists())) {
            System.err.println("IE_properties file not found at the location, " + propertiesFile + ".  Please specify with --IE_properties PATH.");
            System.exit(0);
        }

        switch (type) {
            case 1:
                IE_properties = new Properties();
                try {
                    IE_properties.load(new FileInputStream(propertiesFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                break;
            case 2:
                ieRunTimeConfiguration = new Properties();
                try {
                    ieRunTimeConfiguration.load(new FileInputStream(propertiesFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                break;
            case 3:    
                DR_properties = new Properties();
                try {
                	DR_properties.load(new FileInputStream(propertiesFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                break;
            default:
                break;
        }

    }

    /**
     * @param debug
     */
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    /**
     * @return boolean
     */
    public static boolean getDebug() {
        return DEBUG;
    }

    /**
     * @param b
     */
    public static void setComputeFeatures(boolean b) {
        computeFeatures = b;
    }

    /**
     * @return boolean
     */
    public static boolean getComputeFeatures() {
        return computeFeatures;
    }

    private static Properties DR_properties;
    
    
    private static Properties IE_properties;
    /**
     * Boolean for whether to Print the output in verbose mode or not.
     */
    private static boolean DEBUG;

    /**
     * Boolean for whether Language features be used for computing rank of a
     * Question.
     */
    private static boolean computeFeatures = true;

    private static Properties ieRunTimeConfiguration;
    public static String getPrefixPath() {
		return prefixPath;
	}

	public static void setPrefixPath(String prefixPath) {
		GlobalProperties.prefixPath = prefixPath;
	}
}
