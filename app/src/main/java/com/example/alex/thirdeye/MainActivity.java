package com.example.alex.thirdeye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mapzen.speakerbox.Speakerbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PIC_REQUEST = 1111;
    String filename;
    String upLoadServerUri = null;
    Button send;
    Uri imageUri;
    Speakerbox speakerbox;
    ImageView mImage;
    ImageView openCamera;
    String message ="you ";

    @Override
    protected void onStart() {
        super.onStart();
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
        send = findViewById(R.id.button4);
        upLoadServerUri = "http://a83f22d1.ngrok.io/api/test";
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //speakerbox.play("Hi, how are you? in this image you can see a girl near a river");
                    speakerbox.play(message);
            }

        });

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                filename = Environment.getExternalStorageDirectory().getPath() +"/test/testfile.jpg";
                imageUri = Uri.fromFile(new File(filename));
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        imageUri);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
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
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != CAMERA_PIC_REQUEST || filename == null)
            return;
        if(resultCode == RESULT_OK ) {
            mImage.setImageURI(imageUri);
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
        Bitmap bm = BitmapFactory.decodeFile(sourceFileUri,options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        // bitmap object
        byte[] byteImage_photo = baos.toByteArray();
        //generate base64 string of image
        String encodedImage = Base64.encodeToString(byteImage_photo,Base64.DEFAULT);
        String data = sendData(encodedImage);
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
                    .url("http://a1879be2.ngrok.io/api/test")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}

