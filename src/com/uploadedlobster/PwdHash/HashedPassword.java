/**
 * PwdHash, HashedPassword.java
 * A password hash implementation for Android.
 *
 * Copyright (c) 2010 Philipp Wolfer
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the RBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * @author Philipp Wolfer <ph.wolfer@googlemail.com>
 */

package com.uploadedlobster.PwdHash;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * Hashed Password
 * 
 * Combination of page URI and plain text password. Treated as a string, it is
 * the hashed password. Based on original JavaScript code from:
 * https://www.pwdhash.com/
 * 
 * @author Philipp Wolfer <ph.wolfer@googlemail.com>
 */
public final class HashedPassword {

	private static final String HMAC_MD5 = "HmacMD5";
	private static final String PasswordPrefix = "@@";

	private String mPassword;
	private String mRealm;
	private String mHash;
	private Queue<Character> mExtras;

	/**
	 * Pattern to match only word characters. Since Java's regex implementation
	 * is unicode aware, the pattern \W would match also non-ASCII word
	 * characters. But since the JavaScript implementation of PwdHash only
	 * considers ASCII characters we stay compatible.
	 */
	private static Pattern NonAlphanumericMatcher = Pattern
			.compile("[^a-zA-Z0-9_]");

	private HashedPassword(String password, String realm) {
		mPassword = password;
		mRealm = realm;
	}

	public static HashedPassword create(String password, String realm) {
		HashedPassword result = new HashedPassword(password, realm);
		result.calculateHash();
		return result;
	}

	@Override
	public String toString() {
		return mHash;
	}

	private void calculateHash() {
		byte[] md5 = createHmacMD5(mPassword, mRealm);
		String hash = Base64.encodeToString(md5, Base64.NO_PADDING
				| Base64.NO_WRAP);
		int size = mPassword.length() + PasswordPrefix.length();
		boolean nonAlphanumeric = NonAlphanumericMatcher.matcher(mPassword)
				.find();

		mHash = applyConstraints(hash, size, nonAlphanumeric);
	}

	private static byte[] createHmacMD5(String key, String data) {
		if (key == null || key.equals(""))
			throw new IllegalArgumentException("key must not be null or empty");
		if (data == null)
			throw new IllegalArgumentException("data must not be null");

		byte[] keyBytes = encodeStringToBytes(key);
		byte[] dataBytes = encodeStringToBytes(data);

		Mac mac = null;
		try {
			mac = Mac.getInstance(HMAC_MD5);
			Key sk = new SecretKeySpec(keyBytes, HMAC_MD5);
			mac.init(sk);
			return mac.doFinal(dataBytes);
		} catch (NoSuchAlgorithmException e) {
			Log.e(HashedPassword.class.getName(),
					"HMAC_MD5 algorithm not supported on this platform.", e);
			return new byte[0];
		} catch (InvalidKeyException e) {
			Log.e(HashedPassword.class.getName(), "Invalid secret key.", e);
			return new byte[0];
		}
	}

	/**
	 * Returns a new byte array with the encoded input string (1 byte per
	 * character).
	 * 
	 * Characters in the Latin 1 range (up to code point 255) will be returned
	 * as Latin 1 encoded bytes. Characters above code point 255 will be
	 * UTF-16le encoded but only the first byte will be used.
	 * 
	 * This matches the original behavior of the PwdHash JavaScript
	 * implementation pwdhash.com and keeps the hash values of passwords
	 * containing non-latin1 characters compatible.
	 * 
	 * @param data
	 * @return Byte array.
	 */
	private static byte[] encodeStringToBytes(String data) {
		byte[] bytes = new byte[data.length()];

		for (int i = 0; i < data.length(); i++) {
			Integer codePoint = Integer.valueOf(data.codePointAt(i));

			if (codePoint <= 255)
				bytes[i] = codePoint.byteValue();
			else {
				try {
					String nonLatin1Char = Character.toString(data.charAt(i));
					byte[] charBytes = nonLatin1Char.getBytes("UTF-16le");
					short unsignedByte = (short) (0x000000FF & ((int) charBytes[0]));
					bytes[i] = (byte) unsignedByte;
				} catch (UnsupportedEncodingException e) {
					Log.w("Decoding error", Character.toString(data.charAt(i))
							+ " could not be decoded as UTF-16le");
					bytes[i] = 0x1A; // SUB
				}
			}
		}

		return bytes;
	}

	private String applyConstraints(String hash, int size,
			boolean nonAlphanumeric) {
		int startingSize = size - 4;
		if (startingSize < 0)
			startingSize = 0;
		else if (startingSize > hash.length())
			startingSize = hash.length();

		String result = hash.substring(0, startingSize);
		mExtras = new LinkedList<Character>();
		for (char c : hash.substring(startingSize).toCharArray()) {
			mExtras.add(c);
		}

		// Add the extra characters
		result += Pattern.compile("[A-Z]").matcher(result).find() ? nextExtraChar()
				: nextBetween('A', 26);
		result += Pattern.compile("[a-z]").matcher(result).find() ? nextExtraChar()
				: nextBetween('a', 26);
		result += Pattern.compile("[0-9]").matcher(result).find() ? nextExtraChar()
				: nextBetween('0', 10);
		result += (NonAlphanumericMatcher.matcher(result).find()
				&& nonAlphanumeric ? nextExtraChar() : '+');
		while (NonAlphanumericMatcher.matcher(result).find()
				&& !nonAlphanumeric) {
			String replacement = Character.toString(nextBetween('A', 26));
			result = NonAlphanumericMatcher.matcher(result).replaceFirst(
					replacement);
		}

		// For long passwords (about > 22 chars) the password might be longer
		// than the hash and mExtras is empty. In that case the constraints
		// above produce 0 bytes at the end of result. If nonAlphanumeric is not
		// set those 0 bytes are replaced, but in other cases they stay around
		// and must be removed here. This is a flaw in the original algorithm
		// which we have to work around here.
		result = result.replace("\0", "");

		// Rotate the result to make it harder to guess the inserted locations
		return rotate(result, nextExtra());
	}

	private int nextExtra() {
		return (int) nextExtraChar();
	}

	private char nextExtraChar() {
		return (mExtras.size() > 0 ? mExtras.remove().charValue() : 0);
	}

	private static int between(int min, int interval, int offset) {
		return min + offset % interval;
	}

	private char nextBetween(char base, int interval) {
		return (char) between((int) base, interval, nextExtra());
	}

	private static String rotate(String s, int amount) {
		Queue<Character> work = new LinkedList<Character>();
		for (char c : s.toCharArray()) {
			work.add(Character.valueOf(c));
		}

		while (amount-- > 0)
			work.add(work.remove());

		StringBuilder b = new StringBuilder(work.size());
		for (char c : work)
			b.append(c);

		return b.toString();
	}
}
