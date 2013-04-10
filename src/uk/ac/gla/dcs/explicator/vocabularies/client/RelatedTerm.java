package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RelatedTerm implements IsSerializable {

	private String m_uri;
	private Collection<Literal> m_prefLabels;
	
	public RelatedTerm() {
		// Need to have a zero argument constructor to be GWT serializable
	}
	
	public RelatedTerm(String uri) {
		m_uri = uri;
	}
	
	public String getUri() {
		return m_uri;
	}
	
	public String getPrefLabel() {
		String prefLabel = null;
		if (m_prefLabels != null) {
			Iterator<Literal> it = m_prefLabels.iterator();
			while (it.hasNext()) {
				Literal literal = it.next();
				String lang = literal.getLanguage();
				if (lang == null || lang.equals("en")) {
					prefLabel = literal.getValue();
				}
			}
		}
		return prefLabel;
	}
	
	public String toString() {
		if (m_prefLabels != null || !m_prefLabels.isEmpty()) {
			return getPrefLabel();
		} else {
			return m_uri;
		}
	}

	public void setPrefLabel(Collection<Literal> prefLabels) {
		m_prefLabels = prefLabels;
	}
	
}
