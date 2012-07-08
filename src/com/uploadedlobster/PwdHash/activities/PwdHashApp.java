/**
 * PwdHash, PwdHashApp.java
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

package com.uploadedlobster.PwdHash.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.uploadedlobster.PwdHash.R;
import com.uploadedlobster.PwdHash.algorithm.DomainExtractor;
import com.uploadedlobster.PwdHash.algorithm.HashedPassword;
import com.uploadedlobster.PwdHash.util.Preferences;

/**
 * @author Philipp Wolfer <ph.wolfer@googlemail.com>
 *
 */
public class PwdHashApp extends Activity {
	private Preferences mPreferences;
	private EditText mSiteAddress;
	private EditText mPassword;
	private TextView mHashedPassword;
	private Button mCopyBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSiteAddress = (EditText) findViewById(R.id.siteAddress);
		mPassword = (EditText) findViewById(R.id.password);
		mHashedPassword = (TextView) findViewById(R.id.hashedPassword);
		mCopyBtn = (Button) findViewById(R.id.copyBtn);

		mPreferences = new Preferences(this);
		
		setWindowGeometry();
		restoreSavedState();
		handleIntents();
		registerEventListeners();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPreferences.setSavedSiteAddress(getDomain());
	}

	private void setWindowGeometry() {
		Window window = getWindow();
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int maxWidth = getResources().getDimensionPixelSize(
				R.dimen.maxWindowWidth);

		if (metrics.widthPixels > maxWidth) {
			window.setLayout(maxWidth, LayoutParams.WRAP_CONTENT);
		}
	}

	private void restoreSavedState() {
		String savedSiteAddress = mPreferences.getSavedSiteAddress();
		
		if (!savedSiteAddress.equals("")) {
			mSiteAddress.setText(savedSiteAddress);
			mSiteAddress.selectAll();
		}
	}

	private void handleIntents() {
		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_SEND)) {
			String siteAddress = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (!siteAddress.equals("")) {
				mSiteAddress.setText(siteAddress);
				mPassword.requestFocus();
			}
		}
	}

	private void registerEventListeners() {
		TextWatcher updatePasswordTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String realm = getDomain();
				String password = mPassword.getText().toString();
				
				updateHashedPassword(realm, password);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		};
		mSiteAddress.addTextChangedListener(updatePasswordTextWatcher);
		mPassword.addTextChangedListener(updatePasswordTextWatcher);

		mCopyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String realm = DomainExtractor.extractDomain(mSiteAddress.getText()
						.toString());
				String password = mPassword.getText().toString();

				if (realm.equals("")) {
					mSiteAddress.requestFocus();
				} else if (password.equals("")) {
					mPassword.requestFocus();
				} else {
					String hashedPassword = updateHashedPassword(realm, password);
	
					if (!hashedPassword.equals("")) {
						copyToClipboard(hashedPassword);
						CharSequence clipboardNotification = getString(R.string.copiedToClipboardNotification);
						showNotification(clipboardNotification);
						finish();
					}
				}
			}
		});
	}
	
	private String getDomain() {
		return DomainExtractor.extractDomain(
			mSiteAddress.getText().toString());
	}

	private String updateHashedPassword(String realm, String password) {
		String result = "";
		
		if (!realm.equals("") && !password.equals("")) {
			HashedPassword hashedPassword = HashedPassword.create(password, realm);
			result = hashedPassword.toString();
		}
		
		if (result.equals(""))
			mCopyBtn.setEnabled(false);
		else
			mCopyBtn.setEnabled(true);
		
		mHashedPassword.setText(result);
		return result;
	}

	private void showNotification(CharSequence text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	protected void copyToClipboard(String hashedPassword) {
		// android.text.ClipboardManager is deprecated since API level 11, but we need it in order to be backward compatible.
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText(hashedPassword);
	}
}
