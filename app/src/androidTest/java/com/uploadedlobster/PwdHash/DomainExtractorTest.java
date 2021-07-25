package com.uploadedlobster.PwdHash;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.uploadedlobster.PwdHash.algorithm.DomainExtractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Philipp Wolfer
 */
@RunWith(AndroidJUnit4.class)
public class DomainExtractorTest {

	private static HashMap<String, String> testSamples;

	@Before
	public void setUp() throws Exception {
		testSamples = new HashMap<>();
		testSamples.put("example.com", "example.com");
		testSamples.put("http://example.com", "example.com");
		testSamples.put("http://example.com/aPath/test.html", "example.com");
		testSamples.put("http://www.example.com", "example.com");
		testSamples.put("https://www.example.com", "example.com");
		testSamples.put("http://www.example.com/aPath/test.html", "example.com");
		testSamples.put("http://login.test.example.com", "example.com");
		testSamples.put("http://example.co.uk", "example.co.uk");
		testSamples.put("http://login.example.co.uk", "example.co.uk");
		testSamples.put("https://login.example.co.uk/test.htm", "example.co.uk");
	}

	@Test
	public void testExtractDomain() throws Exception {
		for (Map.Entry<String, String> t : testSamples.entrySet()) {
			assertEquals(t.getValue(),
					DomainExtractor.extractDomain(t.getKey()));
		}
	}

	@Test
	public void testExtractDomainWithEmptyStringInput() throws Exception {
		assertEquals("", DomainExtractor.extractDomain(""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExtractDomainWithNullInput() throws Exception {
		DomainExtractor.extractDomain(null);
	}

}
