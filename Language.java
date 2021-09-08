package com.example.smartalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class Language extends AppCompatActivity
{
    ImageView imageView;
    TextView textView;
    ImageButton imageButton, imageButton2;
    Button button;
    Toolbar toolbar;
    SharedPreferences preferences;
    int index = 0; //index επιλογής γλώσσας

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
        setContentView(R.layout.activity_language);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project
        imageView = findViewById(R.id.imageView); //σύνδεση με xml μέσω ids
        imageButton = findViewById(R.id.imageButton);
        imageButton2 = findViewById(R.id.imageButton2);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        imageView.setImageResource(R.drawable.englandflag); //αγγλική σημαία αρχικά
        textView.setText("English");

        if(preferences.contains("language")) //αν έχει ήδη επιλεχθεί γλώσσα από χρήστη
        {
            button.setText(R.string.language_button_save);
        }
        else
        {
            button.setText(R.string.language_button_next);
        }
    }

    public void nextLanguage(View view) //πάτημα δεξιού βέλους
    {
        index++; //increment του index
        if(index == 1) //δεύτερη φωτό
        {
            imageView.setImageResource(R.drawable.greeceflag);
            textView.setText("Ελληνικά");
        }
        else if(index == 2) //τελευταία φωτό
        {
            imageView.setImageResource(R.drawable.spainflag);
            textView.setText("Español");
        }
        else //ξαναρχίζει ο κύκλος
        {
            index = 0; //επιστροφή στην πρώτη
            imageView.setImageResource(R.drawable.englandflag);
            textView.setText("English");
        }
    }

    public void previousLanguage(View view) //πάτημα αριστερού βέλους
    {
        index--; //decrement του index
        if(index == 1) //δεύτερη φωτό
        {
            imageView.setImageResource(R.drawable.greeceflag);
            textView.setText("Ελληνικά");
        }
        else if(index == 0) //πρώτη φωτό
        {
            imageView.setImageResource(R.drawable.englandflag);
            textView.setText("English");
        }
        else //ξαναρχίζει ο κύκλος
        {
            index = 2; //επιστροφή στην τελευταία
            imageView.setImageResource(R.drawable.spainflag);
            textView.setText("Español");
        }
    }



    public void next(View view) //πάτημα κουμπιού Next (ή Save)
    {
        SharedPreferences.Editor editor = preferences.edit(); //έτοιμοι για edit στα preferences
        if(index == 0)
        {
            editor.putString("language", "English"); //εισαγωγή στοιχείου
        }
        else if(index == 1)
        {
            editor.putString("language", "Ελληνικά");
        }
        else
        {
            editor.putString("language", "Español");
        }
        editor.commit(); //αποθήκευση αλλαγών


        if(preferences.contains("language")) //αν έχει επιλεχθεί γλώσσα από χρήστη
        {
            String chosenLanguage = preferences.getString("language", "en");
            //πάλι αλλαγή του locale βάσει της επιλεγμένης στα SharedPreferences γλώσσας
            if(chosenLanguage.equals("English"))
            {
                Locale locale = new Locale("en");
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


        if(preferences.contains("initialContactsSet")) //αν ο χρήστης έχει ήδη αποθηκεύσει επαφές
        {
            Toast.makeText(this, R.string.toast_language_saved, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class); //redirect σε MainActivity
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, R.string.toast_language_selected, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MyContacts.class); //redirect σε MyContacts
            startActivity(intent);
        }
    }
}