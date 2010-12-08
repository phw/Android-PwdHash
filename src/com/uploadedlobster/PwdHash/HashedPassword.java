/**
 * 
 */
package com.uploadedlobster.PwdHash;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

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
	
	private String mPassword;
	private String mRealm;
	
	public HashedPassword(String password, String realm) {
		mPassword = password;
		mRealm = realm;
	}

	@Override
	public String toString() {
		//String hash = Base64.encodeToString(input, flags);
		byte[] md5 = createHmacMD5(mPassword, mRealm);
		String hash = Base64.encodeToString(md5, Base64.DEFAULT);
		
		
		String result = hash;
		return result;
	}
	
	private static final String HMAC_MD5 = "HmacMD5";
	
	private static byte[] createHmacMD5(String key, String data) {
		Key sk = new SecretKeySpec(key.getBytes(), HMAC_MD5);
		
		Mac mac = null;
		try {
			mac = Mac.getInstance(HMAC_MD5);
			mac.init(sk);
		} catch (NoSuchAlgorithmException e) {
			Log.e(HashedPassword.class.getName(), "HMAC_MD5 algorithm not supported on this platform.", e);
		} catch (InvalidKeyException e) {
			Log.e(HashedPassword.class.getName(), "Invalid secret key.", e);
		}
		
		return mac.doFinal(data.getBytes());
	}
}
