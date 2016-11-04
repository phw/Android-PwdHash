package com.uploadedlobster.PwdHash;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.uploadedlobster.PwdHash.activities.PwdHashApp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.not;

/**
 * @author Philipp Wolfer
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
	private ViewInteraction siteAddressInput;
	private ViewInteraction passwordInput;
	private ViewInteraction copyBtn;

	@Rule
	public ActivityTestRule mActivityRule = new ActivityTestRule<>(
			PwdHashApp.class);

	@Before
	public void setUp() throws Exception {
		siteAddressInput = onView(withId(R.id.siteAddress));
		passwordInput = onView(withId(R.id.password));
		copyBtn = onView(withId(R.id.copyBtn));
	}

	@Test
	public void testCopyButtonDisabledWhenAllInputsAreEmpty() {
		clearAllInputs();
		copyBtn.check(matches(not(isEnabled())));
	}

	@Test
	public void testCopyButtonEnabledWhenAllInputsAreSet() {
		clearAllInputs();
		siteAddressInput.perform(typeText("http://www.example.com/test"));
		passwordInput.perform(typeText("mysecret"));
		copyBtn.check(matches(isEnabled()));
	}

	@Test
	public void testCopyButtonDisabledWhenOnlySiteAdressInputIsSet() {
		clearAllInputs();
		siteAddressInput.perform(typeText("http://www.example.com/test"));
		copyBtn.check(matches(not(isEnabled())));
	}

	@Test
	public void testCopyButtonDisabledWhenOnlyPasswordInputIsSet() {
		clearAllInputs();
		passwordInput.perform(typeText("mysecret"));
		copyBtn.check(matches(not(isEnabled())));
	}

	@Test
	public void testCopyToClipboard() {
		clearAllInputs();
		siteAddressInput.perform(typeText("http://www.example.com/test"));
		passwordInput.perform(typeText("mysecret"));
		copyBtn.perform(click());

		getInstrumentation().runOnMainSync(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				final Context context = getTargetContext();
				String clipboardContent;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					final ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
					clipboardContent = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
				}
				else {
					final android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
					clipboardContent = clipboard.getText().toString();
				}

				assertEquals("C3bvEXk6rU", clipboardContent);
			}
		});
	}

//	@Test
//	public void testToastMessage() {
//		clearAllInputs();
//
//		siteAddressInput.perform(typeText("http://www.example.com/test"));
//		passwordInput.perform(typeText("mysecret"));
//		copyBtn.perform(click());
//
//		// Check display of toast message
//		onView(withText(R.string.copiedToClipboardNotification))
//				.inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView())))
//				.check(matches(isDisplayed()));
//		onView(withText(R.string.copiedToClipboardNotification)).inRoot(new ToastMatcher())
//				.check(matches(isDisplayed()));
//	}

	@Test
	public void testDisplayOfPassword() {
		ViewInteraction hashedPassword = onView(withId(R.id.hashedPassword));
		hashedPassword.check(matches(not(withText("C3bvEXk6rU"))));

		clearAllInputs();
		siteAddressInput.perform(typeText("http://www.example.com/test"));
		passwordInput.perform(typeText("mysecret"));
		hashedPassword.check(matches(withText("C3bvEXk6rU")));

		passwordInput.perform(replaceText("myothersecret"));
		hashedPassword.check(matches(not(withText("C3bvEXk6rU"))));
		hashedPassword.check(matches(withText("dG4KvuJTNGrWRY2")));
	}

	private void clearAllInputs() {
		siteAddressInput.perform(clearText());
		passwordInput.perform(clearText());
	}
}