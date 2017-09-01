package com.example.nsrijan.virtualassitance;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private TextView info;
    private TextView txvResult;
    private ListView suggestionList;
    private ArrayAdapter adapter;

    String msg = "";
    String contact;
    HashMap<String, String> cl = new HashMap<>();
    List<String> multiContact = new ArrayList<>();

    final String MSG_ALERT = "Sure You want to text ";
    final String CALL_ALERT = "Sure You want to call ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        txvResult = (TextView) findViewById(R.id.txvResult);
        suggestionList = (ListView) findViewById(R.id.list_suggestion);

        info.setText("To send message say \"Send message to <contact> <your message>\"");

        getContactList();

    }

    //getting list of contacts
    public void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {

                        String phoneType = "";
                        int type = pCur.getInt(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
                        switch (type){
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                phoneType = "Home";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                phoneType = "Mobile";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                phoneType = "Work";
                                break;
                            default:
                                phoneType = "Other";
                                break;
                        }

                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        /*Toast.makeText(getApplicationContext(), "Name: " + name
                                + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();*/
                        cl.put(name+" " + phoneType, phoneNo);
                    }
                    pCur.close();
                }
            }
        }
    }

    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                    String msgBody[] = txvResult.getText().toString().split(" ");

                    if ( msgBody[0].toLowerCase().trim().equals("call") ) {
                        callContact(msgBody);
                        Toast.makeText(getApplicationContext(), "Calling " + cl.get(contact.toLowerCase()),
                                Toast.LENGTH_LONG).show();
                    }



                   // if ( txvResult.getText().toString().contains("send message") ) {
                    if ( msgBody[0].toLowerCase().trim().equals("send") ) {

                        try {
                            sendMessage(msgBody);

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                    Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                        }

                    }


                }

                break;
        }

    }

    private void callContact(String[] msgBody) {
        contact = msgBody[1].toLowerCase();

        Toast.makeText(getApplicationContext(), contact +":" + cl.get(contact),
                Toast.LENGTH_LONG).show();
        multiContact.clear();
        for ( String key : cl.keySet() ) {
            if ( key.toLowerCase().contains(contact.toLowerCase()) ) {
                multiContact.add(key);
            }
            adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, multiContact);
            adapter.notifyDataSetChanged();
            suggestionList.setAdapter(adapter);
            suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a,
                                        View v, int position, long id) {
                    alertConfirmation(suggestionList.getItemAtPosition(position).toString(), CALL_ALERT);
                    Toast.makeText(getApplicationContext(),"selected item is : " + suggestionList.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    public void sendMessage(String[] msgBody) {
        contact = msgBody[3].toLowerCase();

        for(int i=4; i< msgBody.length; i++) {
            msg = msg + msgBody[i] + " ";
        }

        Toast.makeText(getApplicationContext(), contact +":" + cl.get(contact),
                Toast.LENGTH_LONG).show();
        multiContact.clear();
        for ( String key : cl.keySet() ) {
            if ( key.toLowerCase().contains(contact.toLowerCase()) ) {
                multiContact.add(key);
            }
            adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, multiContact);
            adapter.notifyDataSetChanged();
            suggestionList.setAdapter(adapter);
            suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a,
                                        View v, int position, long id) {
                    alertConfirmation(suggestionList.getItemAtPosition(position).toString(), MSG_ALERT);
                    Toast.makeText(getApplicationContext(),"selected item is : " + suggestionList.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void alertConfirmation(final String selectedContact, final String flag) {
        //for prompt
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog message
        alertDialogBuilder
                .setMessage(flag + " " + contact + " " + cl.get(selectedContact) + "?")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if ( flag.equals(MSG_ALERT) ) {
                                    smsManager(selectedContact);
                                }

                                if ( flag.equals(CALL_ALERT)) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + cl.get(selectedContact)));
                                    startActivity(intent);
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Toast.makeText(getApplicationContext(), "Canceled",
                                        Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle("Confirm!!!");
        // show it
        alertDialog.show();
    }

    public void smsManager(String sc) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(cl.get(sc), null, msg, null, null);
        Toast.makeText(getApplicationContext(), "Sending Message to " + cl.get(sc) + "...",
                Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "Message Sent",
                Toast.LENGTH_LONG).show();
    }



}
