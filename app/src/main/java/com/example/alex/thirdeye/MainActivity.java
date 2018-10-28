package com.example.alex.thirdeye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mapzen.speakerbox.Speakerbox;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnDSListener {
    private static final int CAMERA_PIC_REQUEST = 1111;
    String filename;
    String upLoadServerUri = null;
    Button send;
    Uri imageUri;
    Speakerbox speakerbox;
    ImageView mImage;
    ImageView openCamera;
    String message ="hi! I'm here to help you";
    DroidSpeech droidSpeech;
    ImageView microphone;
    String hello = "hi! I'm here to help you";
    private boolean isSpeakButtonLongPressed = false;


    @Override
    protected void onStart() {
        super.onStart();
        speakerbox = new Speakerbox(getApplication());
        speakerbox.play(hello);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mImage = findViewById(R.id.camera_image);
        openCamera = findViewById(R.id.button3);
        Button play = findViewById(R.id.play);
        speakerbox = new Speakerbox(getApplication());
        upLoadServerUri = "http://be1d525c.ngrok.io/api/test";
        microphone = findViewById(R.id.microphone);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //speakerbox.play("Hi, how are you? in this image you can see a girl near a river");
                    speakerbox.play(message);
            }

        });
        droidSpeech = new DroidSpeech(getApplicationContext(), null);
        droidSpeech.setOnDroidSpeechListener(MainActivity.this);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        microphone.setOnLongClickListener(speakHoldListener);
        microphone.setOnTouchListener(speakTouchListener);

    }

    public void openCamera(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        filename = Environment.getExternalStorageDirectory().getPath() +"/test/testfile.jpg";
        imageUri = Uri.fromFile(new File(filename));
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                imageUri);
        cameraIntent.putExtra("android.intent.extra.quickCapture",true);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private View.OnLongClickListener speakHoldListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View pView) {
            // Do something when your hold starts here.
            isSpeakButtonLongPressed = true;
            Log.d("intra aici", "in on long");
            droidSpeech.startDroidSpeechRecognition();
            return true;
        }
    };

    private View.OnTouchListener speakTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.onTouchEvent(motionEvent);
            // We're only interested in when the button is released.
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // We're only interested in anything if our speak button is currently pressed.
//                if (isSpeakButtonLongPressed) {
//                    Log.d("intra aici", "a iesit din on pressed");
//                    // Do something when the button is released.
//
//                    isSpeakButtonLongPressed = false;
//                }
            }
            return false;
        }

    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != CAMERA_PIC_REQUEST || filename == null)
            return;
        if(resultCode == RESULT_OK ) {
           // mImage.setImageURI(imageUri);
            new Thread(new Runnable() {
                @Override
                public boolean equals(Object obj) {
                    return super.equals(obj);
                }

                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // messageText.setText("uploading started.....");
                        }

                    });
                    try {
                        uploadFile("/sdcard/test/testfile.jpg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private final OkHttpClient client = new OkHttpClient();

    public void uploadFile(String sourceFileUri) throws JSONException {

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = 4;
        options.inPurgeable = true;
        Bitmap bm = BitmapFactory.decodeFile(sourceFileUri,options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        // bitmap object
        byte[] byteImage_photo = baos.toByteArray();
        //generate base64 string of image
        String encodedImage = Base64.encodeToString(byteImage_photo,Base64.DEFAULT);
        String data = sendData(encodedImage);
        Log.d("data", data);
        JSONArray arr = new JSONArray(data);
        JSONObject jObj = arr.getJSONObject(0);
        message = jObj.getString("phrase");
        Log.d("TAG", message);
        Speakerbox speakerbox1 = new Speakerbox(getApplication());
        speakerbox1.play(message);
    }
    public String sendData(String image){
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("image", image)
                    .build();

            Request request = new Request.Builder()
                    .url("http://a363ffcb.ngrok.io/api/test")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {

    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {
        Log.d("live", liveSpeechResult);
        if(liveSpeechResult.equals("repeat")){
            droidSpeech.closeDroidSpeechOperations();
            speakerbox = new Speakerbox(getApplication());
            speakerbox.play(message);

        } else if(liveSpeechResult.equals("picture")){
            droidSpeech.closeDroidSpeechOperations();
            openCamera();

        } else {
            droidSpeech.closeDroidSpeechOperations();
        }


    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult)
    {
        Log.d("tag", finalSpeechResult);
    }

    @Override
    public void onDroidSpeechClosedByUser() {

    }

    @Override
    public void onDroidSpeechError(String errorMsg) {
        Log.e("tag", errorMsg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close speech recognition
        droidSpeech.closeDroidSpeechOperations();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        hello = "";
    }
}

