package resources;


import com.babelscape.util.UniversalPOS;
import conceptExtraction.ContextProcessing;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;

import javax.xml.bind.Element;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//BabelNet resource class

public class BabelNetResource {

    /**
    *Public object related to babelnet's search
    *which keeps the senses, glosses and context of a given babelsynset
    */
    public class SearchObject implements Comparable<SearchObject>{

    //Attributes

        private List<String> senses;
        private List<String> glosses;
        private Set<String> bgw;
        private int order;
        private BabelSynset bs;


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
                    "\t| Senses: " + this.getSenses();
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


    public List<BabelSynset> getSynset(String target) {
        BabelNetQuery query = new BabelNetQuery.Builder(target).from(Language.EN).POS(UniversalPOS.NOUN).build();
        return bn.getSynsets(query);
    }


//Methods

    /**
     * Babelnet's search method (returns a list
     *of babelsynsets, senses and glosses for a given string target)
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
     * Creator of SearchObjects, that represents the aggregation of recovered synsets,
     * glosses and words that composes the synset's definition
     */

    private SearchObject createSearchObject(BabelSynset bs, int order){
        List<String> senses = lemmatizer(bs.getSenses(Language.EN), true);
        List<String> glosses = lemmatizer(bs.getGlosses(Language.EN), false);
        SearchObject so = new SearchObject(senses, glosses, bs, order);
        return so;
    }

    /**
     * Creates a 'babel bag of words' containing the senses of the current synset
     * and the glosses that represents it, each retrieved from BabelNet database
     */

    public Set<String> babelBow(List<String> senses, List<String> glosses) {
        Set toReturn = new HashSet();
        toReturn.addAll(senses);
        toReturn.addAll(glosses);
        return toReturn;
    }


//Lemmatizers

    /**
     * Hypernym nomenclature lemmatizer, mainly used at
     *the matching process
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
    * Lemmatizer for each senses and glosses retrieved from a BabelSynset
     */

    public List<String> lemmatizer(List<?>elements, boolean type) {

        List<String> lemma = new ArrayList<>();
        List<String> split;
        BabelSense sense;

        for (Object e : elements) {

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
        }

        return lemmatize(lemma);
    }


//Auxiliary lemmatization methods

    /**
     * Auxiliary lemmatizer of which remove stop words, duplications
     *and return all tokens to its "root" form using a StandfordLemmatizer instance
     */

    private List<String> lemmatize(List<String>context) {
        Set<String> set = new HashSet<>();
        List<String>toReturn = new ArrayList<>();
        String contextCast = this.bs.getLemmatizer().toLemmatize(context);
        context = bs.getLemmatizer().lemmatize(contextCast);
        set.addAll(context);
        set = bs.getLemmatizer().rmStopWords(set);
        toReturn.addAll(set);
        set.clear();
        return toReturn;
    }

    /**
     *Method that returns if a word is only composed by
     *words. If it is not, returns false
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