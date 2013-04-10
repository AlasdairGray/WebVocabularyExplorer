/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.gla.terrier.indexing.Collection;
import uk.ac.gla.terrier.indexing.Document;
import uk.ac.gla.terrier.indexing.TRECDocument;

/**
 * @author agray
 *
 */
public class ConceptCollection implements Collection {
	
	private static final Logger log = 
		Logger.getLogger(ConceptCollection.class.getName());

	private String[] m_conceptIds;
	private int m_offset = 0;

	private Map<String, SKOSConcept> m_concepts;

	//populate an array of ontology UIDs
	public ConceptCollection(Map<String, SKOSConcept> concepts) {
		log.info("Entering ConceptCollection()");
		m_concepts = concepts;
		Set<String> uriSet = concepts.keySet();
		m_conceptIds = new String[uriSet.size()];
		int i = 0;
		Iterator<String> it = uriSet.iterator();
		while (it.hasNext()) {
			m_conceptIds[i] = it.next();
			i++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Number of conceptIds: " + m_conceptIds.length);
		}
		log.info("Exiting ConceptCollection()");
	}
	
	//increment your offset in the arrays of ontology UIDs
	//return false if end reached
	public boolean nextDocument()
	{
		if (m_offset < m_conceptIds.length - 1) {
			m_offset++;
			return true;
		} else {
			return false;
		}
	}

	public Document getDocument() {
		log.trace("Entering getDocument()");
		//get the information for your concept by UID
		SKOSConcept concept = m_concepts.get(m_conceptIds[m_offset]);
		// get concept information to index
		Reader document = concept.getIndexDocument();
		log.trace("Exiting getDocument()");
		return new TRECDocument(document, null);
	}
	
	//return the actual UID
	public String getDocid() {
	   return m_conceptIds[m_offset];
	} 
	
	//return true if end of array of UIDs reached
	public boolean endOfCollection() {
		if ((m_offset + 1) == m_conceptIds.length) {
			return true;
		} else {
			return false;
		}
	} 
	
	//move counter back to the start of the UIDs array
	public void reset() {
		m_offset = 0;
	}

	/* (non-Javadoc)
	 * @see uk.ac.gla.terrier.indexing.Collection#close()
	 */
	public void close() {		
		
	} 
	
}
