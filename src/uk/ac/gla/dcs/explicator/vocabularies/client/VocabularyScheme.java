/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author agray
 *
 */
public class VocabularyScheme implements IsSerializable {
	
	private String m_URI;
	private String m_vocabName;
	private String m_created;
	
	public VocabularyScheme() {
		// Need to have a zero argument constructor to be GWT serializable
	}
	
	public VocabularyScheme(String contextURI){ 
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

	public String print() {
		StringBuffer output = new StringBuffer();
		output.append("\n\tURI: " + m_URI);
		output.append("\n\tVocab Name: " + m_vocabName);
		return output.toString();
	}
	
}
