package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the available vocabularies and mappings as well as user
 * configurable settings. The user can select which vocabularies and
 * mappings should be used to conduct their search.
 */
public class ConfigurationPanel extends VerticalPanel {
	
	private Map<String, VocabularyScheme> m_vocabs;
	private Map<String, VocabularyMapping> m_mappings;
	
	private Set<String> m_vocabConfig;
	private Set<String> m_mappingsConfig;
	private Button m_searchButton;
	private DisclosurePanel vocabsPanel;
	private DisclosurePanel mappingsPanel;
	
	private DisclosurePanel settingsPanel;
	private RadioButton relationshipsRadioButton;
	private boolean m_isVocabCallbackComplete;
	private boolean m_isMappingCallbackComplete;
	private RadioButton mappingsRadioButton;
	
	private int m_resultsPerPage = 10;
	
	/**
	 * Initialise the configuration panel
	 * @param vocabExplorerService 
	 * @param searchButton enabled once vocabularies and mappings are available
	 */
	public ConfigurationPanel(VocabularyExplorerServiceAsync vocabExplorerService, 
			Button searchButton) {
		m_searchButton = searchButton;
		m_isVocabCallbackComplete = false;
		m_isMappingCallbackComplete = false;
		
		// Create vocabularies disclosure panel
		vocabsPanel = new DisclosurePanel("Vocabularies");	
		mappingsPanel = new DisclosurePanel("Mappings");
		
		// Calls for details about available vocabularies and mappings
		vocabExplorerService.getVocabularies(new VocabulariesCallBack());
		vocabExplorerService.getMappings(new MappingsCallBack());
		
		/*
		 * Initialise maps that will store the vocabulary and 
		 * mapping details
		 */
		m_vocabs = new HashMap<String, VocabularyScheme>();
		m_mappings = new HashMap<String, VocabularyMapping>();
		
		// Initialise configuration sets
		m_vocabConfig = new HashSet<String>();
		m_mappingsConfig = new HashSet<String>();
		m_resultsPerPage = 10;
		
		// Create settings disclosure panel
		settingsPanel = new DisclosurePanel("Settings");
		settingsPanel.addEventHandler(
				new SettingsDisclosureEventHandler());
		settingsPanel.setContent(initSettingsPanel());
		
		// Add panels for displaying the details
		add(initHeader());
		add(vocabsPanel);
		add(mappingsPanel);
		add(settingsPanel);
		
		setBorderWidth(1);		
	}
	
	public int getResultsPerPage() {
		return m_resultsPerPage;
	}

	private Widget initHeader() {
		HTML headerPane = new HTML("<h2>Configuration</h2>");
		return headerPane;
	}
	
	private Widget initSettingsPanel() {
		VerticalPanel settingsPane = new VerticalPanel();
		HTML htmlText = new HTML("<p>Display mappings as:</p>");
		
		// Create radio buttons
		relationshipsRadioButton = new RadioButton("mappingDisplay", 
				"Relationships");
		mappingsRadioButton = new RadioButton("mappingDisplay", "Mappings");
		
		// Initial configuration is to have the mappings displayed in line
		relationshipsRadioButton.setChecked(true);
		
		// Add sections to panel
		settingsPane.add(htmlText);
		settingsPane.add(relationshipsRadioButton);
		settingsPane.add(mappingsRadioButton);
		return settingsPane;
	}
	
	/**
	 * Test whether mapping relationships should be displayed as
	 * normal relationships or as separate mapping relationships
	 * 
	 * @return true if displayed with normal relationships
	 */
	public boolean displayAsRelationships() {
		//FIXME: Can we push this so that display is updated dynamically
		return relationshipsRadioButton.isChecked();
	}

	/**
	 * Get the scheme URIs for the vocabularies that have been
	 * selected
	 * @return
	 */
	public Set<String> getVocabConfiguration() {
		return m_vocabConfig;
	}
	
	/**
	 * Get the context URI for the selected vocabularies
	 * @return
	 */
	public Set<String> getSelectedVocabURIs() {
//		GWT.log("Select vocabularies: " + printSet(m_vocabConfig), null);
		return m_vocabConfig;
	}
	
	/**
	 * Get the title of the vocabulary
	 * @param vocab the scheme URI of the vocabulary
	 * @return
	 */
	public String getVocabName(String vocab) {
		VocabularyScheme vDetails = (VocabularyScheme) m_vocabs.get(vocab);
		return vDetails.toString();
	}
	
	/**
	 * Get the context URIs for the mappings that have been selected
	 * @return
	 */
	public Set<String> getMappingConfiguration() {
		return m_mappingsConfig;
	}
		
	protected String printConfiguration() {
		StringBuffer out = new StringBuffer();
		out.append("\n\nVocabularies\n");
		out.append(printSet(m_vocabConfig));
		out.append("\n\nMappings\n");
		out.append(printSet(m_mappingsConfig));
		return out.toString();
	}
	
	private String printSet(Set<String> set) {
		StringBuffer out = new StringBuffer();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			out.append("\n\t" + it.next());
		}
		return out.toString();
	}

	public class VocabulariesCallBack 
	implements AsyncCallback<Map<String, VocabularyScheme>> {

		public void onFailure(Throwable caught) {
			GWT.log("Error ", caught);
			caught.printStackTrace();
		}

		public void onSuccess(Map<String, VocabularyScheme> result) {
			GWT.log("Number of vocabularies: " + result.size(), null);
			
			// Store vocabulary details and initially select all
			m_vocabs = result;
			Map<String, VocabularyDescriptor> vocabs = 
				extractDescriptions(m_vocabs);
			m_vocabConfig.addAll(result.keySet());
			GWT.log("Configuration: " + printConfiguration(), null);
			
			// Add content to disclosure panel
			vocabsPanel.addEventHandler(new DetailsDisclosureEventHandler(
					vocabsPanel, vocabs, m_vocabConfig));
			
			/*
			 * Enable search button once mappings and vocabularies 
			 * have been retrieved and processed
			 */
			m_isVocabCallbackComplete = true;
			if (m_isMappingCallbackComplete) {
				m_searchButton.setEnabled(true);
				/*
				 *  To allow someone to link into a page for a vocabulary 
				 *  concept we need to call the history to check the incoming
				 *  URL
				 */
				History.fireCurrentHistoryState();
			}			
		}

		private Map<String, VocabularyDescriptor> extractDescriptions(
				Map<String, VocabularyScheme> m_vocabs) {
			Map<String, VocabularyDescriptor> vocabs = 
				new HashMap<String, VocabularyDescriptor>();
			Set<String> keys = m_vocabs.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				VocabularyScheme vocab = m_vocabs.get(key);
				String title = vocab.getVocabName();
				VocabularyDescriptor descriptor = 
					new VocabularyDescriptor(key, title);
				vocabs.put(key, descriptor);
			}
			return vocabs;
		}

	}

	public class MappingsCallBack 
	implements AsyncCallback<Map<String, VocabularyMapping>> {

		public void onFailure(Throwable caught) {
			GWT.log("Error ", caught);
			caught.printStackTrace();
		}

		public void onSuccess(Map<String, VocabularyMapping> result) {
			GWT.log("Number of mappings: " + result.size(), null);
			
			// Store vocabulary details and initially select all
			m_mappings = result;
			Map<String, VocabularyDescriptor> mappings = 
				extractDescriptions(m_mappings);
			m_mappingsConfig.addAll(result.keySet());
			GWT.log("Configuration: " + printConfiguration(), null);
			
			// Add content to disclosure panel
			mappingsPanel.addEventHandler(new DetailsDisclosureEventHandler(
					mappingsPanel, mappings, m_mappingsConfig));
			
			/*
			 * Enable search button once mappings and vocabularies 
			 * have been retrieved and processed
			 */
			m_isMappingCallbackComplete = true;
			if (m_isVocabCallbackComplete) {
				m_searchButton.setEnabled(true);
				/*
				 *  To allow someone to link into a page for a vocabulary 
				 *  concept we need to call the history to check the incoming
				 *  URL
				 */
				History.fireCurrentHistoryState();
			}
		}
		
		private Map<String, VocabularyDescriptor> extractDescriptions(
				Map<String, VocabularyMapping> m_mappings) {
			Map<String, VocabularyDescriptor> mappings = 
				new HashMap<String, VocabularyDescriptor>();
			Set<String> keys = m_mappings.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				VocabularyMapping mapping = m_mappings.get(key);
				String title = mapping.getTitle();
				VocabularyDescriptor descriptor = 
					new VocabularyDescriptor(key, title);
				mappings.put(key, descriptor);
			}
			return mappings;
		}

	}

	/**
	 * Handles the display of the vocabularies and the check boxes
	 *
	 */
	public class DetailsDisclosureEventHandler 
	implements DisclosureHandler, ClickListener {

		private Map<String, CheckBox> m_checkBoxes;
		private Set<String> m_config;
		private Map<String, VocabularyDescriptor> m_details;
		private DisclosurePanel m_panel;
		
		private String selectAllText = "Select All";
		private String clearAllText = "Clear All";

		public DetailsDisclosureEventHandler(DisclosurePanel dPanel, 
				Map<String, VocabularyDescriptor> details, 
				Set<String> config) {
			m_panel = dPanel;
			m_details = details;
			m_config = config;
			m_checkBoxes = new HashMap<String, CheckBox>();
		}

		public void onClose(DisclosureEvent event) {
			m_panel.setOpen(false);
		}

		public void onOpen(DisclosureEvent event) {
			m_panel.setContent(displayDetails());
			m_panel.setOpen(true);
		}

		private Panel displayDetails() {
			Iterator<VocabularyDescriptor> it = m_details.values().iterator();
			VerticalPanel detailsPanel = new VerticalPanel();
			/*
			 * Create a selection box next to each vocabulary  
			 * if the vocabulary is in the selected vocabularies
			 * then the box is checked
			 */ 
			while (it.hasNext()) {
				VocabularyDescriptor entry = it.next();
				// Retrieve entry details
				String entryName = entry.getTitle();
				String entryID = entry.getURI();

				// Create check box for the entry
				CheckBox checkBox = new CheckBox(entryName, true);
				// Identify the check box with the entry's URL
				checkBox.setName(entryID);
				// Create a listener for the check box
				checkBox.addClickListener(this);
				// Add to the list of check boxes known to the dialog box
				m_checkBoxes.put(entryID, checkBox);

				if (m_config.contains(entryID)) {
					checkBox.setChecked(true);
				}

				// Add check box to display
				detailsPanel.add(checkBox);
			}
			// Display the selection buttons
			Panel selectionButtonsPane = initSelectionButtons();		
			detailsPanel.add(selectionButtonsPane);
			return detailsPanel;
		}

		/**
		 * Initialise the select all and clear all buttons
		 * 
		 * @return a panel containing the buttons
		 */
		private Panel initSelectionButtons() {
			HorizontalPanel vocabButtonsPane = new HorizontalPanel();
			// Select all and clear all buttons
			Button selectAll = new Button(selectAllText, this);
			Button clearAll = new Button(clearAllText, this);				
			vocabButtonsPane.add(selectAll);
			vocabButtonsPane.add(clearAll);
			return vocabButtonsPane;
		}

		public void onClick(Widget sender) {
			if (sender instanceof Button) {
				// Perform action for button pressed
				Button senderButton = (Button) sender;
				GWT.log("Button pressed " + senderButton.getText(), null);
				if (senderButton.getText().equals(selectAllText)) {
					// Select all check boxes
					GWT.log("Select all button clicked", null);
					updateAllCheckBoxes(true);
				} else if (senderButton.getText().equals(clearAllText)) {
					// Clear all check box selections
					GWT.log("Clear all button clicked", null);
					updateAllCheckBoxes(false);
				}
			} else {
				// Update individual check box selected
				CheckBox cb = (CheckBox) sender;
				GWT.log("Check box clicked " + cb.getName(), null);
				if (cb.isChecked()) {
					m_config.add(cb.getName());
				} else { //if (!cb.isChecked() && !isMapping) {
					m_config.remove(cb.getName());
				}
			}
			GWT.log("configuration: " + printConfiguration(), null);
		}
		
		/**
		 * Set all the check boxes to either true or false
		 * 
		 * @param selected
		 */
		private void updateAllCheckBoxes(boolean selected) {
			Set<String> schemes = m_checkBoxes.keySet();
//			GWT.log("Scheme size: " + schemes.size() + " Config size: " + m_config.size(), null);
			Iterator<String> it = schemes.iterator();
			while (it.hasNext()) {
				String scheme = (String) it.next();
//				GWT.log("Scheme: " + scheme, null);
				CheckBox cb = (CheckBox) m_checkBoxes.get(scheme);
				cb.setChecked(selected);
				if (selected) {
					m_config.add(scheme);
				} else {
					m_config.remove(scheme);
				}
			}	
//			GWT.log("Config size: " + m_config.size(), null);
		}

	}

	/**
	 * Handles the display of user configurable settings and the
	 * associated radio buttons
	 *
	 */
	public class SettingsDisclosureEventHandler implements DisclosureHandler {		

		public void onClose(DisclosureEvent event) {
			settingsPanel.setOpen(false);
		}

		public void onOpen(DisclosureEvent event) {			
			settingsPanel.setOpen(true);
		}

	}

}
