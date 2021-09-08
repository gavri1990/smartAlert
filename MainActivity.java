package com.example.smartalert;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener
{
    FirebaseDatabase database = FirebaseDatabase.getInstance(); //αντικείμενο για σύνδεση με Firebase
    DatabaseReference myRefUsers = database.getReference("Users/"); //ρίζα εγγραφών σε Firebase
    DatabaseReference myRefUser; //παιδί του myRefAlert, θα έχει το userId
    private StorageReference myStorageRef; //αντικείμενο για σύνδεση με Firebase Storage
    private LocationManager locationManager; //αντικείμενα τάξεων LocationManager, SensorManager και Sensor
    private SensorManager sensorManager;
    private Sensor sensor;
    SmartAlertDbHelper smartAlertDbHelper; // instance της class SmartAlertDbHelper (που κάνει extend την SQLiteOpenHelper)
    SharedPreferences preferences; //αντικείμενο SharedPreferences
    CountDownTimer timer;
    String currentDateTime;
    private final int PERMISSION_ALL = 1;
    private static final int CAMERA_REQUEST = 1888;
    static MediaPlayer mediaPlayer;
    static MediaPlayer mediaPlayer2;
    TextView textView;
    Button button, button2;
    ImageView imageView;
    Toolbar toolbar;
    List<String> contactTel; //για αποθήκευση τηλεφώνων επαφών
    static boolean counterStart = false;
    int count = 30;
    double x;
    double y;
    static String alertId; //θα κρατά το key του τελευταίου alert που καταχωρίστηκε στη Firebase


    @Override
    public boolean onCreateOptionsMenu(Menu menu) //override μεθόδου για εισαγωγή custom menu
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.smart_alert_menu, menu); //το δικό μας menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) //σε επιλογή κάποιου item του μενού
    {
        switch (item.getItemId()) //διάφορα σενάρια εικονιδίου γλώσσας και κειμένου από κάτω
        {
            case R.id.item1:
                Intent intent = new Intent(this, Language.class); //μετάβαση σε activity για αλλαγή γλώσσας
                startActivity(intent);
                return true;
            case R.id.item2:
                Intent intent2 = new Intent(this, MyContacts.class); //μετάβαση σε activity για διαχείριση επαφών
                startActivity(intent2);
                return true;
            case R.id.item3:
                Intent intent3 = new Intent(this, Statistics.class); //μετάβαση σε activity για προβολή στατιστικών
                startActivity(intent3);
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() //σε κλείσιμο Activity
    {
        sensorManager.unregisterListener(this); //κάνουμε unregister τον Listener του accelerometer
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myStorageRef = FirebaseStorage.getInstance().getReference(); //ρίζα εγγραφών σε Firebase Storage

        if (preferences.contains("language")) //αν έχει επιλεχθεί γλώσσα από χρήστη
        {
            String chosenLanguage = preferences.getString("language", "en");
            if (chosenLanguage.equals("English")) //έλεγχος επιλεγμένης γλώσσας, αποθ. στα SharedPreferences
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
            else if (chosenLanguage.equals("Ελληνικά"))
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
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //ορισμός τύπου sensor (Accelerometer)
        //δημιουργία Listener για αλλαγές του Sensor
        sensorManager.registerListener((SensorEventListener) this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE); //υπηρεσία εντοπισμού
        smartAlertDbHelper = new SmartAlertDbHelper(getApplicationContext(), null, null, 1); //κλήση constructor της class και κατασκευή
        textView = findViewById(R.id.textView); //σύνδεση με xml μέσω ids
        textView.setVisibility(View.INVISIBLE);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button2.setVisibility(View.INVISIBLE);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.fire);
        mediaPlayer = MediaPlayer.create(this, R.raw.timer);
        mediaPlayer2 = MediaPlayer.create(this, R.raw.timerfinalbeep);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //λειτουργία του custom toolbar ως action bar για το project

        //άν ο χρήστης έχει δώσει ήδη άδεια για χρήση location, ενεργοποιούμε location updates
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0, this);
        }

        //string array με τα permissions που χρειαζόμαστε
        String[] PERMISSIONS = {
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA
        };

        //array με permissions δίνεται ως παράμετρος σε μέθοδο
        if (!hasPermissions(this, PERMISSIONS))
        {
            //αν δεν έχουν δοθεί όλα τα permissions, ξαναζητούνται
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        List<Contact> cList = readContactsFromDb(); //κλήση της readContactsFromDb(), ό,τι λίστα γυρίσει θα εκχωρηθεί στην cList
        contactTel = new ArrayList<>(); //2η λίστα, θα περιέχει το τηλέφωνο κάθε επαφής
        for (int i = 0; i < cList.size(); i++)
        {
            contactTel.add(cList.get(i).getContact_telephone()); //το τηλ του αντίστοιχου Contact object μπαίνει σε λίστα
        }

        if(!preferences.contains("userId")) //αν δεν έχει id ο user στη Firebase
        {
            SharedPreferences.Editor editor = preferences.edit(); //έτοιμοι για edit στα preferences
            String userId = myRefUsers.push().getKey(); //αποθηκεύουμε το key του user στη μεταβλητή userId
            editor.putString("userId", userId); //αποθήκευση userId στα SharedPreferences
            editor.commit(); //αποθ. αλλαγών σε SharedPreferences

        }
        //μονοπάτι όπου θα αρχίσουμε να εγγράφουμε τους συναγερμούς του συγκεκριμένου χρήστη
        myRefUser = database.getReference("Users/"
                + preferences.getString("userId", "noId") + "/Alerts/");
    }

    public static boolean hasPermissions(Context context, String[] permissions)
    {
        if (context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false; //αν δεν έχει δώσει όλα τα απαραίτητα permission ο χρήστης, γυρνά false
                }
            }
        }
        return true; //αλλιώς γυρνά true
    }

    //όταν ο χρήστης δώσει το permission(ή όχι) για τα services
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case PERMISSION_ALL:
            {
                //αν έχει δώσει οκ για ACCESS_FINE_LOCATION, ενεργοποιούμε location updates
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0, this);
                }
                //αν πατήσει Deny για το permission της Camera
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, R.string.toast_camera_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //αν έχουμε αποτέλεσμα ως προς τη λήψη φωτογραφίας από τον χρήστη και όλα πήγαν καλά
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            //στις επόμενες γραμμές παίρνουμε τη φωτό σε μορφή Bitmap και τη μετατρέπουμε σε array από bytes
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] photoBytes = baos.toByteArray();

            //κάνουμε push στη Realtime Database ώστε να λάβουμε ένα τυχαίο key ως όνομα της φωτό που θα ανεβάσουμε στο Storage
            String photoId = myRefUsers.push().getKey();
            //δημιουργία φακέλου photos, υποφακέλου με id χρήστη συσκευής(υπάρχει στα Preferences) και όνομα της φωτό
            StorageReference photosRef = myStorageRef.child("photos/" + preferences.getString("userId", "noId") + "/" + photoId);

            //αποθήκευση της φωτό σε μορφή bytes στο παραπάνω ορισμένο μονοπάτι του Storage
            UploadTask uploadTask = photosRef.putBytes(photoBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    //λόγω του getApplicationContext(), μέσω string resources θα έδειχνε μήνυμα σε locale συσκευής και όχι εφαρμογής, οπότε βάζουμε string literal στο Toast
                    String chosenLanguage = preferences.getString("language", "en");
                    if(chosenLanguage.equals("English")) //έλεγχος επιλεγμένης γλώσσας, αποθ. στα SharedPreferences
                    {
                        Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                    else if(chosenLanguage.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(), "Το αρχείο μεταφορτώθηκε επιτυχώς", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Archivo colgado con éxito", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    //λόγω του getApplicationContext(), μέσω string resources θα έδειχνε μήνυμα σε locale συσκευής και όχι εφαρμογής, οπότε βάζουμε string literal στο Toast
                    String chosenLanguage = preferences.getString("language", "en");
                    if(chosenLanguage.equals("English")) //έλεγχος επιλεγμένης γλώσσας, αποθ. στα SharedPreferences
                    {
                        Toast.makeText(getApplicationContext(), "File not uploaded", Toast.LENGTH_SHORT).show();
                    }
                    else if(chosenLanguage.equals("Ελληνικά"))
                    {
                        Toast.makeText(getApplicationContext(), "Το αρχείο δεν μεταφορτώθηκε", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Archivo no colgado", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean sendSMSMessage(String message) //μέθοδος με το κείμενο του μηνύματος ως παράμετρο
    {
        try
        {
            if(message.length() > 100) //αν μεγάλο το μήνυμα
            {
                for(int i = 0; i < contactTel.size(); i++) //αποστολή SMS σε όλες τις επαφές
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> parts = smsManager.divideMessage(message); //χωρισμός σε τμήματα
                    //αποστολή SMS σε τμήματα
                    smsManager.sendMultipartTextMessage(contactTel.get(i), null, parts, null, null);
                }
            }
            else
            {
                for(int i = 0; i < contactTel.size(); i++) //αποστολή SMS σε όλες τις επαφές
                {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contactTel.get(i), null, message, null, null); //αποστολή SMS
                }
            }
            return  true;
        }
        catch(Exception e) //πχ αν δεν έχει δώσει permission για αποστολή SMS
        {
            return false;
        }

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


    @Override
    public void onSensorChanged(SensorEvent event) //αλλαγή τιμών Sensor
    {
        double accX = event.values[0];
        double accY = event.values[1];
        double accZ = event.values[2];

        double accelerationReader = Math.sqrt(Math.pow(accX, 2)
                + Math.pow(accY, 2)
                + Math.pow(accZ, 2));

        if (accelerationReader > 0.3 && accelerationReader < 0.5) //εάν έχουμε πτώση
        {
            if(counterStart == false) //θα είναι false μόνο αν δεν έχει ξεκινήσει ήδη αντίστροφη μέτρηση
            {
                textView.setVisibility(View.VISIBLE); //εμφάνιση controls για πτώση, εξαφάνιση κουμπιού για φωτιά
                button2.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);
                imageView.setImageResource(R.drawable.fall);
                timer = new CountDownTimer(30000, 1000) //νέο αντικείμενο CountDownTimer με παραμέτρους
                {
                    @Override
                    public void onTick(long millisUntilFinished) //σε κάθε tick
                    {
                        textView.setText(String.valueOf(count)); //προβολή αριθμού αντίστρ. μέτρησης
                        mediaPlayer.start(); //αναπαραγωγή ήχου beep
                        count -= 1; //μείωση κατά 1
                    }

                    @Override
                    public void onFinish() //σε τέλος αντίστροφης μέτρησης
                    {
                        count = 30; //count τίθεται πάλι 30
                        mediaPlayer2.start();
                        counterStart = false;
                        String location; //θα πάρει τις συντεταγμένες σε String για καταχώριση σε Firebase
                        //τρέχουσα ημερομηνία και ώρα εκχωρείται στην μεταβλητή
                        currentDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                        textView.setTextColor(Color.parseColor("#E69900"));
                        if(x == 0 || y == 0) //αν δεν έχει στίγμα ο χρήστης
                        {
                            location = "Unidentified"; //έτσι θα αποθηκευτεί στη Firebase
                            if(sendSMSMessage("SOS")) //κλήση μεθόδου για αποστολή sms σε επαφές χρήστη, αν γύρισε true
                            {
                                textView.setText(R.string.text_sos_sent); //χρήση string resources
                            }
                            else
                            {
                                textView.setText(R.string.text_sms_not_sent); //χρήση string resources
                            }
                        }
                        else //αν έχει στίγμα ο χρήστης, στέλνουμε επιπλέον γεωγρ. μήκος και πλάτος
                        {
                            location = x + " , " + y; //αποθήκευση συντεταγμένων σε String για καταχώριση σε Firebase
                            if(sendSMSMessage(getString(R.string.sms_sos_part1) + " " +
                                    x + ", " +
                                    getString(R.string.sms_sos_part2) + " " +
                                    y)) //κλήση μεθόδου για αποστολή sms σε επαφές χρήστη, αν γύρισε true
                            {
                                textView.setText(R.string.text_sos_sent); //χρήση string resources
                            }
                            else
                            {
                                textView.setText(R.string.text_sms_not_sent); //χρήση string resources
                            }
                        }
                        alertId = myRefUser.push().getKey(); //αποθηκεύουμε το key στο alertId
                        //καταχώριση σε Firebase με χρήση του παραπάνω key, ώστε να μπορούμε update σε περίπτωση false alarm
                        myRefUser.child(alertId).setValue(new Alert("Fall", location, currentDateTime.toString(), "false"));
                    }
                };
                timer.start(); //εκκίνηση του timer
                counterStart = true; //boolean ιδιότητα τίθεται true, ο timer ξεκίνησε
            }
        }
    }

    public void abort (View view) //πάτημα κουμπιού Abort
    {
        if (counterStart == false) //δεν εκτελείται αντίστροφη μέτρηση
        {
            myRefUser.child(alertId + "/falseAlarm").setValue("true"); //update σε Firebase του πεδίου falseAlarm σε true
            textView.setVisibility(View.INVISIBLE); //απόκρυψη κουμπιών και textView αντίστροφης μέτρησης
            button2.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setTextColor(Color.WHITE);
            imageView.setImageResource(R.drawable.fire);
            if(sendSMSMessage(getString(R.string.sms_false_alarm))) //κλήση μεθόδου για αποστολή sms σε επαφές χρήστη, αν γύρισε true
            {
                Toast.makeText(this, R.string.toast_false_alarm, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, R.string.toast_sms_not_sent, Toast.LENGTH_SHORT).show();
            }
        }
        else //αν ο timer 'τρέχει' ήδη
        {
            timer.cancel(); //ακύρωση timer
            timer = null;
            counterStart = false;
            count = 30; //count τίθεται πάλι 30
            MainActivity.mediaPlayer.pause(); //διακοπή ηχητικών εφέ
            MainActivity.mediaPlayer.seekTo(0);
            MainActivity.mediaPlayer2.pause();
            MainActivity.mediaPlayer2.seekTo(0);
            textView.setVisibility(View.INVISIBLE); //απόκρυψη κουμπιών πτώσης, εμφάνιση κουμπιών πυρκαγιάς
            button2.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setTextColor(Color.WHITE);
            imageView.setImageResource(R.drawable.fire); //αλλαγή εικόνας
            Toast.makeText(this, R.string.toast_alert_aborted, Toast.LENGTH_SHORT).show();
        }
    }

    public void fire (View view) //πάτημα κουμπιού Fire
    {
        if(x == 0 || y == 0) //αν δεν έχει στίγμα ο χρήστης
        {
            //ενημέρωση να ξαναπροσπαθήσει, δεν στέλνεται SMS ούτε εγγράφεται το γεγονός στην Firebase
            Toast.makeText(this, R.string.toast_location_not_available, Toast.LENGTH_SHORT).show();
        }
        else
        {
            //τρέχουσα ημερομηνία και ώρα εκχωρείται στην μεταβλητή
            currentDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            String location = x + " , " + y; //αποθήκευση συντεταγμένων σε String για καταχώριση σε Firebase
            //καταχώριση σε Firebase με νέο key μέσω της push()
            myRefUser.push().setValue( new Alert("Fire", location, currentDateTime.toString()));
            if(sendSMSMessage(getString(R.string.sms_fire_part1) + " " + x + " "
                    + getString(R.string.sms_fire_part2) + " " + y + " "
                    + getString(R.string.sms_fire_part3))) //κλήση μεθόδου για αποστολή sms σε επαφές χρήστη, αν γύρισε true
            {
                Toast.makeText(this, R.string.toast_fire, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, R.string.toast_sms_not_sent, Toast.LENGTH_SHORT).show();
            }
            //αν ο χρήστης έχει δώσει permission για χρήση της κάμερας
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            {   //χρήση της προεγκατεστημένης εφαρμογής κάμερας για λήψη φωτογραφίας
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    @Override
    public void onLocationChanged(Location location) //σε update location, λήψη νέων συντεταγμένων
    {
        x = location.getLongitude(); //εκχώρηση στις μεταβλητές
        y = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
}
