package com.example.smartalert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SmartAlertDbHelper extends SQLiteOpenHelper
{
    //ιδιότητες class  = στοιχεία της database
    public static final String DATABASE_NAME = "ContactDB.db"; //όνομα DB
    public static final int DATABASE_VERSION = 3; //έκδοση DB
    private static final String TEXT_TYPE = " TEXT NOT NULL";
    private static final String COMMA_SEP = ",";

    //κατασκευή strings - εντολών για database
    //1.Δημιουργία table
    private static final String SQL_CREATE_CONTACT_TABLE =
            "CREATE TABLE " + SmartAlertDbContract.ContactEntry.TABLE_NAME + " (" +
                    SmartAlertDbContract.ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
                    SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_TELEPHONE + TEXT_TYPE + " )";

    //2.Διαγραφή table
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SmartAlertDbContract.ContactEntry.TABLE_NAME;




    //constructor
    public SmartAlertDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db); //κλήση της OnCreate με παράμετρο τη db
    }
}
