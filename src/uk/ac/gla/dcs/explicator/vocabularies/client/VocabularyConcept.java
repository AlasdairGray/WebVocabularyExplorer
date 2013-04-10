package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.user.client.rpc.IsSerializable;

public class VocabularyConcept implements IsSerializable {
	
	private String m_uri;
	private Collection<Literal> m_altLabels;
	private Collection<Literal> m_prefLabels;
	private Collection<Literal> m_definition;
	private Collection<Literal> m_scopeNotes;
	private Collection<String> m_inSchemes;
	private Collection<RelatedTerm> m_broader;
	private Collection<RelatedTerm> m_narrower;
	private Collection<RelatedTerm> m_related;
	private Collection<RelatedTerm> m_exactMatches;
	private Collection<RelatedTerm> m_broadMatches;
	private Collection<RelatedTerm> m_narrowMatches;
	private Collection<RelatedTerm> m_relatedMatches;

	
	public VocabularyConcept() {
		// Need to have a zero argument constructor to be GWT serializable
	}
	
	public VocabularyConcept(String uri, 
			Collection<Literal> prefLabels, 
			Collection<Literal> altLabels, 
			Collection<Literal> definition, 
			Collection<Literal> scopeNotes, 
			Collection<String> inSchemes, 
			Collection<RelatedTerm> broader, 
			Collection<RelatedTerm> narrower, 
			Collection<RelatedTerm> related, 
			Collection<RelatedTerm> exactMatches, 
			Collection<RelatedTerm> broadMatches, 
			Collection<RelatedTerm> narrowMatches, 
			Collection<RelatedTerm> relatedMatches) {
		m_uri = uri;
		m_prefLabels = prefLabels;
		m_altLabels = altLabels;
		m_definition = definition;
		m_scopeNotes = scopeNotes;
		m_inSchemes = inSchemes;
		m_broader = broader;
		m_narrower = narrower;
		m_related = related;
		m_exactMatches = exactMatches;
		m_broadMatches = broadMatches;
		m_narrowMatches = narrowMatches;
		m_relatedMatches = relatedMatches;
	}
	
	public String getUri() {
		return m_uri;
	}

	public Collection<Literal> getPrefLabels() {
		return m_prefLabels;
	}

	public Collection<Literal> getAltLabels() {
		return m_altLabels;
	}

	public Collection<Literal> getDefinition() {
		return m_definition;
	}

	public Collection<Literal> getScopeNotes() {
		return m_scopeNotes;
	}

	public Collection<String> getInSchemes() {
		return m_inSchemes;
	}

	public Collection<RelatedTerm> getBroaderTerms() {
		return m_broader;
	}

	public Collection<RelatedTerm> getNarrowerTerms() {
		return m_narrower;
	}

	public Collection<RelatedTerm> getRelatedTerms() {
		return m_related;
	}
	
	public Collection<RelatedTerm> getExactMatches() {
		return m_exactMatches;
	}
	
	public Collection<RelatedTerm> getBroadMatches() {
		return m_broadMatches;
	}
	
	public Collection<RelatedTerm> getNarrowMatches() {
		return m_narrowMatches;
	}

	public Collection<RelatedTerm> getRelatedMatches() {
		return m_relatedMatches;
	}
	
	public String toString() {
		String output = m_uri;
		if (m_prefLabels != null) {
			Iterator<Literal> it = m_prefLabels.iterator();
			while (it.hasNext()) {
				Literal literal = it.next();
				String lang = literal.getLanguage();
				if (lang == null || lang.equals("en")) {
					output = literal.getValue();
				}
			}
		}
		return output;
	}

}
