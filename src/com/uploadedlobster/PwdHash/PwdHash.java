package com.uploadedlobster.PwdHash;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

public class PwdHash extends Activity {
	private EditText mSiteAddress;
	private EditText mPassword;
	private EditText mHashedPassword;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		mSiteAddress = (EditText) findViewById(R.id.siteAddress);
		mPassword = (EditText) findViewById(R.id.password);
		mHashedPassword = (EditText) findViewById(R.id.hashedPassword);
		Button generateBtn = (Button) findViewById(R.id.generateBtn);
		
		generateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String realm = DomainExtractor.extractDomain(mSiteAddress.getText().toString());
				String password = mPassword.getText().toString();
				HashedPassword hashedPassword = new HashedPassword(password, realm);
				mHashedPassword.setText(hashedPassword.toString());
			}
		});
	}
}