/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author agray
 *
 */
public interface VocabularyExplorerServiceAsync {

	public void findConcept(String searchTerm, 
			Set<String> vocabularyConfiguration, 
			AsyncCallback<Integer> callBack);
	
	public void getConcept(String conceptURI, 
			Set<String> vocabularyConfiguration, 
			Set<String> mappingConfiguration, 
			AsyncCallback<VocabularyConcept> callBack);
	
	public void getSearchResults(String searchTerm, 
			Set<String> vocabularyConfiguration, 
			Set<String> mappingConfiguration, 
			int start, int end, 
			AsyncCallback<Collection<VocabularyConcept>> callback);

	public void getVocabularies(
			AsyncCallback<Map<String, VocabularyScheme>> callback);
	
	public void getMappings(
			AsyncCallback<Map<String, VocabularyMapping>> callback);
	
}
