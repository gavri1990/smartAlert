package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyContacts extends AppCompatActivity
{
    ListView listView;
    Toolbar toolbar;
    SmartAlertDbHelper smartAlertDbHelper; // instance της class SmartAlertDbHelper (που κάνει extend την SQLiteOpenHelper)
    SharedPreferences preferences;
    Button button5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) //override μεθόδου για εισαγωγή custom menu
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_contacts_menu, menu); //inflate του menu το δικό μας menu
        if(!preferences.contains("initialContactsNextPressed")) //δεν έχει αποθηκεύσει επαφές για πράτη φορά
        {
            menu.getItem(0).setVisible(false); //απόκρυψη επιλογής για μετάβαση σε mainActivity
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) //σε επιλογή κάποιου item του μενού
    {
        switch (item.getItemId()) //διάφορα σενάρια εικονιδίου γλώσσας και κειμένου από κάτω
        {
            case R.id.item1:
                Intent intent = new Intent(this, MainActivity.class); //μετάβαση σε Main activity
                startActivity(intent);
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.contains("language")) //αν έχει επιλεχθεί γλώσσα από χρήστη
        {
            String chosenLanguage = preferences.getString("language", "en");
            if(chosenLanguage.equals("English")) //έλεγχος επιλεγμένης γλώσσας, αποθ. στα SharedPreferences
            {
                Locale locale = new Locale("en"); //αλλαγή του locale τοπικά στην εφαρμογή
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                if (Build.VERSION.SDK_INT >= 17)
                {
                    config.setLocale(locale);
                }
                else
                {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }
            else if(chosenLanguage.equals("Ελληνικά"))
            {
                Locale locale = new Locale("el");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                if (Build.VERSION.SDK_INT >= 17)
                {
                    config.setLocale(locale);
                }
                else
                {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }
            else
            {
                Locale locale = new Locale("es");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                if (Build.VERSION.SDK_INT >= 17)
                {
                    config.setLocale(locale);
                }
                else
                {
                    config.locale = locale;
                }
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }
        }
        //παραπάνω κώδικας τρέχει πριν την setContentView, που ακολουθεί
        setContentView(R.layout.activity_mycontacts);
        smartAlertDbHelper = new SmartAlertDbHelper(getApplicationContext(), null, null, 1); //κλήση constructor της class και κατασκευή

        button5 = findViewById(R.id.button5); //σύνδεση με xml μέσω ids
        listView = findViewById(R.id.listView);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project


        if(preferences.contains("initialContactsNextPressed")) //αν έχει ήδη γίνει η αποθήκευση αρχικών επαφών
        {
            button5.setText(R.string.my_contacts_button_delete_contact); //διαφορετικό κείμενο button5 ανά περίπτωση
        }
        else
        {
            button5.setText(R.string.my_contacts_button_next);
        }

        List<Contact> cList = readContactsFromDb(); //κλήση της readContactsFromDb(), ό,τι λίστα γυρίσει θα εκχωρηθεί στην cList
        List<String> contactName = new ArrayList<>(); //2η λίστα, θα περιέχει όνομα και τηλ κάθε επαφής
        for(int i = 0; i < cList.size(); i++)
        {
            //το όνομα και το τηλ του αντίστοιχου Contact object
            contactName.add(cList.get(i).getContact_name() + ": " + cList.get(i).getContact_telephone());
        }
        //για εμφάνιση κάθε γραμμής του Listview, χρήση adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactName);
        listView.setAdapter(adapter); //"κολλάμε" τον adapter στη listview
    }

    public List<Contact> readContactsFromDb() //μέθοδος για ανάγνωση από ΒΔ
    {
        SQLiteDatabase db = smartAlertDbHelper.getReadableDatabase(); //object για ανάγνωση από ΒΔ
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

    public void goAddContact(View view) //πάτημα κουμπιού Add Contact
    {
        Intent intent = new Intent(this, AddContact.class); //μετάβαση στο αντίστοιχο activity
        startActivity(intent);
    }

    public void goDeleteContacts(View view) //πάτημα κουμπιού Delete Contact (ή Next)
    {
        if(preferences.contains("initialContactsSet")) //αν έχει αποθηκευτεί τουλάχιστον μία επαφή
        {
            if(button5.getText().equals(getString(R.string.my_contacts_button_delete_contact)))
            {
                Intent intent = new Intent(this, DeleteContacts.class); //μετάβαση στο αντίστοιχο activity
                startActivity(intent);
            }
            else //αν το κουμπί γράφει 'NEXT'
            {
                SharedPreferences.Editor editor = preferences.edit(); //έτοιμοι για edit στα preferences
                editor.putString("initialContactsNextPressed", "true"); //εισαγωγή στοιχείου
                editor.commit(); //αποθήκευση αλλαγών
                Intent intent = new Intent(this, MainActivity.class); //μετάβαση στο αντίστοιχο activity
                startActivity(intent);
            }
        }
        else //αν δεν έχει αποθηκευτεί καμία επαφή, ενημέρωση σε πάτημα κουμπιού NEXT
        {
            Toast.makeText(this, R.string.toast_add_contacts, Toast.LENGTH_SHORT).show();
        }
    }
}