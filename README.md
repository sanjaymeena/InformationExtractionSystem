InformationExtractionSystem
===========================

Information Extraction System can perform NLP tasks like Named Entity Recognition, Sentence Simplification, Relation Extraction etc. 

This document is an overview of various modules. For more information, please refer to InformationExtractionSystem.pdf in Resources folder. 

## Modules in Information Extraction System
### Module 1 : Tagger Module 
The tagger module performs following tasks: text preprocessing, syntactic parsing using stanford parser, sense tagging using super sense tagger, coreference resolution. 
### Module 2: Fact Extraction Module 
 This module performs various syntactic transformations on sentences to extract factual information. Syntactic transformations are based on English syntactic Rules.
 ### Module 3: Entity Extraction Module 
 The Entity extraction module extracts the entities from the text. The entity types are based on wordnet senses. In total, there are 27 noun categories and 15 verb categories 
### Module 4: Relation Extraction Module
Relation Extraction Module will extract the triplet: predicate, subject, object which will be present in sentences. For complex sentences, more than one triplet can be present. 

## About the Code
Please run com.asus.ctc.ie.InformationExtraction for demo run. The IE system is memory intensive. please provide -Xmx1024m as VM argument. 

### Configuration 
Configuration files are kept in resources/core_ie_resources/ie_data. THey are controlled in com.asus.ctc.ie.config.GlobalProperties

The main configuration files are: 
1. ie_processing_configuration.properties : Run time control over different modules in IE System
2. ie_properties.properties : All the necessary filepaths for various resources are mentioned here. 
3. stanford_parser_configuration.xml : Stanford Parser configuration to run it as as socket server. 
4. supersense_tagger_configuration.xml : Super Sense tagger configuration to run it as a socket server. 





