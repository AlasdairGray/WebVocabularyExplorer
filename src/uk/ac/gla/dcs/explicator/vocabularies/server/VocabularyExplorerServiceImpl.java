/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import uk.ac.gla.dcs.explicator.vocabularies.client.Literal;
import uk.ac.gla.dcs.explicator.vocabularies.client.RelatedTerm;
import uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyMapping;
import uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyScheme;
import uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyConcept;
import uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyExplorerService;

import uk.ac.gla.terrier.indexing.BasicIndexer;
import uk.ac.gla.terrier.indexing.BlockIndexer;
import uk.ac.gla.terrier.indexing.Indexer;
import uk.ac.gla.terrier.matching.ResultSet;
import uk.ac.gla.terrier.querying.Manager;
import uk.ac.gla.terrier.querying.PostFilter;
import uk.ac.gla.terrier.querying.SearchRequest;
import uk.ac.gla.terrier.structures.DocumentIndex;
import uk.ac.gla.terrier.structures.Index;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class VocabularyExplorerServiceImpl 
extends RemoteServiceServlet 
implements VocabularyExplorerService {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log;	
	
	/**
	 * Contains a local copy of a concept in the 
	 * org.ivoa.vocabularies.client.concept form.
	 * Stored under a String representation of the URI
	 * 
	 * Lazy population, i.e. concepts are only cached as
	 * they are returned the first time to a user query.
	 */

	
	/**
	 * Stores details of the vocabulary schemes 
	 */
	private Map<String, SKOSScheme> m_vocabularies;
	
	/**
	 * Stores details of the mapping files 
	 */
	private Map<String, SKOSMapping> m_mappingFiles;
	
	/**
	 * Stores the available concepts 
	 */
	private Map<String, SKOSConcept> m_concepts;
	
	private String m_defaultLanguage = "en";
	private Manager m_queryingManager;
	
	private boolean Terrier_Block_Indexing = false;
	
	public VocabularyExplorerServiceImpl() {
		super();
		m_concepts = new HashMap<String, SKOSConcept>();		
	}
	
	public void init() throws ServletException {
		// Initialise the service
		super.init();
		
		// Get the location of configuration files 
		ServletContext context = getServletContext();
		String prefix = context.getRealPath("/");	

		// Initialise logging
		PropertyConfigurator.configure(prefix + "etc/log4j.properties");
		log = Logger.getLogger(getClass().getName());
		
//		// Needed when testing on laptop tomcat in the department
//		System.setProperty("http.proxyHost", "wwwcache.dcs.gla.ac.uk");
//		System.setProperty("http.proxyPort", "8080");
		
		// Checking proxy settings
		if (log.isInfoEnabled()) {
			log.info("Proxy: " + System.getProperty("http.proxySet") + " " +
					System.getProperty("http.proxyHost") + " " + 
					System.getProperty("http.proxyPort"));
		}
		
		// Initialise Terrier variable
		System.setProperty("terrier.home", prefix);		
		
		// Set the location of the vocabulary configuration file
		String vocabConfigFile = prefix + "etc/vocabularies-list.xml";
		String mappingConfigFile = prefix + "etc/mappings-list.xml";
		
		// load vocabularies
		loadVocabularies(vocabConfigFile);
		// load mappings
		loadMappings(mappingConfigFile);
		
		// Initialise the search collection
		log.debug("Initialise the collection");
		 ConceptCollection conceptCollection = 
			new ConceptCollection(m_concepts);
		ConceptCollection[] collections = new ConceptCollection[1];
		collections[0] = conceptCollection;
		
		// Index the collection
		// the logging suddenly changed as Terrier steals the root logger :(
		log.debug("Index the collection");
		Indexer indexedCollection = null;
//		if (Terrier_Block_Indexing)
//			indexedCollection = new BlockIndexer("index", "data");
//		else
			indexedCollection = new BasicIndexer("index", "data");
		
		indexedCollection.createDirectIndex(collections);
		indexedCollection.createInvertedIndex();
		
		
		
		// Load in the index
		Index index = Index.createIndex("index", "data");

		// Initialise query manager
		m_queryingManager = new Manager(index);
		
		log.info("Exiting VocabularyExplorerServiceImpl()");
	}
	
	/**
	 * @param vocabConfigFile 
	 * 
	 */
	private void loadVocabularies(String vocabConfigFile) {
		log.debug("Entering loadVocabularies()");
		/* 
		 * Load in vocabularies from location and format
		 * specified in configuration file
		 */
		VocabularyConfiguration vocabParser = 
			new VocabularyConfiguration(vocabConfigFile, m_concepts);
		// Load and parse vocabularies 
		m_vocabularies = vocabParser.parseVocabularies();

		log.debug("Exiting loadVocabularies()");
	}
	
	private void loadMappings(String mappingConfigFile) {
		log.debug("Entering loadMappings()");
		
		MappingConfiguration mappingParser =
			new MappingConfiguration(mappingConfigFile);
		
		// Load and parse mappings
		m_mappingFiles = mappingParser.parseMappings();

		log.debug("Exiting loadMappings()");
	}
	
//	/**
//	 * @param rdf
//	 * @return
//	 */
//	private String convertRdfFormat(String rdf) {
//		log.trace("Entering convertRdfFormat()");
//		String result = "";
//		if (rdf.equals("TURTLE")) {
//			result = RDFConstants.TURTLE;
//		} else if (rdf.equals("XML")) {
//			result = RDFConstants.RDFXML;
//		}
//		log.trace("Exiting convertRdfFormat()");
//		return result;
//	}

	public Integer findConcept(String searchTerm, Set<String> vocabConfiguration) {
		if (log.isInfoEnabled()) {
			log.info("Entering findConcept() with " + searchTerm + 
					"\n from configuration " + 
					printCollection(vocabConfiguration));
		}
		if (searchTerm == null || searchTerm.equals("")) {
			return new Integer(0);
		}
		
		/* 
		 * Multiple concurrent interactions interfere with each other, 
		 * perform search each time
		 * Get the result set for the search term
		 */
		ResultSet rs = performSearch(searchTerm, vocabConfiguration);
		int rsSize = rs.getResultSize();
		if (log.isInfoEnabled()) {
			log.info("Number of results: " + rsSize);
		}
		return new Integer(rsSize);
	}

	/**
	 * Performs the task of searching terrier index for matching
	 * concepts. Result set is filtered to the selected vocabularies
	 * 
	 * @param searchTerm user entered search term
	 * @param configuration set of vocabulary scheme URIs
	 * @return result set containing ranked matched concept URIs
	 */
	private ResultSet performSearch(String searchTerm, Set<String> configuration) {
		if (log.isDebugEnabled()) {
			log.debug("Entering performSearch() with " + searchTerm);
		}
		if (log.isTraceEnabled()) {
			log.trace("Configuration: " + printCollection(configuration));
		}

		// Retrieval from Terrier index
		// Create Search Request
		SearchRequest srq = 
			m_queryingManager.newSearchRequest("query", searchTerm);

		// Add matching model
		//FIXME: Change to using tf.idf
		srq.addMatchingModel("Matching", "PL2");
		
		// Create PostFilter to limit the search to selected vocabularies
		PostFilter postFilter = new VocabPostFilter(configuration);
		srq.addPostFilter("vf", postFilter);

		log.trace("Run terrier query process");
		m_queryingManager.runPreProcessing(srq);
		m_queryingManager.runMatching(srq);		
		m_queryingManager.runPostProcessing(srq);
		m_queryingManager.runPostFilters(srq);

		// Return result set
		ResultSet rs = srq.getResultSet();
		int resultSize = rs.getResultSize();
		if (log.isDebugEnabled()) {
			log.debug("Exiting performSearch(), number of matches: " + 
					resultSize);
		}
		return rs;
	}

	private String printCollection(Collection<String> collection) {
		if (collection == null) {
			return "";
		}
		if (log.isTraceEnabled()) {
			log.trace("Entering printCollection(). collection size: " + 
					collection.size());
		}
		StringBuffer out = new StringBuffer();
		Iterator<String> it = collection.iterator();
		while (it.hasNext()) {
			out.append("\n\t" + it.next());
		}
		if (log.isTraceEnabled()) {
			log.trace("Exiting printCollection() with:\n" + out.toString());
		}
		return out.toString();
	}
	
	private String printConceptURIs(
			Collection<VocabularyConcept> concepts, int offset) {
		StringBuffer out = new StringBuffer();
		int position = offset;
		Iterator<VocabularyConcept> it = concepts.iterator();
		while (it.hasNext()) {
			VocabularyConcept concept = it.next();
			out.append("\n\t" + position + "\t" + concept.getUri());
			position++;
		}
		return out.toString();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyExplorerService#getSearchResults(java.lang.String, java.util.Set, int, int)
	 */
	public Collection<VocabularyConcept> getSearchResults(
			String searchTerm, 
			Set<String> vocabularyConfiguration,
			Set<String> mappingConfiguration,
			int start, int end) {
		
		if (log.isInfoEnabled()) {
			log.info("Entering getSearchResults() for " + searchTerm + 
					" from " + start + " to " + end + 
					" configuration:\n\tvocabularies " +
					printCollection(vocabularyConfiguration) +
					"\n\tmappings " + printCollection(mappingConfiguration));
		}
		
		Collection<VocabularyConcept> foundConcepts = 
			new Vector<VocabularyConcept>();
		
		/*
		 * Capture the case when someone has given us a bad parameter
		 */
		if (end < start) {
			log.info("Exiting getSearchResults() with empty set. " +
					"Invalid range parameters");
			return foundConcepts;
		}
		
		/* 
		 * Multiple concurrent interactions interfere with each other, 
		 * perform search each time
		 * Get the result set for the search term
		 */
		ResultSet rs = performSearch(searchTerm, vocabularyConfiguration);
		
		/* 
		 * Retrieve the document index for converting docid back 
		 * into a document
		 */
		DocumentIndex doi = 
			m_queryingManager.getIndex().getDocumentIndex();

		int[] matchingURIs = rs.getDocids();
		int resultSize = matchingURIs.length;
		for (int i = start; i <= end; i++) {
			/*
			 * Client is using offsets starting from 1
			 * Result set is using offsets starting from 0
			 * Get the result rank by taking away 1
			 */
			int rank = i - 1;
			if (log.isDebugEnabled()) {
				log.debug("Get concept ranked: " + rank);
			}
			if (i > resultSize) {
				if (log.isDebugEnabled()) {
					log.debug("No more documents found. i = " + i);
				}
				break;
			}
			String conceptURI = doi.getDocumentNumber(matchingURIs[rank]);
			if (log.isDebugEnabled()) {
				log.debug("Document ID: " + conceptURI);
			}
			
			// Add concept to result set
			VocabularyConcept vocabConcept = constructConcept(mappingConfiguration, 
					m_concepts.get(conceptURI)); 
			foundConcepts.add(vocabConcept);
		}		
		if (log.isInfoEnabled()) {
			log.info("Exiting getSearchResults() with results: " +
					printConceptURIs(foundConcepts, start));
		}
		return foundConcepts;
	}

	/* 
	 * Retrieve the details for a specific concept
	 */
	public VocabularyConcept getConcept(String uri, Set<String> vocabConfig, 
			Set<String> mappingConfiguration) {
		if (log.isInfoEnabled()) {
			log.info("Entering getConcept() with \n\turi " + uri + 
					"\n using vocabulary configuration " + 
					printCollection(vocabConfig) + "\n and mappings " +
					printCollection(mappingConfiguration));
		}
		
		if (vocabConfig.isEmpty() && mappingConfiguration.isEmpty()) {
			log.debug("External link, assume all vocabularies and " +
					"mappings are in context");
			vocabConfig.addAll(m_vocabularies.keySet());
			mappingConfiguration.addAll(m_mappingFiles.keySet());
			if (log.isInfoEnabled()) {
				log.info("Updated configuration to: \nvocabulary configuration " + 
						printCollection(vocabConfig) + "\n and mappings " +
						printCollection(mappingConfiguration));
			}
		}
		
		SKOSConcept concept = null;
		VocabularyConcept vocabConcept = null;
		if (m_concepts.containsKey(uri)) {
			concept = m_concepts.get(uri);
			vocabConcept = constructConcept(mappingConfiguration,
					concept);
		}
		
		if (log.isInfoEnabled()) {
			log.info("Exiting getConcept() with " + vocabConcept);
		}
		return vocabConcept;
	}

	private VocabularyConcept constructConcept(
			Set<String> mappingConfiguration, SKOSConcept concept) {
		if (log.isDebugEnabled()) {
			log.debug("Entering constructConcept() for " + concept.getURI());
		}
		/* 
		 * Use mapping configuration to limit the mappings used
		 * 
		 * Mapping configuration may have changed, need to get 
		 * the available mapping details for the concept with the 
		 * current configuration from the repository
		 */ 		
		Map<String,List<String>> exactMatches = new HashMap<String, List<String>>();
		Map<String,List<String>> broadMatches = new HashMap<String, List<String>>();
		Map<String,List<String>> narrowMatches = new HashMap<String, List<String>>();
		Map<String,List<String>> relatedMatches = new HashMap<String, List<String>>();
		calculateAvailableMappings(mappingConfiguration, exactMatches, broadMatches, 
				narrowMatches, relatedMatches);
		VocabularyConcept vocabConcept = convertConcept(concept, exactMatches, 
				broadMatches, narrowMatches, relatedMatches);
		log.debug("Exiting constructConcept()");
		return vocabConcept;
	}
	
	/**
	 * Convert a SKOS concept into a vocabulary concept
	 * @param concept
	 * @param exactMatches 
	 * @param relatedMatches 
	 * @param narrowMatches 
	 * @param broadMatches 
	 * @return
	 */
	private VocabularyConcept convertConcept(SKOSConcept concept, 
			Map<String, List<String>> exactMatches, 
			Map<String, List<String>> broadMatches, 
			Map<String, List<String>> narrowMatches, 
			Map<String, List<String>> relatedMatches) {
		log.debug("Entering convertConcept()");
		String conceptURI = concept.getURI();
		Collection<Literal> prefLabels = 
			convertLiteralCollection(concept.getPrefLabel());
		Collection<Literal> altLabels = 
			convertLiteralCollection(concept.getAltLabels());
		Collection<Literal> definitions = 
			convertLiteralCollection(concept.getDefinitions());
		Collection<Literal> scopeNotes = 
			convertLiteralCollection(concept.getScopeNotes());
		Collection<String> inSchemes = concept.getSchemes();
		Collection<RelatedTerm> broader = 
			expandRelatedTerms(concept.getBroader());
		Collection<RelatedTerm> narrower = 
			expandRelatedTerms(concept.getNarrower());
		Collection<RelatedTerm> related = 
			expandRelatedTerms(concept.getRelated());
		Collection<RelatedTerm> conceptExactMatches = 
			findMappings(exactMatches, conceptURI, "exactMatch");
		Collection<RelatedTerm> conceptBroadMatches = 
			findMappings(broadMatches, conceptURI, "broadMatch");
		Collection<RelatedTerm> conceptNarrowMatches = 
			findMappings(narrowMatches, conceptURI, "narrowMatch");
		Collection<RelatedTerm> conceptRelatedMatches = 
			findMappings(relatedMatches, conceptURI, "relatedMatch");
		
		VocabularyConcept vocabConcept = new VocabularyConcept(
				conceptURI, prefLabels, altLabels, definitions, 
				scopeNotes, inSchemes, broader, narrower, related, conceptExactMatches,
				conceptBroadMatches, conceptNarrowMatches, conceptRelatedMatches);
		log.debug("Exiting convertConcept()");
		return vocabConcept;
	}

	private Collection<RelatedTerm> findMappings(
			Map<String, List<String>> mappings, String conceptURI, String matchType) {
		if (log.isTraceEnabled()) {
			log.trace("Entering findMappings() for " + matchType + " of " + 
					conceptURI);
		}
		Collection<RelatedTerm> result = new Vector<RelatedTerm>();
		if (mappings.containsKey(conceptURI)) {
			List<String> matches = mappings.get(conceptURI);
			Iterator<String> it = matches.iterator();
			while (it.hasNext()) {
				String relatedUri = it.next();
				RelatedTerm relatedTerm = new RelatedTerm(relatedUri);
				Collection<SKOSLiteral> prefLabels = 
					m_concepts.get(relatedUri).getPrefLabel();
				relatedTerm.setPrefLabel(convertLiteralCollection(prefLabels));
				result.add(relatedTerm);
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("Exiting findMappings(), number of " + matchType + 
					" mappings: " + result.size());
		}
		return result;
	}

	private Collection<RelatedTerm> expandRelatedTerms(
			Collection<RelatedTerm> terms) {
		Iterator<RelatedTerm> it = terms.iterator();
		while (it.hasNext()) {
			RelatedTerm term = it.next();
			String relatedUri = term.getUri();
			Collection<SKOSLiteral> prefLabels = 
				m_concepts.get(relatedUri).getPrefLabel();
			term.setPrefLabel(convertLiteralCollection(prefLabels));
		}
		return terms;
	}

	/**
	 * Convert a collection of skos literals into a collection
	 * of vocabulary literals.
	 * 
	 * @param skosLiterals
	 * @return
	 */
	private Collection<Literal> convertLiteralCollection(
			Collection<SKOSLiteral> skosLiterals) {
		log.trace("Entering convertLiteralCollection()");
		if (skosLiterals == null) {
			return null;
		}
		if (log.isTraceEnabled()) {
			log.trace("Size of incoming collection: " + skosLiterals.size());
		}
		Collection<Literal> literalCollection = new Vector<Literal>();
		Iterator<SKOSLiteral> it = skosLiterals.iterator();
		while (it.hasNext()) {
			SKOSLiteral skosLiteral = it.next();
			Literal literal = new Literal(skosLiteral.getValue(), 
					skosLiteral.getLanguage());
			literalCollection.add(literal);
		}
		if (log.isTraceEnabled()) {
			log.trace("Size of output collection: " + 
					literalCollection.size());
		}
		log.trace("Exiting convertLiteralCollection()");
		return literalCollection;
	}
	
	private void calculateAvailableMappings(Set<String> mappingConfiguration, 
			Map<String, List<String>> exactMatches, 
			Map<String, List<String>> broadMatches, 
			Map<String, List<String>> narrowMatches, 
			Map<String, List<String>> relatedMatches) {
		log.debug("Entering calculateAvailableMappings()");
		Iterator<String> it = mappingConfiguration.iterator();
		while (it.hasNext()) {
			String mappingUri = it.next();
			SKOSMapping mappingDetail = m_mappingFiles.get(mappingUri);
			exactMatches.putAll(mappingDetail.getExactMatches());
			broadMatches.putAll(mappingDetail.getBroadMatches());
			narrowMatches.putAll(mappingDetail.getNarrowMatches());
			relatedMatches.putAll(mappingDetail.getRelatedMatches());
		}
		if (log.isDebugEnabled()) {
			log.debug("Exiting calculateAvailableMappings()");
		}
	}

	public Map<String, VocabularyScheme> getVocabularies() {
		Map<String, VocabularyScheme> output = new HashMap<String, VocabularyScheme>();
		Set<String> keyset = m_vocabularies.keySet();
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String key = it.next();
			SKOSScheme scheme = m_vocabularies.get(key);
			output.put(key, scheme.toVocabularyScheme());
		}
		return output;
	}

	public Map<String, VocabularyMapping> getMappings() {
		return convertMappingFileDetails(m_mappingFiles);
	}

	private Map<String, VocabularyMapping> convertMappingFileDetails(
			Map<String, SKOSMapping> mappings) {
		if (log.isDebugEnabled()) {
			log.debug("Entering convertMappingFileDetails(), number of files: " + 
					mappings.size());
		}
		Map<String,VocabularyMapping> result = new HashMap<String, VocabularyMapping>();
		Collection<SKOSMapping> valueset = mappings.values();
		Iterator<SKOSMapping> it = valueset.iterator();
		while (it.hasNext()) {
			SKOSMapping mappingFileDetail = it.next();
			String uri = mappingFileDetail.getURI();
			VocabularyMapping vocabMappingDetail = new VocabularyMapping(uri);
			vocabMappingDetail.setCreatedDate(mappingFileDetail.getCreatedDate());
			vocabMappingDetail.setCreator(mappingFileDetail.getCreator());
			vocabMappingDetail.setTitle(mappingFileDetail.getTitle());
			result.put(uri, vocabMappingDetail);
		}
		log.debug("Exiting convertMappingFileDetails()");
		return result;
	}

}
