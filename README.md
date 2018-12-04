# Ontology-Matcher-Babel

  * This software is being developed by the NLP group at PUCRS.
  * For more information, please check http://www.inf.pucrs.br/linatural/wordpress/.

## About this software

  The Ontology Matcher Babel is a top-domain-level ontology matcher system
 based on https://github.com/danielasch/Ontology-Matcher. The alternative 
 explored here uses BabelNet as main resource for the alignment process
 instead of WordNet. This tool uses SUMO and DOLCE top-level ontologies to
 be aligned with any domain ontology.
 
  For more information check the links below:
 * 1st https://wordnet.princeton.edu/, used at the cited repository (above).
 * 2nd https://babelnet.org/, used in this software tool.
 
## Minimum system requirements
  
  * Enviroment: Java SE Runtime Environmet 8+
  * Memory:     5,7GiB
  * CPU:        Intel Core i5-3470S CPU@2.90GHzx4
  * OS-type:    64-bit
  
  ###### Observation: The memory usage depends on the size of the ontologies used.
  
## VM arguments:				       
 
  * 1st [domain ontology path][string]
 	e.g.: C:/Users/.../ontology.owl

  * 2nd [.rdf alignment path] (to be generated)[string]
	e.g.: C:/Users/.../alignment.rdf

  * 3rd [top ontology] (dolce or sumo)[string]
	e.g.: dolce

  * 4th [alignment technique] (1 or 2)[integer]
    	e.g.: 2
        
	* Observation
        	* 1:  Context technique(LESK)
        	* 2:  Word embedding technique
            		(if selected, use -> 2:model, where model = 'google' or 'glove')

  * 5th [context selecting] (0 or 1)[integer]
	e.g.: 0

	* Observation
		* 0:  Concept context for disambiguation (only)
		* 1:  Concept context + domain onyology context for disambiguation

  * 6th [reference alignment path](optional)[string]
  	e.g.: C:/Users/.../referenceAlign.rdf
