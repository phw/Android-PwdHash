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
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the RBrainz project nor the names of the
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
 * @author Philipp Wolfer <ph.wolfer></ph.wolfer>@gmail.com>
 */
package com.uploadedlobster.PwdHash.storage

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

class HistoryDataSource(context: Context?) {
    private var mDatabase: SQLiteDatabase? = null
    private val mDbHelper: HistoryOpenHelper = HistoryOpenHelper(context)

    @Throws(SQLException::class)
    fun open() {
        mDatabase = mDbHelper.writableDatabase
    }

    fun close() {
        mDbHelper.close()
    }

    fun insertHistoryEntry(realm: String) {
        val id = getExistingEntryId(realm)
        val values = arrayOf(
            if (id < 0) null else id.toString(),
            realm, realm)
        val sql = ("INSERT OR REPLACE INTO history ("
                + HistoryOpenHelper.Companion.COLUMN_ID + ", "
                + HistoryOpenHelper.Companion.COLUMN_REALM + ", "
                + HistoryOpenHelper.Companion.COLUMN_USAGE_COUNT + ", "
                + HistoryOpenHelper.Companion.COLUMN_LAST_ACCESS + ") "
                + "VALUES (?, ?, "
                + "(SELECT "
                + HistoryOpenHelper.Companion.COLUMN_USAGE_COUNT
                + " + 1 FROM history WHERE "
                + HistoryOpenHelper.Companion.COLUMN_REALM
                + " = ?), datetime('now'))")
        mDatabase!!.execSQL(sql, values)
    }

    fun getHistoryCursor(partialRealm: String): Cursor {
        val columns = arrayOf<String>(HistoryOpenHelper.Companion.COLUMN_ID,
            HistoryOpenHelper.COLUMN_REALM)
        val selection: String = HistoryOpenHelper.Companion.COLUMN_REALM + " LIKE ?"
        val selectionArgs = arrayOf("%$partialRealm%")
        val orderBy: String = (HistoryOpenHelper.Companion.COLUMN_USAGE_COUNT
                + " DESC, "
                + HistoryOpenHelper.Companion.COLUMN_LAST_ACCESS
                + " DESC")
        val limit = SUGGESTION_LIMIT.toString()
        return mDatabase!!.query(
            HistoryOpenHelper.Companion.TABLE_HISTORY,
            columns,
            selection,
            selectionArgs,
            "", "",
            orderBy,
            limit)
    }

    private fun getExistingEntryId(realm: String): Int {
        val columns = arrayOf<String>(HistoryOpenHelper.Companion.COLUMN_ID)
        val selection: String = HistoryOpenHelper.Companion.COLUMN_REALM + " LIKE ?"
        val selectionArgs = arrayOf(realm)
        val cursor = mDatabase!!.query(HistoryOpenHelper.Companion.TABLE_HISTORY,
            columns,
            selection,
            selectionArgs,
            "",
            "",
            "")
        return if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndex(HistoryOpenHelper.Companion.COLUMN_ID)
            val id = cursor.getInt(idColumn)
            cursor.close()
            id
        } else {
            -1
        }
    }

    companion object {
        private const val SUGGESTION_LIMIT = 6
    }

}