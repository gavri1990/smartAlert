package com.example.smartalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeleteContacts extends AppCompatActivity
{
    ListView listView;
    Toolbar toolbar; //αντικείμενο toolbar για αντικατάσταση του action bar
    SmartAlertDbHelper smartAlertDbHelper; // instance της class SmartAlertDbHelper (που κάνει extend την SQLiteOpenHelper)
    List<Integer> contactId = new ArrayList<>(); //χρήση για διαγραφή επιλεγμένων επαφών
    int deletedCount = 0; //θα μετρά τις επαφές που διαγράφηκαν
    SQLiteDatabase db; //για σύνδεση με sqlite ΒΔ

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contacts);
        smartAlertDbHelper = new SmartAlertDbHelper(getApplicationContext(), null, null, 1); //κλήση constructor της class και κατασκευή
        listView = findViewById(R.id.listView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project

        List<Contact> cList = readContactsFromDb(); //κλήση της readContactsFromDb(), ό,τι λίστα γυρίσει θα εκχωρηθεί στην cList
        List<String> contactName = new ArrayList<>(); //2η λίστα, θα περιέχει όνομα και τηλ κάθε επαφής
        for(int i = 0; i < cList.size(); i++)
        {
            contactId.add(cList.get(i).getContact_id()); //περνάμε το id του αντίστοιχου Contact object
            //το όνομα και το τηλ του αντίστοιχου Contact object
            contactName.add(cList.get(i).getContact_name() + ": " + cList.get(i).getContact_telephone());
        }
        //για εμφάνιση κάθε γραμμής του Listview με checkbox χρήση του multiplechoice
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, contactName);
        listView.setAdapter(adapter);

        //φτιάχνουμε listener για τα clicks σε στοιχεία της Listview
        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        //καμία ενέργεια, θα τικάρονται όμως τα checkboxes
                    }
                };
        listView.setOnItemClickListener(itemClickListener); //"κολλάμε" και τον clickedListener στη listview
    }

    public List<Contact> readContactsFromDb() //μέθοδος για ανάγνωση από ΒΔ
    {
        db = smartAlertDbHelper.getReadableDatabase(); //object της τάξης για να διαβάσουμε από ΒΔ
        List<Contact> contactList = new ArrayList<>();
        String [] projection = //array με strings
                {
                        SmartAlertDbContract.ContactEntry._ID,
                        SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_NAME,
                        SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_TELEPHONE
                };

        //μέθοδος του object db η db.query()
        Cursor c = db.query(
                SmartAlertDbContract.ContactEntry.TABLE_NAME, // το table όπου θα εκτελεστεί το query
                projection,                                   // τα columns που θα επιστρέψει το query
                null,                                // όχι WHERE clause
                null,                             // όχι values για το WHERE clause, αφού όχι WHERE clause
                null,                                // όχι group statement
                null,                                 // όχι having, αφού όχι group
                SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_NAME    //sort βάσει ονόματος, αλφαβητικά
        );
        while (c.moveToNext()) //ο cursor πηγαίνει στην επόμενη γραμμή και ταυτόχρονα ελέγχει αν true(αν υπάρχει η γραμμή) και εκτελεί κώδικα
        {
            //δημιουργούμε κάθε φορά object της class Contact με παραμέρους τα περιεχόμενα των columns
            Contact contact = new Contact(c.getInt(c.getColumnIndex(SmartAlertDbContract.ContactEntry._ID)),
                    c.getString(c.getColumnIndex(SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_NAME)),
                    c.getString(c.getColumnIndex(SmartAlertDbContract.ContactEntry.COLUMN_NAME_CONTACT_TELEPHONE)));
            contactList.add(contact); // και προσθέτουμε το object στη List
        }
        db.close(); //πάντα κλείνουμε σύνδεση με ΒΔ
        //η method θα γυρίσει τη λίστα με τα objects που φτιάχτηκαν διαβἀζοντας και αντιγράφοντας από τη ΒΔ
        return contactList;
    }

    public int deleteContactFromDb(int id) //μέθοδος για διαγραφή από ΒΔ
    {
        SQLiteDatabase db = smartAlertDbHelper.getReadableDatabase();
        String selection = SmartAlertDbContract.ContactEntry._ID + " =?"; //selection είναι το expression
        String[] selectionArgs = {String.valueOf(id)}; //μεταβλητή για το expression, το id - παράμετρος μεθόδου
        int count = db.delete(
                SmartAlertDbContract.ContactEntry.TABLE_NAME,
                selection,
                selectionArgs);
        db.close(); //κλείσιμο σύνδεσης με ΒΔ
        return count; //επιστροφή του αριθμού γραμμών που διαγράφηκαν
    }

    public void deleteContacts(View view) //πάτημα κουμπιού Delete
    {
        SparseBooleanArray isChecked = listView.getCheckedItemPositions(); //θα έχει 1 στις θέσεις των τσεκαρισμένων στοιχείων

        try
        {
            for(int i = 0; i < listView.getAdapter().getCount(); i++) //έλεγχος όλων των στοιχείων listview
            {
                if(isChecked.get(i)) //αν τσεκαρισμένο το υπό έλεγχο στοιχείο
                {
                    deletedCount += deleteContactFromDb(contactId.get(i)); //κλήση μεθόδου για διαγραφή από ΒΔ
                }
            }
            if(deletedCount > 0) //αν έχει επιλεχθεί τουλάχιστον 1 επαφή για να διαγραφεί
            {
                if(deletedCount == 1)
                {
                    Toast.makeText(this, String.valueOf(deletedCount) + " " + getString(R.string.toast_one_contact_deleted), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, String.valueOf(deletedCount) + " " + getString(R.string.toast_contacts_deleted), Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(this, MyContacts.class); //επιστροφή σε MyContacts
                startActivity(intent);
            }
            else
            {
                Toast.makeText(this, R.string.toast_choose_contacts_delete, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_SHORT).show();
            db.close();
        }

    }
}