package matchingProcess;
import java.text.SimpleDateFormat;
import java.util.*;

import it.uniroma1.lcl.babelnet.BabelSynset;

import objects.Concept;
import objects.ConceptManager;
import org.omg.CORBA.INTERNAL;
import resources.BabelNetResource;
import resources.BaseResource;
import resources.Utilities;
import synsetSelection.SynsetDisambiguationLESK;
import synsetSelection.SynsetDisambiguationWE;

/**
 * This class matches domain-level ontology concepts with top-level ontology concepts
 */

public class Matching {

//Attributes

	private BabelNetResource bn;						//The resource base for BabelNet operations
	private BaseResource base;							//The resource base for common pln processes
	private SynsetDisambiguationLESK disambLESK;		//Class that contains lesk technique processes
    private SynsetDisambiguationWE disambWE;			//Class that contains word embedding technique processes



//Constructor	
	
	public Matching() {
		this.bn = new BabelNetResource();
		this.base =  new BaseResource();
		this.disambLESK = new SynsetDisambiguationLESK(base);
		this.disambWE = new SynsetDisambiguationWE(base);
	}

	public Matching(String model){
		this.bn = new BabelNetResource();
		this.base =  new BaseResource(model);
		this.disambLESK = new SynsetDisambiguationLESK(base);
		this.disambWE = new SynsetDisambiguationWE(base);

	}


//Log methods
	
	private void initLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Matching ontologies..." );
	}
	
	private void finalLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Ontologies matched!" );
	}


//Methods


	/**
	 * Matches a pair of concepts (domain - top) throug the babelnet's hypernym structure recovered from
	 * a synset assigned to the domain concept
     * @param dom A list of first-level concepts extracted from the argued domain-level ontology
     * @param up A list of top-level concepts extracted from the argued top-level ontology
     * @return A list of mappings representing the possible correspondences between a pair of
     * different-level concepts
	 */

	public List<Mapping> matchBabel(List<Concept>dom, List<Concept>up, int tec){
		initLog();
		Set<BabelNetResource.SearchObject> hyp = new HashSet<>();
		List<BabelSynset> path = new LinkedList<>();
		List<Mapping> listM = new ArrayList<>();
        BabelSynset selected = null;

		for(Concept d : dom){
			path.clear();

			int levels = 0;
			int limit = 10;
			boolean matched;

			if(d.getGoodSynset() != null) {
				BabelSynset bs = d.getGoodSynset().getSynset();
                String hypernym = bn.lemmatizeHypernym(bs.toString());
                matched = tryMatch(hypernym,up,d,listM,levels);
                path.add(bs);
                levels++;
				while(levels != limit){
                    if(matched) {
                        d.getUtilities().setHypernyms(path.toString());
                        break;
                    }
                    hyp = bn.getHypernyms(bs,hyp,path);
                    if(!hyp.isEmpty()) {
                        if (tec == 1) selected = disambLESK.leskTechnique(hyp, d).getSynset();
                        else if (tec ==2) selected = disambWE.weTechnique(hyp, d, false).getSynset();
						hypernym = bn.lemmatizeHypernym(selected.toString());
						matched = tryMatch(hypernym, up, d, listM, levels);
						bs = selected;
						levels++;
					}else levels = limit;
					path.add(bs);
				}
			}
		}
		finalLog();
		return listM;
	}

	/**
	 * Method responsible for analyzing every possibility for a hypernym to match with a top-level concept
	 * visiting every top-level ontology concept present in the argued ontology (sumo or dolce)
	 * @param hypernym The nomenclature of the hypernym found in BabelNet hypernym structure of a disambiguated
	 * synset (assigned to a domain-level concept), which may match with a top-level concept nomenclature
	 * @param up A list containing all the top-level concepts extracted from the argued top-level ontology
	 * @param dom A list containing all the domain-level concepts extracted from the argued domain-level ontology
	 * @param listM A list containing all mappings found in the matching process
	 * @param levels The level where the match was found (if found)
	 * @return A boolean telling if a correspondence was found or not with that hypernym nomenclature
	 */
	private boolean tryMatch(String hypernym, List<Concept>up, Concept dom, List<Mapping> listM, int levels){
		Boolean matched = false;
		ConceptManager man = new ConceptManager();
		for (Concept u : up) {
			if (u.getClassName().toLowerCase().equals(hypernym)) {
				Mapping map = new Mapping();
				map.setSource(dom.get_owlClass().getIRI().toString());
				man.configAliClass(dom, u.get_owlClass());
				map.setTarget(dom.getAliClass().getIRI().toString());
				map.setRelation("&lt;");
				map.setMeasure("1.0");
				listM.add(map);
				Utilities ut = dom.getUtilities();
				ut.setSelectedHypernym(hypernym);
				ut.setLevel(levels);
				matched = true;
				break;
			}
		}
		return matched;
	}
	
}

