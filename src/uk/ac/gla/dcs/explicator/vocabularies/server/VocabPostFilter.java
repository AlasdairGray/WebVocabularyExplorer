package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.gla.terrier.matching.ResultSet;
import uk.ac.gla.terrier.querying.Manager;
import uk.ac.gla.terrier.querying.PostFilter;
import uk.ac.gla.terrier.querying.SearchRequest;

public class VocabPostFilter implements PostFilter {
	
	private static final Logger log = 
		Logger.getLogger(VocabPostFilter.class.getName());
	
	private Set<String> m_configuration;
	
	public VocabPostFilter(Set<String> configuration) {
		log.info("Entering VocabPostFilter()");
		m_configuration = configuration;
		log.debug("Configuration: " + printConfiguration());
		log.info("Exiting VocabPostFilter()");
	}
	
//	public void init(Set configuration) {
//		log.info("Entering init()");
//		m_configuration = configuration;
//		log.debug("Configuration: " + printConfiguration());
//		log.info("Exiting init()");
//	}

	public byte filter(Manager manager, SearchRequest srq, ResultSet rs,
			int docAtNum, int docNo) {
		log.debug("Entering filter() with document " + docNo);
		String conceptURI = 
			manager.getIndex().getDocumentIndex().getDocumentNumber(docNo);
		log.trace("Concept URI: " + conceptURI);
		Iterator<String> it = m_configuration.iterator();
		while (it.hasNext()) {
			String testURI = (String) it.next();
			log.trace("Comparing concept URI " + conceptURI + " with " + testURI);
			if (conceptURI.startsWith(testURI)) {
				// concept in select vocabulary, leave in result set
				log.debug("Exiting filter() with " + FILTER_OK);
				return FILTER_OK;
			}
		}
		log.debug("Exiting filter() with " + FILTER_REMOVE);
		return FILTER_REMOVE;
	}

	public void new_query(Manager manager, SearchRequest srq, ResultSet rs) {
		log.info("Entering new_query()");
		if (m_configuration != null) {
			log.debug("Configuration: " + printConfiguration());
		} else {
			log.debug("Not previously configured");
		}
		log.info("Exiting new_query()");
	}

	private String printConfiguration() {
		StringBuffer out = new StringBuffer();
		Iterator<String> it = m_configuration.iterator();
		while (it.hasNext()) {
			out.append("\n" + it.next());
		}
		return out.toString();
	}

}
