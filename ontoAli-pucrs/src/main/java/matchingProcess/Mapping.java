package matchingProcess;

/**
 * This class 'maps' (as a guideline) the alignment
 * between domain-level ontology classes and top-level ontology classes
 */
public class Mapping {
	
//Attributes

	private String source;		//Alignment's source entity (domain-level concept)
	private String target;		//Alignment's target entity (top-level concept)
	private String relation;	//The relation between both source and target
	private String measure;		//Trust measure


//Getters
	
	String getSource(){
		return source;
	}

	String getTarget() {
		return target;
	}

    String getRelation() {
        return relation;
    }

    String getMeasure() {
        return measure;
    }


//Setters

    void setSource(String _source) {
        source = _source;
    }

    void setTarget(String _target) {
        target = _target;
    }

    void setRelation(String _relation) { relation = _relation; }

	void setMeasure(String _measure) { measure = _measure; }
}
