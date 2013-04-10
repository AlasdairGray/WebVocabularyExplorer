package uk.ac.gla.dcs.explicator.vocabularies.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebVocabularyExplorer implements EntryPoint, HistoryListener {
	//TODO: Tidy up this class!
	//TODO: Add a mechanism for exploring a vocabulary from the top level concepts down
	
	private DockPanel panel;
	private VerticalPanel topPanel;
	private FlowPanel infoPanel;
	
	//Removing configuration options for now
	private ConfigurationPanel configurationPanel;
	private VerticalPanel resultPanel;
	private HorizontalPanel pageNavigationPanel;

	private VocabularyExplorerServiceAsync vocabExplorerService;
	private ServiceDefTarget target;
	private String relativeUrl;
	
	private Button searchButton;
	private TextBox searchBox;
	private String m_searchTerm;

	private HTML infoPanelMessage;

	private DisclosurePanel m_conceptDisplayPanel;

	private static final String m_startMessage = 
		"<p>Enter an astronomical search term and select search</p>";
	
	// Result display parameters
	private int m_resultSize;
	private int m_numPages;
	private int m_currentPage;

	private HorizontalPanel searchPanel;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		//TODO: Refactor code to isolate search and browse features
		m_searchTerm = "";
		initService();
		/*
		 * Initialise and disable search button here so that 
		 * it can be passed to the configuration panel to be 
		 * enabled when the vocabularies and mappings have loaded
		 */
		searchButton = new Button("Search"); 
		searchButton.setEnabled(false);
		
		/* Initialise the configuration panel and retrieve
		 * vocabularies and mappings
		 */ 
		configurationPanel = new ConfigurationPanel(vocabExplorerService, 
				searchButton);
		// Create history listener
		History.addHistoryListener(this);
		
		// Initialise panels that make up the interface
		initMainPanel();
		initTopPanel();	
		initResultPanel();
		initPageNavPanel();
		
		// Configure widths of display panels
		panel.setCellWidth(topPanel, "70%");
		panel.setCellWidth(configurationPanel, "40%");
		panel.setCellHorizontalAlignment(configurationPanel, 
				HasHorizontalAlignment.ALIGN_RIGHT);
		topPanel.setCellHorizontalAlignment(infoPanel, 
				HasHorizontalAlignment.ALIGN_RIGHT);
		
		// Display panel 
		RootPanel.get("conceptSearch").add(panel);
	}
	
	/**
	 * This keeps track of the history
	 */
	public void onHistoryChanged(String historyToken) {
		GWT.log("Entering onHistoryChanged() with " + historyToken, null);
		
		// Safari history strings contains '%3D' instead of '='		
		historyToken = URL.decodeComponent(historyToken);
		GWT.log("Decoded history token: " + historyToken, null);
		
		// Do different things for a search and a browse and no match
		if (historyToken.startsWith("search=")) {
			/* 
			 * Retrieve the search term from the history token 
			 * and repeat search
			 */
			GWT.log("Performing a search", null);
			GWT.log("Configuration: " + configurationPanel.printConfiguration(), 
					null);
			callForFindMatchingConcepts(historyToken.substring(7));
		} else if (historyToken.startsWith("browse=")) {
			GWT.log("Performing a browse", null);
			// Retrieve the uri and the concept name from history token
			String uri = historyToken.substring(7);
			GWT.log("Browsing concept " + uri, null);
			// Retrieve last seen concept
			callForGetConceptDetails(uri);
		} else {
			// Clear the results display
			GWT.log("No matching history", null);
			clearAll();
		}
		GWT.log("Exiting onHistoryChanged()", null);
	}

	/**
	 * 
	 */
	private void initService() {
		vocabExplorerService = (VocabularyExplorerServiceAsync) 
			GWT.create(VocabularyExplorerService.class);
		target = (ServiceDefTarget) vocabExplorerService;
		relativeUrl = GWT.getModuleBaseURL() + "vocabExplorer";
		target.setServiceEntryPoint(relativeUrl);
	}

	/**
	 * 
	 */
	private void initMainPanel() {
		panel = new DockPanel();
		panel.setWidth("100%");
//		panel.setBorderWidth(1);
		panel.add(configurationPanel, DockPanel.EAST);
	}
	
	private void initTopPanel() {
		topPanel = new VerticalPanel();
		topPanel.setWidth("100%");
//		topPanel.setBorderWidth(1);
		panel.add(topPanel, DockPanel.NORTH);
		initSearchPanel();
		initInfoPanel();
	}
	
	private void initInfoPanel() {
		infoPanel = new FlowPanel();
		infoPanelMessage = new HTML(m_startMessage);
		infoPanel.add(infoPanelMessage);
		topPanel.add(infoPanel);
	}

	private void initPageNavPanel() {
		pageNavigationPanel = new HorizontalPanel();
		pageNavigationPanel.setWidth("100%");
		panel.add(pageNavigationPanel, DockPanel.SOUTH);
	}
	
	public void initSearchPanel() {
		searchPanel = new HorizontalPanel();
		searchPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

		searchBox = new TextBox();
		searchBox.addKeyboardListener(new SearchBoxListener());
		searchPanel.add(searchBox);	

		// Button initialised in onModuleLoad()
		searchButton.addClickListener(new SearchClickListener());
		searchPanel.add(searchButton);

//		searchPanel.setBorderWidth(1);
		
		topPanel.add(searchPanel);
	}
	
	private void initResultPanel() {
		m_conceptDisplayPanel = null;
		resultPanel = new VerticalPanel();		
		resultPanel.setWidth("100%");
		resultPanel.setBorderWidth(1);
		panel.add(resultPanel, DockPanel.CENTER);
	}
	
	private void clearAll() {
		clearPageNavigation();
		resultPanel.clear();
		searchBox.setText("");
		infoPanelMessage.setHTML(m_startMessage);
		searchBox.setFocus(true);
	}
	
	private void clearPageNavigation() {
		pageNavigationPanel.clear();
	}

	public void callForDisplayFoundConcepts(int displayPage) {
		GWT.log("Entering callForDisplayFoundConcepts with page " + displayPage, null);
		Set<String> vocabConfig = configurationPanel.getVocabConfiguration();
		// Check that some vocabularies have been selected
		GWT.log("Number of selected vocabularies: " + vocabConfig.size(), null);
		if (vocabConfig.isEmpty()) {
			GWT.log("No vocabularies selected.", null);
			String text = "Please select a vocabulary from the list " +
					"in the configuration pane";
			ErrorPopup errorPopup = new ErrorPopup(text);
			int left = searchBox.getAbsoluteLeft() + 10;
			int top = searchBox.getAbsoluteTop() + 10;
			errorPopup.setPopupPosition(left, top);
			errorPopup.show();
		} else {
			m_currentPage = displayPage;
			int resultsPerPage = configurationPanel.getResultsPerPage();
			// Calculate the start and end offsets.
			int start = ((displayPage - 1) * resultsPerPage) + 1;
			int end = calculateEnd(start, resultsPerPage);
			Set<String> mappingConfig = configurationPanel.getMappingConfiguration();
			//FIXME: Need to update the number of return results to capture the case that the configuration has been changed between pages of results
			vocabExplorerService.getSearchResults(m_searchTerm, vocabConfig, 
					mappingConfig, start, end, new DisplayResultsCallback());
			// Update info panel
			infoPanelMessage.setHTML("<p style=\"text-align: right;\">" +
					"Results " + start + " - " + end + " of " + m_resultSize + 
					" for \"" + m_searchTerm + "\"</p>");
			m_numPages = calculateNumberRequiredResultPages(resultsPerPage);
			GWT.log("Number of pages: " + m_numPages, null);
		}
		GWT.log("Exiting callForDisplayFoundConcepts", null);
	}
	
	/**
	 * Calculates the end offset for the required display page and
	 * returns the lower of that value and the number of search results.
	 * @param start offset to the first result for the desired page
	 * @param resultsPerPage number of results displayed per page
	 * @return end offset for the page
	 */
	private int calculateEnd(int start, int resultsPerPage) {
		int end = start + resultsPerPage - 1;
		return Math.min(end, m_resultSize);
	}
	
	/**
	 * Calculate the number of pages required to display the results
	 * @param resultsPerPage number of results to display per page
	 * @return number of result pages
	 */
	private int calculateNumberRequiredResultPages(int resultsPerPage) {
		// Calculate the number of pages needed to display the results
		if (m_resultSize%resultsPerPage==0) {
			// Number exactly divisible by results per page
			return (int) Math.ceil(m_resultSize/resultsPerPage);
		} else {
			// Need an extra page to deal with the remainder
			return (int) Math.ceil(m_resultSize/resultsPerPage) + 1;
		}
	}

	public void callForFindMatchingConcepts(String searchTerm) {
		GWT.log("Entering callForFindMatchingConcepts() with " + 
				searchTerm, null);
		
		// Check that at least one vocabulary has been chosen
		Set<String> vocabConfiguration = configurationPanel.getVocabConfiguration();
		GWT.log("Number of selected vocabularies: " + vocabConfiguration.size(), 
				null);
		if (vocabConfiguration.isEmpty()) {
			GWT.log("No vocabularies selected.", null);
			String text = "Please select a vocabulary from the list " +
					"on the right";
			ErrorPopup errorPopup = new ErrorPopup(text);
			int left = searchBox.getAbsoluteLeft() + 10;
			int top = searchBox.getAbsoluteTop() + 10;
			errorPopup.setPopupPosition(left, top);
			errorPopup.show();
		} else {
			m_searchTerm = searchTerm;
			// Pass the configuration as a parameter to the search
			vocabExplorerService.findConcept(m_searchTerm, vocabConfiguration, 
					new FoundConceptsCallback());
			processingUserRequest("Searching for \"" + m_searchTerm + "\"");
		}
		GWT.log("Exiting callForFindMatchingConcepts()", null);
	}

	public void callForGetConceptDetails(String conceptUri) {
		GWT.log("Entering callForGetConceptDetails() with " + 
				conceptUri, null);
		// Get current vocab URIs
		Set<String> vocabURIs = configurationPanel.getSelectedVocabURIs();
		// Get current mapping configuration
		Set<String> mappingConfig = configurationPanel.getMappingConfiguration();
//		GWT.log("Number of mappings to use: " + mappingConfig.size(), null)
		vocabExplorerService.getConcept(conceptUri, vocabURIs, mappingConfig, 
				new DisplayConceptCallback());
		processingUserRequest("Getting concept details for \"" + 
				conceptUri + "\"");
		GWT.log("Exiting callForGetConceptDetails()", null);
	}
	
	/**
	 * @param displayString Information to be displayed to the user
	 * 
	 */
	private void processingUserRequest(String displayString) {
		GWT.log("Entering processingUserRequest()", null);
		infoPanelMessage.setHTML("<p>" + displayString + "...</p>");
		try {
			resultPanel.clear();
		} catch (Exception e){
		}
		GWT.log("Exiting processingUserRequest()", null);
	}

	public class SearchClickListener implements ClickListener {
		public void onClick(Widget sender) {			
			String searchText = searchBox.getText();
			// Check that at least one vocabulary has been chosen
			Set<String> vocabConfiguration = 
				configurationPanel.getVocabConfiguration();
			GWT.log("Number of selected vocabularies: " + 
					vocabConfiguration.size(), null);
			if (vocabConfiguration.isEmpty()) {
				GWT.log("No vocabularies selected.", null);
				String text = "Please select a vocabulary from the list " +
						"on the right";
				ErrorPopup errorPopup = new ErrorPopup(text);
				int left = searchBox.getAbsoluteLeft() + 10;
				int top = searchBox.getAbsoluteTop() + 10;
				errorPopup.setPopupPosition(left, top);
				errorPopup.show();
			} else if (searchText.length() == 0) {
				GWT.log("No search text entered", null);
				String text = "Please enter a search term";
				ErrorPopup errorPopup = new ErrorPopup(text);
				int left = sender.getAbsoluteLeft() + 10;
				int top = sender.getAbsoluteTop() + 10;
				errorPopup.setPopupPosition(left, top);
				errorPopup.show(); 
			} else {
				History.newItem("search=" + searchBox.getText());
			}
		}
	}
	
	public class SearchBoxListener extends KeyboardListenerAdapter {
	      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
	    	  if (keyCode == (char) KEY_ENTER) {
	    		  // Mimic the user clicking the button with the mouse
	    		  searchButton.click();
	    	  }
	      }
	}
	
	/**
	 * @param concept
	 * @return 
	 * @return
	 */
	Panel displayConceptDetails(VocabularyConcept concept) {
		Panel detailsPanel = new VerticalPanel();
		// Order of call determines order of display
		
		// Display alternative labels
		// TODO: Should exactMatch be shown here as they are somehow the alt label mapping? 
		detailsPanel.add(displayLexicalDetails(concept.getAltLabels(), 
				"Alternative Labels"));
		
		// Display definition and scope
		detailsPanel.add(displayLexicalDetails(concept.getDefinition(), 
				"Definitions"));
		detailsPanel.add(displayLexicalDetails(concept.getScopeNotes(), 
				"Scope Notes"));

		// Display vocabulary schemes
		detailsPanel.add(displayVocabularyDetails(concept.getInSchemes()));
		
		// Equivalent concepts always displayed in their own section
		detailsPanel.add(displayRelationshipDetails(concept.getExactMatches(), 
				"Equivalent Concepts"));

		// Display mappings based on user preference
		boolean displayAsRelationships = 
			configurationPanel.displayAsRelationships();
		GWT.log("Mappings display configuration. asRelationships: " + 
				displayAsRelationships, null);
				if (displayAsRelationships) {
			displayMappingsWithRelationships(detailsPanel, concept);
		} else {
			displaySeparateRelationshipsAndMappings(detailsPanel, concept);
		}
		return detailsPanel;
	}

	/**
	 * @param lexicalTerms
	 * @param termType
	 * @return
	 */
	private Widget displayLexicalDetails(Collection<Literal> lexicalTerms, 
			String termType) {
		StringBuffer output = new StringBuffer("");
		if (lexicalTerms != null && !lexicalTerms.isEmpty()) {
			output.append("<h3>" + termType + "</h3>");
			Iterator<Literal> it = lexicalTerms.iterator();
			while (it.hasNext()) {
				Literal literal = it.next();
				output.append("<p>" + literal.getValue() + "</p>");
			}
		}
		VerticalPanel returnPanel = new VerticalPanel();
		HTML displayText = new HTML(output.toString());
		returnPanel.add(displayText);
		return returnPanel;
	}

	private void displayMappingsWithRelationships(Panel detailsPanel,
			VocabularyConcept concept) {
		// Order of calls determines display order
		detailsPanel.add(displayTogether(concept.getBroaderTerms(), 
				concept.getBroadMatches(), "Broader Terms"));
		detailsPanel.add(displayTogether(concept.getNarrowerTerms(), 
				concept.getNarrowMatches(), "Narrower Terms"));
		detailsPanel.add(displayTogether(concept.getRelatedTerms(), 
				concept.getRelatedMatches(), "Related Terms"));		
	}
	
	private Widget displayTogether(Collection<RelatedTerm> collection, 
			Collection<RelatedTerm> collection2, String type) {
		Collection<RelatedTerm> relationships = new Vector<RelatedTerm>();
		if (collection != null) {
			relationships.addAll(collection);
		}
		if (collection2 != null) {
			relationships.addAll(collection2);
		}
		return displayRelationshipDetails(relationships, type);
	}
	
	private void displaySeparateRelationshipsAndMappings(Panel detailsPanel, 
			VocabularyConcept concept) {
		// Order of calls determines display order
		detailsPanel.add(displayRelationshipDetails(concept.getBroaderTerms(),
				"Broader Terms"));
		detailsPanel.add(displayRelationshipDetails(concept.getBroadMatches(),
				"Broad Matches"));
		detailsPanel.add(displayRelationshipDetails(concept.getNarrowerTerms(), 
				"Narrower Terms"));
		detailsPanel.add(displayRelationshipDetails(concept.getNarrowMatches(),
				"Narrow Matches"));
		detailsPanel.add(displayRelationshipDetails(concept.getRelatedTerms(), 
				"Related Terms"));
		detailsPanel.add(displayRelationshipDetails(concept.getRelatedMatches(), 
				"Related Matches"));
	}

	/**
	 * Displays a set of relationships or mappings
	 * @param collection the related concepts
	 * @param relationshipType the type of the relationship
	 * @return
	 */
	private Widget displayRelationshipDetails(
			Collection<RelatedTerm> collection,
			String relationshipType) {
		if (collection != null) {
			GWT.log("Displaying " + relationshipType + " number of members " + 
					collection.size(), null);
		}
		VerticalPanel relationshipPanel = new VerticalPanel();
		if (collection != null && !collection.isEmpty()) {
			HTML heading = new HTML("<h3>" + relationshipType + "</h3><ul>");
			relationshipPanel.add(heading);
			Iterator<RelatedTerm> it = collection.iterator();
			displayRelatedConcept(relationshipPanel, it);
			HTML close = new HTML("</ul>");
			relationshipPanel.add(close);
		}
		return relationshipPanel;
	}

	private void displayRelatedConcept(Panel relationshipPanel, 
			Iterator<RelatedTerm> it) {
		while (it.hasNext()) {
			StringBuffer output = new StringBuffer();
			RelatedTerm relatedTerm = it.next();
			// Make these hyperlinks to the related term
			output.append("<li><a href='javascript:;'>" + 
					relatedTerm + "</a></li>");
			HTML relationshipDetail = new HTML(output.toString());
			relationshipPanel.add(relationshipDetail);
			relationshipDetail.addClickListener(
					new RelationshipClickListener(relatedTerm.getUri()));
		}
	}

	public class RelationshipClickListener implements ClickListener {

		private String m_conceptUri;
		
		public RelationshipClickListener(String conceptUri) {
			m_conceptUri = conceptUri;
		}
		/* (non-Javadoc)
		 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
		 */
		public void onClick(Widget sender) {
			GWT.log("concept uri: " + m_conceptUri, null);
			History.newItem("browse=" + m_conceptUri);
		}
		
	}
	
	/**
	 * @param vocabs
	 * @return
	 */
	private Widget displayVocabularyDetails(Collection<String> vocabs) {
		StringBuffer output = new StringBuffer();
		if (vocabs != null && !vocabs.isEmpty()) {
			output.append("<h3>Vocabulary</h3><ul>");
			Iterator<String> it = vocabs.iterator();
			while (it.hasNext()) {
				// Gets the ConceptScheme URI from the repository
				String vocab = (String) it.next();
				/*
				 * Get the vocabulary name from the copy stored in 
				 * the configuration panel
				 */				
				output.append("<li>" + configurationPanel.getVocabName(vocab) +  
						"</li>");
			}
			output.append("</ul>");
		}
		VerticalPanel returnPanel = new VerticalPanel();
		HTML display = new HTML(output.toString());
		returnPanel.add(display);
		return returnPanel;
	}

	/**
	 * Class to control the display of search results
	 */
	public class DisplayResultsCallback implements AsyncCallback<Collection<VocabularyConcept>> {
		
		public void onFailure(Throwable caught) {
		    GWT.log("Error ", caught);
			caught.printStackTrace();
		}

		public void onSuccess(Collection<VocabularyConcept> foundConcepts) { 
			GWT.log("Entering display results callback", null);
			// Clear existing results
			resultPanel.clear();
			
			/*
			 * Check that there are results to display
			 */
			if (foundConcepts.isEmpty()) {
				// No results to display, update number of results
				infoPanelMessage.setHTML("<p>No results to display. " +
						"Try searching for a concept.</p>");
			} else {
				// Display the returned concepts
				Iterator<VocabularyConcept> it = foundConcepts.iterator();
				while (it.hasNext()) {
					VocabularyConcept concept =  it.next();
					DisclosurePanel conceptDisplay = displayConcept(concept);
					resultPanel.add(conceptDisplay);
				}
			}
			
			if (m_numPages > 1) {
				// Display page navigation
				updatePageNavigation();
			}

			GWT.log("Exiting display results callback", null);
		}

		private DisclosurePanel displayConcept(VocabularyConcept concept) {
			/*
			 * Displays the prefLabel for the concept if it exists
			 * otherwise it shows the URI
			 */
			String prefLabel = concept.toString();
			DisclosurePanel conceptDisplayPanel = 
				new DisclosurePanel(prefLabel);	
			conceptDisplayPanel.addEventHandler(
					new ConceptDisplayDisclosureEventHandler(
							conceptDisplayPanel, concept));
			return conceptDisplayPanel;
		}

		private void updatePageNavigation() {
			GWT.log("Entering displayPageNavigation()", null);
			// Clear existing navigation
			clearPageNavigation();
			
			// Add a link to the previous page of results
			if (m_currentPage > 1) {
				/*
				 * Needs to be a change of 2 as we are going from
				 * a page number to an array offset
				 */ 
				createLink("Previous", m_currentPage - 2);
			}
			
			// Add links to a window of 10 pages
			int pageLow = Math.max(0, m_currentPage - 5);
			int pageHigh = Math.min(m_numPages, m_currentPage + 4);
			for (int i = pageLow; i < pageHigh; i++) {
				int pageNumber = i + 1;
				if (m_currentPage == pageNumber) {
					StringBuffer output = new StringBuffer();
					output.append(pageNumber);
					HTML pageLink = new HTML(output.toString());
					pageNavigationPanel.add(pageLink);
				} else {
					createLink(Integer.toString(pageNumber), i);
				}
			}
			
			// Add a link to the next page of results
			if (m_currentPage < m_numPages) {
				/*
				 * The number of the current page is the 
				 * offset of the desired page
				 */ 
				createLink("Next", m_currentPage);
			}
			GWT.log("Exiting displayPageNavigation()", null);
		}

		private void createLink(String pageNumber, int pageIndex) {
			// Create hyperlinks to subsequent pages of results
			StringBuffer output = new StringBuffer();
			output.append("<a href='javascript:;'>" + 
					pageNumber + "</a>");
			HTML pageLink = new HTML(output.toString());
			pageNavigationPanel.add(pageLink);
			pageLink.addClickListener(
					new PageLinkClickListener(pageIndex));
		}

		public class PageLinkClickListener implements ClickListener {
			
			private int m_index;
			
			public PageLinkClickListener(int index) {
				m_index = index;
			}
			/* (non-Javadoc)
			 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
			 */
			public void onClick(Widget sender) {
				callForDisplayFoundConcepts(m_index + 1);
			}
			
		}
		
	}
	
	/**
	 * Class to control the display of search results
	 */
	public class FoundConceptsCallback implements AsyncCallback<Integer> {			

		public void onFailure(Throwable caught) {
		    GWT.log("Error ", caught);
			caught.printStackTrace();
		}

		public void onSuccess(Integer result) {
			GWT.log("Entering found concepts callback", null);

			// remove any existing navigation
			clearPageNavigation();			
			
			// Display details of the number of search results
			m_resultSize = result;
			GWT.log("Found " + m_resultSize + " concepts.", null);			

			// Capture the case when 0 results are found
			if (m_resultSize <= 0) {
				infoPanelMessage.setHTML("");
				HTML noMatches = new HTML("<h2>No Matches</h2>" +
						"<p>The vocabularies do not contain a concept with " +
						"the label " + m_searchTerm + ".</p> <p>You may " +
						"want to check the spelling of your search term.</p>");
				resultPanel.add(noMatches);
			} else {
				// Retrieve relevant information from the server for page 1
				callForDisplayFoundConcepts(1);
			}			
			GWT.log("Exiting found concepts callback", null);
		}

	}

	public class ConceptDisplayDisclosureEventHandler 
	implements DisclosureHandler {
		
		private DisclosurePanel m_panel;
		private VocabularyConcept m_concept;

		/**
		 * @param conceptDisplayPanel 
		 * @param concept
		 */
		public ConceptDisplayDisclosureEventHandler(
				DisclosurePanel conceptDisplayPanel, 
				VocabularyConcept concept) {
			m_panel = conceptDisplayPanel;
			m_concept = concept;
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.user.client.ui.DisclosureHandler#onClose(com.google.gwt.user.client.ui.DisclosureEvent)
		 */
		public void onClose(DisclosureEvent event) {
			m_panel.setOpen(false);
		}

		/* (non-Javadoc)
		 * @see com.google.gwt.user.client.ui.DisclosureHandler#onOpen(com.google.gwt.user.client.ui.DisclosureEvent)
		 */
		public void onOpen(DisclosureEvent event) {			
			m_panel.setContent(displayConceptDetails(m_concept));
			m_panel.setOpen(true);
		}
		
	}
	
	/**
	 * Class to control the display of a user selected concept
	 * 
	 * @author agray
	 *
	 */
	public class DisplayConceptCallback 
	implements AsyncCallback<VocabularyConcept> {

		public void onFailure(Throwable caught) {
		    GWT.log("Error ", caught);
			caught.printStackTrace();
		}

		public void onSuccess(VocabularyConcept concept) {
			GWT.log("Entering concept callback", null);

			if (concept == null) {
				// Handle the case the the concept is not found
				HTML heading = new HTML("<h2>No matching concept</h2>" +
						"<p>Try searching for the required concept.</p>");
				resultPanel.add(heading);
			} else {

				// Display concept based on browse or search mode
				if (m_conceptDisplayPanel == null) {
					infoPanelMessage.setHTML("");
					clearPageNavigation();
					HTML heading = new HTML("<h2>" + concept.toString() + "</h2>");
					resultPanel.add(heading);
					resultPanel.add(displayConceptDetails(concept));
				} else {
					m_conceptDisplayPanel.setContent(
							displayConceptDetails(concept));
				}
			}
			GWT.log("Exiting concept callback", null);
		}
		
	}
		
}
