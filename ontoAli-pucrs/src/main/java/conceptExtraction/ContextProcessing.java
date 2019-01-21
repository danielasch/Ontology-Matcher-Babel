package conceptExtraction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Set<String> aux = splitStringsContainedInSet(context);		//Split strings, turning all elements of the context into tokens
		Set<String> temp = new HashSet<>();							//Split strings with upperCase avoiding tokens separated by
		                                            				//	upperCase that were in the description and were not separated
		for(String word: aux) {

			if(word.length() != 0) {

				if(hasUpperCase(word)) {

					String tempStr = removeSpecialChar(word);
					temp.addAll(splitStringByUpperCase(tempStr));

				} else {

					String tempStr = removeSpecialChar(word);
					temp.add(tempStr.toLowerCase());
				}
			}
		}

		temp = base.getLemmatizer().rmStopWords(temp);
		temp = lemmatizer(temp);
		temp = base.getLemmatizer().rmStopWords(temp);

		return temp;
	}


	//Lemmatizing methods


	/**
	 * Removes some unwanted chars from parametrized string
	 */

	private String removeSpecialChar(String word) {
		char x = '"';

		String asp = String.valueOf(x);

		if(!isSite(word)) word = word.replace(asp, "");

        word = this.base.getLemmatizer().rmSpecialChar(word);

		return word;
	}


    /**
     * Lemmatizes all elements of a set
     */

    private Set<String> lemmatizer(Set<String> context) {
        List<String> lemma;

        StanfordLemmatizer slem = this.base.getLemmatizer();

        String toLemma = slem.toLemmatize(slem.toList(context));

        lemma = slem.lemmatize(toLemma);

        return slem.toSet(lemma);
    }


    //Splitting methods


	/**
	 * Split strings separated with "_", " ", "�" and upperCase
	 */

	private Set<String> splitStringsContainedInSet(Set<String> context) {
		Set<String> temp= new HashSet<>();

		for(String str: context) {
			String[] split;

			if(str.contains("_")) {
				split = str.split("_");

				for(String strSplit: split) {
					temp.add(strSplit.toLowerCase());
				}
			}

			else if(hasWhiteSpace(str)) {
				split = str.split(" |�");

				for(String strSplit: split) {
					temp.add(strSplit);
				}
			}

			else if(hasUpperCase(str)) {
				temp.addAll(splitStringByUpperCase(str));
			}

			else {
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

	private Set<String> splitStringByUpperCase(String wordComp) {
		Set<String> sep = new HashSet<>();
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


    //Verification methods


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

			for(int index = 1; index < x; index++) {

				if(Character.isUpperCase(word.charAt(index))) {
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
