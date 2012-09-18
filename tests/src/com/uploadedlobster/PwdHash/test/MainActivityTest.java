package com.uploadedlobster.PwdHash.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;
import com.uploadedlobster.PwdHash.activities.PwdHashApp;
import com.uploadedlobster.PwdHash.R;

/**
 * @author Philipp Wolfer
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<PwdHashApp> {
	private Solo solo;
	
	private EditText siteAddressInput;
	private EditText passwordInput;
	private Button copyBtn;
	
	public MainActivityTest() {
        super(PwdHashApp.class);
    }
	
	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		
		siteAddressInput = (EditText) solo.getView(R.id.siteAddress);
		passwordInput = (EditText) solo.getView(R.id.password);
		copyBtn = (Button) solo.getView(R.id.copyBtn);
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}
	
	public void testCopyButtonDisabled() {
		solo.clearEditText(siteAddressInput);
		solo.clearEditText(passwordInput);
		assertFalse(copyBtn.isEnabled());
		
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");
		assertTrue(copyBtn.isEnabled());
		
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.clearEditText(passwordInput);
		assertFalse(copyBtn.isEnabled());
		
		solo.clearEditText(siteAddressInput);
		solo.enterText(passwordInput, "mysecret");
		assertFalse(copyBtn.isEnabled());
	}
	
	public void testCopyButton()
	{
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");
		
		solo.clickOnView(copyBtn);
		
		String toastMessage = getActivity().getResources().getString(R.string.copiedToClipboardNotification);
		assertTrue(solo.searchText(toastMessage));
	}
	
	public void testDisplayOfPasswordButton()
	{
		assertFalse(solo.searchText("C3bvEXk6rU"));
		
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");
		assertTrue(solo.searchText("C3bvEXk6rU"));
		
		solo.clearEditText(passwordInput);
		solo.enterText(passwordInput, "myothersecret");
		assertFalse(solo.searchText("C3bvEXk6rU"));
		assertTrue(solo.searchText("dG4KvuJTNGrWRY2"));
	}
}
