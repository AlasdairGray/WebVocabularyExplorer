package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import uk.ac.gla.dcs.explicator.vocabularies.client.RelatedTerm;

public class SKOSConcept {

	private Logger log= 
		Logger.getLogger(SKOSConcept.class.getName());
	
	private String m_uri;
	private Collection<SKOSLiteral> m_prefLabel;
	private Collection<SKOSLiteral> m_altLabels;
	private Collection<SKOSLiteral> m_hiddenLabels;
	
	private Collection<SKOSLiteral> m_definitions;
	
	private Collection<String> m_schemes;
	
	private Collection<RelatedTerm> m_broader;
	private Collection<RelatedTerm> m_narrower;
	private Collection<RelatedTerm> m_related;
	private Collection<SKOSLiteral> m_notations;
	private Collection<SKOSLiteral> m_scopeNotes;

	public SKOSConcept(String uri) {
		m_uri = uri;
		m_prefLabel = new Vector<SKOSLiteral>();
		m_altLabels = new Vector<SKOSLiteral>();
		m_hiddenLabels = new Vector<SKOSLiteral>();
		
		m_schemes = new Vector<String>();
		
		m_definitions = new Vector<SKOSLiteral>();
		m_scopeNotes = new Vector<SKOSLiteral>();
		m_notations = new Vector<SKOSLiteral>();
		
		m_broader = new Vector<RelatedTerm>();
		m_narrower = new Vector<RelatedTerm>();
		m_related = new Vector<RelatedTerm>();
	}

	public String getURI() {
		return m_uri;
	}
	
//	public String toString() {
//		if (m_prefLabel == null || m_prefLabel.isEmpty()) {
//			return m_uri;
//		} else {
//			return getPrefLabel();
//		}
//	}

	public Collection<SKOSLiteral> getPrefLabel() {
		return m_prefLabel;
	}
	//
//	public String getPrefLabel() {
//		String label = getPrefLabel("en");
//		if (label != null) {
//			return label;
//		}
//		label = getPrefLabel(null);
//		return label;
//	}
//	
//	private String getPrefLabel(String lang) {
//		for (int i = 0; i < m_prefLabel.size(); i++) {
//			SKOSLiteral label = m_prefLabel.get(i);
//			if (label.getLanguage().equals(lang)) {
//				return label.getValue();
//			}
//		}
//		return null;
//	}

	public void addPrefLabel(String label, String language) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setLanguage(language);
		m_prefLabel.add(literal);
	}

	public Collection<SKOSLiteral> getAltLabels() {
		return m_altLabels;
	}
	
	public void addAltLabel(String label, String language) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setLanguage(language);
		m_altLabels.add(literal);
	}

	public Collection<SKOSLiteral> getHiddenLabels() {
		return m_hiddenLabels;
	}
	
	public void addHiddenLabel(String label, String language) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setLanguage(language);
		m_hiddenLabels.add(literal);
	}

	public Collection<SKOSLiteral> getDefinitions() {
		return m_definitions;
	}
	
	public void addDefinition(String label, String language) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setLanguage(language);
		m_definitions.add(literal);
	}
	
	public Collection<SKOSLiteral> getScopeNotes() {
		return m_scopeNotes;
	}
	
	public void addScopeNote(String label, String language) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setLanguage(language);
		m_scopeNotes.add(literal);
	}
	
	public Collection<SKOSLiteral> getNotations() {
		return m_notations;
	}
	
	public void addNotation(String label, String datatype) {
		SKOSLiteral literal = new SKOSLiteral(label);
		literal.setDatatype(datatype);
		m_notations.add(literal);
	}
	
	public Collection<String> getSchemes() {
		return m_schemes;
	}

	public void addScheme(String uri) {
		m_schemes.add(uri);
	}
	
	public Collection<RelatedTerm> getBroader() {
		return m_broader;
	}
	
	public void addBroader(RelatedTerm term) {
		m_broader.add(term);
	}

	public Collection<RelatedTerm> getNarrower() {
		return m_narrower;
	}
	
	public void addNarrower(RelatedTerm term) {
		m_narrower.add(term);
	}
	
	public Collection<RelatedTerm> getRelated() {
		return m_related;
	}

	public void addRelated(RelatedTerm term) {
		m_related.add(term);
	}

	public StringReader getIndexDocument() {
		log.debug("Entering getIndexDocument()");
		StringBuilder document = new StringBuilder();
		//Start document
		document.append("<CONCEPT>");
		// Add identifier
		document.append("<URI>" + getURI() + "</URI>");
		//for each text field in concept
		document.append(getLiteralValues(m_prefLabel, "preflabel"));
		document.append(getLiteralValues(m_altLabels, "altlabel"));
		document.append(getLiteralValues(m_hiddenLabels, "hiddenlabel"));
		document.append(getLiteralValues(m_definitions, "definition"));
		document.append(getLiteralValues(m_scopeNotes, "scopenote"));
		//End document
		document.append("</CONCEPT>");
		if (log.isDebugEnabled()) {
			log.debug("Exiting getIndexDocument(): " + document.toString());
		}
		return new StringReader(document.toString());
	}

	private String getLiteralValues(Collection<SKOSLiteral> labels, 
			String label) {
		if (log.isTraceEnabled()) {
			log.trace("Entering getLiteralValues() for " + labels.size() + 
					" " + label);
		}
		StringBuilder document = new StringBuilder();
		Iterator<SKOSLiteral> it = labels.iterator();
		while (it.hasNext()) {
			SKOSLiteral literal = it.next();
			String lang = literal.getLanguage();
			if (lang == null || lang.equals("en")) {
				document.append("<" + label + ">");
				document.append(literal.getValue());//.append(' ');
				document.append("</" + label + ">");
			}
		}
		if (log.isTraceEnabled()) {
			log.trace("Exiting getLiteralValues() with \n" + document.toString());
		}
		return document.toString();
	}

}
