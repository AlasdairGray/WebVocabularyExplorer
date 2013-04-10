package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.util.Collection;

import uk.ac.gla.dcs.explicator.vocabularies.client.VocabularyScheme;

public class SKOSScheme {
	
	private String m_URI;
	private String m_created;
	private Collection<String> m_topConcepts;
	private String m_vocabName;
	
	public SKOSScheme(String contextURI){ 
		m_URI = contextURI;
	}
	
	public String getURI() {
		return m_URI;
	}
	
	public void setVocabName(String vocabName) {
		m_vocabName = vocabName;
	}
	
	public String getVocabName() {
		return m_vocabName;
	}
	
	public void setCreatedDate(String created) {
		m_created = created;
	}
	
	public String getCreatedDate() {
		return m_created;
	}
	
	public String toString() {
		if (m_vocabName == null) {
			return m_URI; 
		} else {
			return m_vocabName;
		}
	}

	public void setTopConcepts(Collection<String> topConcepts) {
		m_topConcepts = topConcepts;
	}
	
	public Collection<String> getTopConcepts() {
		return m_topConcepts;
	}
	
	public VocabularyScheme toVocabularyScheme() {
		VocabularyScheme output = new VocabularyScheme(m_URI);
		output.setCreatedDate(m_created);
		output.setVocabName(m_vocabName);
		return output;
	}

}
