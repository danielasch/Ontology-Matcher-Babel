package synsetSelection;


import java.text.SimpleDateFormat;
import java.util.*;

import fr.inrialpes.exmo.align.impl.Similarity;
import objects.Concept;
import objects.ConceptManager;
import resources.BabelNetResource;
import resources.BaseResource;
import resources.StanfordLemmatizer;
import resources.Utilities;

/**
 * This class disambiguates a synset using the 'Word Embedding' parametrized model from GloVe/Google
*/
public class SynsetDisambiguationWE  {

    /**
     * Object to represent the similarity between a pair of elements,
     * one from a concept context and the other from a synset context
     */

    private class WordEmbbedingPair{

    //Attributes

        private String synsetContextElement;
        private String conceptContextElement;
        private Double modelSimilarity;

    //Constructor

        public WordEmbbedingPair( String conceptContextElement, String synsetContextElement, Double modelSimilarity) {
            this.conceptContextElement = conceptContextElement;
            this.synsetContextElement = synsetContextElement;
            this.modelSimilarity = modelSimilarity;
        }

    //Getters

        public String getConceptContextElement() { return conceptContextElement; }

        public String getSynsetContextElement() { return synsetContextElement; }

        public Double getModelSimilarity() { return modelSimilarity; }

    }

	/**
	 * Object to represent the similarity between a concept and
	 * a synset called 'mapped pair'
	*/

	public class WordEmbeddingObject{

	//Attributes

		private BabelNetResource.SearchObject originSynset;
		private Concept originConcept;
		private Double similarity;
		private Set<WordEmbbedingPair> distributivePairs;

	//Constructor

		public WordEmbeddingObject(BabelNetResource.SearchObject originSynset, Concept originConcept){
			this.originSynset = originSynset;
			this.originConcept = originConcept;
			this.distributivePairs = new HashSet<>();
		}

	//Getters


		public Double getSimilarity() { return similarity; }

		public BabelNetResource.SearchObject getOriginSynset() { return originSynset; }

		public Concept getOriginConcept() { return originConcept; }

		public Set getDistributivePairs() { return this.distributivePairs; }

	//Setters

		public void setSimilarity(Double similarity) { this.similarity = similarity; }

	//Methods

		public String toString(){
		    int count = 1;
		    String s =  "|Concept: " + this.originConcept.getClassName() + "\n";
		    s +=        "|Synset: " + this.originSynset.getSynset().getMainSense() + "\n";
		    s +=        "|Similarity: " + this.similarity + "\n";
		    s +=        "\nPAIRS:\n";

		    for(WordEmbbedingPair wePair : this.distributivePairs){
		        s += "<PAIR " + count + "> " + wePair.conceptContextElement +
                        " & " + wePair.synsetContextElement +
                        " -> " + wePair.modelSimilarity + "\n";
		        count ++;
            }

		    return s;
        }
	}

//Attributes

	private BaseResource base;
	private BabelNetResource bn;


//Constructor	
	
	public SynsetDisambiguationWE(BaseResource _base) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Synset didambiguation with Word embedding selected!" );
		this.base = _base;
		this.bn = new BabelNetResource();
	}


//Log methods
	
	private void initLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Disambiguating Synsets..." );
	}


	private void finalLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Synsets disambiguated!" );
	}


//Methods

	/**
	 * This method selects the right synset for all concepts
     * which came from a certain domain-level ontology
     */
	public void disambiguation(List<Concept> listCon) {
		initLog();
		for (Concept concept : listCon) {
			bestSynset(concept);
		}
		finalLog();
	}


	/**
	 * Word embeddings disambiguation process based on the average cossine distance between a
     * concept context and a synset context
	 *
	 */
	void bestSynset(Concept concept)  {
		StanfordLemmatizer slem = this.base.getLemmatizer();
		ConceptManager man = new ConceptManager();
		Utilities ut = new Utilities();
		List<WordEmbeddingObject> mapping = new ArrayList<>();
		WordEmbeddingObject selected = null;

		Set<String> context = slem.toSet(slem.toList(concept.getContext()));

		String name = man.getConceptName(concept);
		String lemmaName = slem.spConceptName(name);

		BabelNetResource.SearchObject bestSynset = null;
        Set<BabelNetResource.SearchObject> searched = bn.search(lemmaName);

		if(!searched.isEmpty()) {
			double maxAverage = 0;

			for (BabelNetResource.SearchObject synset : searched) {
				double totalAverage = 0;
				WordEmbeddingObject weObj = new WordEmbeddingObject(synset, concept);

			    for(String cntxElement: context) {
			    	double average = 0;

			    	for(String bagElement: synset.getBgw()) {
						double sim = this.base.getWord2Vec().getword2Vec().similarity(cntxElement, bagElement);
						weObj.distributivePairs.add(new WordEmbbedingPair(cntxElement, bagElement, sim));

                        if (!(Double.isNaN(sim))) average = average + sim * 10;
					}
			    	average = average / synset.getBgw().size();
			    	totalAverage += average;
			    }
			    totalAverage = totalAverage / context.size();
			    weObj.setSimilarity(totalAverage);
			    mapping.add(weObj);

				if(totalAverage> maxAverage) {
			    	maxAverage = totalAverage;
                    bestSynset = synset;
			    }
			}
			man.configSynset(concept, bestSynset);
		}
		ut.setSynsetCntx(searched);
		ut.setMappings(mapping);
		ut.setBestPair(selected);
		man.configUtilities(concept, ut);
	}
}
