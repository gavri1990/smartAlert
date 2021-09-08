package com.example.smartalert;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Redirect extends Activity
{
    SharedPreferences preferences; //αντικείμενο SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(!preferences.contains("language")) //αν δεν έχει επιλεγεί γλώσσα από χρήστη
        {
            Intent intent = new Intent(this, Language.class); //redirect σε activity Language
            startActivity(intent);
            finish(); //τερματισμός Activity, μη διαθέσιμη η εμφάνισή της μέσω κουμπιού back στη νέα Activity
        }
        else if(!preferences.contains("initialContactsSet")) //αν δεν έχει προστεθεί τουλάχιστον 1 επαφή
        {
            Intent intent = new Intent(this, AddContact.class); //redirect σε MyContacts activity
            startActivity(intent);
            finish(); //τερματισμός Activity, μη διαθέσιμη η εμφάνισή της μέσω κουμπιού back στη νέα Activity
        }
        else //αν έχει επιλεχθεί γλώσσα και έχει προστεθεί τουλάχιστον 1 επαφή, ξεκινάμε από την Main
        {
            Intent intent = new Intent(this, MainActivity.class); //redirect σε MainActivity
            startActivity(intent);
            finish(); //τερματισμός Activity, μη διαθέσιμη η εμφάνισή της μέσω κουμπιού back στη νέα Activity
        }
    }
}