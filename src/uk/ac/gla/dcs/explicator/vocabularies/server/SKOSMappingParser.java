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
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParser;

public class SKOSMappingParser {
	
	private Logger log= 
		Logger.getLogger(SKOSMappingParser.class.getName());
	Map<String, String> namespaces;
	private Vector<Statement> statementCollection;
	private StatementCollector skosHandler;
	private SKOSErrorListener skosParseErrorListener;

	private SKOSMapping m_mappingFile;
		
	public SKOSMappingParser() {
		statementCollection = new Vector<Statement>();
		namespaces = new HashMap<String, String>();
		skosHandler = new StatementCollector(statementCollection, namespaces);
		skosParseErrorListener = new SKOSErrorListener();
	}
	
	/**
	 * @param url URL where the vocabulary is stored
	 * @param rdfFormat RDF file format
	 * @param isInverseComplete 
	 * @return a representation of the mapping file metadata
	 * @throws Exception
	 */
	public SKOSMapping parse(URL url, String rdfFormat, 
			boolean isInverseComplete) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Entering parse(url) " + url.toString());
		}
		m_mappingFile = new SKOSMapping(url.toString());
		try {
			log.info("Retrieving URL");
			// Add accept header to url for rdf+xml 
			URLConnection urlc = url.openConnection();
			urlc.addRequestProperty("accept", "application/rdf+xml");
			InputStream in = urlc.getInputStream();
			log.info("Parsing vocabulary");
			parse(in, rdfFormat, "", isInverseComplete);
		} catch (IOException e) {
			log.error("Problem reading url " + url +". " + e);
			throw new Exception("Could not parse vocabulary");
		}
		if (log.isInfoEnabled()) {
			log.info("Exiting parse(url), number of mappings: " + 
					m_mappingFile.size());
		}
		return m_mappingFile;
	}

	/**
	 * @param file File where the vocabulary is stored
	 * @param rdfFormat RDF file format
	 * @return a representation of the vocabulary
	 * @throws Exception
	 */
	public SKOSMapping parse(File file, String rdfFormat, 
			boolean isInverseComplete) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Entering parse(file) " + file.getAbsolutePath());
		}
		m_mappingFile = new SKOSMapping(file.toString());
		try {
			log.info("Opening file");
			InputStream in = new FileInputStream(file);
			log.info("Parse file");
			parse(in, rdfFormat, "", isInverseComplete);
		} catch (FileNotFoundException e) {
			log.error("Error parsing file" + e);
			throw new Exception("Could not parse vocabulary");
		} 
		if (log.isInfoEnabled()) {
			log.info("Exiting parse(file), number of mappings: " +
					m_mappingFile.size());
		}
		return m_mappingFile;
	}
	
	private void parse(InputStream in, String rdfFormat, 
			String baseURI, boolean isInverseComplete) throws Exception {
		log.debug("Entering parse()");
		try { 
			//FIXME: Need to use the correct parser for the rdfFormat
			RDFParser parser = new RDFXMLParser();
			parser.setRDFHandler(skosHandler);
			parser.setParseErrorListener(skosParseErrorListener);
			parser.parse(in, baseURI);
		} catch (IOException e) {
			log.error("Error parsing mapping file " + e);
			throw new Exception("Could not parse mapping file");
		} catch (RDFParseException e) {
			log.error("Error parsing mapping file " + e);
			throw new Exception("Could not parse mapping file");
		} catch (RDFHandlerException e) {
			log.error("Error parsing mapping file " + e);
			throw new Exception("Could not parse mapping file");
		}
		// Process the RDF triples
		processStatements(baseURI, isInverseComplete);

		if (log.isDebugEnabled()) {
			log.debug("Number of statements: " + statementCollection.size());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Exiting parse(). Number of mappings: " + m_mappingFile.size());
		}
	}

	private void processStatements(String schemeURI, 
			boolean isInverseComplete) throws Exception {
		log.debug("Entering processStatements()");
		Iterator<Statement> it = statementCollection.iterator();
		while (it.hasNext()) {
			Statement st = it.next();
			String predicate = st.getPredicate().getLocalName();
			if (predicate.equals("creator")) {
				log.trace("Adding Creator");
				m_mappingFile.setCreator(st.getObject().stringValue());
			} else if (predicate.equals("created")) {
				log.trace("Adding Created");
				m_mappingFile.setCreatedDate(st.getObject().stringValue());
			} else if (predicate.equals("title")) {
				log.trace("Adding Title");
				m_mappingFile.setTitle(st.getObject().stringValue());
			} else if (predicate.equals("exactMatch")) {	
				log.trace("Adding exactMatch mapping");
				String subject = st.getSubject().stringValue();
				String object = st.getObject().stringValue();
				m_mappingFile.addExactMatch(subject, object, isInverseComplete);
			} else if (predicate.equals("broadMatch")) {
				log.trace("Adding broadMatch mapping");
				String subject = st.getSubject().stringValue();
				String object = st.getObject().stringValue();
				m_mappingFile.addBroadMatch(subject, object, isInverseComplete);
			} else if (predicate.equals("narrowMatch")) {	
				log.trace("Adding narrowMatch mapping");
				String subject = st.getSubject().stringValue();
				String object = st.getObject().stringValue();
				m_mappingFile.addNarrowMatch(subject, object, isInverseComplete);
			} else if (predicate.equals("relatedMatch")) {	
				log.trace("Adding relatedMatch mapping");
				String subject = st.getSubject().stringValue();
				String object = st.getObject().stringValue();
				m_mappingFile.addRelatedMatch(subject, object, isInverseComplete);
			} else {
				/*
				 *  Don't want to fail when we see something 
				 *  we don't recognise
				 */
				log.warn("Unrecognised mapping: " + st.toString());
			}
		}
		log.debug("Exiting processStatements()");
	}

}
