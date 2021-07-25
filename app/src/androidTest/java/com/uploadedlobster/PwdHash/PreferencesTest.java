package com.uploadedlobster.PwdHash;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.uploadedlobster.PwdHash.util.Preferences;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class PreferencesTest {
    @Test
    public void testSaveSiteAddress() {
        final Context context = InstrumentationRegistry.getTargetContext();
        Preferences preferences = new Preferences(context);

        String urlToSave = "https://www.example.com/storage-test";
        String urlLoaded = preferences.getSavedSiteAddress();

        assertNotEquals(urlToSave, urlLoaded);

        preferences.setSavedSiteAddress(urlToSave);
        urlLoaded = preferences.getSavedSiteAddress();
        assertEquals(urlToSave, urlLoaded);
    }
}
