package com.example.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Statistics extends AppCompatActivity
{
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //αντικείμενο για σύνδεση με Firebase
    DatabaseReference myRefUser; //έχει το userId του χρήστη στη Firebase
    SharedPreferences preferences; //αντικείμενο SharedPreferences
    Toolbar toolbar;
    Spinner spinner;
    String language;
    ListView listView;
    List<String> alerts = new ArrayList<>(); //2η λίστα, θα περιέχει όνομα και τηλ κάθε επαφής;
    ArrayAdapter<String> adapter1; //αντικείμενο adapter για σύνδεση με listview
    StringBuilder builder = new StringBuilder(); //StringBuilders για μεταφορά δεδομένων από Firebase
    StringBuilder builderFalseAlarm = new StringBuilder();
    Query alertsTimeOrder; //3 αντικείμενα της τάξης Query για ανάκτηση δεδομένων από την FireBase
    Query alertsFall;
    Query alertsFire;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        spinner = findViewById(R.id.spinner); //σύνδεση με xml μέσω ids
        listView = findViewById(R.id.listView);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project

        //παίρνουμε instance των SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        language = preferences.getString("language", "en"); //παίρνουμε αποθηκευμένη γλώσσα
        //μονοπάτι από όπου θα αρχίσουμε να διαβάζουμε τους συναγερμούς του συγκεκριμένου χρήστη
        myRefUser = database.getReference("Users/"
                + preferences.getString("userId", "noId") + "/Alerts/");

        alertsTimeOrder = myRefUser.orderByChild("timeStamp"); //δίνουμε περιεχόμενο σε queries
        alertsFall = myRefUser.orderByChild("hazard").equalTo("Fall");
        alertsFire = myRefUser.orderByChild("hazard").equalTo("Fire");

        //καλούμε constructor του adapter1 με τις απαραίτητες παραμέτρους
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alerts);
        listView.setAdapter(adapter1); //"κολλάμε" τον adapter1 στη listview

        final List<String> filters = new ArrayList<>(); //arrayList με δεδομένα για το μενού του spinner
        filters.add(getString(R.string.spinner_item_filter_by));
        filters.add(getString(R.string.spinner_item_chronol_order));
        filters.add("    " + getString(R.string.spinner_item_ascending));
        filters.add("    " + getString(R.string.spinner_item_descending));
        filters.add(getString(R.string.spinner_item_hazard_type));
        filters.add("    " + getString(R.string.spinner_item_fall));
        filters.add("    " + getString(R.string.spinner_item_fire));

        //δημιουργία adapter για τον spinner με παράμετρο την παραπάνω ArrayList
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, filters)
        {
            //override 2 μεθόδων του adapter2 για αλλαγή εμφάνισης και λειτουργικότητας spinner
            @Override
            public boolean isEnabled(int position) //ρύθμιση του αν θα είναι enabled κάθε στοιχείο του spinner
            {
                if(position == 0 || position == 1 || position == 4) //disabled τα στοιχεία σε αυτές τις θέσεις
                {
                    return false;
                }
                return true; //δεν χρειάζεται else
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
            {
                //επεξεργασία εμφάνισης στοιχείων
                View spinnerView = super.getDropDownView(position, convertView, parent); //ανάθεση σε spinnerView
                TextView spinnerTextView = (TextView) spinnerView;
                if(position == 1 || position == 4) //περαιτέρω επεξεργασία στοιχείων σε θέσεις 1 και 4
                {
                    spinnerTextView.setTextSize(20); //μεγαλύτερη γραμματοσειρά
                    spinnerTextView.setTypeface(null, Typeface.BOLD); //και bold στα non-selectable στοιχεία spinner
                }
                return spinnerView; //return του επεξεργασμένου από εμάς spinnerView που θα αλλάξει την εμφάνιση του spinner
            }
        };
        spinner.setAdapter(adapter2); //"κολλάμε" τον adapter2 στον spinner

        //listener για επιλογή item του spinner από τον χρήστη
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            { //εκτελείται όταν επιλεχτεί κάποιο item
                if (position == 2) //ascending order
                {
                    //κλήση μεθόδου με παραμέτρους το προς εκτέλεση query και το είδος φιλτραρίσματος
                    setFirebaseListener(alertsTimeOrder, "ascending");
                }
                else if (position == 3) //descending order
                {
                    setFirebaseListener(alertsTimeOrder, "descending");
                }
                else if (position == 5) //fall
                {
                    setFirebaseListener(alertsFall, "fall");
                }
                else if (position == 6) //fire
                {
                    setFirebaseListener(alertsFire, "fire");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener); //ορισμός του παραπάνω ως listener του spinner
    }

    //μέθοδος που δέχεται ως όρισμα το query που επιλέχθηκε από χρήστη και γυρνά ένα στιγμιότυπο της Firebase
    public void setFirebaseListener(Query query, final String queryType)
    {
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                alerts.clear(); //διαγραφή περιεχομένου alerts, θα δημιουργηθεί νέο περιεχόμενο
                if(dataSnapshot.hasChildren()) //αν υπάρχουν εγγραφές σε Firebase για αυτό που ζητήθηκε
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String alertKey = ds.getKey(); //παίρνουμε ο κλειδί κάθε alert του συγκεκριμένου χρήστη
                        builder.delete(0,builder.length()); //καθαρισμός StringBuilders πριν και μετά κάθε εσωτερικού loop
                        builderFalseAlarm.delete(0,builderFalseAlarm.length());
                        //for loop θα περάσει από τα 4 πεδία κάθε καταχώρισης alert
                        for(DataSnapshot dSnapshot : dataSnapshot.child(alertKey).getChildren())
                        {
                            if(dSnapshot.getKey().equals("hazard")) //πεδίο hazard
                            {
                                //μεταβλητή alertField θα πάρει την τιμή του πεδίου στη Firebase
                                String alertField = dSnapshot.getValue(String.class);
                                if(alertField.equals("Fall"))
                                {
                                    //χρήση String resources
                                    builder.append(getString(R.string.text_colummn_hazard) + " " + getString(R.string.text_hazard_value_fall) + ", ");
                                }
                                else
                                {
                                    builder.append(getString(R.string.text_colummn_hazard) + " " +
                                            getString(R.string.text_hazard_value_fire) + ", ");
                                }
                            }
                            else if(dSnapshot.getKey().equals("location"))
                            {
                                String alertField = dSnapshot.getValue(String.class);
                                if(alertField.equals("Unidentified"))
                                {
                                    builder.append(getString(R.string.text_colummn_location) + " " + getString(R.string.text_location_value_unidentified) + ", ");
                                }
                                else
                                {
                                    builder.append(getString(R.string.text_colummn_location) + " " + alertField + ", ");
                                }
                            }
                            else if(dSnapshot.getKey().equals("timeStamp"))
                            {
                                String alertField = dSnapshot.getValue(String.class);
                                builder.append(getString(R.string.text_colummn_timestamp) + " " + alertField);
                            }
                            //σε ξεχωριστό builder το falseAlarm για να το τοποθετήσουμε στο τέλος
                            else if(dSnapshot.getKey().equals("falseAlarm"))
                            {
                                String alertField = dSnapshot.getValue(String.class);
                                if(alertField.equals("false"))
                                {
                                    builderFalseAlarm.append(", " + getString(R.string.text_colummn_falsealarm) + " " + getString(R.string.text_falsealarm_value_false));
                                }
                                else
                                {
                                    builderFalseAlarm.append(", " + getString(R.string.text_colummn_falsealarm) + " " + getString(R.string.text_falsealarm_value_true));
                                }
                            }
                        }
                        //προσθήκη του τελικού String στη λίστα alerts
                        alerts.add(builder.toString() + builderFalseAlarm.toString());
                        adapter1.notifyDataSetChanged(); //να ενημερώνει το περιεχόμενο ο adapter αν υπάρξουν αλλαγές στη List alerts
                    }
                    if(queryType.equals("descending")) //μόνο αν το query αφορά σε ταξινόμηση σε φθίνουσα χρονική σειρά
                    {
                        Collections.reverse(alerts); //με το reverse ταξινομούνται σε φθίνουσα μέσα στη λίστα alerts
                    }
                }
                else //αν δεν έχει αποτελέσματα το συγκεκριμένο query
                {
                    adapter1.notifyDataSetChanged(); //να ενημερώνει το περιεχόμενο ο adapter αν υπάρξουν αλλαγές
                    //λόγω του getApplicationContext(), μέσω string resources θα έδειχνε μήνυμα σε locale συσκευής και όχι εφαρμογής, οπότε βάζουμε string literal στην Toast
                    String chosenLanguage = preferences.getString("language", "en");
                    if(chosenLanguage.equals("English")) //έλεγχος επιλεγμένης γλώσσας, αποθ. στα SharedPreferences
                    {
                        Toast.makeText(getApplicationContext(), "No alerts registered", Toast.LENGTH_SHORT).show();
                    }
                    else if(chosenLanguage.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(), "Δεν έχουν καταχωριστεί συναγερμοί", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "No alarmas registradas", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}