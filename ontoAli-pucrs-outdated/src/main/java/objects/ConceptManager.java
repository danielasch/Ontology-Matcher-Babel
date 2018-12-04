package objects;

import java.util.List;
import java.util.Set;

import it.uniroma1.lcl.babelnet.BabelSynset;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import resources.BabelNetResource;
import resources.Utilities;

/*
 * This class is used to bring the object Concept to the local package
 */
public class ConceptManager {

    //public methods
    /*
     *These methods calls protected methods from the Concept class.
     *This way, enabling modify the Concept class outside the Objects package.
     */
    public void config_owlOntology(Concept cnp, OWLOntology onto) {
        cnp.set_owlOntology(onto);
    }

    public void config_owlClass(Concept cnp, OWLClass cls) {
        cnp.set_owlClass(cls);
    }

    public void config_classId(Concept cnp, String id) {
        cnp.set_classID(id);
    }

    public void config_className(Concept cnp, String name) {
        cnp.set_className(name);
    }

    public void config_description(Concept cnp, String desc) {
        cnp.set_desc(desc);
    }

    public void config_context(Concept cnp, Set<String> cntxt) {
        cnp.set_context(cntxt);
    }

    public void config_supers(Concept cnp, List<OWLClassExpression> supers) {
        cnp.set_supers(supers);
    }

    public void config_subs(Concept cnp, List<OWLClassExpression> subs) {
        cnp.set_subs(subs);
    }

    public String conceptName_wn(Concept cnp) {
        return cnp.sp_conceptName();
    }

    public void config_synset(Concept cnp, BabelNetResource.SearchObject synset) {
        cnp.set_goodSynset(synset);
    }

    public void config_aliClass(Concept cnp, OWLClass cls) {
        cnp.set_aliClass(cls);
    }

    public void config_utilities(Concept cnp, Utilities ut) {
        cnp.set_utilities(ut);
    }

    public void config_object(Concept cnp, Object obj) {
        cnp.set_obj(obj);
    }
}
