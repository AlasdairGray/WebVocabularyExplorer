package uk.ac.gla.dcs.explicator.vocabularies.server;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SKOSParserTest {

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
		Map<String, SKOSConcept> concepts = new HashMap<String, SKOSConcept>();
//		File testVocab = new File("web/etc/vocabs/test.rdf");
//		File testVocab = new File("web/etc/vocabs/AAkeys.rdf");
		File testVocab = new File("web/etc/vocabs/AVM.rdf");
//		File testVocab = new File("web/etc/vocabs/IAUT93.rdf");
//		File testVocab = new File("web/etc/vocabs/IVOAT.rdf");
//		File testVocab = new File("web/etc/vocabs/UCD.rdf");
		SKOSParser parser = new SKOSParser(concepts);
		try {
			SKOSScheme vocabulary = parser.parse(testVocab, null);
			assertEquals(false, vocabulary.getVocabName() == null);
			//IVOAT and UCD do not have a created date
//			assertEquals(false, vocabulary.getCreated() == null);
		} catch (Exception e) {
			fail("Exception thrown");
		}		
	}

}
