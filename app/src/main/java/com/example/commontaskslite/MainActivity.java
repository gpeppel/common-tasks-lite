package com.example.commontaskslite;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.os.Build;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View.OnClickListener;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    Button buttonStartSetDialog;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;

    final static int RQS_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textAlarmPrompt = (TextView) findViewById(R.id.alarmPrompt);

        builder = new AlertDialog.Builder(this);
        buttonStartSetDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textAlarmPrompt.setText("");
                openTimePickerDialog(false);
            }
        });
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    public class ContactsFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor>,
            AdapterView.OnItemClickListener {
        /*
         * Defines an array that contains column names to move from
         * the Cursor to the ListView.
         */
        @SuppressLint("InlinedApi")
        private final static String[] FROM_COLUMNS = {
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        };
        /*
         * Defines an array that contains resource ids for the layout views
         * that get the Cursor column contents. The id is pre-defined in
         * the Android framework, so it is prefaced with "android.R.id"
         */
        private final static int[] TO_IDS = {
                android.R.id.text1
        };
        // Define global mutable variables
        // Define a ListView object
        ListView contactsList;
        // Define variables for the contact the user selects
        // The contact's _ID value
        long contactId;
        // The contact's LOOKUP_KEY
        String contactKey;
        // A content URI for the selected contact
        Uri contactUri;
        // An adapter that binds the result Cursor to the ListView
        private SimpleCursorAdapter cursorAdapter;
    }

//    private void setContact(int REQUEST_SELECT_PHONE_NUMBER) {
//        editTextPhone
//        Intent intent = Intent(Intent.ACTION_PICK);
//        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
//        startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
//    }

}
