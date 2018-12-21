package synsetSelection;

import java.text.SimpleDateFormat;
import java.util.*;

import it.uniroma1.lcl.babelnet.BabelSynset;
import objects.Concept;
import objects.ConceptManager;
import resources.BabelNetResource;
import resources.BaseResource;
import resources.StanfordLemmatizer;
import resources.Utilities;

/**
 * This class disambiguate a list of recovered synsets for a
 * specific concept which came from an argued ontology using
 * LESK technique (overlapping over two sets)
 */
public class SynsetDisambiguationLESK {

//Attributes


    private BaseResource base;      //Representation of common basic resources to realize the disambiguation and also other processes
    private BabelNetResource bn;    //Representation of every common BabelNet related 'operation' used in many processes


//Constructor

    public SynsetDisambiguationLESK(BaseResource _base) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Synset didambiguation selected!");
        this.base = _base;
        this.bn = new BabelNetResource();
    }


//Log Methods

    private void initLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Disambiguating Synsets...");
    }

    private void finalLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Synsets disambiguated!");
    }


//Methods

    /**
     * This method initiates the synset disambiguation over the ontology concepts
     * @param listCon A list of first level concepts from the argued ontology
     */
    public void disambiguation(List<Concept> listCon) {
            initLog();
            for (Concept concept : listCon) {
                bestSynset(concept);
            }
            finalLog();
    }


    /**
     * Lesk disambiguation process based on the overlapping between a concept context
     * and a synset context in order to select the greatest intersection value between
     * the generated distributive
     * @param concept A concept to be related to a BabelSynset through the LESK technique
     */

    public void bestSynset(Concept concept) {
        StanfordLemmatizer slem = base.getLemmatizer();
        ConceptManager man = new ConceptManager();
        Utilities ut = new Utilities();

        String name = man.getConceptName(concept);

        String lemmaName = slem.fullConceptName(name);
        Set<BabelNetResource.SearchObject> searched = bn.search(lemmaName);

        if (searched.size() != 1) {
            man.configSynset(concept,leskTechnique(searched, concept));
            ut.setNumSy(searched.size());

        } else {
            BabelNetResource.SearchObject synset = searched.iterator().next();
            man.configSynset(concept, synset);
            ut.setNumSy(1);
        }

        ut.setSynsetCntx(searched);
        man.configUtilities(concept, ut);
    }


    /**
     * Method responsible for overlap between two contexts, one representing the parametrized
     * concept at 'bestSynset' and the other representing a BabelSynset recovered from a BabelNet
     * search containing the nomenclature of the concept
     * @param synsets A list of 'SearchObjects' of which each of them contains a recovered synset
     * from a BabelNet search and its bag of words to be overlapped
     * @param concept The concept to be attached to the 'best synset' found through the technique
     * @return A single BabelSynset, that returned the greatest intersection value from the overlap,
     * to be linked with a specific concept
     */
    public BabelNetResource.SearchObject leskTechnique(Set<BabelNetResource.SearchObject> synsets, Concept concept){
        BabelNetResource.SearchObject selected = null;
        List<String> context = this.base.getLemmatizer().toList(concept.getConceptContext());
        int max = -1;

        for(BabelNetResource.SearchObject so : synsets) {

            int test = intersection(so.getBgw(), context);

            if (test > max) {
                selected = so;
                max = test;
            }
        }

        return selected;
    }


    /**
     * The concrete overlap between two contexts
     * @param context The concept's context
     * @param bagSynset A single synset's concept
     * @return The intersection value
     */
    public int intersection(Set<String> bagSynset,List<String> context) {
        int inter = 0;
        for (String word : context) {
            word = word.toLowerCase();
            for (String wordCompared : bagSynset) {
                if (word.equals(wordCompared)) {
                    inter++;
                    break;
                }
            }
        }
        return inter;
    }

}
