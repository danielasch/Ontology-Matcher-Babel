package conceptExtraction;

import java.text.SimpleDateFormat;
import java.util.*;


import objects.Ontology;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import objects.Concept;
import objects.ConceptManager;

/**
 * This class extract the information about a concept from the ontology
 */
public class ContextExtraction {

//Constructor

    ContextExtraction() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Context method selected!");
    }

//Log methods

    private void initLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Extracting domain ontology classes...");
    }

    private void finalLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Domain ontology classes Extracted!");
    }

    private void initLogUpper() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Extracting upper ontology classes...");
    }

    private void finalLogUpper() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Upper ontology classes Extracted!");
    }

//Methods

    /**
     * Extracts the concept and its information from domain ontology
     */
    protected List<Concept> extract(OWLOntology onto) {

        initLog();

        List<Concept> listCon = new ArrayList<Concept>();
        ConceptManager man = new ConceptManager();

        for (OWLClass owlClass : onto.getClassesInSignature()) {

            if (!owlClass.isTopEntity()) {

                Concept concept = new Concept();
                Set<String> context = new HashSet<String>();
                List<OWLClassExpression> listSup = new ArrayList<OWLClassExpression>();
                List<OWLClassExpression> listSub = new ArrayList<OWLClassExpression>();
                String desc;

                extractSuperClass(onto, owlClass, context, listSup);

                if (listSup.isEmpty() || verifyThing(listSup)) {

                    manageOntology(man, concept, onto, owlClass);

                    context.add(owlClass.getIRI().getFragment());

                    desc = extractAnnotation(onto, owlClass, context);

                    man.configDescription(concept, desc);

                    extractSubClass(onto, owlClass, context, listSub);

                    man.configContext(concept, context);

                    man.configSupers(concept, listSup);

                    man.configSubs(concept, listSub);

                    listCon.add(concept);
                }
            }
        }
        finalLog();
        return listCon;
    }

    /**
     * Extracts the concept and its information from domain ontology + all ontology concepts
     */
    protected List<Concept> extractWithContext(OWLOntology onto) {
        initLog();

        List<Concept> listCon = new ArrayList<Concept>();
        ConceptManager man = new ConceptManager();
        Set<String> ontology_context = new HashSet<>();

        for (OWLClass owlClass : onto.getClassesInSignature()) {

            if (!owlClass.isTopEntity()) {

                ontology_context.add(owlClass.getIRI().getFragment());

                Concept concept = new Concept();
                Set<String> context = new HashSet<String>();
                List<OWLClassExpression> listSup = new ArrayList<OWLClassExpression>();
                List<OWLClassExpression> listSub = new ArrayList<OWLClassExpression>();

                extractSuperClass(onto, owlClass, null, listSup);

                String desc;

                if (listSup.isEmpty() || verifyThing(listSup)) {

                    manageOntology(man, concept, onto, owlClass);

                    desc = extractAnnotation(onto, owlClass, context);

                    man.configDescription(concept, desc);

                    man.configContext(concept, context);

                    man.configSupers(concept, listSup);

                    man.configSubs(concept, listSub);

                    listCon.add(concept);
                }
            }
        }
        for (Concept c : listCon) {
            c.getConceptContext().addAll(ontology_context);
        }
        finalLog();
        return listCon;
    }


    /**
     * Extracts the concept and its information from top ontology
     */
    protected List<Concept> extractUpper(OWLOntology onto) {
        initLogUpper();

        List<Concept> listCon = new ArrayList<Concept>();
        ConceptManager man = new ConceptManager();

        for (OWLClass owlClass : onto.getClassesInSignature()) {

            Concept concept = new Concept();

            String desc;

            manageOntology(man, concept, onto, owlClass);

            desc = extractAnnotationUpper(onto, owlClass);

            man.configDescription(concept, desc);

            listCon.add(concept);
        }
        finalLogUpper();
        return listCon;
    }

    /**
     * Manage concept class attributes
     */
    private void manageOntology(ConceptManager man, Concept concept, OWLOntology onto, OWLClass owlClass){
        man.configOwlOntology(concept, onto);
        man.configOwlClass(concept, owlClass);
        man.configClassId(concept, owlClass.toString());
        man.configClassName(concept, owlClass.getIRI().getFragment());
    }

    /**
     * Method responsible for aggregating all annotations from the top-level ontology classes
     */
    private String extractAnnotationUpper(OWLOntology onto, OWLClass cls) {
        String annotations = null;

        for (OWLAnnotation anno : cls.getAnnotations(onto)) {
            annotations = anno.getValue().toString();
        }

        return annotations;
    }


    /**
     * Extracts the annotation of the domain ont. concept
     */
    private String extractAnnotation(OWLOntology onto, OWLClass cls, Set<String> cntxt) {

        String desc = null;

        for (OWLAnnotation anno : cls.getAnnotations(onto)) {

            String backup = null;

            if ((anno.getProperty().getIRI().getFragment().equals("comment") && anno != null)       //Get the annotation at comment label
            || (anno.getProperty().getIRI().getFragment().equals("definition") && anno != null)     //Get the annotation at definition label
            || (anno.getProperty().getIRI().getFragment().equals("example") && anno != null)) {     //Get the annotation at example label

                String aux = anno.getValue().toString();
                aux = rmSuffix(aux);
                backup = aux;
                aux = removeSpecialChar(aux);
                cntxt.add(aux);
            }

            //Condition that avoid the apparition of null and repeated elements into the concept description
            if (desc == null && backup != null) {
                desc = backup;
            } else if ((desc != null && backup != null) && !desc.contains(backup)) {
                desc = desc + backup;
            }

        }
        return desc;
    }


    /**
     * Extracts the super classes of a owlClass
     */
    protected void extractSuperClass(OWLOntology onto, OWLClass cls, Set<String> cntxt, List<OWLClassExpression> list) {
        for (OWLClassExpression sup : cls.getSuperClasses(onto)) {
            if (!sup.isAnonymous()) {
                if(cntxt!=null) cntxt.add(sup.asOWLClass().getIRI().getFragment());
                list.add(sup);
                extractSuperRecurClass(onto, sup, cntxt, list);

            }
        }
    }


    /**
     * Recursive call of extractSuperClass
     */
    protected void extractSuperRecurClass(OWLOntology onto, OWLClassExpression su, Set<String> cntxt, List<OWLClassExpression> list) {
        if (su != null) {
            for (OWLClassExpression sup : su.asOWLClass().getSuperClasses(onto)) {
                if (!sup.isAnonymous()) {
                    list.add(sup);
                    if(cntxt!=null) cntxt.add(sup.asOWLClass().getIRI().getFragment());
                    extractSuperRecurClass(onto, sup, cntxt, list);
                }
            }
        }
    }


    /**
     * Extracts the sub classes of a owlClass
     */
    protected void extractSubClass(OWLOntology onto, OWLClass cls, Set<String> cntxt, List<OWLClassExpression> list) {
        for (OWLClassExpression sub : cls.getSubClasses(onto)) {
            if (!sub.isAnonymous() && cntxt != null) {
                cntxt.add(sub.asOWLClass().getIRI().getFragment());
                list.add(sub);

                extractSubRecurClass(onto, sub, cntxt, list);

            }
        }
    }


    /**
     * Recursive call of extractSubClass
     */
    protected void extractSubRecurClass(OWLOntology onto, OWLClassExpression su, Set<String> cntxt, List<OWLClassExpression> list) {
        if (su != null) {
            for (OWLClassExpression sub : su.asOWLClass().getSubClasses(onto)) {
                if (!sub.isAnonymous()) {
                    list.add(sub);
                    cntxt.add(sub.asOWLClass().getIRI().getFragment().toString());

                    extractSubRecurClass(onto, sub, cntxt, list);
                }
            }
        }
    }


//Auxiliary methods 

    /**
     * Removes the suffix of the annotation
     */
    private String rmSuffix(String aux) {
        if (aux.endsWith("^^xsd:string")) {
            aux = aux.replace("^^xsd:string", "");
        }
        if (aux.endsWith("@en")) {
            aux = aux.replace("@en", "");
        }

        return aux;
    }


    /**
     * Verifies if the list contains owl:Thing
     */
    private boolean verifyThing(List<OWLClassExpression> list) {
        if (list.get(0).asOWLClass().isTopEntity()) {
            return true;
        }
        return false;
    }


    /**
     * Removes some chars of a string
     */
    private String removeSpecialChar(String word) {

        if (!isSite(word)) {
            String aux = word;
            char x = '"';
            String z = String.valueOf(x);

            if (aux.contains("  ")) {
                aux = aux.replaceAll("  ", " ");
            }

            if (aux.contains(z)) {
                aux = aux.replace(z, "");
            }

            if (aux.contains(".")) {
                aux = aux.replace(".", " ");
            }

            if (aux.contains(",")) {
                aux = aux.replace(",", "");
            }

            if (aux.contains("?")) {
                aux = aux.replace("?", " ");
            }

            if (aux.contains(":")) {
                aux = aux.replace(":", " ");
            }

            if (aux.contains("!")) {
                aux = aux.replace("!", " ");
            }

            if (aux.contains("  ")) {
                aux = aux.replaceAll("  ", " ");
            }
            return aux;
        }
        return word;
    }


    /**
     * Checks if a string is representing a site
     */
    private boolean isSite(String word) {
        if (word.contains("http:")) {
            return true;
        }
        return false;
    }
}
