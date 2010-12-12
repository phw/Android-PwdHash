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
		HashedPassword hashedPassword = HashedPassword.create("my53cret#",
				"example.com");
		assertEquals("Bu6aSm+Zcsf", hashedPassword.toString());
	}

	public void testToStringWithNonAsciiChars() {
		HashedPassword hashedPassword = HashedPassword.create("mü53crét#",
				"example.com");
		assertEquals("r9qeSjv+lwJ", hashedPassword.toString());
	}

	public void testToStringWithNonLatin1Chars() {
		HashedPassword hashedPassword = HashedPassword.create("中文العربي",
				"example.com");
		assertEquals("AwMz3+BdMT", hashedPassword.toString());
	}

	public void testToStringWithoutNonAlphanumeric() {
		HashedPassword hashedPassword = HashedPassword.create("my53cret",
				"example.com");
		assertEquals("CIUD4SCSgh", hashedPassword.toString());
	}

	public void testToStringWithShortSecret() {
		HashedPassword hashedPassword = HashedPassword.create("ab",
				"example.com");
		assertEquals("0IKv", hashedPassword.toString());
	}

	public void testToStringWithShortestSecret() {
		HashedPassword hashedPassword = HashedPassword.create("a",
				"example.com");
		assertEquals("9FBo", hashedPassword.toString());
	}

	public void testToStringWithEmptySecret() {
		try {
			HashedPassword hashedPassword = HashedPassword.create("",
					"example.com");
			hashedPassword.toString();
			assert (false);
		} catch (IllegalArgumentException e) {
			assert (true);
		}
	}
}
