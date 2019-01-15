package resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class contains and initiate the resources used in the process 
 */
public class BaseResource {

//Attributes

	private List<String> stpWords = new ArrayList<String>();		//A list of strings that represents the stop words
	private StanfordLemmatizer slem;						//Lemmatizer resource
	private Word2Vector w2v;								//Word2Vector resource


//Constructors
	
	/**
	 * Default Constructor
	 */
	public BaseResource() {
		initLog();
		rdStpWords();
		this.slem = new StanfordLemmatizer(stpWords);
		this.w2v = null;
	}


	/**
	 * This constructor receive a string representing the
	 * model to be used in the word embedding technique
	 * and instantiates it through the word to vector method
	 */
	public BaseResource(String model) {
		initLog();
		rdStpWords();
		this.slem = new StanfordLemmatizer(this.stpWords);
		this.w2v = new Word2Vector(model);

	}


//Log methods
	
	private void initLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Initializing resources!" );
	}


	private void stpWordsLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Reading Stop Words..." );
	}


//Getters

    public List<String> getStpWords() {
        return this.stpWords;
    }

    public StanfordLemmatizer getLemmatizer() { return this.slem; }

    public Word2Vector getWord2Vec() {
        return this.w2v;
    }


//Methods

	/**
	 * This method reads the stopwords2.text file
	 * and put the lines into a list
	 */
	private void rdStpWords() {
		stpWordsLog();
		try {
			BufferedReader br = new BufferedReader(new FileReader("resources/stopwords2.txt"));
			String line;
			
			while((line = br.readLine()) != null) {
				this.stpWords.add(line.toLowerCase());
			}
			br.close();
		} catch(FileNotFoundException e) {
	    	System.out.println("StopWords file was not found!");
	    	System.out.println("error: " + e);
	    } catch (IOException e) {
	    	System.out.println("I/O operation failed - StopWords file!");
	    	System.out.println("error: " + e);
	    }
	}

}
