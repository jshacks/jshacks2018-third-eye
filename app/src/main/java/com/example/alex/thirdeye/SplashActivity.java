package com.example.alex.thirdeye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapzen.speakerbox.Speakerbox;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Speakerbox speakerbox = new Speakerbox(getApplication());
        //speakerbox.play("hi! I am here to help you");
        speakerbox.playAndOnDone("hi! I am here to help you ", new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
