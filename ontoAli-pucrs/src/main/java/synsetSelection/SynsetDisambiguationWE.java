package synsetSelection;


import java.text.SimpleDateFormat;
import java.util.*;

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
     * Object that represents the similarity between a pair of elements,
     * one from a concept context and the other from a synset context,
	 * used at the word embedding distributive between both contexts
     */

    private class WordEmbbedingPair{

    //Attributes

        private String synsetContextElement;	//A token present at the context of a specific synset (we distributive)
        private String conceptContextElement;	//A token present at the context of a specific concept (we distributive)
        private Double modelSimilarity;			//The similarity between the pair of tokens above, retrieved by the w2v model

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
	 * a synset called 'mapped pair', which will be compared with other
	 * mapped pairs in order to find the 'best' mapped pair to be used
	 * at the alignment phase
	*/

	public class WordEmbeddingObject implements Comparable<WordEmbeddingObject>{

	//Attributes

		private BabelNetResource.SearchObject originSynset;	//The synset that contains the necessary 'synset context' to be used at the we distributive
		private Concept originConcept;						//The concept that contains the necessary 'concept context' to be used at the we distributive
		private Double similarity;							//The final similarity encountered for this pair (considering all 'intermediate' wordEmbeddingPairs)
		private Set<WordEmbbedingPair> distributivePairs;	//The set containing all pairs resultant from both contexts distributed

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
		    String s =  "|Conceito: " + this.originConcept.getClassName() + "\n";
		    s +=        "|Synset: " + this.originSynset.getSynset().getMainSense() + "\n";
		    s +=        "|Similaridade: " + this.similarity + "\n";
		    s +=        "\nPARES:\n";

		    for(WordEmbbedingPair wePair : this.distributivePairs){
		        s += "<PAR " + count + "> " + wePair.conceptContextElement +
                        " & " + wePair.synsetContextElement +
                        " -> " + wePair.modelSimilarity + "\n";
		        count ++;
            }

		    return s;
        }
        @Override
		public int compareTo(WordEmbeddingObject weObj){
			if(this.similarity > weObj.similarity) return -1;
			else if(this.similarity < weObj.similarity) return 1;
			return 0;
		}
	}

//Attributes

	private BaseResource base;					//Base resources
	private BabelNetResource bn;				//Babel net related resources and actions
	private Set<WordEmbeddingObject> mapping;	//A set containing all mapped pairs


//Constructor	
	
	public SynsetDisambiguationWE(BaseResource base) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Synset didambiguation with Word embedding selected!" );
		this.base = base;
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
	 * Method responsible for initiating and finishing the disambiguation process
	 * through we technique for a single concept (which will be related
	 * to a single synset)
	 */

	public void bestSynset(Concept concept)  {
		StanfordLemmatizer slem = this.base.getLemmatizer();
		ConceptManager man = new ConceptManager();
		Utilities ut = new Utilities();

		String name = man.getConceptName(concept);
		String lemmaName = slem.spConceptName(name);

        Set<BabelNetResource.SearchObject> searched = bn.search(lemmaName);

		if(!searched.isEmpty()) {
			man.configSynset(concept, weTechnique(searched, concept, true));
		}
		ut.setSynsetCntx(searched);
		ut.setNumSy(searched.size());
		ut.setMappings(mapping);
		ut.setBestPair(mapping.iterator().next());
		man.configUtilities(concept, ut);
	}

	/**
	 * Word embeddings disambiguation process based on the average
	 * cosine distance between aconcept context and a synset context
	 */

	public BabelNetResource.SearchObject weTechnique(Set<BabelNetResource.SearchObject> synsets, Concept concept, boolean useTreeSet){
		double maxAverage = 0;
		if(useTreeSet) this.mapping = new TreeSet<>();
		BabelNetResource.SearchObject bestSynset = null;
		Set<String> context = this.base.getLemmatizer().toSet(this.base.getLemmatizer().toList(concept.getConceptContext()));

		for (BabelNetResource.SearchObject synset : synsets) {
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
			if(useTreeSet) this.mapping.add(weObj);

			if(totalAverage> maxAverage) {
				maxAverage = totalAverage;
				bestSynset = synset;
			}
		}

		return bestSynset;
	}
}
