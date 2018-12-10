package matchingProcess;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import objects.Concept;
import objects.ConceptManager;
import objects.Ontology;
import objects.OutObjectWE;
import resources.BaseResource;

	
/**
 * This class matches Domain Ont. classes with Top Ont. classes
 */
public class MatchingWE {

//Attributes

	private BaseResource baseresource;


//Constructor
		
		public MatchingWE(BaseResource br) {
			log();
			baseresource = br;
		}


//Log methods

		private void log() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Word Embeddings Matcher selected!" );
		}
		
		private void initLog() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Matching ontologies..." );
		}
		
		private void finalLog() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - [log] - Ontologies matched!" );
		}


//Methods

		public List<Mapping> match(List<Concept> listDom, List<Concept> listUp) {
			initLog();
			List<Mapping> listMap = new ArrayList<>();
			for(Concept cnpDom: listDom) {

				ConceptManager man = new ConceptManager();
				List<OutObjectWE> ooList = new ArrayList<>();

				Concept align = null;
				Set<String> contextDom = cnpDom.getContext();

				int sizeDom = contextDom.size();
				double max = 0;
				int aux = 0;


				for(Concept cnpUp: listUp) {

					HashMap<String, Object> map = new HashMap<>();

					OutObjectWE oo = new OutObjectWE(sizeDom);
					Double[] vec = new Double[sizeDom];

					Set<String> contextUp = cnpUp.getContext();

					int sizeUp = contextUp.size();
					double mediaT = 0;
					int aux_1 = 0;

					for(String elDom: contextDom) {

						HashMap<String, Double> map_1 = new HashMap<>();
						double media = 0;

						for(String elUp: contextUp) {

							double sim = similarity(elDom, elUp);

							media = media + sim;
							
							map_1.put(elUp, sim);	
						}
						
						media = media / sizeUp;

						mediaT = mediaT + media;

						vec[aux_1] = media;

						map.put(elDom, map_1);

						aux_1++;
					}
					
					mediaT = mediaT / sizeDom;
					
					if(mediaT > max) {

						max = mediaT;

						align = cnpUp;
					}

					oo.setTopConcept(cnpUp);

					oo.setMap(map);

					oo.setVector(vec);

					oo.setTotalAverage(mediaT);
					
					if(aux < 5) {

						ooList.add(oo);
						Collections.sort(ooList);
						aux++;

					} else {

						ooList.add(oo);
						Collections.sort(ooList);
						ooList.remove(4);
						aux++;

					}
				}
				
				Mapping map = new Mapping();
				man.configAliClass(cnpDom, align.get_owlClass());
				man.configObject(cnpDom, ooList);
				map.setSource(cnpDom.getClassID());
				map.setTarget(align.getClassID());
				map.setMeasure("1.0");
				map.setRelation("&lt;");
				listMap.add(map);
			}
			finalLog();
			return listMap;
		}

/*
		public void matchInv(List<Concept> listDom, List<Concept> listUp) {
			initLog();
			for(Concept cnpDom: listDom) {
				Set<String> contextDom = cnpDom.getContext();
				double max = 0;
				Concept align = null;
				ConceptManager man = new ConceptManager();
				int sizeDom = contextDom.size();
				
				List<OutObjectWE> ooList = new ArrayList<>();
				int aux = 0;

				for(Concept cnpUp: listUp) {
					OutObjectWE oo = new OutObjectWE(sizeDom);
					Set<String> contextUp = cnpUp.getContext();
					double mediaT = 0;
					int sizeUp = contextUp.size();
					
					HashMap<String, Object> map = new HashMap<>();
					Double[] vec = new Double[sizeUp];
					int aux_1 = 0;
					for(String elUp: contextUp) {
						double media = 0;
						
						HashMap<String, Double> map_1 = new HashMap<>();
						
						for(String elDom: contextDom) {
							double sim = similarity(elDom, elUp);
							media = media + sim;
							
							map_1.put(elDom, sim);	
						}
						
						media = media / sizeDom;
						mediaT = mediaT + media;
						vec[aux_1] = media;
						map.put(elUp, map_1);
						aux_1++;
					}
					
					mediaT = mediaT / sizeUp;
					
					if(mediaT > max) {
						max = mediaT;
						align = cnpUp;
					}
					

					oo.setTopConcept(cnpUp);

					oo.setMap(map);

					oo.setVector(vec);

					oo.setTotalAverage(mediaT);
					
					if(aux < 5) {
						ooList.add(oo);
						Collections.sort(ooList);
						aux++;
					} else {
						ooList.add(oo);
						Collections.sort(ooList);
						ooList.remove(4);
						aux++;
					}
				}
				
				Mapping map = new Mapping();
				man.configAliClass(cnpDom, align.get_owlClass());
				man.configObject(cnpDom, ooList);
				map.setSource(cnpDom.getClassID());
				map.setTarget(align.getClassID());
				map.setMeasure("1.0");
				map.setRelation("&lt;");
				this.listMap.add(map);			
			}
			finalLog();
		}

*/
		private double similarity(String elDom, String elUp) {
			double sim = this.baseresource.getWord2Vec().getword2Vec().similarity(elDom, elUp);
			if(!(Double.isNaN(sim))) {
				//increment the media and adds the similarity
				return (sim * 10);
            } else {
            	//case similarity is null
            	return 0;
            }
		}

}
