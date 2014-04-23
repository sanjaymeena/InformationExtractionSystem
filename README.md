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


## About Code

Please run com.asus.ctc.ie.InformationExtraction for demo run. 

### Configuration 
Configuration files are kept in resources/core_ie_resources/ie_data
The main configuration files are: 
1. item
2. item
3. item

















