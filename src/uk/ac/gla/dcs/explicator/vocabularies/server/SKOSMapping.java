/**
 * 
 */
package uk.ac.gla.dcs.explicator.vocabularies.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author agray
 *
 */
public class SKOSMapping {
	
	private String m_URI;
	private String m_title;
	private String m_created;
	private String m_creator;
	
	/*
	 * Using maps of lists to represent mappings in order to speed up 
	 * retrieval of mapped to objects for a specific concept URI 
	 * (the stored key in the map).
	 */
	/**
	 * Contains the exact match mappings stored by subject uri 
	 */
	private Map<String,List<String>> m_exactMatches;
	/**
	 * Contains the broad match mappings stored by subject uri 
	 */
	private Map<String,List<String>> m_broadMatches;
	/**
	 * Contains the narrow match mappings stored by subject uri 
	 */
	private Map<String,List<String>> m_narrowMatches;
	/**
	 * Contains the related match mappings stored by subject uri 
	 */
	private Map<String,List<String>> m_relatedMatches;
	
	public SKOSMapping(String url){ 
		m_URI = url;
		m_exactMatches = new HashMap<String, List<String>>();
		m_broadMatches = new HashMap<String, List<String>>();
		m_narrowMatches = new HashMap<String, List<String>>();
		m_relatedMatches = new HashMap<String, List<String>>();
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
	
	public Map<String, List<String>> getExactMatches() {
		return m_exactMatches;
	}
	
	public Map<String, List<String>> getBroadMatches() {
		return m_broadMatches;
	}
	
	public Map<String, List<String>> getNarrowMatches() {
		return m_narrowMatches;
	}
	
	public Map<String, List<String>> getRelatedMatches() {
		return m_relatedMatches;
	}

	/**
	 * Returns the total number of mappings, including inferred inverse mappings,
	 * declared in the mapping file
	 * @return number of mappings
	 */
	public int size() {
		int result = 0;
		result += numMappings(m_exactMatches);
		result += numMappings(m_broadMatches);
		result += numMappings(m_narrowMatches);
		result += numMappings(m_relatedMatches);
		return result;
	}
	
	/**
	 * Calculates the size of a map by counting the number of value entries
	 * in each of the lists in the map
	 * @param map
	 * @return number of items that are involved in the specific kind of mapping
	 */
	private int numMappings(Map<String, List<String>> map) {
		int count = 0;
		Collection<List<String>> valueset = map.values();
		Iterator<List<String>> it = valueset.iterator();
		while (it.hasNext()) {
			List<String> entry = it.next();
			count += entry.size();
		}
		return count;
	}

	public void addExactMatch(String subject, String object,
			boolean isInverseComplete) {
		addMapping(m_exactMatches, subject, object);
		if (!isInverseComplete) {
			// exact match is the inverse of exact match
			addMapping(m_exactMatches, object, subject);
		}
	}

	public void addBroadMatch(String subject, String object,
			boolean isInverseComplete) {
		addMapping(m_broadMatches, subject, object);
		if (!isInverseComplete) {
			// narrow match is the inverse of broad match
			addMapping(m_narrowMatches, object, subject);
		}
	}

	public void addNarrowMatch(String subject, String object,
			boolean isInverseComplete) {
		addMapping(m_narrowMatches, subject, object);
		if (!isInverseComplete) {
			// broad match is the inverse of narrow match
			addMapping(m_broadMatches, object, subject);
		}
	}

	public void addRelatedMatch(String subject, String object,
			boolean isInverseComplete) {
		addMapping(m_relatedMatches, subject, object);
		if (!isInverseComplete) {
			// related match is the inverse of related match
			addMapping(m_relatedMatches, object, subject);
		}
	}

	private void addMapping(Map<String, List<String>> matches, String subject,
			String object) {
//		log.trace("Entering addMapping() with " + subject + " " + object);
		List<String> objects;
		if (matches.containsKey(subject)) {
			objects = matches.get(subject);
		} else {
			objects = new ArrayList<String>();
		}
		objects.add(object);
		matches.put(subject, objects);
//		log.trace("Exiting addMapping()");
	}
	
}
