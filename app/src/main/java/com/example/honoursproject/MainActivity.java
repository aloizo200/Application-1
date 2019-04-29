package com.example.honoursproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    SpeechRecognizer SpeechListener;
    Intent SpeechListenerIntent;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        //If the app doesn’t have the CALL_PHONE permission, request it//

       requestPermission();


        editText = findViewById(R.id.editText2);
        SpeechListener = SpeechRecognizer.createSpeechRecognizer(this);
        SpeechListenerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SpeechListenerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        /** Set the language the app will listen to
         * Its set to listen to the default language of the user's mobile phone
         */
        SpeechListenerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        SpeechListener.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
               ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null)
                    if(matches.get(0).toLowerCase().contains("call")){
                        //call functioon
                        String number = matches.get(0).toLowerCase().replace("call ","");

                        if(isNumber(number.replace(" ",""))){
                            number = number.replace(" ","");
                            System.out.println(number);
                            call(number);
                        }else {
                            List<String> contacts = AddContactstoArray();

                            boolean foundContact = false;

                            for (String s : contacts) {

                                String name = s.split(":")[0].toLowerCase();
                                if(name.contains(number)){

                                    foundContact=true;
                                    call(s.split(":")[1].replaceAll(" ",""));
                                }
                            }
                            if(!foundContact){
                                //error message
                                editText.setHint("please reenter");
                            }
                        }
                    }else if(matches.get(0).toLowerCase().contains("message")){
                        //message function
                        if(matches.get(0).toLowerCase().contains("and say")) {
                            String[] answers = matches.get(0).toLowerCase().split(" and say ");
                            String receipient = answers[0].replace("message ", "");
                            if (receipient.charAt(receipient.length() - 1) == ' ') {
                                String temp = "";
                                for (int i = 0; i < receipient.length() - 1; i++) {
                                    temp += receipient.charAt(i);
                                }
                                receipient = temp;

                            }
                            String message ="";
                            if(answers.length>1) {
                                message = answers[1];
                            }
                            if(message.equals("")&&receipient.equals("")){
                               //print invalid receipient or message
                                System.out.println("Error !");
                            }else {
                                message(message, receipient);
                            }
                        }else{
                            // print say the correct format of the message
                        }
                    }else{
                        // Say again please
                    }
                    editText.setHint(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){

                    case MotionEvent.ACTION_UP:
                        editText.setHint("Just a sec");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        editText.setHint("");
                        editText.setHint("Listening...");
                        SpeechListener.startListening(SpeechListenerIntent);
                        break;


                }
                return false;
            }
        });
    }

    private boolean isNumber(String number) {
        return number.matches("[0-9]+");
    }

    /** If the Version of the Android phone is Marshmallow and above then I need to check
     * for permission of audio recording on the start of the application
     * If the permission is not granted then the appasks the user for permission, if the user does not grand permision
     * the application can not be used
     */
    private void checkForPermission (){
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }

        }
    }


    public boolean checkPermission() {

        int CallPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE);
        int ContactPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS);
        int SmSPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS);
        return CallPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ContactPermissionResult == PackageManager.PERMISSION_GRANTED&&
                SmSPermissionResult==PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS

                }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:
                Button button = (Button) findViewById(R.id.button);

                if (grantResults.length > 0) {

                    boolean CallPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadContactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean MakeSms = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (CallPermission && ReadContactsPermission&&MakeSms) {

                        Toast.makeText(MainActivity.this,
                                "Permission accepted", Toast.LENGTH_LONG).show();

//If permission is denied...//

                    } else {
                        Toast.makeText(MainActivity.this,
                                "Permission denied", Toast.LENGTH_LONG).show();

//....disable the Call and Contacts buttons//

                        button.setEnabled(false);

                    }
                    break;
                }
        }
    }

    public void call(String phoneNum)
    {
            if(!TextUtils.isEmpty(phoneNum)) {
            String dial = "tel:" + phoneNum;

//Make an Intent object of type intent.ACTION_CALL//

            startActivity(new Intent(Intent.ACTION_CALL,

//Extract the telephone number from the URI//

                    Uri.parse(dial)));
        }

    }
    public ArrayList<String> AddContactstoArray(){
        ArrayList<String> contactsArray = new ArrayList<>();

//Query the phone number table using the URI stored in CONTENT_URI//

       Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

//Get the display name for each contact//

          String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
          System.out.println(name);
//Get the phone number for each contact//

           String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

//Add each display name and phone number to the Array//
if(!contactsArray.contains(name +":"+contactNumber))
            contactsArray.add(name + ":" + contactNumber);
        }

        cursor.close();
        return contactsArray;
    }

public void message(String message, String receipient){

    if(!TextUtils.isEmpty(message) && !TextUtils.isEmpty(receipient)) {
        if(checkPermission()) {

//Get the default SmsManager//

            SmsManager smsManager = SmsManager.getDefault();

//Send the SMS//

            if (!isNumber(receipient)) {
                String phoneNum = "";
                List<String> contacts = AddContactstoArray();

                boolean foundContact = false;

                for (String s : contacts) {

                    String name = s.split(":")[0].toLowerCase();
                    if(name.contains(receipient)){

                       foundContact=true;
                       System.out.println("the number: "+s.split(":")[1].replaceAll(" ",""));
                       phoneNum=s.split(":")[1].replaceAll(" ","");
                       smsManager.sendTextMessage(phoneNum, null, message, null, null);

                    }
                }

                if(!foundContact){
                    //error message
                    editText.setHint("please reenter");
                }
            }else {
                receipient = receipient.replace(" ","");
                smsManager.sendTextMessage(receipient, null, message, null, null);
            }

        }else {
            Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}


}
