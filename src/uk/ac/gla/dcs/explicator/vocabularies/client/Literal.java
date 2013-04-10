package uk.ac.gla.dcs.explicator.vocabularies.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Literal implements IsSerializable {

	private String m_value;
	private String m_lang;

	public Literal() {
		// Need to have a zero argument constructor to be GWT serializable
	}
	
	public Literal(String value, String lang) {
		m_value = value;
		m_lang = lang;
	}
	
	public String getValue() {
		return m_value;
	}
	
	public String getLanguage() {
		return m_lang;
	}
	
}
