package uk.ac.gla.dcs.explicator.vocabularies.client;

public class VocabularyDescriptor {

	protected String m_URI;
	private String m_title;

	public VocabularyDescriptor() {
	}
	
	public VocabularyDescriptor(String uri, String title) {
		m_URI = uri;
		m_title = title;
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

	public String toString() {
		if (m_title == null) {
			return m_URI; 
		} else {
			return m_title;
		}
	}

	public String print() {
		StringBuffer output = new StringBuffer();
		output.append("\n\tURI: " + m_URI);
		output.append("\n\tVocab Name: " + m_title);
		return output.toString();
	}

}