/**
 * PwdHash, HistoryStorage.java
 * A password hash implementation for Android.
 *
 * Copyright (c) 2012 Philipp Wolfer
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
 * @author Philipp Wolfer <ph.wolfer@gmail.com>
 */

package com.uploadedlobster.PwdHash.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDataSource {
	
	private SQLiteDatabase mDatabase;
	private HistoryOpenHelper mDbHelper;
	
	private static final int SUGGESTION_LIMIT = 6;
	
	public HistoryDataSource(Context context) {
		mDbHelper = new HistoryOpenHelper(context);
	}
	
	public void open() throws SQLException {
		mDatabase = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}
	
	public void insertHistoryEntry(String realm) {
		int id = getExistingEntryId(realm);
		
		String[] values = new String[] {
				id < 0 ? null : String.valueOf(id),
				realm, realm };
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder
			.append("INSERT OR REPLACE INTO history (")
			.append(HistoryOpenHelper.COLUMN_ID).append(", ")
			.append(HistoryOpenHelper.COLUMN_REALM).append(", ")
			.append(HistoryOpenHelper.COLUMN_USAGE_COUNT).append(", ")
			.append(HistoryOpenHelper.COLUMN_LAST_ACCESS).append(") ")
			.append("VALUES (?, ?, ")
			.append("(SELECT ")
			.append(HistoryOpenHelper.COLUMN_USAGE_COUNT)
			.append(" + 1 FROM history WHERE ")
			.append(HistoryOpenHelper.COLUMN_REALM)
			.append(" = ?), datetime('now'))");
		
		mDatabase.execSQL(sqlBuilder.toString(), values);
	}

	public Cursor getHistoryCursor(String partialRealm) {
		String[] columns = new String[] { HistoryOpenHelper.COLUMN_ID, HistoryOpenHelper.COLUMN_REALM };
		String selection = HistoryOpenHelper.COLUMN_REALM + " LIKE ?";
		String[] selectionArgs = new String[] { "%" + partialRealm + "%" };
		
		StringBuilder orderBy = new StringBuilder()
			.append(HistoryOpenHelper.COLUMN_USAGE_COUNT)
			.append(" DESC, ")
			.append(HistoryOpenHelper.COLUMN_LAST_ACCESS)
			.append(" DESC");
		
		String limit = String.valueOf(SUGGESTION_LIMIT);
		
		Cursor cursor = mDatabase.query(
				HistoryOpenHelper.TABLE_HISTORY,
				columns,
				selection,
				selectionArgs,
				"", "",
				orderBy.toString(),
				limit);
		
		return cursor;
	}
	
	private int getExistingEntryId(String realm) {
		String[] columns = new String[] { HistoryOpenHelper.COLUMN_ID };
		String selection = HistoryOpenHelper.COLUMN_REALM + " LIKE ?";
		String[] selectionArgs = new String[] { realm };
		
		Cursor cursor = mDatabase.query(HistoryOpenHelper.TABLE_HISTORY, columns, selection, selectionArgs, "", "", "");
		
		if (cursor.moveToFirst()) {
			int idColumn = cursor.getColumnIndex(HistoryOpenHelper.COLUMN_ID);
			return cursor.getInt(idColumn);
		}
		else {
			return -1;
		}
	}
}
