package com.example.nsrijan.virtualassitance;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private TextView info;
    private TextView txvResult;

    String msg = "";
    String contact;
    HashMap<String, String> cl = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        txvResult = (TextView) findViewById(R.id.txvResult);

        info.setText("To send message say \"Send message to <contact> <your message>\"");



        //getting list of contacts
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
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        /*Toast.makeText(getApplicationContext(), "Name: " + name
                                + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();*/
                        cl.put(name.toLowerCase(), phoneNo);
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

                    Toast.makeText(getApplicationContext(), "First word: " + msgBody[0],
                            Toast.LENGTH_LONG).show();

                    if ( msgBody[0].toLowerCase() == "call") {
                        Toast.makeText(getApplicationContext(), "Calling " + cl.get(contact),
                                Toast.LENGTH_LONG).show();
                        Log.i("msgBody[0]", msgBody[0]);
                    }



                    if ( txvResult.getText().toString().contains("send message") ) {
                   // if ( msgBody[0].toLowerCase() == "send" ) {
                        /*String msgBody[] = txvResult.getText().toString().split(" ");
                        String msg = "";

                        Intent sendMessage = new Intent(Intent.ACTION_VIEW);
                        sendMessage.setData(Uri.parse("sms:"));

                        for(int i=2; i< msgBody.length; i++) {
                            msg = msg + msgBody[i] + " ";
                        }

                        sendMessage.putExtra("sms_body", msg);
                        Toast.makeText(this, "Sending Message", Toast.LENGTH_LONG).show();
                        startActivity(sendMessage);*/

                        try {

                            contact = msgBody[3].toLowerCase();

                            Toast.makeText(getApplicationContext(), contact +":" + cl.get(contact),
                                    Toast.LENGTH_LONG).show();


                            for(int i=2; i< msgBody.length; i++) {
                                msg = msg + msgBody[i] + " ";
                            }

                            //for prompt
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    this);


                            // set dialog message
                            alertDialogBuilder
                                    .setMessage("Sure you want to call " + contact + " " + cl.get(contact) + "?")
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    // get user input and set it to result
                                                    // edit text
                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    smsManager.sendTextMessage(cl.get(contact), null, msg, null, null);
                                                        Toast.makeText(getApplicationContext(), "Sending Message...",
                                                                Toast.LENGTH_LONG).show();
                                                        Toast.makeText(getApplicationContext(), "Message Sent",
                                                                Toast.LENGTH_LONG).show();
                                                        /*Toast.makeText(getApplicationContext(), "Message permission denied",
                                                                Toast.LENGTH_LONG).show();*/
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



}
