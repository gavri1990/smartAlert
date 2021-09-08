package com.example.smartalert;

import android.provider.BaseColumns;

public class SmartAlertDbContract
{
    private SmartAlertDbContract() //private constructor για προστασία
    { }

    public static class ContactEntry implements BaseColumns //implementation του interface BaseColumns
    {
        public static final String TABLE_NAME = "contact"; //όνομα table
        public static final String COLUMN_NAME_CONTACT_NAME = "contact_name"; //ονόματα columns
        public static final String COLUMN_NAME_CONTACT_TELEPHONE = "contact_telephone";
    }
}
