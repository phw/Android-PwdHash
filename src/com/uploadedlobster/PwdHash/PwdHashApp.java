package com.uploadedlobster.PwdHash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
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
		
		Button generateBtn = (Button) findViewById(R.id.generateBtn);
		generateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String hashedPassword = generateHashedPassword();
				mHashedPassword.setText(hashedPassword);
			}
		});

		Button copyBtn = (Button) findViewById(R.id.copyBtn);
		copyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String hashedPassword = generateHashedPassword();

				if (!hashedPassword.equals("")) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboard.setText(hashedPassword);
					CharSequence clipboardNotification = getString(R.string.copiedToClipboardNotification);
					showNotification(clipboardNotification);
					finish();
				}
			}
		});
	}

	private void handleIntents() {
		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_SEND))
		{
			String siteAddress = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (!siteAddress.equals("")) {
				mSiteAddress.setText(siteAddress);
				mPassword.requestFocus();
			}
		}
	}

	private String generateHashedPassword() {
		String realm = DomainExtractor.extractDomain(mSiteAddress.getText()
				.toString());
		String password = mPassword.getText().toString();

		if (realm.equals("")) {
			mSiteAddress.requestFocus();
			return "";
		} else if (password.equals("")) {
			mPassword.requestFocus();
			return "";
		} else {
			HashedPassword hashedPassword = new HashedPassword(password, realm);
			return hashedPassword.toString();
		}
	}

	private void showNotification(CharSequence text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}