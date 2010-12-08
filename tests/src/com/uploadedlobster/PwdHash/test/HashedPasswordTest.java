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
}
