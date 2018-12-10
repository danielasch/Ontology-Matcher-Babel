/****************************************************
*	Top-match is a tool to align top and domain     *
* ontologies using BabelNet.                        *
*****************************************************
* 	This is the main class that calls specialized   *
* classes to generate the alignment.                *
*                                                   *
*                                                   *
* @authors Henrique Kops & Rafael Basso              *
****************************************************/
package conceptExtraction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import matchingProcess.RdfGenerator;
import org.apache.commons.io.output.TeeOutputStream;

import matchingProcess.Matching;
import matchingProcess.MatchingWE;
import objects.Concept;
import objects.Ontology;
import resources.BaseResource;
import resources.Evaluator;
import resources.OutFiles;
import synsetSelection.SynsetDisambiguation;
import synsetSelection.SynsetDisambiguationWE;

/**
 * Main class which instantiates and calls all necessary classes
 * to execute the matching process
 */
public class Main {

/**
 * Main method
 * arg 0 = path to the domain onto
 * arg 1 = path to out .rdf (match)
 * arg 2 = string selection for "dolce" or "sumo"
 * arg 3 = integer representing the technique to be used
 * 		if case 2 -> 2:model [model = google or glove]
 * arg 4 = integer representing hole ontology as context [0 : no, 1 : yes]
 * arg 5 = option parameter representing the path to the referece alignment
*/

	/**
	 * Selects the course of execution through argued preferences
	 * */

	public static void main(String[] args) {
		long start = sTime();

		verify(args);

		String model = spModel(args);

		int tec = Integer.parseInt(args[3]);

		switch(tec) {
			case 1:
				context(args);
				break;
			case 2:
				wordEmbedding(args, model);
				break;
			default:
				errorMessageDeault();
				break;
		}
		fTime(start);
	}

	/**
	 * Context technique which overlaps between the concept's context
	 * and the recovered babelnet synset bag of words
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
				match(domain, upperD, args[1], listDom, listUp);
				out(args[1], listDom);
				evaluate(args);
				break;
			case "sumo":
				Ontology upperS = new Ontology("resources/sumo.owl");
				listDom = domain(domain,context);
				listUp = sumo(upperS);
				disamb(listDom);
				match(domain, upperS, args[1], listDom, listUp);
				out(args[1], listDom);
				evaluate(args);
				break;
			default:
				System.out.println("Invalid Upper Ontology selection! Choose SUMO, or DOLCE!");
				break;
		}
	}


	private static void wordEmbedding(String[] args, String model) {
		String topOnto = args[2].toLowerCase();
		int context = Integer.parseInt(args[4]);
		List<Concept> listDom;
		List<Concept> listUp;
		
		Ontology domain = new Ontology(args[0]);
		switch(topOnto) {
			case "dolce":
				Ontology upperD = new Ontology("resources/DUL.owl");
				listDom = domain(domain,context);
				listUp = dolce(upperD);
				disambWE(listDom, model);
				match(domain, upperD, args[1], listDom, listUp);
				//outWNWE(args[1], listDom);
				evaluate(args);
				break;
			case "sumo":
				Ontology upperS = new Ontology("resources/sumo.owl");
				listDom = domain(domain,context);
				listUp = sumo(upperS);
				disambWE(listDom, model);
				match(domain, upperS, args[1], listDom, listUp);
				//outWNWE(args[1], listDom);
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
		SynsetDisambiguation disam = new SynsetDisambiguation(base);
		disam.disambiguation(listDom);
	}


	/**
	 * Method that generates the .txt and .rdf files
	 */

	private static void out(String outPath, List<Concept> listDom) {
		OutFiles out = new OutFiles(outPath);
		out.outFile(listDom);
	}

//WordNet technique related methods

	private static void disambWE(List<Concept> listDom, String model) {
		BaseResource base = new BaseResource(model);
		ContextProcessing proc = new ContextProcessing(base);
		proc.process(listDom);
		SynsetDisambiguationWE disam = new SynsetDisambiguationWE(base);
		disam.disambiguation(listDom);
	}

	/*
	private static List<Concept> dolceT(Ontology upperS) {
		List<Concept> listUp = new ArrayList<Concept>();

		ContextExtraction exct = new ContextExtraction();
		listUp = exct.extractUpperWE(upperS.getOntology());
		return listUp;
	}

	private static List<Concept> sumoT(Ontology upperS) {
		List<Concept> listUp = new ArrayList<Concept>();

		ContextExtraction exct = new ContextExtraction();
		listUp = exct.extractUpperWE(upperS.getOntology());
		return listUp;
	}


	private static void matchWE(Ontology domain, Ontology upper, String outPath, List<Concept> listDom, List<Concept> listUp, String model) {
		BaseResource base = new BaseResource(model);
		ContextProcessing proc = new ContextProcessing(base);
		proc.procWE(listDom);
		proc.procWE(listUp);

		MatchingWE match = new MatchingWE(outPath, base);
		match.matchInv(listDom, listUp);
		match.outRdf(domain, upper);
	}


	private static void outWE(String outPath, List<Concept> listDom) {
		OutFiles out = new OutFiles(outPath);
		//out.out_file_we(listDom);
	}
	*/

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
	private static void match(Ontology dom, Ontology up, String outPath, List<Concept>listDom, List<Concept>listUp){
		Matching match = new Matching();
		RdfGenerator gen = new RdfGenerator(outPath);
		gen.generateHeader(dom, up);
		gen.mapEverything(match.matchBabel(listDom, listUp));

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


	private static String spModel(String[] args) {
		if(args[3].contains(":")) {
			int aux = args[3].indexOf(":");
			String model = args[3].substring(aux+1);
			args[3] = args[3].substring(0, aux);
			return model;
		} else {
			return "";
		}
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
				"1º) domain ontology path\n" +
				"2º) out file path\n" +
				"3º) top ontology selection [sumo or dolce]\n" +
				"4º) technic selection [1, 2, 3, 4 or 5 - the numbers correspond to a certain technic]\n" +
				"\t 1 - Overlapping\n" +
				"\t 2 - WordEmbeddings\n" +
				"\t 3 - Resnik\n" +
				"\t 4 - Lin\n" +
				"\t 5 - Wup\n" +
				"5º) reference alignment path [optional]");
	}
				
}
