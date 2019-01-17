package conceptExtraction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import objects.Concept;
import objects.ConceptManager;
import resources.BaseResource;
import resources.StanfordLemmatizer;

/**
 * This class process the context of a concept
 */
public class ContextProcessing {

//Attributes

	private BaseResource base;		//BaseResource contains the necessary resources to execute the context process


//Constructor

	public ContextProcessing(BaseResource _base) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Context process selected!" );
		this.base = _base;
	}


//Log methods

	private void init_log() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Processing the context..." );
	}
	private void final_log() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Context processed!" );
	}


//Methods

	/**
	 * Initiating the context processing
	 */
	protected void procWE(List<Concept> listUp) {
		init_log();
		
		for(Concept cnp: listUp) {
			processManager(cnp);
		}
		
		final_log();
	}

	/**
	 * Manager of the description processing
	 */
	private void processManager(Concept cnp) {
		ConceptManager cMan = new ConceptManager();
		List<String> list;
		Set<String> set;
		list = contextTokenizerManager(cnp.getConceptContext());
		list = contextRmStopWords(list);
		list = separateElementsByHashtag(list);
		set = cleanSet(list);
		set = addWithoutNumbers(set);
		cMan.configContext(cnp, set);
	}

	/**
	 * Manage the tokenizing of a parametrized concept
	 */
	private List<String> contextTokenizerManager(Set<String> ctx) {
		List<String> list = new ArrayList<>();
		List<String> aux = new ArrayList<>();

		for(String el: ctx) { contextTokenizer(el, " ", aux); }

		for(String el: aux) { contextTokenizer(el, "\n", list); }

		aux.clear();

		return list;
	}

	/**
	 * Tokenizing all the context of a concept by separators and putting its contents on a list
	 */
	private void contextTokenizer(String el, String sp, List<String> list) {

		StringTokenizer st = new StringTokenizer(el, sp);
		while (st.hasMoreTokens()) {
 		   	String token = st.nextToken();

 		   	if (!list.contains(token)) {
 		   		list.add(token);
 		   	}
		}
	}

	/**
	 * Removing the stop words of a tokenized context
	 */
	private List<String> contextRmStopWords(List<String> tokenizedContext) {
		List<String> list = new ArrayList<>();

		for (String word : tokenizedContext) {
			String str;
			str = removeSpecialChar(word);
			contextTokenizer(str, " ", list);
		}

		tokenizedContext.clear();
		return list;
	}


	/**
	 * Removes some unwanted chars from parametrized string
	 */
	private String removeSpecialChar(String word) {
		char x = '"';
		String asp = String.valueOf(x);
		if(!isSite(word)) {
			word = word.replace(".", "").replace(",", "").replace("'s", "").replace(asp, "").replace(")", "").replace("(", " ").replace(";", "").replace("&", "").replace("'", "").replace("?", "").replace("!", "").replace("%", "").replace(":", "");
		} else {
			word = word.replace(",", "").replace("'s", "").replace(asp, "").replace(")", "").replace("(", "").replace("'", "").replace("?", "").replace("!", "");
		}
		return word;
	}

	/**
	 * Checks if a string represents a site
	 */
	private boolean isSite(String word) {
		if(word.contains("http:")) {
			return true;
		}
		return false;
	}

	/**
	 * Splitting all context elements that contains # symbol
	 */
	private List<String> separateElementsByHashtag(List<String> cleanContext) {
		List<String> list = new ArrayList<>();

		for (String word : cleanContext) {

			String str;

			if(!isSite(word)) {
				str = putCharSequence(word);
				contextTokenizer(str, "#", list);
			}

			else {
				str = word;
				contextTokenizer(str, "#", list);
			}

		}
		cleanContext.clear();
		return list;
	}

	/**
	 * Ordering tokenizing context elements
	 */
	private String putCharSequence(String contextElement) {
		String str = "";
		int sIndex = 0;

		for(int i = 0; i < contextElement.length(); i++) {
			char ch = contextElement.charAt(i);
			
			if(Character.isUpperCase(ch) && i != contextElement.length()-1) {
				char next = contextElement.charAt(i+1);
				
				if(i!=0 && Character.isLowerCase(next)) {
					char prev = contextElement.charAt(i-1);

					if(prev != '#') {
						str = str + contextElement.substring(sIndex, i) + "#";
						sIndex = i;
					}
				}
			}

			else if(!Character.isLetterOrDigit(ch)) {
				contextElement = contextElement.replace("-", "#").replace("/", "#");
			}
		}

		str = str + contextElement.substring(sIndex);
		return str;
	}

	/**
	 * Cleaning set taking of the stop words and lemmatizing all of its contents
	 */
	private Set<String> cleanSet(List<String> aux) {
		List<String> list;
		Set<String> set;
		list = addWithoutStopWords(aux);
		set = lemmatizeList(list);
		aux.clear();
		return set;
	}

	/**
	 * Creating lists without stop words
	 */
	private List<String> addWithoutStopWords(List<String> aux) {
		List<String> stpWords = this.base.getStpWords();
		List<String> list = new ArrayList<>();

		for(String word: aux) {
			String wordLow = word.toLowerCase();

			if(!(stpWords.contains(wordLow))) {
				list.add(wordLow);
			}  
		}
		return list;
	}

	/**
	 * Lemmatizing strings of a list
	 */
	private Set<String> lemmatizeList(List<String> aux) {
		List<String> lemma;
		StanfordLemmatizer slem = this.base.getLemmatizer();
		String toLemma = slem.toLemmatize(aux);
		lemma = slem.lemmatize(toLemma);
		aux.clear();
		return slem.toSet(lemma);
	}

	/**
	 * Creating sets with strings without numbers
	 */
	private Set<String> addWithoutNumbers(Set<String> aux) {
		Set<String> set = new HashSet<>();
		for(String word: aux) {
			String str = removeNumbers(word);
			if(!str.equals("")) {
				set.add(str);
			}
		}
		aux.clear();
		return set;
	}

	/**
	 * Removes all numbers found in string
	 */
	private String removeNumbers(String word) {
		word = word.replaceAll("[*0-9]", "");
		return word;
	}


	/**
	 * This method processes the context
	 */
	protected void process(List<Concept> listCon) {
		ConceptManager man = new ConceptManager();
		init_log();

		for(Concept concept: listCon) {

			Set<String> context = init(concept.getConceptContext());
			man.configContext(concept, context);
		}
		final_log();
	}


	/**
	 * Split process
	 */

	private Set<String> init(Set<String> context) {
		Set<String> aux = spString(context);		//Split strings, turning all elements of the context into tokens
		Set<String> temp = new HashSet<String>();		//Split strings with upperCase avoiding tokens separated by
													// upperCase that were in the description and were not separated
		for(String word: aux) {

			if(word.length() != 0) {

				if(hasUpperCase(word)) {

					String tempStr = base.getLemmatizer().rmSpecialChar(word);
					temp.addAll(spUpperCase(tempStr));

				} else {

					String tempStr = base.getLemmatizer().rmSpecialChar(word);
					temp.add(tempStr.toLowerCase());
				}
			}
		}

		temp = base.getLemmatizer().rmStopWords(temp);
		temp = lemmatizer(temp);
		temp = base.getLemmatizer().rmStopWords(temp);

		return temp;
	}


	/**
	 * Lemmatizes all elements of a set
	 */

	private Set<String> lemmatizer(Set<String> context) {
		List<String> lemma = new ArrayList<String>();
		StanfordLemmatizer slem = this.base.getLemmatizer();
		String toLemma = slem.toLemmatize(slem.toList(context));
		lemma = slem.lemmatize(toLemma);
		return slem.toSet(lemma);
	}


	/**
	 * Split strings separated with "_", " ", "�" and upperCase
	 */

	private Set<String> spString(Set<String> context) {
		Set<String> temp= new HashSet<String>();

		for(String str: context) {
			String[] split = null;
			if(str.contains("_")) {
				split = str.split("_");
				for(String strSplit: split) {
					temp.add(strSplit.toLowerCase());
				}
			} else if(hasWhiteSpace(str)) {
				split = str.split(" |�");
				for(String strSplit: split) {
					temp.add(strSplit);
				}
			} else if(hasUpperCase(str)) {
				temp.addAll(spUpperCase(str));
			} else {
				temp.add(str);
			}	
		}
		context.clear();
		context = temp;
		return context;
	}


	/**
	 * Splits strings separated by upperCase
	 */

	private Set<String> spUpperCase(String wordComp) {
		Set<String> sep = new HashSet<String>();
		int x = wordComp.length();
		int up, aux = 0;

		for(int y = 1; y < x; y++) {
			if(Character.isUpperCase(wordComp.charAt(y))) {
				up = y;
				sep.add(wordComp.substring(aux, up).toLowerCase());
				aux = up;
			}	
		}
		sep.add(wordComp.substring(aux).toLowerCase());
		return sep;
	}


//Auxiliary methods


	/**
	 * Verifies if a string is separated by space
	 */


	private boolean hasWhiteSpace(String str) {
		int length = str.length();

		for(int y = 1; y < length; y++) {
			if(Character.isWhitespace(str.charAt(y))) {
				return true;
			}	
		}
		return false;	
	}


	/**
	 * Verifies if a string is separated by UpperCase
	 */

	private boolean hasUpperCase(String word) {
		int x = word.length();
		if(!hasConsecutiveUpperCase(word)) {
			for(int y = 1; y < x; y++) {
				if(Character.isUpperCase(word.charAt(y))) {
					return true;
				}	
			}
		}
		return false;	
	}

	/**
	 * Checks if a string contains consecutive upper cased chars
	 */

	private boolean hasConsecutiveUpperCase(String word) {
		int cont = 0;
		int size = word.length();

		if(size != 0) {
			for(int y = 0; y < word.length(); y++) {
				if(Character.isUpperCase(word.charAt(y))) {
					cont++;
				}	
			}
			float aux = (cont / size) * 10;
			if(aux > 6) {
				return true;
			} 
		}
		return false;
	}


}
