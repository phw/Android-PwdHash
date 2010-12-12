package com.uploadedlobster.PwdHash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PwdHashApp extends Activity {
	private EditText mSiteAddress;
	private EditText mPassword;
	private TextView mHashedPassword;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		mSiteAddress = (EditText) findViewById(R.id.siteAddress);
		mPassword = (EditText) findViewById(R.id.password);
		mHashedPassword = (TextView) findViewById(R.id.hashedPassword);

		handleIntents();
		registerEventListeners();
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
				String realm = DomainExtractor.extractDomain(mSiteAddress.getText()
						.toString());
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

		Button copyBtn = (Button) findViewById(R.id.copyBtn);
		copyBtn.setOnClickListener(new View.OnClickListener() {
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
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						clipboard.setText(hashedPassword);
						CharSequence clipboardNotification = getString(R.string.copiedToClipboardNotification);
						showNotification(clipboardNotification);
						finish();
					}
				}
			}
		});
	}

	private String updateHashedPassword(String realm, String password) {
		String result = "";
		
		if (!realm.equals("") && !password.equals("")) {
			HashedPassword hashedPassword = HashedPassword.create(password, realm);
			result = hashedPassword.toString();
		}
		
		mHashedPassword.setText(result);
		return result;
	}

	private void showNotification(CharSequence text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}