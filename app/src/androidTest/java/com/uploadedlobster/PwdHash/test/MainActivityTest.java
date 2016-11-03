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
public class MainActivityTest extends
		ActivityInstrumentationTestCase2<PwdHashApp> {
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

	public void testCopyButtonDisabledWhenAllInputsAreEmpty() {
		clearAllInputs();
		assertFalse(copyBtn.isEnabled());
	}

	public void testCopyButtonEnabledWhenAllInputsAreSet() {
		clearAllInputs();
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");
		assertTrue(copyBtn.isEnabled());
	}

	public void testCopyButtonDisabledWhenOnlySiteAdressInputIsSet() {
		clearAllInputs();
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		assertFalse(copyBtn.isEnabled());
	}

	public void testCopyButtonDisabledWhenOnlyPasswordInputIsSet() {
		clearAllInputs();
		solo.enterText(passwordInput, "mysecret");
		assertFalse(copyBtn.isEnabled());
	}

	public void testCopyButton() {
		clearAllInputs();
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");

		solo.clickOnView(copyBtn);

		String toastMessage = getActivity().getResources().getString(
				R.string.copiedToClipboardNotification);
		assertTrue(solo.searchText(toastMessage));
	}

	public void testDisplayOfPassword() {
		assertFalse(solo.searchText("C3bvEXk6rU"));

		clearAllInputs();
		solo.enterText(siteAddressInput, "http://www.example.com/test");
		solo.enterText(passwordInput, "mysecret");
		assertTrue(solo.searchText("C3bvEXk6rU"));

		solo.clearEditText(passwordInput);
		solo.enterText(passwordInput, "myothersecret");
		assertFalse(solo.searchText("C3bvEXk6rU"));
		assertTrue(solo.searchText("dG4KvuJTNGrWRY2"));
	}
	
	private void clearAllInputs() {
		solo.clearEditText(siteAddressInput);
		solo.clearEditText(passwordInput);
	}
}
