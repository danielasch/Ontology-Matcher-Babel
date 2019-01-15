package resources;

import java.util.*;

import synsetSelection.SynsetDisambiguationWE;

/**
 * This class saves the information (about a concept) used to generate the text files.
 */
public class Utilities {

//Attributes
	
	private Set<BabelNetResource.SearchObject> disambiguatedSynsets;	//All the successfully disambiguated (LESK) synsets related to a concept
	private Set<SynsetDisambiguationWE.WordEmbeddingObject> mappings;	//All the successfully disambiguated (WE) synsets related to a concept
	private SynsetDisambiguationWE.WordEmbeddingObject bestWePair;		//The best pair (concept-synset) found through WE
	private String hypernyms, selected_hypernym;						//The path-to-top-ontology of hypernyms found and the hypernym that matched with a top-level concept
	private int numSy, level;											//The synset and alignment level counters


//Constructor	
	
	public Utilities() {
		this.disambiguatedSynsets = null;
		this.numSy = level = 0;

	}


//Getters

    public Set<BabelNetResource.SearchObject> getDisambiguatedSynsets() { return disambiguatedSynsets; }

	public int getNumSy() {
		return numSy;
	}

	public String getHypernyms(){ return this.hypernyms; }

    public String getSelectedHypernym() { return selected_hypernym; }

    public int getLevel() { return level; }

    public Set<SynsetDisambiguationWE.WordEmbeddingObject> getMappings() { return mappings; }

    public SynsetDisambiguationWE.WordEmbeddingObject getBestPair() { return bestWePair; }


//Setters

    public void setSynsetCntx(Set<BabelNetResource.SearchObject> synsetCntxt){ this.disambiguatedSynsets = synsetCntxt; }

    public void setNumSy(int num) {
        this.numSy = num;
    }

    public void setHypernyms(String hypernyms) { this.hypernyms = hypernyms; }

    public void setSelectedHypernym(String selected_hypernym) { this.selected_hypernym = selected_hypernym; }

    public void setLevel(int level) { this.level = level; }

    public void setMappings(Set<SynsetDisambiguationWE.WordEmbeddingObject> map) { this.mappings = map; }

	public void setBestPair(SynsetDisambiguationWE.WordEmbeddingObject weObj) { this.bestWePair = weObj; }

}
