/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

/**
 *
 */
public class MappingConfiguration {

	private static final Logger log = 
		Logger.getLogger(MappingConfiguration.class.getName());
	private String m_configFile;

	/**
	 * @param configFile
	 * @param concepts 
	 */
	public MappingConfiguration(String configFile) {
		log.info("Entering MappingConfiguration()");
		m_configFile = configFile;		
		log.info("Exiting MappingConfiguration()");		
	}

	/**
	 * Parse the configuration file for mappings
	 *
	 * @throws FileNotFoundException if filePath does not exist
	 */
	public Map<String, SKOSMapping> parseMappings() {
		log.info("Entering parseMappings()");
		Map<String, SKOSMapping> mappingFiles = 
			new HashMap<String, SKOSMapping>();
		try {
			DocumentBuilderFactory docBuilderFactory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = 
				docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(m_configFile);
			
			// normalize text representation
			doc.getDocumentElement ().normalize ();

			processSkosDocs(mappingFiles, doc, "mapping");

		} catch (Exception e) {
			log.error("Problem parsing configuration file. " + e);
		}
		log.info("Exiting parseMappings()");
		return mappingFiles;
	}

	/**
	 * @param vocabs
	 * @param doc
	 */
	private void processSkosDocs(Map<String, SKOSMapping> mappingFiles, 
			Document doc, String docType) {
		if (log.isDebugEnabled()) {
			log.debug("Entering processSkosDoc() with " + docType);
		}
		NodeList listOfDocs = doc.getElementsByTagName(docType);
		int numDocs = listOfDocs.getLength();
		if (log.isDebugEnabled()) {
			log.debug("Number of mapping files: " + numDocs);
		}

		for(int s=0; s<listOfDocs.getLength() ; s++){
			Node vocabNode = listOfDocs.item(s);
			if(vocabNode.getNodeType() == Node.ELEMENT_NODE){
				Element vocabulary = (Element)vocabNode;

				String urlString = null, fileString = null;
				
				NodeList urlNode = vocabulary.getElementsByTagName("url");
				if (urlNode != null && urlNode.getLength() > 0) {
					// Process url
					log.trace("Process url");
					urlString = processNode(urlNode);
				} else {
					// Process file
					NodeList fileNode = vocabulary.getElementsByTagName("file");
					if (fileNode != null && fileNode.getLength() > 0) {
						log.trace("Process file");
						fileString = processNode(fileNode);
					} else {
						log.warn("No location specified");
						/* Continue onto the next iteration */
						continue;
					}
				}

				// Process rdf format
				String rdf = processNode(
						vocabulary.getElementsByTagName("rdf-format"));

				// Process includes-inverse
				String includesInverse = processNode(
						vocabulary.getElementsByTagName("includes-inverses"));
				boolean isInverseComplete;
				if (includesInverse.equals("false")) {
					isInverseComplete = false;
				} else {
					isInverseComplete = true;
				}
				
				// Parse source
				log.debug("Parsing a mapping file");
				try {
					SKOSMappingParser skosParser = new SKOSMappingParser();
					SKOSMapping mappingFile;
					if (urlString != null) {
						URL url = new URL(urlString);
						mappingFile = skosParser.parse(url, rdf, 
								isInverseComplete);
					} else {
						File file = new File(fileString);
						mappingFile = skosParser.parse(file, rdf, isInverseComplete);
					}
					mappingFiles.put(mappingFile.getURI(), mappingFile);
				} catch (Exception e) {
					log.error("Problem parsing vocabulary");
				}
			}
		}
	}

	private String processNode(NodeList nodeList) {
		if (log.isDebugEnabled()) {
			log.debug("Entering processNode()");
		}
		Element nodeElement = (Element)nodeList.item(0);
		NodeList leafList = nodeElement.getChildNodes();
		String result = ((Node)leafList.item(0)).getNodeValue().trim();
		log.debug("Exiting processNode()");
		return result;
	}

}
