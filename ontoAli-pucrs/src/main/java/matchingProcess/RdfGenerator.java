package matchingProcess;

import objects.Ontology;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class RdfGenerator {


//Attributes

    private FileWriter fileWriter;
    private PrintWriter printWriter;


//Constructor

    public RdfGenerator(String localfile) {
        try {
            this.fileWriter = new FileWriter(localfile);
            this.printWriter = new PrintWriter(fileWriter);
        }catch(IOException e) {
            System.out.println("[ERROR]: Cannot operate over file out path!\n");
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
     * Generates a rdf file header, which represents the
     * alignment between two ontologies (in this case, different-level ontologies)
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
     * Turns the mapping class into a string
     * to write it in the out rdf file
     */

    private String generateMapping(Mapping m){
        String out = "\t<map>\n" +
                "\t\t<Cell>\n" +
                "\t\t\t<entity1 rdf:resource='"+ m.getTarget() +"'/>\n" +
                "\t\t\t<entity2 rdf:resource='"+ m.getSource() +"'/>\n" +
                "\t\t\t<relation>" + m.getRelation() + "</relation>\n" +
                "\t\t\t<measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>"+ m.getMeasure() +"</measure>\n" +
                "\t\t</Cell>\n" + "\t</map>\n";
        return out;
    }


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
