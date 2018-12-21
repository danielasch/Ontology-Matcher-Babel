package resources;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import objects.Concept;
import synsetSelection.SynsetDisambiguationWE;

/**
 * This class generates the text files
 */

public class OutFiles {
	
//Attributes
	
	private String outPath;


//Constructor	
	
	/**
	 * This constructor receive the path to the alignment and
	 * change .rdf by _1.text, this way, the text file will be
	 * generated at the save folder as the alignment
	 */

	public OutFiles(String path) {
		String aux = path.replace(".rdf", "_1.txt");
		this.outPath = aux;
	}


//Methods
	
	/**
	 * Generates the text file for the parametrized technique
	 */

	public void outFile(List<Concept> listDomain, int tec) {
		try {
			FileWriter arq = new FileWriter(this.outPath);
			PrintWriter printer = new PrintWriter(arq);
			Set<String> bgwSelect;
			Utilities ut;

			for(Concept cnp: listDomain) {
				ut = cnp.getUtilities();
				printer.print("\n>Ontology Info.<\n");
				printer.print("Nome do conceito de domínio: " + cnp.getClassName() + "\n");
				printer.print("Descrição: " + cnp.getConseptDesc() + "\n");
				printer.print("Supers: " + cnp.getSupers() + "\n");
				printer.print("Subs: " + cnp.getSubs() + "\n");
				printer.print("Contexto Domínio conceito: " + cnp.getConceptContext() + "\n");
				printer.print("Conceito Topo alinhado: ");

				if(cnp.getAliClass() != null) {
					String top = cnp.getAliClass().toString();
					printer.print(top.substring(top.lastIndexOf("/")+1,top.length()-1) + "\n");
				}

				else{
					printer.print("Não foi possível realizar o alinhamento!\n");
				}

				printer.print("\n>BabelNet Info.<\n");
				printer.print("Synset selecionado BabelNet: ");

				if(cnp.getGoodSynset() != null) {
					printer.print(cnp.getGoodSynset().getSynset().getMainSense() + "\n");

					if (cnp.getUtilities().getSelectedHypernym() != null) {
						printer.print("Hiperonímio selecionado: " + ut.getSelectedHypernym() +
								" no nível de busca " + ut.getLevel() + "\n");
						printer.print("Caminho realizado: " + ut.getHypernyms() + "\n");
					} else {
						printer.print("Hiperonímio selecionado: Não foi encontrado nenhum hiperonímio na ontologia de topo!\n");
						printer.print("Caminho realizado: Nenhum caminho encontrado!\n");
					}

					printer.print("Número de Synsets recuperados: " + ut.getNumSy() + "\n\n");
					printer.print("Conjunto de synsets recuperados:\n");
					Set<BabelNetResource.SearchObject> synsets = ut.getSynsetCntx();

					int count = 1;
					for (BabelNetResource.SearchObject so : synsets) {
						printer.print("\n" + count + ")\n");
						printer.print(">Synset: " + so.getSynset() + "\n");
						printer.print(">Sentidos: " + so.getSenses() + "\n");
						printer.print(">Glosses: " + so.getGlosses() + "\n");
						printer.print(">BOW: " + so.getBgw().toString() + "\n");
						printer.print("\n");
						count++;
					}

					printer.print("\n>Technique Info.<\n");

					if (tec == 1) {
						printer.print("\tLESK TECHNIQUE\n");
						bgwSelect = cnp.getGoodSynset().getBgw();
						printer.print("Intersecção de palavras encontrada - LESK:");

						for (String element : bgwSelect) {

							if (cnp.getConceptContext().contains(element)) {
								printer.print(" " + element + " ");
							}
						}
					} else if (tec == 2) {
						printer.print("\tWORD EMBEDDING TECHNIQUE\n");
						int index = 1;

						for (SynsetDisambiguationWE.WordEmbeddingObject weObj : ut.getMappings()) {
							printer.print("\n" + index + ") Distributiva: \n");
							printer.print(weObj.toString());
							index++;
						}
					}

					if (listDomain.indexOf(cnp) != listDomain.size() - 1) {
						printer.print("\n-----------------------------------------------------------------------------------------------------\n");
						printer.print("-------------------------------------------New concept-----------------------------------------------\n");
					}
				}

				else{
					printer.print("Não foi possível encontar synsets para esse conceito!");
				}
			}
			arq.close();
		}

		catch(IOException e) {
			System.out.println("Operação I/O interrompida, no arquivo de saída syCNTXT!");
	    	System.out.println("erro: " + e);
		}
	}
}
