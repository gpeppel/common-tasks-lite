package com.example.commontaskslite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CALL_PHONE;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.View.OnClickListener;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lstNames;
    Button redialButton;
    TextView phoneNumber;
    Button buttonStartSetDialog;
    Button contactsButton;
    Button addressButton;
    EditText edit_text;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;
    TextView nText;
    TextView nPhone;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 99;

    final static int RQS_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressButton = (Button) findViewById(R.id.addressButton);
        addressButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                float latitude = (float) 40.7423;
                float longitude = (float) 74.1793;
                String locationName = "NJIT";
                String geoUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + locationName + ")";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                if (mapIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    getApplicationContext().startActivity(mapIntent);
                }
            }
        });

        lstNames = (ListView) findViewById(R.id.lstnames);
        edit_text = (EditText) findViewById(R.id.edit_text);
        nText = (TextView) findViewById(R.id.nText);
        nPhone = (TextView) findViewById(R.id.nPhone);
        contactsButton = (Button) findViewById(R.id.contact_button);
        contactsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_text.getText().toString();
                getContact(name);
            }
        });

        textAlarmPrompt = (TextView) findViewById(R.id.alarmPrompt);
        buttonStartSetDialog = (Button) findViewById(R.id.alarmButton);
        buttonStartSetDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textAlarmPrompt.setText("");
                openTimePickerDialog(false);
            }
        });

        phoneNumber = (TextView) findViewById(R.id.redialPrompt);
        redialButton = (Button) findViewById(R.id.redial_button);
        redialButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastCalledNumber = CallLog.Calls.getLastOutgoingCall(getApplicationContext());
                phoneNumber.setText(lastCalledNumber);
            }
        });

        phoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastCalledNumber = phoneNumber.getText().toString();
                Intent redialCall = new Intent(Intent.ACTION_CALL);
                redialCall.setData(Uri.parse("tel:" + lastCalledNumber));
                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(redialCall);
                } else {
                    requestPermissions(new String[]{CALL_PHONE}, 1);
                }
            }
        });
    }

    private List<String> getContact(String n) {
        List<String> c = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            List<List> contacts = getContacts();
            int size = contacts.size();
            for (int q = 0; q < contacts.size(); q++) {
                List<String> contact = new ArrayList<>();
                contact = contacts.get(q);
                if (contact.contains(n)) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contact);
                    lstNames.setAdapter(adapter);
                    return contact;
                }
            }
            c.add("Sorry! " + n + " was not found in your contacts.");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, c);
            lstNames.setAdapter(adapter);
        }
        return c;
    }

    private List<List> getContacts() {
        ContentResolver contentResolver = getContentResolver();
        String contactId = null;
        String displayName = null;
        List<List> contacts = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    List<String> contact = new ArrayList<>();
                    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contact.add(contactId);
                    contact.add(displayName);

                    Cursor pCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    if (pCursor.moveToNext()) {
                        String phoneNumber = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact.add(phoneNumber);
                    }
                    pCursor.close();
                    contacts.add(contact);
                }
            }
        }
        cursor.close();
        return contacts;
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            } else {
                Toast.makeText(this, "DENIED!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openTimePickerDialog(boolean is24r) {
        Calendar calendar = Calendar.getInstance();
        timePickerDialog = new TimePickerDialog(MainActivity.this,
                onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), is24r);
        timePickerDialog.setTitle("Set an Alarm Time");
        timePickerDialog.show();
    }

    OnTimeSetListener onTimeSetListener = new OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();
            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);

            if (calSet.compareTo(calNow) <= 0) {
                calSet.add(Calendar.DATE, 1);
            }
            setAlarm(calSet);
        }
    };

    private void setAlarm(Calendar targetCal) {
        textAlarmPrompt.setText("ALARM SET FOR ---> " + targetCal.getTime());

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }
}
