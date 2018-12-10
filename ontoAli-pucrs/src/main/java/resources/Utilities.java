package resources;

import java.util.*;

import synsetSelection.SynsetDisambiguationWE;

/**
 * This class saves the information (about a concept) used to generate the text files.
 */
public class Utilities {

//Attributes
	
	private Set<BabelNetResource.SearchObject> synsetCntxt;
	private List<SynsetDisambiguationWE.WordEmbeddingObject> mappings;
	private SynsetDisambiguationWE.WordEmbeddingObject bestWePair;
	private String hypernyms, selected_hypernym;
	private int numSy, level;


//Constructor	
	
	public Utilities() {
		this.synsetCntxt = null;
		this.numSy = level = 0;

	}


//Getters

    public Set<BabelNetResource.SearchObject> getSynsetCntx() { return synsetCntxt; }

	public int getNumSy() {
		return numSy;
	}

	public String getHypernyms(){ return this.hypernyms; }

    public String getSelectedHypernym() { return selected_hypernym; }

    public int getLevel() { return level; }

    public List<SynsetDisambiguationWE.WordEmbeddingObject> getMappings() { return mappings; }

    public SynsetDisambiguationWE.WordEmbeddingObject getBestPair() { return bestWePair; }


//Setters

    public void setSynsetCntx(Set<BabelNetResource.SearchObject> synsetCntxt){ this.synsetCntxt = synsetCntxt; }

    public void setNumSy(int num) {
        this.numSy = num;
    }

    public void setHypernyms(String hypernyms) { this.hypernyms = hypernyms; }

    public void setSelectedHypernym(String selected_hypernym) { this.selected_hypernym = selected_hypernym; }

    public void setLevel(int level) { this.level = level; }

    public void setMappings(List<SynsetDisambiguationWE.WordEmbeddingObject> map) { this.mappings = map; }

	public void setBestPair(SynsetDisambiguationWE.WordEmbeddingObject weObj) { this.bestWePair = weObj; }

}
