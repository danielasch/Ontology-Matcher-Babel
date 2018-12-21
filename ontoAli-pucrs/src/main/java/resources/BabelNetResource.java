package resources;


import com.babelscape.util.UniversalPOS;
import edu.umd.cs.findbugs.annotations.NoWarning;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class responsible for searching, filtering, lemmatizing
 * and grouping every synset and context related to BabelNet
 */

public class BabelNetResource {

    /**
    * Public object related to babelnet's search
    * which groups the senses, glosses and context of a given babelsynset
    */
    public class SearchObject implements Comparable<SearchObject>{

    //Attributes

        private List<String> senses;        //Synonyms and key words that represents the current synset
        private List<String> glosses;       //Little explanations about the current synset
        private Set<String> bgw;            //A 'bag' (representation of a set) that contains the union of both glosses and senses
        private int order;                  //An integer to track the index of this object in a tree set
        private BabelSynset bs;             //The BabelSynset represented by this object


        //Constructor

        public SearchObject(List<String> senses, List<String> glosses, BabelSynset bs, int order) {
            this.senses =senses;
            this.glosses = glosses;
            this.order = order;
            this.bs = bs;
            setBgw(babelBow(this.senses, this.glosses));
        }


    //Getters

        public List<String> getSenses() {
            return senses;
        }

        public List<String> getGlosses() { return glosses; }

        public Set<String> getBgw() { return bgw; }

        public BabelSynset getSynset() {
            return bs;
        }


    //Setters

        public void setBgw(Set<String> bgw){ this.bgw = bgw; }


    //Methods

        public int compareTo(SearchObject Other){
            return (this.order-Other.order);
        }


        public String toString(){
            return ">Search Object ( id - " + this.bs.getID() + "):\n" +
                    "\t| Nomenclature: " + this.bs.getMainSense() + "\n" +
                    "\t| Glosses: " + this.glosses + "\n" +
                    "\t| Senses: " + this.getSenses() + "\n";
        }
    }


//Attributes

    private BabelNet bn;
    private BaseResource bs;


//Constructor

    public BabelNetResource() {
        this.bn = BabelNet.getInstance();
        this.bs = new BaseResource();
    }


//Getters

    /**
     * This method recover all hypernyms related to the parametrized synset,
     * only used at the matching process.
     * @param bs Synset that points to its most broad synsets
     * @param hypernyms An empty set, which is also returned by this method, containing all the recovered synsets
     * @param searched A list containing the last hypernyms recovered from other searches
     * @return A set containing the recovered hypernyms of the parametrized synset
     */

    public Set<SearchObject> getHypernyms(BabelSynset bs, Set<SearchObject> hypernyms, List<BabelSynset>searched) {
        int order = 0;
        hypernyms.clear();

        List<BabelSynsetRelation> lbsr = bs.getOutgoingEdges(BabelPointer.ANY_HYPERNYM);

        if(lbsr == null || lbsr.size() == 0) { return hypernyms; }

        for (BabelSynsetRelation bsr : lbsr) {

            BabelSynsetID bsid = bsr.getBabelSynsetIDTarget();
            BabelSynset basy = bn.getSynset(bsid);
            SearchObject so = createSearchObject(basy, order);
            order++;

            if(basy.getID() != bs.getID()) {
                if(!searched.contains(basy)) {
                    hypernyms.add(so);
                }
            }
        }
        lbsr.clear();

        return hypernyms;
    }

    /**
     * Method that returns a list of synsets that may represent the parametrized target String
     * @param target A String representing the concept to be translated into a BabelSynset through BabelNet
     * @return A list of BabelSynsets
     */
    public List<BabelSynset> getSynset(String target) {
        BabelNetQuery query = new BabelNetQuery.Builder(target).from(Language.EN).POS(UniversalPOS.NOUN).build();
        return bn.getSynsets(query);
    }


//Methods

    /**
     * BabelNet search method, returns a set of SearchObjects
     * @param target String representing a ontology concept
     * @return A tree set of SearchedObjects representing the recovered synsets for the parametrized target
     */
    public Set<SearchObject> search(String target) {
        Set<BabelNetResource.SearchObject> searchedObjects = new TreeSet<>(SearchObject::compareTo);
        List<BabelSynset> bsl = getSynset(target);
        int order = 0;
        for (BabelSynset bs : bsl) {
            if (bs != null) {
                searchedObjects.add(createSearchObject(bs, order));
                order++;
            }
        }
        return searchedObjects;
    }

    /**
     * Method that creates a specific SearchObject over the parametrized BabelSynset,
     * which represents the aggregation of the recovered synset,
     * its glosses and key words that composes its definition in BabelNet
     * @param bs A BabelSynset that works as the source for retrieving its senses and glosses
     * @param order The index of the parametrized BabelSynset at the search query
     * @return A SearchObject
     */

    private SearchObject createSearchObject(BabelSynset bs, int order){
        List<String> senses = lemmatizer(bs.getMainSenses(Language.EN), true);
        List<String> glosses = lemmatizer(bs.getGlosses(Language.EN), false);
        SearchObject searchObject = new SearchObject(senses, glosses, bs, order);
        return searchObject;
    }

    /**
     * Creates a 'Babel bag of words' containing the senses of the current synset
     * and the glosses that represents it, each retrieved from BabelNet database
     * @param senses The senses of a synset
     * @param glosses The glosses of the same synset
     * @return A set containing the union of a synset's glosses and senses
     */

    public Set<String> babelBow(List<String> senses, List<String> glosses) {
        Set bagOfWords = new HashSet();
        bagOfWords.addAll(senses);
        bagOfWords.addAll(glosses);
        return bagOfWords;
    }


//Lemmatizers

    /**
     * Hypernym nomenclature lemmatizer, mainly used at the matching process
     * @param hypernym A string representing the main sense of a synset, which,
     * at this point, is seen as a hypernym of another synset
     * @return a 'lemmatized' string (hypernym's 'name')
     */

    public String lemmatizeHypernym(String hypernym) {

        if (hypernym.contains("#n")) {
           hypernym = hypernym.substring(0, hypernym.indexOf("#"));
        }

        if (hypernym.contains(":") && hypernym.contains("]")) {
            hypernym = hypernym.substring(hypernym.lastIndexOf(":") + 1, hypernym.indexOf("]"));
        }

        if (hypernym.contains(":")) {
           hypernym = hypernym.substring(hypernym.lastIndexOf(":") + 1);
        }

        return hypernym;
    }

    /**
     * Lemmatizer for each sense and gloss retrieved from a BabelSynset,
     * necessary for generating a correct bag of words and SearchObject
     * @param elements A list of BabelGLoss/BabelSense, wich contains all
     * tokens of the defined type to be lemmatized
     * @param type A integer representing the 'type' of the list parametrized
     * @return A list containing all 'lemmatized tokens'
     */

    public List<String> lemmatizer(List<?>elements, boolean type) {

        List<String> lemma = new ArrayList<>();
        List<String> split;
        BabelSense sense;
        int limitCounter = 0;

        for (Object e : elements) {

            if(limitCounter > 20) break;

            if(type) {
                sense = (BabelSense) e;
                split = (Stream.of(sense.getSimpleLemma().toLowerCase().split("_")).collect(Collectors.toList()));
            }
            else {
                split = Stream.of(e.toString().toLowerCase().split(" ")).collect(Collectors.toList());
            }

            for(int i = 0; i < split.size(); i++){

                String token = split.get(i);
                split.set(i, bs.getLemmatizer().rmSpecialChar(split.get(i)));

                if (token.contains("-")) {
                    for(String s : token.split("-")) split.add(s);
                    split.remove(token);
                    i--;
                }

                else if(!letterOnly(token)){
                    split.remove(token);
                    i--;
                }

            }

            lemma.addAll(split);
            limitCounter++;
        }

        return lemmatize(lemma);
    }


//Auxiliary lemmatization methods

    /**
     * Auxiliary lemmatizer of which removes stop words, duplications and also
     * convert all tokens to its "root" form using a StandfordLemmatizer instance
     * @param context The synset's 'context', represented as a list o BabelSense/BabelGloss
     * @return A lemmatized context
     */

    private List<String> lemmatize(List<String>context) {
        Set<String> set = new HashSet<>();
        List<String> lemmatized = new ArrayList<>();
        String contextCast = this.bs.getLemmatizer().toLemmatize(context);
        context = bs.getLemmatizer().lemmatize(contextCast);
        set.addAll(context);
        set = bs.getLemmatizer().rmStopWords(set);
        lemmatized.addAll(set);
        set.clear();
        return lemmatized;
    }

    /**
     * Method that returns if a word is only composed by
     * words (if it is not, returns false)
     * @param word A word to be analysed
     * @return The 'state' (boolean) of the parametrized string
     */

    private Boolean letterOnly(String word){
        char [] chars = word.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

}