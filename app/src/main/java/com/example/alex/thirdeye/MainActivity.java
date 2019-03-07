package com.example.alex.thirdeye;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapzen.speakerbox.Speakerbox;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDSListener {

    public enum Mode {
        CAPTIONING,
        TEXT,
        BARCODE,
        MONEY
    }
    private Speakerbox speakerbox;
    private ImageView mImage;
    private ImageView openCamera;
    private String message ="hi! I'm here to help you";
    private DroidSpeech droidSpeech;
    private ImageView microphone;
    private String hello;
    private boolean isSpeakButtonLongPressed = false;
    private Mode mode;
    private RelativeLayout relativeLayout;
    private TextView textViewMode;
    int index = 0;
    @Override
    protected void onStart() {
        super.onStart();
        speakerbox = new Speakerbox(getApplication());
        hello = getResources().getString(R.string.hello_text);
        speakerbox.play(hello);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = Mode.CAPTIONING;
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mImage = findViewById(R.id.camera_image);
        openCamera = findViewById(R.id.button3);
        relativeLayout = findViewById(R.id.container);
        Button play = findViewById(R.id.play);
        speakerbox = new Speakerbox(getApplication());
        microphone = findViewById(R.id.microphone);
        textViewMode = findViewById(R.id.mode);
        textViewMode.setText("captioning image");
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakerbox.play(getResources().getString(R.string.hello_text));
            }

        });

        droidSpeech = new DroidSpeech(getApplicationContext(), null);
        droidSpeech.setOnDroidSpeechListener(MainActivity.this);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               goToCameraFragment();
            }
        });

        openCamera.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//
                return false;
            }
        });
        microphone.setOnLongClickListener(speakHoldListener);
        microphone.setOnTouchListener(speakTouchListener);

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {

            int downX, upX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = (int) event.getX();
                    Log.i("event.getX()", " downX " + downX);
                    return true;
                }

                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    upX = (int) event.getX();
                    Log.i("event.getX()", " upX " + upX);
                    if (upX - downX > 100) {
                       // Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_LONG).show();
                        // swipe right
                        if (mode == Mode.CAPTIONING) {
                            mode = Mode.BARCODE;
                            textViewMode.setText("product recognition");
                            index = 2;
                            speakerbox.play(getResources().getString(R.string.bar_code_mode));
                            return true;
                        }
                        if (mode == Mode.BARCODE) {
                            mode = Mode.TEXT;
                            textViewMode.setText("read text");
                            index = 0;
                            speakerbox.play(getResources().getString(R.string.read_text_mode));
                            return true;
                        }
                        if (mode == Mode.TEXT) {
                            mode = Mode.CAPTIONING;
                            textViewMode.setText("captioning image");
                            speakerbox.play(getResources().getString(R.string.captioning_mode));
                            index = 0;
                            return true;
                        }
                    }

                    else if (downX - upX > -100) {
                       // Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_LONG).show();
                        // swipe left
                        if (mode == Mode.CAPTIONING) {
                            mode = Mode.TEXT;
                            textViewMode.setText("read text");
                            index++;
                            speakerbox.play(getResources().getString(R.string.read_text_mode));
                            return true;
                        }
                        if (mode == Mode.TEXT) {
                            mode = Mode.BARCODE;
                            index++;
                            textViewMode.setText("product recognition");
                            speakerbox.play(getResources().getString(R.string.bar_code_mode));
                            return true;
                        }
                        if (mode == Mode.BARCODE) {
                            mode = Mode.CAPTIONING;
                            textViewMode.setText("captioning image");
                            speakerbox.play(getResources().getString(R.string.captioning_mode));
                            index = 0;
                            return true;
                        }
                    }
                    return true;

                }
                return false;
            }
        });

    }

    private void goToCameraFragment(){
        CameraFragment cameraFragment = new CameraFragment();
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, cameraFragment).addToBackStack(null).commit();
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
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            }
            return false;
        }

    };

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
            speakerbox.play(getResources().getString(R.string.hello_text));

        } else if(liveSpeechResult.equals("picture")){
            droidSpeech.closeDroidSpeechOperations();
            goToCameraFragment();
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

