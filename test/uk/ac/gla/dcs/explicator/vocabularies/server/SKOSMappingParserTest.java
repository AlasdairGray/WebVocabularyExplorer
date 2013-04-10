package uk.ac.gla.dcs.explicator.vocabularies.server;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SKOSMappingParserTest {

	private Logger log;

	@Before
	public void setUp() throws Exception {
		PropertyConfigurator.configure("web/etc/log4j.properties");
		log = Logger.getLogger(getClass().getName());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseFileStringString() {
		File testVocab = new File("web/etc/vocabs/AAkeys2AVM.rdf");
		SKOSMappingParser parser = new SKOSMappingParser();
		try {
			SKOSMapping mappingSet = parser.parse(testVocab, null, false);
			assertEquals(false, mappingSet.getCreator() == null);
			assertEquals(true, mappingSet.getCreator().equals("Alasdair J G Gray"));
			assertEquals(false, mappingSet.getCreatedDate() == null);
			assertEquals(true, mappingSet.getCreatedDate().equals("2008-07-03"));
			assertEquals(false, mappingSet.getTitle() == null);
			assertEquals(true, mappingSet.getTitle().equals("Mappings " +
					"from Astronomy & Astrophysics Journal keywords to the " +
					"AVM taxonomy"));
			assertEquals(356, mappingSet.size());
		} catch (Exception e) {
			log.error("Exception thrown. " + e);
			fail("Exception thrown");
		}	
		
		/* Test that if we don't infer the additional mappings
		 * then the size is as in the file
		 */
		testVocab = new File("web/etc/vocabs/AAkeys2AVM.rdf");
		parser = new SKOSMappingParser();
		try {
			SKOSMapping mappingSet = parser.parse(testVocab, null, true);
			assertEquals(178, mappingSet.size());
		} catch (Exception e) {
			log.error("Exception thrown. " + e);
			fail("Exception thrown");
		}	
	}

}
