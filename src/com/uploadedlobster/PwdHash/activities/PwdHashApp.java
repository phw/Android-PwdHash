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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.uploadedlobster.PwdHash.R;
import com.uploadedlobster.PwdHash.algorithm.DomainExtractor;
import com.uploadedlobster.PwdHash.algorithm.HashedPassword;
import com.uploadedlobster.PwdHash.storage.HistoryDataSource;
import com.uploadedlobster.PwdHash.storage.HistoryOpenHelper;
import com.uploadedlobster.PwdHash.storage.UpdateHistoryTask;
import com.uploadedlobster.PwdHash.util.Preferences;

/**
 * @author Philipp Wolfer <ph.wolfer@googlemail.com>
 *
 */
public class PwdHashApp extends Activity {
	private Preferences mPreferences;
	private HistoryDataSource mHistory;
	
	private AutoCompleteTextView mSiteAddress;
	private EditText mPassword;
	private TextView mHashedPassword;
	private Button mCopyBtn;
	
	private boolean mSaveStateOnExit = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		}
		
		setContentView(R.layout.main);

		mSiteAddress = (AutoCompleteTextView) findViewById(R.id.siteAddress);
		mPassword = (EditText) findViewById(R.id.password);
		mHashedPassword = (TextView) findViewById(R.id.hashedPassword);
		mCopyBtn = (Button) findViewById(R.id.copyBtn);

		mPreferences = new Preferences(this);
		mHistory = new HistoryDataSource(this);
		
		setWindowGeometry();
		restoreSavedState();
		handleIntents();
		registerEventListeners();
		initAutoComplete();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mSaveStateOnExit) {
			mPreferences.setSavedSiteAddress(getDomain());
		}
		else {
			mPreferences.setSavedSiteAddress("");
		}
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
		}
	}

	private void handleIntents() {
		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();
			if (action != null && action.equals(Intent.ACTION_SEND)) {
				String siteAddress = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (siteAddress != null && !siteAddress.equals("")) {
					siteAddress = DomainExtractor.extractDomain(siteAddress);
					mSiteAddress.setText(siteAddress);
					mPassword.requestFocus();
				}
			}
		}
	}

	private void initAutoComplete() {
		mHistory.open();
		String[] from = new String[] { HistoryOpenHelper.COLUMN_REALM };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_dropdown_item_1line, null, from, to, 0);

		// Set the CursorToStringConverter, to provide the labels for the
		// choices to be displayed in the AutoCompleteTextView.
		adapter.setCursorToStringConverter(new CursorToStringConverter() {
			public String convertToString(android.database.Cursor cursor) {
				final int columnIndex = cursor
						.getColumnIndexOrThrow(HistoryOpenHelper.COLUMN_REALM);
				final String domain = cursor.getString(columnIndex);
				return domain;
			}
		});

		// Set the FilterQueryProvider, to run queries for choices
		// that match the specified input.
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String partialInput = (constraint != null ? constraint
						.toString() : "");
				Cursor cursor = mHistory.getHistoryCursor(partialInput);
				return cursor;
			}
		});

		mSiteAddress.setAdapter(adapter);
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
				String realm = getDomain();
				String password = mPassword.getText().toString();

				if (realm.equals("")) {
					mSiteAddress.requestFocus();
				} else if (password.equals("")) {
					mPassword.requestFocus();
				} else {
					String hashedPassword = updateHashedPassword(realm, password);
	
					if (!hashedPassword.equals("")) {
						new UpdateHistoryTask(mHistory).execute(realm);
						copyToClipboard(hashedPassword);
						CharSequence clipboardNotification = getString(R.string.copiedToClipboardNotification);
						showNotification(clipboardNotification);
						mSaveStateOnExit = false;
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
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	protected void copyToClipboard(String hashedPassword) {
		try {
			// android.text.ClipboardManager is deprecated since API level 11, but we need it in order to be backward compatible.
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(hashedPassword);
		}
		catch (IllegalStateException e) {
			// Workaround for some Android 4.3 devices, where writing to the clipboard manager raises an exception
			// if there is an active clipboard listener.
			Log.w("PwdHashApp", "IllegalStateException raised when accessing clipboard.");
		}
	}
}
