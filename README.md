# Ontology-Matcher-Babel

  * This software is being developed by the NLP group at PUCRS.
  * For more information, please check [our page!](http://www.inf.pucrs.br/linatural/wordpress/)

## About this software

The Ontology Matcher Babel is a top-domain-level ontology matcher system based on [Daniela Schmidt's ontology matching
repository.](https://github.com/danielasch/Ontology-Matcher) The alternative explored here uses [BabelNet](https://babelnet.org/) as main resource for the alignment process instead of [WordNet.](https://wordnet.princeton.edu/)  This tool uses SUMO and DOLCE top-level ontologies to be aligned with any domain ontology.
 
## Minimum system requirements
  
  * Enviroment: Java SE Runtime Environmet 8+
  * Memory:     5,7GiB
  * CPU:        Intel Core i5-3470S CPU@2.90GHzx4
  * OS-type:    64-bit
  
  ###### Observation: The memory usage depends on the size of the selected ontologies.
  
## Program arguments:				       
 
  * 1st: domain ontology path [string]
	* _e.g.: C:/Users/.../ontology.owl_

  * 2nd: .rdf alignment path (to be generated) [string]
	* _e.g.: C:/Users/.../folder_containing_alignment_

  * 3rd: top ontology (dolce or sumo) [string]
	* _e.g.: dolce_

  * 4th: alignment technique (1 or 2) [integer]
	* _e.g.: 1_
        
	* **Important**
		* 1:  Context technique(LESK)
		* 2:  Word embedding technique

       ###### Observation: If selected, the 2nd technique must use the notation _2:model_, where model = 'google' or 'glove' (_e.g.: 2:google_)

  * 5th: context selecting (0 or 1) [integer]
	* _e.g.: 0_

	* **Important**
		* 0:  Concept context for disambiguation (only)
		* 1:  Concept context + domain onyology context for disambiguation

  * 6th: reference alignment path (optional) [string]
  	* _e.g.: C:/Users/.../referenceAlign.rdf_
	
## Program requirements

  To achieve great alignments and processes for your ontologies you must download some third-party resources such as BabelNet 4.0.1 indices (for offline processing of big data using BabelNet) and the stanford GloVe algorithm (which obtains vector representation for words).  The reason why both of these resources are not included in this software tool its because they're both too large for GitHub's repository storage, and to obtain BabelNet indices you must belong to a research instituition, so they cannot be distributed by the PLN group. If your profile fits to these requirements, you shall follow the next steps:
  
  1. Item 1
  1. Item 2
  1. Item 3
  	1. Item 3.1























