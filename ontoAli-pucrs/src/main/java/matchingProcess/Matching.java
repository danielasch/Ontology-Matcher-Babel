package matchingProcess;
import java.text.SimpleDateFormat;
import java.util.*;

import it.uniroma1.lcl.babelnet.BabelSynset;

import objects.Concept;
import objects.ConceptManager;
import resources.BabelNetResource;
import resources.BaseResource;
import resources.Utilities;
import synsetSelection.SynsetDisambiguation;

/**
 * This class matches domain-level ontology concepts with top-level ontology concepts
 */

public class Matching {

//Attributes

	private BabelNetResource bn;
	private BaseResource base = new BaseResource();
	private SynsetDisambiguation disamb = new SynsetDisambiguation(base);


//Constructor	
	
	public Matching() {
		this.bn = new BabelNetResource();
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
	 * Matches a pair of concepts (domain - top) through
	 * the babelnet's hypernym structure recovered from
	 * the synset assigned to the domain concept
	 */

	public List<Mapping> matchBabel(List<Concept>dom, List<Concept>up){
		initLog();

		Set<BabelNetResource.SearchObject> hyp = new HashSet<>();
		List<BabelSynset> path = new LinkedList<>();
		List<Mapping> listM = new ArrayList<>();
		List<String> match_context = new LinkedList<>();
        BabelSynset selected;

		for(Concept d : dom){
			path.clear();
			match_context.clear();
			match_context.addAll(d.getConceptContext());

			int levels = 0;
			int limit = 10;
			Boolean matched;

			if(d.getGoodSynset() != null) {
				System.out.println("\nDomain concept: " + d.getClassName());
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
						selected = disamb.leskTechnique(hyp, match_context).getSynset();
						hypernym = bn.lemmatizeHypernym(selected.toString());
						matched = tryMatch(hypernym, up, d, listM, levels);
						bs = selected;
						System.out.println(levels);
						levels++;
					}else levels = limit;
					path.add(bs);
				}
				if(!matched) System.out.println("Could not match!\n");
			}
		}
		finalLog();
		return listM;
	}


	private boolean tryMatch(String hypernym, List<Concept>up, Concept d, List<Mapping> listM, int levels){
		Boolean matched = false;
		ConceptManager man = new ConceptManager();
		for (Concept u : up) {
			if (u.getClassName().toLowerCase().equals(hypernym)) {
				Mapping map = new Mapping();
				map.setSource(d.get_owlClass().getIRI().toString());
				man.configAliClass(d, u.get_owlClass());
				map.setTarget(d.getAliClass().getIRI().toString());
				map.setRelation("&lt;");
				map.setMeasure("1.0");
				listM.add(map);
				Utilities ut = d.getUtilities();
				ut.setSelectedHypernym(hypernym);
				ut.setLevel(levels);
                System.out.println("Matched with: " + u.getClassName()+ "\n");
				matched = true;
				break;
			}
		}
		return matched;
	}
	
}

