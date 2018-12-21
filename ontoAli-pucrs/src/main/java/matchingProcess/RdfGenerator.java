package matchingProcess;

import objects.Ontology;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Class responsible for generating the .rdf file that concretely represents the alignment
 * produced by this software tool
 */

public class RdfGenerator {


//Attributes

    private FileWriter fileWriter;      //Class necessary to realize the file writing
    private PrintWriter printWriter;    //Class that also used to file write that contains some additional methods


//Constructor

    public RdfGenerator(String localfile) {
        try {
            this.fileWriter = new FileWriter(localfile);
            this.printWriter = new PrintWriter(fileWriter);
        }
        catch(IOException e) {
            System.out.println("[ERROR]: Cannot operate over out file path!\n");
            e.printStackTrace();
        }
    }


//Log methods

    private void outLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - RDF file generated!" );
    }


//Methods


    /**
     * This method generates a .rdf file header, which references the ontologies
     * to be aligned (in this case, a pair of different-level ontologies)
     * @param onto1 The first ontology of the matching process (commonly domain-level)
     * @param onto2 The second ontology of the matching process (commonly top-level)
     */

    public void generateHeader(Ontology onto1, Ontology onto2) {

            printWriter.print("<?xml version='1.0' encoding='utf-8' standalone='no'?>\n" +
                        "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'\n" +
                        "\t\t xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
                        "\t\t xmlns:xsd='http://www.w3.org/2001/XMLSchema#'\n" +
                        "\t\t xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'>\n");

            printWriter.print("<Alignment>\n" +
                        "\t<xml>yes</xml>\n" +
                        "\t<level>0</level>\n" +
                        "\t<type>11</type>\n");

            printWriter.print("\t<onto1>\n" + "\t\t<Ontology rdf:about=" + '"' + onto2.getOntologyID().getOntologyIRI().toString() + '"' + ">\n" +
                        "\t\t\t<location>file:" + onto2.getFileName() + "</location>\n" +
                        "\t\t</Ontology>\n" + "\t</onto1>\n");

            printWriter.print("\t<onto2>\n" + "\t\t<Ontology rdf:about=" + '"' + onto1.getOntologyID().getOntologyIRI().toString() + '"' + ">\n" +
                        "\t\t\t<location>file:" + onto1.getFileName() + "</location>\n" +
                        "\t\t</Ontology>\n" + "\t</onto2>\n");

    }


    /**
     * Turns the mapping class into a string to write it in the out rdf file
     * @param ontologyMapping A mapping class instance that represents the alignment of
     * a pair of OWLClasses (concepts), one as the target (top-level) and the other as
     * source (domain-level)
     */

    private String generateMapping(Mapping ontologyMapping){
        String out = "\t<map>\n" +
                "\t\t<Cell>\n" +
                "\t\t\t<entity1 rdf:resource='"+ ontologyMapping.getTarget() +"'/>\n" +
                "\t\t\t<entity2 rdf:resource='"+ ontologyMapping.getSource() +"'/>\n" +
                "\t\t\t<relation>" + ontologyMapping.getRelation() + "</relation>\n" +
                "\t\t\t<measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>"+ ontologyMapping.getMeasure() +"</measure>\n" +
                "\t\t</Cell>\n" + "\t</map>\n";
        return out;
    }

    /**
     * This method generates in the .rdf out file every mapping found by
     * the matching process in order to visualize all possible alignments
     * between the two sets of concepts represented by a pair of different-level ontologies
     * @param mappings A list of all possible matches, each of them built as a mapping instance
     */
    public void mapEverything(List<Mapping>mappings){
        try {
            for (Mapping m : mappings) {
                if (!m.getMeasure().equals("false")) {
                    printWriter.print(generateMapping(m));
                }
            }

            printWriter.print("</Alignment>\n" + "</rdf:RDF>");
            fileWriter.close();
            outLog();
        }catch (IOException e){
            System.out.println("[ERROR]: Error occured while mapping ontologies in rdf file generation!\n");
            e.printStackTrace();
        }
    }

}
