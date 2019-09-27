/**
 * PwdHash, HistoryOpenHelper.java
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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uploadedlobster.PwdHash.util.Constants;

public class HistoryOpenHelper extends SQLiteOpenHelper {

	public static final String TABLE_HISTORY = "history";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_REALM = "realm";
	public static final String COLUMN_USAGE_COUNT = "usage_count";
	public static final String COLUMN_LAST_ACCESS = "last_access";
	
	private static final String CREATE_TABLE =
		"CREATE TABLE " + TABLE_HISTORY + " (" +
		COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
		COLUMN_REALM + " TEXT," +
		COLUMN_USAGE_COUNT + " INTEGER NOT NULL DEFAULT 0," +
		COLUMN_LAST_ACCESS + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)";
	
	HistoryOpenHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
