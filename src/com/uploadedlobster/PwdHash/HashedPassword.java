/**
 * 
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
public class HashedPassword {

	private static final String HMAC_MD5 = "HmacMD5";
	private static final String PasswordPrefix = "@@";

	private String mPassword;
	private String mRealm;
	private Queue<Character> mExtras;

	/**
	 * Pattern to match only word characters. Since Java's regex implementation
	 * is unicode aware, the pattern \W would match also non-ASCII word
	 * characters. But since the JavaScript implementation of PwdHash only
	 * considers ASCII characters we stay compatible.
	 */
	private static Pattern NonAlphanumericMatcher = Pattern
			.compile("[^a-zA-Z0-9_]");

	public HashedPassword(String password, String realm) {
		mPassword = password;
		mRealm = realm;
	}

	@Override
	public String toString() {
		byte[] md5 = createHmacMD5(mPassword, mRealm);
		String hash = Base64.encodeToString(md5, Base64.NO_PADDING
				| Base64.NO_WRAP);
		int size = mPassword.length() + PasswordPrefix.length();
		boolean nonAlphanumeric = NonAlphanumericMatcher.matcher(mPassword)
				.find();

		String result = applyConstraints(hash, size, nonAlphanumeric);
		return result;
	}

	private static byte[] createHmacMD5(String key, String data) {
		byte[] keyBytes = encodeStringToBytes(key);
		byte[] dataBytes = encodeStringToBytes(data);

		Key sk = new SecretKeySpec(keyBytes, HMAC_MD5);

		Mac mac = null;
		try {
			mac = Mac.getInstance(HMAC_MD5);
			mac.init(sk);
		} catch (NoSuchAlgorithmException e) {
			Log.e(HashedPassword.class.getName(),
					"HMAC_MD5 algorithm not supported on this platform.", e);
			return new byte[0];
		} catch (InvalidKeyException e) {
			Log.e(HashedPassword.class.getName(), "Invalid secret key.", e);
			return new byte[0];
		}

		return mac.doFinal(dataBytes);
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
	 * containing non-latin-1 characters compatible.
	 * 
	 * @param data
	 * @return Byte array.
	 */
	private static byte[] encodeStringToBytes(String data) {
		byte[] bytes = new byte[data.length()];

		for (int i = 0; i < data.length(); i++) {
			Integer codePoint = new Integer(data.codePointAt(i));

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
		String result = startingSize > hash.length() ? hash : hash.substring(0,
				startingSize);
		mExtras = new LinkedList<Character>();
		int extraStart = startingSize > hash.length() ? hash.length()
				: startingSize;
		for (char c : hash.substring(extraStart).toCharArray()) {
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
			work.add(new Character(c));
		}

		while (amount-- > 0)
			work.add(work.remove());

		StringBuilder b = new StringBuilder(work.size());
		for (char c : work)
			b.append(c);

		return b.toString();
	}
}
