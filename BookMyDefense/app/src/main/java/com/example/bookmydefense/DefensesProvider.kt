package com.example.bookmydefense

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.lang.IllegalArgumentException
import java.util.HashMap

class DefensesProvider : ContentProvider() {
    companion object {
        val PROVIDER_NAME = "com.example.bookmydefense/DefensesProvider"
        val URL = "content://$PROVIDER_NAME/Defenses"
        val CONTENT_URI = Uri.parse(URL)
        val _ID = "_id"
        val STUDENT_NAME = "STUDENT_NAME"
        val THESIS = "THESIS"
        val COM_NAME_1 = "COMMITTEE_NAME_1"
        val COM_NAME_2 = "COMMITTEE_NAME_2"
        val COM_NAME_3 = "COMMITTEE_NAME_3"
        val ROOM = "ROOM"
        val DATE = "DATE"
        val TIME = "TIME"

        private val DEFENSES_PROJECTION_MAP: HashMap<String, String>? = null
        val DEFENSE = 1
        val DEFENSE_ID = 2
        val uriMatcher: UriMatcher? = null
        val DATABASE_NAME = "PSUT"
        val DEFENSES_TABLE_NAME = "Defenses"
        val DATABASE_VERSION = 1
        val CREATE_DB_TABLE =
            " CREATE TABLE " + DEFENSES_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " STUDENT_NAME TEXT, " +
                    " THESIS TEXT, " +
                    " COMMITTEE_NAME_1 TEXT, " +
                    " COMMITTEE_NAME_2 TEXT, " +
                    " COMMITTEE_NAME_3 TEXT, " +
                    " DATE TEXT, " +
                    " TIME TEXT, " +
                    " ROOM TEXT);"
    }

    /*----------------------------------------------------------------------------------------------------------*/
    private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH);

    init {
        sUriMatcher.addURI(PROVIDER_NAME, "students", DEFENSE);
        sUriMatcher.addURI(PROVIDER_NAME, "students/#", DEFENSE_ID);
    }

    /*----------------------------------------------------------------------------------------------------------*/
    private var db: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + DEFENSES_TABLE_NAME)
            onCreate(db)
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)

        db = dbHelper.writableDatabase
        return if (db == null) false else true
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        val rowID = db!!.insert(DEFENSES_TABLE_NAME, "", values)

        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }
        throw SQLException("Failed to add a record into $uri")
    }
    /*----------------------------------------------------------------------------------------------------------*/
    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        //return db?.query(DEFENSES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)

        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = DEFENSES_TABLE_NAME

        if (sortOrder == null || sortOrder === "") {

            sortOrder = STUDENT_NAME
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)

        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        count = db!!.delete(
            DEFENSES_TABLE_NAME, selection,
            selectionArgs)

        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            DEFENSE -> count = db!!.update(
                DEFENSES_TABLE_NAME, values, selection,
                selectionArgs
            )
            DEFENSE_ID -> count = db!!.update(
                DEFENSES_TABLE_NAME,
                values,
                _ID + " = " + uri.pathSegments[1] + (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else ""),
                selectionArgs
            )
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun getType(uri: Uri): String? {
        when (uriMatcher!!.match(uri)) {
            DEFENSE -> return "vnd.android.cursor.dir/vnd.example.students"
            DEFENSE_ID -> return "vnd.android.cursor.item/vnd.example.students"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }
}

