/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author agray
 *
 */
public class VocabularyMapping implements IsSerializable {
	
	private String m_URI;
	private String m_title;
	private String m_created;
	private String m_creator;
	
	public VocabularyMapping() {
		// Need to have a zero argument constructor to be GWT serializable
	}
	
	public VocabularyMapping(String url){ 
		m_URI = url;
	}
	
	public String getURI() {
		return m_URI;
	}
	
	public void setTitle(String title) {
		m_title = title;
	}
	
	public String getTitle() {
		return m_title;
	}
	
	public void setCreatedDate(String created) {
		m_created = created;
	}
	
	public String getCreatedDate() {
		return m_created;
	}
	
	public void setCreator(String creator) {
		m_creator = creator;
	}
	
	public String getCreator() {
		return m_creator;
	}
	
}
