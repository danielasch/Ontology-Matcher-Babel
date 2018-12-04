package synsetSelection;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import objects.Concept;
import objects.ConceptManager;
import resources.BabelNetResource;
import resources.BaseResource;
import resources.StanfordLemmatizer;
import resources.Utilities;

/**
 * This class disambiguate the recovered synsets for a concept
 */
public class SynsetDisambiguation {

//Attributes

    //BaseResource contains the necessary resources to execute the disambiguation
    private BaseResource base;
    private BabelNetResource bn;


//Constructor

    public SynsetDisambiguation(BaseResource _base) {
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
     * This method selects the right synset to a concept
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
     */
    public void rcGoodSynset(Concept concept) {
        StanfordLemmatizer slem = base.getLemmatizer();
        ConceptManager man = new ConceptManager();
        Utilities ut = new Utilities();

        List<String> context = slem.toList(concept.getContext());
        String name = man.getConceptNameWn(concept);
        List<String> cnpNameLemma = slem.lemmatize(name);

        int i = cnpNameLemma.size();
        name = cnpNameLemma.get(i - 1);

        System.out.println("\nConcept name: " + name + "\n");

        BabelNetResource.SearchObject bestSynset;
        Set<BabelNetResource.SearchObject> searched = bn.search(name);

        if (searched.size() != 1) {
            bestSynset = leskTechnique(searched, context);
            man.configSynset(concept, bestSynset);
            ut.setNumSy(searched.size()-1);

        } else {
            BabelNetResource.SearchObject synset = searched.iterator().next();
            man.configSynset(concept, synset);
            ut.setNumSy(1);
        }

        ut.setSynsetCntx(searched);
        man.configUtilities(concept, ut);
    }


    public BabelNetResource.SearchObject leskTechnique(Set<BabelNetResource.SearchObject>context_1,
                                                       List<String>context_2){
        System.out.println("\nEntering Lesk Technique\n");
        BabelNetResource.SearchObject selected = null;
        int max = -1;
        for(BabelNetResource.SearchObject so : context_1) {
            System.out.println(so.toString());
            System.out.println(">SearchObject " + so.getSynset().getMainSense() + " context: " + so.getBgw());
            System.out.println(">Ontology Context: " + context_2);
            int test = intersection(so.getBgw(), context_2);
            System.out.println(">Intersection: " + test);
            if (test > max) {
                selected = so;
                max = test;
            }
            System.out.println("\n");
        }
        System.out.println("\nExiting Lesk Technique\n");
        return selected;
    }


    /**
     * Overlapping between two lists
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
