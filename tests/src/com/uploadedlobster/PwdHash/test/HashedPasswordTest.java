/**
 * 
 */
package com.uploadedlobster.PwdHash.test;

import android.test.AndroidTestCase;

import com.uploadedlobster.PwdHash.HashedPassword;

/**
 * @author Philipp Wolfer
 */
public class HashedPasswordTest extends AndroidTestCase {

	public void testToString() {
		HashedPassword hashedPassword = new HashedPassword("my53cret#",
				"example.com");
		assertEquals("Bu6aSm+Zcsf", hashedPassword.toString());
	}

	public void testToStringWithNonAsciiChars() {
		HashedPassword hashedPassword = new HashedPassword("mü53crét#",
				"example.com");
		assertEquals("r9qeSjv+lwJ", hashedPassword.toString());
	}

	public void testToStringWithNonLatin1Chars() {
		HashedPassword hashedPassword = new HashedPassword("中文العربي",
				"example.com");
		assertEquals("AwMz3+BdMT", hashedPassword.toString());
	}

	public void testToStringWithoutNonAlphanumeric() {
		HashedPassword hashedPassword = new HashedPassword("my53cret",
				"example.com");
		assertEquals("CIUD4SCSgh", hashedPassword.toString());
	}

	public void testToStringWithShortSecret() {
		HashedPassword hashedPassword = new HashedPassword("ab", "example.com");
		assertEquals("0IKv", hashedPassword.toString());
	}

	public void testToStringWithShortestSecret() {
		HashedPassword hashedPassword = new HashedPassword("a", "example.com");
		assertEquals("9FBo", hashedPassword.toString());
	}

	public void testToStringWithEmptySecret() {
		try {
			HashedPassword hashedPassword = new HashedPassword("",
					"example.com");
			hashedPassword.toString();
			assert (false);
		} catch (IllegalArgumentException e) {
			assert (true);
		}
	}
}
