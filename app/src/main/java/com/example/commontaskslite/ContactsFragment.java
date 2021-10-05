package com.example.commontaskslite;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.content.CursorLoader;

public class ContactsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    private final static int[] TO_IDS = {
            android.R.id.text1
    };

    ListView contactsList;
    long contactId;
    String contactKey;
    Uri contactUri;
    private SimpleCursorAdapter cursorAdapter;

    public ContactsFragment() {
    }

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.contact_list_fragment,
                container, false);
    }

    @SuppressLint("ResourceType")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        contactsList.setOnItemClickListener(this);
        // Gets the ListView from the View list of the parent activity
        contactsList =
                (ListView) getActivity().findViewById(R.layout.contact_list_view);
        // Gets a CursorAdapter
        cursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.contact_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        // Sets the adapter for the ListView
        contactsList.setAdapter(cursorAdapter);
    }

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Data._ID,
                    // The primary display name
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                    // The contact's _ID, to construct a content URI
                    ContactsContract.Data.CONTACT_ID,
                    // The contact's LOOKUP_KEY, to construct a content URI
                    ContactsContract.Data.LOOKUP_KEY // A permanent link to the contact

            };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the CONTACT_KEY column
    private static final int CONTACT_KEY_INDEX = 1;

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {
        // Get the Cursor
        Cursor cursor = (Cursor) parent.getAdapter();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        contactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        contactKey = cursor.getString(CONTACT_KEY_INDEX);
        // Create the contact's content Uri
        contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);
        /*
         * You can use contactUri as the content URI for retrieving
         * the details for a contact.
         */
    }

    // Called just before the Fragment displays its UI
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Always call the super method first
        super.onCreate(savedInstanceState);
        // Initializes the loader
        getLoaderManager().initLoader(0, null, this);

    }

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
    // Defines a variable for the search string
    private String searchString;
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {""};

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        selectionArgs[0] = "%" + searchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        cursorAdapter.swapCursor(null);

    }
}

