package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddContact extends AppCompatActivity
{
    EditText editText1, editText2;
    Toolbar toolbar; //αντικείμενο toolbar για αντικατάσταση του action bar
    SmartAlertDbHelper smartAlertDbHelper; // instance της class SmartAlertDbHelper (που κάνει extend την SQLiteOpenHelper)
    SharedPreferences preferences; //αντικείμενο τάξης SharedPreferences


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        smartAlertDbHelper = new SmartAlertDbHelper(getApplicationContext(), null, null, 1); //κλήση constructor της class και κατασκευή
        editText1 = findViewById(R.id.editText1); //σύνδεση controls με τα ids
        editText2 = findViewById(R.id.editText2);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project
    }


    public void addContactToDb(Contact contact) //μέθοδος για εισαγωγή όποια παραμέτρου της δοθεί σε ΒΔ
    {
        SQLiteDatabase db = smartAlertDbHelper.getWritableDatabase(); //object της τάξης για να γράψουμε σε ΒΔ

        ContentValues values = new ContentValues(); //αντικείμενο της τάξης ContentValues
        /*βάζουμε values παίρνοντας τις ιδιότητες του object newMovie μέσω των μεθόδων της class
        που έχουν πρόσβαση σε αυτές*/
        values.put(SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_NAME, contact.getContact_name());
        values.put(SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_TELEPHONE, contact.getContact_telephone());

        //Εισαγωγή εγγραφής
        try
        {
            //Γυρνάει το result, είναι μορφής long
            long result = db.insert(SmartAlertDbContract.ContactEntry.TABLE_NAME, null, values);
            db.close(); //κλείνουμε connection με database
            Toast.makeText(this, contact.getContact_name() + " " + getString(R.string.toast_contact_added), Toast.LENGTH_SHORT).show();
            if(!preferences.contains("initialContactsSet")) //αν μόλις αποθηκεύτηκε η πρώτη επαφή
            {
                SharedPreferences.Editor editor = preferences.edit(); //έτοιμοι για edit στα preferences
                editor.putString("initialContactsSet", "true"); //εισαγωγή στοιχείου
                editor.commit(); //αποθήκευση αλλαγών
            }
            Intent intent = new Intent(this, MyContacts.class); //μεταφορά στην Activity προβολής επαφών
            startActivity(intent);
        }
        catch (Exception e)
        {
            Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_SHORT).show();
            db.close();
        }
    }

    public void addContact(View view) //πάτημα κουμπιού 'ADD'
    {
        if(editText1.getText().toString().trim().isEmpty() || editText2.getText().toString().trim().isEmpty()) //trim αφαιρεί whitespace σε αρχή και τέλος string
        {
            Toast.makeText(this, R.string.toast_required_fields, Toast.LENGTH_SHORT).show();
        }
        else
        {
            //κλήση constructor της class Contact με παραμέτρους τα κείμενα των editTexts
            Contact newContact = new Contact(editText1.getText().toString(), editText2.getText().toString());
            addContactToDb(newContact); //κλήση της αποπάνω method με παράμετρο τη νέα class
        }
    }
}