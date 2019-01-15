package resources;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * This class makes the evaluation between the alignment generated and the reference alignment
 */
public class Evaluator {
	
//Attributes
	
	String referenceAlignment;		//String representing the path to the reference alignment
	String alignment;				//String representing the path to the generated alignment
	PRecEvaluator evaluator;		//Special class that evaluates the condition of an alignment (.rdf file)
									// based on another


//Constructor
	
	public Evaluator(String referenceAlignment, String alignment) {
		setAlignment(alignment);
		setReference(referenceAlignment);
	}


//Log methods
	
	private void log() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Evaluating the matching..." );
	}


//Getters

    protected PRecEvaluator getEvaluator() {
        return this.evaluator;
    }

	String getReference() {
		return this.referenceAlignment;
	}

    String getAlignment() {
        return this.alignment;
    }


//Setters

    void setReference(String _ref) {
        this.referenceAlignment = _ref;
    }

	void setAlignment(String _alin) {
		this.alignment = _alin;
	}


//Methods
	
	/**
	 * This method makes the evaluation
	 */
	public void evaluate() {
		Alignment ref;
		Alignment alin;

		AlignmentParser ap1 = new AlignmentParser(0);
		AlignmentParser ap2 = new AlignmentParser(0);
		log();
		try {
			ref = ap1.parse(new File(getReference()).toURI() );
			alin = ap2.parse(new File(getAlignment()).toURI());
			Properties p = new Properties();
			PRecEvaluator eva = new PRecEvaluator(ref, alin);
			eva.eval(p);
			System.out.println("REF - ALI:\n" + "F-Measure: " + eva.getFmeasure() + "\nPrecision:  " + eva.getPrecision() + "\nRecall: " + eva.getRecall() + "\nOverall: " + eva.getOverall());
		}
		catch (AlignmentException e) {
			System.out.println("error: Reference-Alignment, or Alignment: format");
			e.printStackTrace();
		}
	}

}
