/****************************************************
*	ontoAli-pucrs is a tool to align top and domain *
* ontologies using BabelNet.                        *
*													*
* @authors Henrique Kops & Rafael Basso             *
****************************************************/
package conceptExtraction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import matchingProcess.RdfGenerator;
import org.apache.commons.io.output.TeeOutputStream;

import matchingProcess.Matching;
import objects.Concept;
import objects.Ontology;
import org.slf4j.LoggerFactory;
import resources.BaseResource;
import resources.Evaluator;
import resources.OutFiles;
import synsetSelection.SynsetDisambiguationLESK;
import synsetSelection.SynsetDisambiguationWE;

/**
 * This is the main class that calls specialized
 * classes to generate the alignment.
 */

public class Main {

/**
 * Main method
 * arg 0 = path to the domain onto
 * arg 1 = path to out .rdf (matchLESK)
 * arg 2 = string selection for "dolce" or "sumo"
 * arg 3 = integer representing the technique to be used
 * 		OBS: if technique = 2 -> 2:model [model = google or glove]
 * arg 4 = integer representing hole ontology as context [0 : no, 1 : yes]
 * arg 5 = option parameter representing the path to the referece alignment
*/

	/**
	 * Selects the course of execution through argued preferences
	 * */
	public static void main(String[] args) {
		long start = sTime();

		Logger logger = ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
		logger.setLevel(Level.ERROR);

		verify(args);

		int tec = getTec(args);

		switch(tec) {
			case 1:
				context(args);
				break;
			case 2:
				wordEmbedding(args);
				break;
			default:
				errorMessageDeault();
				break;
		}
		fTime(start);
	}


	/**
	 * Context technique which overlaps between the concept's context
	 * and the recovered BabelNet synset bag of words looking for the
	 * greatest number of intersections
	 */

	private static void context(String[] args) {
		String topOnto = args[2].toLowerCase();
		int context = Integer.parseInt(args[4]);
		List<Concept> listDom;
		List<Concept> listUp;
		Ontology domain = new Ontology(args[0]);

		switch(topOnto) {
			case "dolce":
				Ontology upperD = new Ontology("resources/DLP_397_Edited.owl");
				listDom = domain(domain,context);
				listUp = dolce(upperD);
				disamb(listDom);
				match(domain, upperD, args[1], listDom, listUp, args);
				out(args[1], listDom, getTec(args));
				evaluate(args);
				break;
			case "sumo":
				Ontology upperS = new Ontology("resources/sumo.owl");
				listDom = domain(domain,context);
				listUp = sumo(upperS);
				disamb(listDom);
				match(domain, upperS, args[1], listDom, listUp, args);
				out(args[1], listDom, getTec(args));
				evaluate(args);
				break;
			default:
				System.out.println("Invalid Upper Ontology selection! Choose SUMO, or DOLCE!");
				break;
		}
	}


	/**
	 * Word embedding technique which overlaps between the concept's context
	 * and the recovered BabelNet synset bag of words using 'word to vector'
	 * parametrized model in order to generate an average of every pair of
	 * contexts, selecting the greatest one
	 */

	private static void wordEmbedding(String[] args) {
		String topOnto = args[2].toLowerCase();
		int context = Integer.parseInt(args[4]);
		List<Concept> listDom;
		List<Concept> listUp;
		Ontology domain = new Ontology(args[0]);

		switch(topOnto) {
			case "dolce":
				Ontology upperD = new Ontology("resources/DLP_397_Edited.owl");
				listDom = domain(domain,context);
				listUp = dolce(upperD);
				disambWE(listDom, getModel(args));
				match(domain, upperD, args[1], listDom, listUp, args);
				out(args[1], listDom, getTec(args));
				evaluate(args);
				break;
			case "sumo":
				Ontology upperS = new Ontology("resources/sumo.owl");
				listDom = domain(domain,context);
				listUp = sumo(upperS);
				disambWE(listDom,getModel(args));
				match(domain, upperS, args[1], listDom, listUp, args);
				out(args[1], listDom, getTec(args));
				evaluate(args);
				break;
			default:
				System.out.println("Invalid Upper Ontology selection! Choose SUMO, or DOLCE!");
				break;
		}
	}


//Context technique related methods


	/**
	 * Context disambiguation method
	 */

	private static void disamb(List<Concept> listDom) {
		BaseResource base = new BaseResource();
		ContextProcessing proc = new ContextProcessing(base);
		proc.process(listDom);
		SynsetDisambiguationLESK disam = new SynsetDisambiguationLESK(base);
		disam.disambiguation(listDom);
	}


//WordNet technique related methods


	private static void disambWE(List<Concept> listDom, String model) {
		BaseResource base = new BaseResource(model);
		ContextProcessing proc = new ContextProcessing(base);
		proc.process(listDom);
		SynsetDisambiguationWE disam = new SynsetDisambiguationWE(base);
		disam.disambiguation(listDom);
	}

//Auxiliary methods


	//Ontology related methods


	/**
	 * Dolce's concept extraction
	 */

	private static List<Concept> dolce(Ontology upper) {
		List<Concept> listUp;
		ContextExtraction exct = new ContextExtraction();
		listUp = exct.extractUpper(upper.getOntology());
		return listUp;
	}


	/**
	 * Sumo's concept extraction
	 */

	private static List<Concept> sumo(Ontology upperS) {
		List<Concept> listUp;
		ContextExtraction exct = new ContextExtraction();
		listUp = exct.extractUpper(upperS.getOntology());
		return listUp;
	}


	/**
	 * Domain ontology concept extraction(using argued path)
	 */

	private static List<Concept> domain(Ontology domain, int context) {
		List<Concept> listDom;
		ContextExtraction exct = new ContextExtraction();
		if(context == 0) {
			listDom = exct.extract(domain.getOntology());
		}
		else listDom = exct.extractWithContext(domain.getOntology());
		return listDom;
	}


	/**
	 * BabelNet matching through hypernym search method
	 */
	private static void match(Ontology dom, Ontology up, String outPath, List<Concept>listDom, List<Concept>listUp, String[]args){
		int tec = getTec(args);
		RdfGenerator gen = new RdfGenerator(outPath);
		gen.generateHeader(dom, up);
		Matching match = null;
		if (tec == 1) match = new Matching();
		else if (tec == 2)  match = new Matching(getModel(args));
		gen.mapEverything(match.matchBabel(listDom, listUp, tec));

	}


	/**
	 * Method that calls the evaluator of the .rdf files
	 * (comparison between an argued reference alignment
	 * and the out file generated)
	 */
	private static void evaluate(String[] args) {
		if(args.length == 6) {
			Evaluator eva = new Evaluator(args[5], args[1]); //REF, ALI
			eva.evaluate();
		}
	}

	/**
	 * Method that generates the .txt and .rdf files
	 */

	private static void out(String outPath, List<Concept> listDom, int tec) {
		OutFiles out = new OutFiles(outPath);
		out.outFile(listDom, tec);
	}


	//Time related methods


	private static long sTime() {
		long start = System.nanoTime();
		return start;
	}
	
	private static void fTime(long start) {
		long end = System.nanoTime();
		end = end - start;
		end = end / 1000000000;
		
		minute(end);
	}
	private static void minute(long time) {
		int aux = 0;
		long sec = 0;
		boolean test = true;
		while(test) {
			time = time - 60;
			aux++;
			if(time < 0) {
				sec = time + 60;
				test = false;
				aux--;
			}
		}
		System.out.println("Execution time: " + aux + ":" + sec);
	}


	//Arguments verification related methods


	/**
	 * Verifies if the arguments argued are in the
	 * right condition to execute the program
	 */
	private static void verify(String[] args) {
		
		if(args[0].contains("\\")) {
			//args[0] = args[0].replaceAll("\\", "/");
		}
		if(!args[0].endsWith(".owl")) {
			args[0] = args[0].concat(".owl");
		}
		
		if(args[1].contains("\\")) {
			//args[1] = args[1].replaceAll("\\", "/");
		}
		
		if(!args[1].endsWith("/")) {
			args[1] = args[1].concat("/");
		}
		args[1] = verifyRdf(args);
	}
	
	private static String verifyRdf(String[] args) {
		String outFile = args[1];
		String outFileLog = args[1];
		
		int sIndex = args[0].lastIndexOf("/");
		int eIndex = args[0].lastIndexOf(".");
		String aux = args[0].substring(sIndex + 1, eIndex);

		outFile = outFile.concat(aux + "-" + args[2]);
		outFileLog = outFileLog.concat("out-" + aux + "-" + args[2]);
		
		if(args[3].contains("2")) {
			outFile = outFile.concat("-WE.rdf");
			outFileLog = outFileLog.concat("-WE.txt");
		} else if(args[3].contains("6")) {
			outFile = outFile.concat("-noWN-WE.rdf");
			outFileLog = outFileLog.concat("-noWN-WE.txt");
		} else {
			outFile = outFile.concat(".rdf");
			outFileLog = outFileLog.concat(".txt");
		}
		
		outputStream(outFileLog);	//LOG PATH + FILE NAME
		return outFile; 	//RDF PATH + FILE NAME
	}


	//Other methods


	private static String getModel(String[]args) {
		if(args[3].contains(":")) {
			int aux = args[3].indexOf(":");
			String model = args[3].substring(aux+1);
			return model;
		}
		return "";
	}

	private static int getTec(String[]args){
		if(args[3].contains(":")) {
			int aux = args[3].indexOf(":");
			return Integer.parseInt(args[3].substring(0, aux));
		}
		return Integer.parseInt(args[3]);
	}


	private static void outputStream(String outFileLog) {
		try {
			FileOutputStream fos = new FileOutputStream(outFileLog);
			try {
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			TeeOutputStream tos = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(tos);
			System.setOut(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	private static void errorMessageDeault(){
		System.out.println("Invalid arguments order, please try:\n" +
				"1st) Domain ontology path\n" +
				"2nd) Out file path\n" +
				"3rd) Top ontology selection [sumo or dolce]\n" +
				"4th) Technic selection [1 or 2- the numbers correspond to a certain technic]\n" +
						"\t 1 - Overlapping [LESK]\n" +
						"\t 2 - WordEmbeddings  [Word2Vector] - Notation 2:model, where model = google or glove\n" +
				"5th) Usage of hole ontology context with the concept context\n" +
						"\t 0 - No\n" +
						"\t 1 - Yes\n" +
				"6th) reference alignment path [optional]");

	}
				
}
