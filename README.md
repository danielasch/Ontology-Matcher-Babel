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
  ###### Ubuntu 17.10 was used for developing and its recommended for the usage as well.


## Program arguments:				       
 
  1. Domain ontology path
  	_e.g.: C:/Users/.../ontology.owl_

  1. Rdf alignment path (to be generated)
  	_e.g.: C:/Users/.../folder_containing_alignment_

  1. Top ontology (dolce or sumo)
  	_e.g.: dolce_

  1. Alignment technique (1 or 2)
  	_e.g.: 1_
	
  	**Important**
		* 1:  Context technique(LESK)
		* 2:  Word embedding technique
	###### Observation: If selected, the 2nd technique must use the notation _2:model_, where model = 'google' or 'glove' (_e.g.: 2:google_)

  1. Context selecting (0 or 1)
 	 _e.g.: 0_

  	**Important**
		* 0:  Concept context for disambiguation (only)
		* 1:  Concept context + domain onyology context for disambiguation

  1. Reference alignment path (optional)
  _e.g.: C:/Users/.../referenceAlign.rdf_


## Program requirements

  To achieve great alignments and processes for your ontologies you must download some third-party resources such as BabelNet 4.0.1 indices (for offline processing of big data using BabelNet) and the stanford GloVe algorithm (which obtains vector representation for words).  The reason why both of these resources are not included in this software tool its because they're both too large for GitHub's repository storage, and to obtain BabelNet indices you must belong to a research instituition, so they cannot be distributed by the PLN group. If your profile fits to these requirements, you shall follow the next steps:
  
  * GloVe
    - Download [_'GloVe.6B.zip'_](https://nlp.stanford.edu/projects/glove/) pack of files and unzip it.
    - Enter into GloVe's unzipped directory and copy _'glove.6B.200d.txt'_ file. 
    - Access the root directory where your _'Ontology-Matcher-Babel'_ was cloned.
    - Double click the _'ontoAli-pucrs'_ directory.
    - Open the resources folder and paste your _'glove.6B.200d.txt'_ file into it.
  
  * BabelNet indices
    - Open your local browser and access the [BabelNet's download page](https://babelnet.org/download) and follow its steps to obtain _'your_name_babelnet-4.0.zip'_ file and unzip it anywhere you want.
    - To use your local indices you must enter the root directory where your _'Ontology-Matcher-Babel'_ was cloned.
    - Acess the _'ontoAli-pucrs'_ directory as well.
    - Enter into the _'config'_ folder and open the _'babelnet.var.properties'_ file.
    - Follow the configuration examples included inside it. Wen you're done, save and close this file.
    
  ###### If you do not belong to a researh instituition you can use BabelNet's RESTFul search service following [these steps](https://babelnet.org/guide#HowcanIdownloadtheBabelNetindices?). Keep in mind that it may affect the results of this software tool.


# ENJOY!
