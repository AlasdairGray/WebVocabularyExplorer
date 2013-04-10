package uk.ac.gla.dcs.explicator.vocabularies.server;

public class SKOSLiteral {
	private String m_value;
	private String m_language;
	private String m_datatype;
	
	public SKOSLiteral(String value) {
		m_value = value;
	}

	public String getValue() {
		return m_value;
	}

	public String getLanguage() {
		return m_language;
	}

	public void setLanguage(String language) {
		m_language = language;
	}
	
	public String getDatatype() {
		return m_datatype;
	}
	
	public void setDatatype(String datatype) {
		m_datatype = datatype;
	}
}
