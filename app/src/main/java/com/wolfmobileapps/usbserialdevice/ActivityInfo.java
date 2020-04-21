package com.wolfmobileapps.usbserialdevice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityInfo extends AppCompatActivity {

    //views
    private LinearLayout version;
    private LinearLayout privacy;
    private LinearLayout info;
    private TextView textViewVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Info");

        version = findViewById(R.id.version);
        privacy = findViewById(R.id.privacy);
        info = findViewById(R.id.info);

        //ustavienie zazwy wersji z gradle
        textViewVersionName = findViewById(R.id.textViewVersionName);
        textViewVersionName.setText(BuildConfig.VERSION_NAME);

        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = "Version:";
                String alertString = BuildConfig.VERSION_NAME;
                createAlertDialog(titule, alertString);
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = "Policy privacy";
                String alertString = getResources().getString(R.string.privacy_policy_description);
                createAlertDialog(titule, alertString);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = "Developer Info";
                String alertString = "Contact:  \n\nwww.wolfmobileapps.com";
                createAlertDialog(titule, alertString);
            }
        });
    }
    // tworzy alert dialog z podanego stringa tutułu i opisu
    private void createAlertDialog(String titule, String alertString) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityInfo.this);
        builder.setTitle(titule);
        if (titule.equals("Developer Info")) {
            builder.setNegativeButton("Open web page", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri webpage = Uri.parse("https://wolfmobileapps.com");
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
        builder.setMessage(alertString);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do something when click OK
            }
        }).create();
        builder.show();

    }

}
