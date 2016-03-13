package com.example.areyouhappy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ResultActivity extends AppCompatActivity {

    ImageView selfieView;
    ImageView auraView;
    ImageView characterView;
    boolean isHappy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        selfieView = (ImageView) findViewById(R.id.selfieView);
        auraView = (ImageView) findViewById(R.id.aura);
        characterView = (ImageView) findViewById(R.id.characterView);

        Intent recievedIntent = getIntent();
        isHappy = recievedIntent.getBooleanExtra("ishappy", true);
        byte[] byteArray = recievedIntent.getByteArrayExtra("byteArray");
        Bitmap selfie = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        int width = auraView.getWidth();
        int halfWidth = width/2;
        characterView.setMaxWidth(halfWidth);
        characterView.setMinimumWidth(halfWidth);


        selfieView.setImageBitmap(selfie);
        if (isHappy) {
            auraView.setBackgroundResource(R.drawable.happy_gradient);
            characterView.setImageResource(R.drawable.cornersun);
        } else if (!isHappy){
            auraView.setBackgroundResource(R.drawable.sad_gradient);
            characterView.setImageResource(R.drawable.cornercloud);
        }
    }
}
