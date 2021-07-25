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
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the RBrainz project nor the names of the
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
 * @author Philipp Wolfer <ph.wolfer></ph.wolfer>@gmail.com>
 */
package com.uploadedlobster.PwdHash.algorithm

import android.util.Base64
import android.util.Log
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Hashed Password
 *
 * Combination of page URI and plain text password. Treated as a string, it is
 * the hashed password. Based on original JavaScript code from:
 * https://www.pwdhash.com/
 *
 * @author Philipp Wolfer <ph.wolfer></ph.wolfer>@gmail.com>
 */
class HashedPassword private constructor(
    private val mPassword: String,
    private val mRealm: String
) {
    private var mHash: String? = null
    private var mExtras: Queue<Char>? = null
    override fun toString(): String {
        return mHash!!
    }

    private fun calculateHash() {
        val md5 = createHmacMD5(mPassword, mRealm)
        val hash = Base64.encodeToString(md5, Base64.NO_PADDING
                or Base64.NO_WRAP)
        val size = mPassword.length + PasswordPrefix.length
        val nonAlphanumeric = NonAlphanumericMatcher.matcher(mPassword)
            .find()
        mHash = applyConstraints(hash, size, nonAlphanumeric)
    }

    private fun applyConstraints(
        hash: String, size: Int,
        nonAlphanumeric: Boolean
    ): String {
        var startingSize = size - 4
        if (startingSize < 0) startingSize = 0 else if (startingSize > hash.length) startingSize =
            hash.length
        var result = hash.substring(0, startingSize)
        mExtras = LinkedList()
        for (c in hash.substring(startingSize).toCharArray()) {
            mExtras!!.add(c)
        }

        // Add the extra characters
        result += if (Pattern.compile("[A-Z]").matcher(result)
                .find()
        ) nextExtraChar() else nextBetween('A', 26)
        result += if (Pattern.compile("[a-z]").matcher(result)
                .find()
        ) nextExtraChar() else nextBetween('a', 26)
        result += if (Pattern.compile("[0-9]").matcher(result)
                .find()
        ) nextExtraChar() else nextBetween('0', 10)
        result += if (NonAlphanumericMatcher.matcher(result).find()
            && nonAlphanumeric
        ) nextExtraChar() else '+'
        while (NonAlphanumericMatcher.matcher(result).find()
            && !nonAlphanumeric
        ) {
            val replacement = Character.toString(nextBetween('A', 26))
            result = NonAlphanumericMatcher.matcher(result).replaceFirst(
                replacement)
        }

        // For long passwords (about > 22 chars) the password might be longer
        // than the hash and mExtras is empty. In that case the constraints
        // above produce 0 bytes at the end of result. If nonAlphanumeric is not
        // set those 0 bytes are replaced, but in other cases they stay around
        // and must be removed here. This is a flaw in the original algorithm
        // which we have to work around here.
        result = result.replace("\u0000", "")

        // Rotate the result to make it harder to guess the inserted locations
        return rotate(result, nextExtra())
    }

    private fun nextExtra(): Int {
        return nextExtraChar().toInt()
    }

    private fun nextExtraChar(): Char {
        return if (mExtras!!.size > 0) mExtras!!.remove() else '0'
    }

    private fun nextBetween(base: Char, interval: Int): Char {
        return between(base.toInt(), interval, nextExtra()).toChar()
    }

    companion object {
        private const val HMAC_MD5 = "HmacMD5"
        private const val PasswordPrefix = "@@"

        /**
         * Pattern to match only word characters. Since Java's regex implementation
         * is unicode aware, the pattern \W would match also non-ASCII word
         * characters. But since the JavaScript implementation of PwdHash only
         * considers ASCII characters we stay compatible.
         */
        private val NonAlphanumericMatcher = Pattern
            .compile("[^a-zA-Z0-9_]")

        @JvmStatic
		fun create(password: String, realm: String): HashedPassword {
            val result = HashedPassword(password, realm)
            result.calculateHash()
            return result
        }

        private fun createHmacMD5(key: String?, data: String?): ByteArray {
            require(!(key == null || key == "")) { "key must not be null or empty" }
            requireNotNull(data) { "data must not be null" }
            val keyBytes = encodeStringToBytes(key)
            val dataBytes = encodeStringToBytes(data)
            return try {
                val mac = Mac.getInstance(HMAC_MD5)
                val sk: Key = SecretKeySpec(keyBytes, HMAC_MD5)
                mac.init(sk)
                mac.doFinal(dataBytes)
            } catch (e: NoSuchAlgorithmException) {
                Log.e(HashedPassword::class.java.name,
                    "HMAC_MD5 algorithm not supported on this platform.", e)
                ByteArray(0)
            } catch (e: InvalidKeyException) {
                Log.e(HashedPassword::class.java.name, "Invalid secret key.", e)
                ByteArray(0)
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
         * @param data Input string
         * @return Byte array.
         */
        private fun encodeStringToBytes(data: String): ByteArray {
            val bytes = ByteArray(data.length)
            for (i in 0 until data.length) {
                val codePoint = data.codePointAt(i)
                if (codePoint <= 255) bytes[i] = codePoint.toByte() else {
                    try {
                        val nonLatin1Char = Character.toString(data[i])
                        val charBytes = nonLatin1Char.toByteArray(charset("UTF-16le"))
                        val unsignedByte = (0x000000FF and charBytes[0]
                            .toInt()).toShort()
                        bytes[i] = unsignedByte.toByte()
                    } catch (e: UnsupportedEncodingException) {
                        Log.w("Decoding error", Character.toString(data[i])
                                + " could not be decoded as UTF-16le")
                        bytes[i] = 0x1A // SUB
                    }
                }
            }
            return bytes
        }

        private fun between(min: Int, interval: Int, offset: Int): Int {
            return min + offset % interval
        }

        private fun rotate(s: String, amount: Int): String {
            var amount = amount
            val work: Queue<Char> = LinkedList()
            for (c in s.toCharArray()) {
                work.add(c)
            }
            while (amount-- > 0) work.add(work.remove())
            val b = StringBuilder(work.size)
            for (c in work) b.append(c)
            return b.toString()
        }
    }
}