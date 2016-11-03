package com.uploadedlobster.PwdHash.test;

import java.util.HashMap;
import java.util.Map;

import android.test.AndroidTestCase;

import com.uploadedlobster.PwdHash.algorithm.DomainExtractor;

/**
 * @author Philipp Wolfer
 */
public class DomainExtractorTest extends AndroidTestCase {

	private static HashMap<String, String> testSamples;

	@Override
	protected void setUp() throws Exception {
		testSamples = new HashMap<>();
		testSamples.put("example.com", "example.com");
		testSamples.put("http://example.com", "example.com");
		testSamples.put("http://example.com/aPath/test.html", "example.com");
		testSamples.put("http://www.example.com", "example.com");
		testSamples.put("https://www.example.com", "example.com");
		testSamples
				.put("http://www.example.com/aPath/test.html", "example.com");
		testSamples.put("http://login.test.example.com", "example.com");
		testSamples.put("http://example.co.uk", "example.co.uk");
		testSamples.put("http://login.example.co.uk", "example.co.uk");
		testSamples
				.put("https://login.example.co.uk/test.htm", "example.co.uk");
	}

	public void testExtractDomain() {
		for (Map.Entry<String, String> t : testSamples.entrySet()) {
			assertEquals(t.getValue(),
					DomainExtractor.extractDomain(t.getKey()));
		}
	}

	public void testExtractDomainWithEmptyStringInput() {
		assertEquals("", DomainExtractor.extractDomain(""));
	}

	public void testExtractDomainWithNullInput() {
		try {
			DomainExtractor.extractDomain(null);
			assert (false);
		} catch (IllegalArgumentException e) {
			assert (true);
		}
	}

}
