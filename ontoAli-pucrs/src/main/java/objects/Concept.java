package objects;

import java.util.*;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import resources.BabelNetResource;
import resources.Utilities;

/**
 * Concept class containing all the information about a OWLClass
 */
public class Concept {

//Attributes 	

	private OWLOntology ontology;
	private OWLClass owlClass;
	private String ontologyID;
	private String classID;
	private String ontologyName;
	private String className;
	private String conseptDesc;
	private Set<String> conceptContext;
	private Utilities ut;
	private Object obj;
	private List<OWLClassExpression> supers;
	private List<OWLClassExpression> subs;
	private BabelNetResource.SearchObject goodSynset;
	private OWLClass aliClass;


//Constructor
	
	public Concept() {
		this.conceptContext = new HashSet<String>();
		this.goodSynset = null;
		this.ut = null;
	}


//Getters
	
	public OWLOntology getOwlOntology() {
		return this.ontology;
	}

	public OWLClass get_owlClass() {
		return owlClass;
	}

	String getOntologyID() {
		return ontologyID;
	}

	public String getClassID() {
		return classID;
	}

	public String getOntologyName() {
		return ontologyName;
	}

	public String getClassName() {
		return className;
	}

	public String getConseptDesc() {
		return conseptDesc;
	}

	public Set<String> getConceptContext() {
		return conceptContext;
	}

    public Utilities getUtilities() {
        return this.ut;
    }

    public List<OWLClassExpression> getSupers() {
        return supers;
    }

    public List<OWLClassExpression> getSubs() {
        return subs;
    }

    public BabelNetResource.SearchObject getGoodSynset() {
        return goodSynset;
    }

    public OWLClass getAliClass() {
        return aliClass;
    }

    public Object getObject() {
        return obj;
    } //this changed to used instead of unused when i've refactored it <<<


//Setters

    protected void setOwlOntology(OWLOntology onto) {
        this.ontology = onto;
    } //HERE

    protected void setOwlClass(OWLClass owlclass) { owlClass = owlclass; }

    void setOntologyID(String _ontologyID) {
        ontologyID = _ontologyID;
    }

    void setClassID(String _classID) {
        classID = _classID;
    }

    void setOntologyName(String _ontologyName) {
        ontologyName = _ontologyName;
    }

    void setClassName(String _className) {
        className = _className;
    }

    protected void setConceptContext(Set<String> set) {
        conceptContext = set;
    }

    protected void setConseptDesc(String _desc) {
        conseptDesc = _desc;
    }

    protected void set_utilities(Utilities ut) {
        this.ut = ut;
    }

	protected void setSupers(List<OWLClassExpression> _supers) {
		supers = _supers;
	}

	protected void setSubs(List<OWLClassExpression> _subs) {
		subs = _subs;
	}

	void setGoodSynset(BabelNetResource.SearchObject _goodSynset) { goodSynset = _goodSynset; }

	void setAliClass(OWLClass _aliclass) {
		aliClass = _aliclass;
	}

	void setObject(Object _obj) {
		obj = _obj;
	}


//Methods

    /**
    *Prints the concept's information
    */
    public void printInfo() {
		
		System.out.println("Concept: " + this.className);
		System.out.println("Description: " + this.conseptDesc);

		if(this.conceptContext != null) {
			String out = "Context: ";
			Iterator<String> iterator = this.conceptContext.iterator();
			while(iterator.hasNext()) {
				String a = iterator.next();
				if(!iterator.hasNext()) {
					out = out + a + ".";
				} else {
					out = out + a + ", "; 
				}
			}
			System.out.println(out + "\n");
		} else {
			System.out.println("Context: null" + "\n");
		}
		
		if(this.goodSynset != null) {
			System.out.println("Synset: " + this.goodSynset.getSynset().toString());
			System.out.println("Gloss: " + this.goodSynset.getGlosses() + "\n");
		} else {
			System.out.println("Synset: null" + "\n");
		}
	}
	
}
