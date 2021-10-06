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
import android.provider.ContactsContract.Contacts;
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
    EditText mEdit;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    final static int RQS_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstNames = (ListView) findViewById(R.id.lstnames);
        mEdit = (EditText) findViewById(R.id.edit_text);
        contactsButton = (Button) findViewById(R.id.contact_button);
        contactsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEdit.getText().toString();
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
        List<String> contact = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            List<String> contacts = getContactNames();
            for (int i = 0; i <= contacts.size(); i++) {
                String name = contacts.get(i);
                if (n.equals(name)) {
                    contact.add(name);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contact);
                    lstNames.setAdapter(adapter);
                    break;
                } else {
                    contact.add("Sorry! " + n + " was not found in your contacts.");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contact);
                    lstNames.setAdapter(adapter);
                    break;
                }
            }
        }
        return contact;
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {

        }
    }

    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
                contacts.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return contacts;
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
