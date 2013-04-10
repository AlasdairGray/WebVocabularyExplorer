package uk.ac.gla.dcs.explicator.vocabularies.server;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParser;

import uk.ac.gla.dcs.explicator.vocabularies.client.RelatedTerm;

public class SKOSParser {
	
	private Logger log= 
		Logger.getLogger(SKOSParser.class.getName());
	Map<String, String> namespaces;
	private Vector<Statement> statementCollection;
	private StatementCollector skosHandler;
	private SKOSErrorListener skosParseErrorListener;

	private Map<String, SKOSConcept> m_skosConcepts;
	private SKOSScheme m_vocabulary;
	private Vector<String> m_topConcepts;

	public SKOSParser(Map<String, SKOSConcept> concepts) {
		statementCollection = new Vector<Statement>();
		namespaces = new HashMap<String, String>();
		skosHandler = new StatementCollector(statementCollection, namespaces);
		skosParseErrorListener = new SKOSErrorListener();
		m_skosConcepts = concepts;
		m_topConcepts = new Vector<String>();
	}
	
	/**
	 * @param url URL where the vocabulary is stored
	 * @param rdfFormat RDF file format
	 * @return a representation of the vocabulary
	 * @throws Exception
	 */
	public SKOSScheme parse(URL url, String rdfFormat) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Entering parse(url) " + url.toString());
		}
		SKOSScheme vocabulary;
		try {
			log.info("Retrieving URL");
			// Add accept header to url for rdf+xml 
			URLConnection urlc = url.openConnection();
			urlc.addRequestProperty("accept", "application/rdf+xml");
			InputStream in = urlc.getInputStream();
			log.info("Parsing vocabulary");
			vocabulary = parse(in, rdfFormat, "");
		} catch (IOException e) {
			log.error("Problem reading url " + url +". " + e);
			throw new Exception("Could not parse vocabulary");
		}
		if (log.isInfoEnabled()) {
			log.info("Exiting parse(url), number of concepts: " + m_skosConcepts.size());
		}
		return vocabulary;
	}

	/**
	 * @param file File where the vocabulary is stored
	 * @param rdfFormat RDF file format
	 * @return a representation of the vocabulary
	 * @throws Exception
	 */
	public SKOSScheme parse(File file, String rdfFormat) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Entering parse(file) " + file.getAbsolutePath());
		}
		SKOSScheme vocabulary;
		try {
			log.info("Opening file");
			InputStream in = new FileInputStream(file);
			log.info("Parse file");
			vocabulary = parse(in, rdfFormat, "");
		} catch (FileNotFoundException e) {
			log.error("Error parsing file" + e);
			throw new Exception("Could not parse vocabulary");
		} 
		if (log.isInfoEnabled()) {
			log.info("Exiting parse(file), number of concepts: " + 
					m_skosConcepts.size());
		}
		return vocabulary;
	}
	
	private SKOSScheme parse(InputStream in, String rdfFormat, String schemeURI) 
	throws Exception {
		log.debug("Entering parse()");
		try { 
			//FIXME: Need to use the correct parser for the rdfFormat
			RDFParser parser = new RDFXMLParser();
			parser.setRDFHandler(skosHandler);
			parser.setParseErrorListener(skosParseErrorListener);
			parser.parse(in, schemeURI);
		} catch (IOException e) {
			log.error("Error parsing vocabulary " + e);
			throw new Exception("Could not parse vocabulary");
		} catch (RDFParseException e) {
			log.error("Error parsing vocabulary " + e);
			throw new Exception("Could not parse vocabulary");
		} catch (RDFHandlerException e) {
			log.error("Error parsing vocabulary " + e);
			throw new Exception("Could not parse vocabulary");
		}
		// Process the RDF triples
		processStatements(schemeURI);
		m_vocabulary.setTopConcepts(m_topConcepts);
		if (log.isDebugEnabled()) {
			log.debug("Number of statements: " + statementCollection.size());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Exiting parse(). Number of concepts " + m_skosConcepts.size());
		}
		return m_vocabulary;
	}

	private void processStatements(String schemeURI) throws Exception {
		log.debug("Entering processStatements()");
		String lastSeen = null;
		Iterator<Statement> it = statementCollection.iterator();
		while (it.hasNext()) {
			Statement st = it.next();
			/* Assuming that we are only coping with concepts for now
			 * i.e. ignoring collections
			 */
			if (st.getPredicate().getLocalName().equals("type")) {
				String objectType = ((URI) st.getObject()).getLocalName();
				log.debug("New object: " + st.getObject().toString());
				if (objectType.equals("Concept")) {
					SKOSConcept concept = new SKOSConcept(st.getSubject().stringValue());
					m_skosConcepts.put(concept.getURI(), concept);
					log.debug("Created concept " + concept);
					lastSeen = "Concept";
				} else if (objectType.equals("Collection")) {
					//TODO: create collection and process
					lastSeen = "Collection";
				} else if (objectType.equals("OrderedCollection")) {
					//TODO: create ordered collection and process
					lastSeen ="Collection";
				} else if (objectType.equals("ConceptScheme")) {
					m_vocabulary = new SKOSScheme(
							st.getSubject().stringValue());
					lastSeen = "Scheme";
				} else {
					log.error("Unrecognised RDF type");
					throw new Exception("Unrecognised RDF type");
				}
			} else {
				if (lastSeen.equals("Scheme")) {
					processSchemeMetadata(st);
				} else if (lastSeen.equals("Concept")) {
					processConcept(st);
				} else ;
			}
		}
		log.debug("Exiting processStatements()");
	}

	private void processSchemeMetadata(Statement st) {
		if (log.isDebugEnabled()) {
			log.debug("Entering processSchemeMetadata() with " + st.toString());
		}
		// Get vocabulary metadata and set it in the vocabulary object
		String predicate = st.getPredicate().getLocalName();
		if (predicate.equals("title")) {
			String title = st.getObject().stringValue();
			if (log.isTraceEnabled()) {
				log.trace("Set title: " + title);
			}
			m_vocabulary.setVocabName(title);
		} else if (predicate.equals("created")) {
			String created = st.getObject().stringValue();
			if (log.isTraceEnabled()) {
				log.trace("Set created: " + created);
			}
			m_vocabulary.setCreatedDate(created);
		} else if (predicate.equals("hasTopConcept")) {
			log.trace("Add top concept");
			m_topConcepts.add(st.getObject().stringValue());
		}
		log.debug("Exiting processSchemeMetadata()");
	}

	private void processConcept(Statement st) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Entering processConcept() with " + st.toString());
		}
		
		try {
			SKOSConcept concept = m_skosConcepts.get(st.getSubject().stringValue());

			String predicateString = st.getPredicate().getLocalName();

			if (predicateString.equals("prefLabel")) {
				log.trace("Add prefLabel");
				Literal literal = (Literal) st.getObject();
				concept.addPrefLabel(literal.getLabel(), literal.getLanguage());
			} else if (predicateString.equals("altLabel")) {
				log.trace("Add altLabel");
				Literal literal = (Literal) st.getObject();
				concept.addAltLabel(literal.getLabel(), literal.getLanguage());
			} else if (predicateString.equals("hiddenLabel")) {
				log.trace("Add hiddenLabel");
				Literal literal = (Literal) st.getObject();
				concept.addHiddenLabel(literal.getLabel(), literal.getLanguage());
			} else if (predicateString.equals("inScheme")) {
				log.trace("Add inScheme");
				URI uri = (URI) st.getObject();
				concept.addScheme(uri.stringValue());
			} else if (predicateString.equals("definition")) {
				log.trace("Add definition");
				Literal literal = (Literal) st.getObject();
				concept.addDefinition(literal.getLabel(), literal.getLanguage());
			} else if (predicateString.equals("scopeNote")) {
				log.trace("Add scopeNote");
				Literal literal = (Literal) st.getObject();
				concept.addScopeNote(literal.getLabel(), literal.getLanguage());				
			} else if (predicateString.equals("broader")) {
				log.trace("Add broader");
				URI uri = (URI) st.getObject();
				concept.addBroader(new RelatedTerm(uri.stringValue()));
			} else if (predicateString.equals("narrower")) {
				log.trace("Add narrower");
				URI uri = (URI) st.getObject();
				concept.addNarrower(new RelatedTerm(uri.stringValue()));
			} else if (predicateString.equals("related")) {
				log.trace("Add related");
				URI uri = (URI) st.getObject();
				concept.addRelated(new RelatedTerm(uri.stringValue()));
			} else if (predicateString.equals("notation")) {
				log.trace("Add notation");
				Literal notation = (Literal) st.getObject();
				URI datatype = notation.getDatatype();
				if (datatype == null) {
					concept.addNotation(notation.getLabel(), null);
				} else {
					concept.addNotation(notation.getLabel(), notation.getDatatype().stringValue());
				}
			}

			// Put the concept back into the map
			m_skosConcepts.put(concept.getURI(), concept);

		} catch (Exception e) {
			if (!e.getMessage().equals("Encountered a blank node")) {
				throw new Exception("Cannot process vocabulary");
			}
		}
		log.debug("Exiting processConcept()");
	}

}
