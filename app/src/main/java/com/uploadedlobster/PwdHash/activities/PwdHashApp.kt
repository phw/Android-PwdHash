/**
 * PwdHash, PwdHashApp.java
 * A password hash implementation for Android.
 *
 * Copyright (c) 2010 - 2013 Philipp Wolfer
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
package com.uploadedlobster.PwdHash.activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.SimpleCursorAdapter.CursorToStringConverter
import com.uploadedlobster.PwdHash.R
import com.uploadedlobster.PwdHash.algorithm.DomainExtractor.extractDomain
import com.uploadedlobster.PwdHash.algorithm.HashedPassword.Companion.create
import com.uploadedlobster.PwdHash.storage.HistoryDataSource
import com.uploadedlobster.PwdHash.storage.HistoryOpenHelper
import com.uploadedlobster.PwdHash.storage.UpdateHistoryTask
import com.uploadedlobster.PwdHash.util.Preferences

/**
 * @author Philipp Wolfer <ph.wolfer></ph.wolfer>@gmail.com>
 */
class PwdHashApp : Activity() {
    private var mPreferences: Preferences? = null
    private var mHistory: HistoryDataSource? = null
    private var mSiteAddress: AutoCompleteTextView? = null
    private var mPassword: EditText? = null
    private var mHashedPassword: TextView? = null
    private var mCopyBtn: Button? = null
    private var mSaveStateOnExit = true

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.main)
        mSiteAddress = findViewById<View>(R.id.siteAddress) as AutoCompleteTextView
        mPassword = findViewById<View>(R.id.password) as EditText
        mHashedPassword = findViewById<View>(R.id.hashedPassword) as TextView
        mCopyBtn = findViewById<View>(R.id.copyBtn) as Button
        mPreferences = Preferences(this)
        mHistory = HistoryDataSource(this)
        setWindowGeometry()
        restoreSavedState()
        handleIntents()
        registerEventListeners()
        initAutoComplete()
    }

    override fun onStop() {
        super.onStop()
        if (mSaveStateOnExit) {
            mPreferences!!.savedSiteAddress = domain
        } else {
            mPreferences!!.savedSiteAddress = ""
        }
    }

    private fun setWindowGeometry() {
        val window = window
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val maxWidth = resources.getDimensionPixelSize(
            R.dimen.maxWindowWidth)
        if (metrics.widthPixels > maxWidth) {
            window.setLayout(maxWidth, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun restoreSavedState() {
        val savedSiteAddress = mPreferences!!.savedSiteAddress
        if (savedSiteAddress != "") {
            mSiteAddress!!.setText(savedSiteAddress)
        }
    }

    private fun handleIntents() {
        val intent = intent
        if (intent != null) {
            val action = intent.action
            if (action != null && action == Intent.ACTION_SEND) {
                var siteAddress = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (siteAddress != null && siteAddress != "") {
                    siteAddress = extractDomain(siteAddress)
                    mSiteAddress!!.setText(siteAddress)
                    mPassword!!.requestFocus()
                }
            }
        }
    }

    private fun initAutoComplete() {
        mHistory!!.open()
        val from = arrayOf(HistoryOpenHelper.COLUMN_REALM)
        val to = intArrayOf(android.R.id.text1)
        val adapter = SimpleCursorAdapter(this,
            android.R.layout.simple_dropdown_item_1line, null, from, to, 0)

        // Set the CursorToStringConverter, to provide the labels for the
        // choices to be displayed in the AutoCompleteTextView.
        adapter.cursorToStringConverter = CursorToStringConverter { cursor: Cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(HistoryOpenHelper.COLUMN_REALM)
            cursor.getString(columnIndex)
        }

        // Set the FilterQueryProvider, to run queries for choices
        // that match the specified input.
        adapter.filterQueryProvider = FilterQueryProvider { constraint ->
            val partialInput = constraint?.toString() ?: ""
            mHistory!!.getHistoryCursor(partialInput)
        }
        mSiteAddress!!.setAdapter(adapter)
    }

    private fun registerEventListeners() {
        val updatePasswordTextWatcher: TextWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val realm = domain
                val password = mPassword!!.text.toString()
                updateHashedPassword(realm, password)
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        }
        mSiteAddress!!.addTextChangedListener(updatePasswordTextWatcher)
        mPassword!!.addTextChangedListener(updatePasswordTextWatcher)
        mCopyBtn!!.setOnClickListener {
            val realm = domain
            val password = mPassword!!.text.toString()
            if (realm == "") {
                mSiteAddress!!.requestFocus()
            } else if (password == "") {
                mPassword!!.requestFocus()
            } else {
                val hashedPassword = updateHashedPassword(realm, password)
                if (hashedPassword != "") {
                    UpdateHistoryTask(mHistory!!).execute(realm)
                    copyToClipboard(hashedPassword)
                    val clipboardNotification: CharSequence =
                        getString(R.string.copiedToClipboardNotification)
                    showNotification(clipboardNotification)
                    mSaveStateOnExit = false
                    finish()
                }
            }
        }
    }

    private val domain: String get() = extractDomain(mSiteAddress!!.text.toString())

    private fun updateHashedPassword(realm: String, password: String): String {
        var result = ""
        if (realm != "" && password != "") {
            val hashedPassword = create(password, realm)
            result = hashedPassword.toString()
        }
        mCopyBtn!!.isEnabled = result != ""
        mHashedPassword!!.text = result
        return result
    }

    private fun showNotification(text: CharSequence) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(this, text, duration)
        toast.show()
    }

    private fun copyToClipboard(hashedPassword: String) {
        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", hashedPassword)
            clipboard.setPrimaryClip(clip)
        } catch (e: IllegalStateException) {
            // Workaround for some Android 4.3 devices, where writing to the clipboard manager raises an exception
            // if there is an active clipboard listener.
            Log.w("PwdHashApp", "IllegalStateException raised when accessing clipboard.")
        }
    }
}