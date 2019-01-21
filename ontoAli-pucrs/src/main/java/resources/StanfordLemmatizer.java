package resources;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import conceptExtraction.ContextProcessing;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class is used to lemmatize strings
 */
public class StanfordLemmatizer {
	
//Attributes
	
	protected StanfordCoreNLP pipeline;		//A pipeline for the lemmatizer
	private List<String> stopWords;			//A list containing strings that have less important meaning


//Constructor
	
	public StanfordLemmatizer(List<String>stopWords) {

	    Properties props;
	    props = new Properties();

	    this.stopWords = stopWords;

	    // Default property to create the lemmatizer 
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    
	    initLog();
	    this.pipeline = new StanfordCoreNLP(props);

	}


//Log methods
	
	private void initLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Initializing Lemmatizer..." );
	}


//Methods
	
	/**
	 * This method lemmatizes a String
	 */
	public List<String> lemmatize(String documentText) {
		List<String> lemmas = new ArrayList<String>();
    	Annotation document = new Annotation(documentText);					// Create an empty Annotation just with the given text
    	pipeline.annotate(document);										// Run all Annotators on this text
    	List<CoreMap> sentences = document.get(SentencesAnnotation.class);	// Iterate over all of the sentences found
    	for(CoreMap sentence: sentences) {
        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {	// Iterate over all tokens in a sentence
            	lemmas.add(token.get(LemmaAnnotation.class));				// Retrieve and add the lemma for each word into the list of lemmas
        	}
    	}
    	return lemmas;
	}

	
	/**
	 * Turn a list into a string, separating the list elements by space
	 */
	public String toLemmatize(List<String> list) {
		String full = "";
		for(String word: list) {
			full = full + rmSpecialChar(word) + " ";
		}
		return full;
	}


	/**
	 * Turn a list into HashSet
	 */
	public HashSet<String> toSet(List<String> list) {
		HashSet<String> set = new HashSet<String>();
		for(String word: list) {
			set.add(word);
		}
		return set;
	}


	/**
	 * Turn a Set into a list
	 */
	public List<String> toList(Set<String> set) {
		List<String> list = new ArrayList<String>();
		for(String word: set) {
			list.add(word);
		}
		return list;
	}

	/**
	 * Removes the stopwords of a set
	 */
	public Set<String> rmStopWords(Set<String> set) {
		Set<String> wordSet = new HashSet<String>();
		for(String word: set) {
			String wordLow = word.toLowerCase();
			if(!(this.stopWords.contains(wordLow))) {
				if( !(word.equals(" ") || word.equals("-") || word.equals("")) ) {
					wordSet.add(wordLow);
				}
			}
		}
		return wordSet;
	}


	/**
	 * Removes some chars from a parametrized string
	 */

	public String rmSpecialChar(String word) {

		if(word.endsWith("-")) word = word.replace("-", "");

		if(word.contains("(")) word = word.replace("(", "");

		if(word.contains(")")) word = word.replace(")", "");

		if(word.contains(",")) word = word.replace(",", "");

		if(word.contains(":")) word = word.replace(":", "");

		if(word.contains("'s")) word = word.replace("'s", "");

		if(word.contains("'")) word = word.replace("'", "");

		if(word.contains("?")) word = word.replace("?", "");

		if(word.contains("!")) word = word.replace("!", "");

		if(word.contains(".")) word = word.replace(".", "");

		if(word.contains(";")) word = word.replace(";", "");

		if(word.contains("`")) word = word.replace("`", "");

		if(word.contains("&")) word = word.replace("&", "");

		if(word.contains("%")) word = word.replace("%", "");

		if(word.contains(("\""))) word = word.replaceAll("\"", "");

		return word;
	}

	/**
	 * This method identify if the concept name is separated by under line, hyphen, UpperCase or
	 * if the concept name is simple, and then it separates the concept name,
	 * returning the last token or the simple term representing it
	 */
	public String spConceptName(String conceptName) {
		String name = null;
		String cnpName = conceptName;

		if(cnpName.contains("_")) {

			String words[];
			words = cnpName.split("_");
			int i = words.length;
			name = words[i - 1];
		}

		else if(cnpName.contains("-")) {

			String words[];
			words = cnpName.split("-");
			int i = words.length;
			name = words[i - 1];
		}

		else if(hasUpperCase(cnpName)) {

			int x = cnpName.length();
			int up = 0;

			for(int y = 1; y < x; y++) {

				if(Character.isUpperCase(cnpName.charAt(y)) && y > up) {

					up = y;
				}
			}

			if(up != 0) {

				name = cnpName.substring(up);
			}
		}

		else {

			name = cnpName;
		}

		return name.toLowerCase();
	}

	/**
	 * This method returns the full name of a concept,
	 * whether it is composed or not
	 */

	public String fullConceptName(String conceptName){
		String name = (conceptName.charAt(0) + "").toLowerCase();
		name += conceptName.substring(1);
		if(name.charAt(name.length()-1) == ' ') name = name.substring(0, name.length()-2);

		if(hasUpperCase(name)){

			int first = 0;
			int last = 0;
			List<String> words = new ArrayList<>();

			for(char c : name.toCharArray()){
				if(Character.isUpperCase(c)){
					if(first != last) {
						words.add(name.substring(first, last));
						first = last;
					}
				}
				last++;
			}
			if(first != name.length()) words.add(name.substring(first,last));
			String ans = "";
			for(String s: words){
				if(words.indexOf(s) == words.size()-1) ans += s;
				else ans += s + "_";
			}
			return ans.toLowerCase();
		}

		name = name.replaceAll("-", "_");
		name = name.replaceAll(" ", "_");

		return name.toLowerCase();
	}

	/**
	 * This methods tests if a string contains upper cased letters
	 */
	private boolean hasUpperCase(String word) {

		int x = word.length();

		for(int y = 1; y < x; y++) {
			if(Character.isUpperCase(word.charAt(y))) {
				return true;
			}
		}
		return false;
	}

}
