package com.uploadedlobster.PwdHash;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PwdHash extends Activity {
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
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(hashedPassword);
				finish();
			}

		});
	}

	private String generateHashedPassword() {
		String realm = DomainExtractor.extractDomain(mSiteAddress.getText()
				.toString());
		String password = mPassword.getText().toString();

		if (!realm.equals("") && !password.equals("")) {
			HashedPassword hashedPassword = new HashedPassword(password, realm);
			return hashedPassword.toString();
		}
		return "";
	}
}