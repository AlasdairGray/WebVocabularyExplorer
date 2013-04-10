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
 * @author agray
 *
 */
public class VocabularyConfiguration {

	private static final Logger log = 
		Logger.getLogger(VocabularyConfiguration.class.getName());
	private String m_configFile;
	private Map<String, SKOSConcept> m_concepts;

	/**
	 * @param configFile
	 * @param concepts 
	 */
	public VocabularyConfiguration(String configFile, 
			Map<String, SKOSConcept> concepts) {
		log.info("Entering VocabularyConfiguration()");
		m_configFile = configFile;		
		m_concepts = concepts;
		log.info("Exiting VocabularyConfiguration()");		
	}

	/**
	 * Parse the configuration file for vocabularies
	 *
	 * @throws FileNotFoundException if filePath does not exist
	 */
	public Map<String, SKOSScheme> parseVocabularies() {
		log.info("Entering parseVocabulareis()");
		Map<String, SKOSScheme> vocabs = 
			new HashMap<String, SKOSScheme>();
		try {
			DocumentBuilderFactory docBuilderFactory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = 
				docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(m_configFile);
			
			// normalize text representation
			doc.getDocumentElement ().normalize ();

			processSkosDocs(vocabs, doc, "vocabulary");

		} catch (Exception e) {
			log.error("Problem parsing configuration file. " + e);
		}
		log.info("Exiting parseVocabulareis()");
		return vocabs;
	}

	/**
	 * @param vocabs
	 * @param doc
	 */
	private void processSkosDocs(Map<String, SKOSScheme> vocabs, 
			Document doc, String docType) {
		if (log.isDebugEnabled()) {
			log.debug("Entering processSkosDoc() with " + docType);
		}
		NodeList listOfDocs = doc.getElementsByTagName(docType);
		int numDocs = listOfDocs.getLength();
		if (log.isDebugEnabled()) {
			log.debug("Number of " + docType + ": " + numDocs);
		}

		for(int s=0; s<listOfDocs.getLength() ; s++){
			Node vocabNode = listOfDocs.item(s);
			if(vocabNode.getNodeType() == Node.ELEMENT_NODE){
				Element vocabulary = (Element)vocabNode;
				String urlString = null, fileString = null;
				// Process url
				NodeList urlNode = vocabulary.getElementsByTagName("url");
				if (urlNode != null && urlNode.getLength() > 0) {
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
				log.trace("Process rdf-format");
				String rdf = processNode(
						vocabulary.getElementsByTagName("rdf-format"));
				
				// Parse source
				log.debug("Parse vocabulary");
				try {
					SKOSParser skosParser = new SKOSParser(m_concepts);
					SKOSScheme vocab;
					if (urlString != null) {
						URL url = new URL(urlString);
						vocab = skosParser.parse(url, rdf);
					} else {
						File file = new File(fileString);
						vocab = skosParser.parse(file, rdf);
					}
					vocabs.put(vocab.getURI(), vocab);
				} catch (Exception e) {
					log.error("Problem parsing vocabulary");
				}
			}
		}
	}

	private String processNode(NodeList nodeList) {
		log.debug("Entering processNode()");
		Element nodeElement = (Element)nodeList.item(0);
		NodeList leafList = nodeElement.getChildNodes();
		String result = ((Node)leafList.item(0)).getNodeValue().trim();
		log.debug("Exiting processNode()");
		return result;
	}

}
