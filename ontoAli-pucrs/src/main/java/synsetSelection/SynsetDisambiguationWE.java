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
 * This class disambiguate the synset using the Word Embedding model from GloVe.
 * Model specifications - 6B tokens, 400K vocab, uncased, & 200d vectors.
 *
*/
public class SynsetDisambiguationWE {

	/**
	 * Object to represent the similarity between the context of
	 * a synset x the context of a concept, called 'mapped pair'
	*/

	public class WordEmbeddingObject{

	//Attributes

		private BabelNetResource.SearchObject originSynset;
		private Concept originConcept;
		private Set<String> bagConcept;
		private Double similarity;

	//Constructor

		public WordEmbeddingObject(BabelNetResource.SearchObject originSynset, Concept originConcept){
			this.originSynset = originSynset;
			this.originConcept = originConcept;
		}

	//Getters

		public Set<String> getBagSynset() { return originSynset.getBgw(); }

		public Set<String> getBagConcept() {return bagConcept; }

		public Double getSimilarity() { return similarity; }

		public BabelNetResource.SearchObject getOriginSynset() { return originSynset; }

		public Concept getOriginConcept() { return originConcept; }

		//Setters

		public void setBagConcept(Set<String> bagConcept) { this.bagConcept = bagConcept; }

		public void setSimilarity(Double similarity) { this.similarity = similarity; }
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
	 * This method selects the right synset to a concept
	 *
     */
	public void disambiguation(List<Concept> listCon) {
		initLog();
		for (Concept concept : listCon) {
			rcGoodSynset(concept);
		}
		finalLog();
	}


	/**
	 * Disambiguation process 
	 *
	 */
	void rcGoodSynset(Concept concept)  {

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

						if (!(Double.isNaN(sim))) average = average + sim * 10;

					}

			    	average = average / synset.getBgw().size();
			    	totalAverage += average;
			    }

			    totalAverage = totalAverage / context.size();

			    weObj.setSimilarity(totalAverage);
			    weObj.setBagConcept(context);
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



	//create the bag of words of a synset
	/*
	private List<String> createBagWords(List<IWord> wordsSynset, String glossSynset) {
	    List<String> list = new LinkedList<String>();
	    Set<String> set = new HashSet<String>();
	    StanfordLemmatizer slem = this.base.getLemmatizer();
	    for (IWord i : wordsSynset) {
	    	StringTokenizer st = new StringTokenizer(i.getLemma().toLowerCase().replace("_"," ")," ");
	    	while (st.hasMoreTokens()) {
	    		  String token = st.nextToken();
	    	 	  if (!list.contains(token)) {
	    	  	      list.add(token);
	    	      }
	    	}
	    }
	    glossSynset = glossSynset.replaceAll(";"," ").replaceAll("\"", " ").replaceAll("-"," ").toLowerCase();
	    StringTokenizer st = new StringTokenizer(glossSynset," ");
    	while (st.hasMoreTokens()) {
    		   String token = st.nextToken().toLowerCase();
    		   token = rm_specialChar(token);
    		   if (!this.base.getStpWords().contains(token) && !list.contains(token)) {
    			   list.add(token);
    		   }
    	}
    	//turn the list into a string to lemmatize the list
    	String toLemma = slem.toLemmatize(list);
    	//clears the list
		list.clear();
		//list receive the string lemmatized
		list = slem.lemmatize(toLemma);
		//turns the list into a set,
		//to avoid repeated lemmatized strings
		set =  slem.toSet(list);
		//turns back the set into a list
		list = slem.toList(set);
	   return list;
	}
    */


}
