package com.uploadedlobster.PwdHash.storage;

import android.os.AsyncTask;

public class UpdateHistoryTask extends AsyncTask<String, Void, Void> {

	HistoryDataSource mDataSource;
	
	public UpdateHistoryTask(HistoryDataSource dataSource) {
		mDataSource = dataSource;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		for (String realm : params) {
			mDataSource.insertHistoryEntry(realm);
		}
		
		return null;
	}

}
