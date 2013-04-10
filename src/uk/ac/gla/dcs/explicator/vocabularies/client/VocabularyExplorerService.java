/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author agray
 *
 */
public interface VocabularyExplorerService extends RemoteService {
	
	public Integer findConcept(String searchTerm, 
			Set<String> vocabularyConfiguration);


	public VocabularyConcept getConcept(String conceptURI, 
			Set<String> vocabularyConfiguration, 
			Set<String> mappingConfiguration);
	
	public Collection<VocabularyConcept> getSearchResults(
			String searchTerm, 
			Set<String> vocabularyConfiguration, 
			Set<String> mappingConfiguration, 
			int start, int end);

	public Map<String, VocabularyScheme> getVocabularies();
	
	public Map<String, VocabularyMapping> getMappings();
	
}
