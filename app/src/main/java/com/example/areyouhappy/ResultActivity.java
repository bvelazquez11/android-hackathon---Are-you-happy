package com.example.areyouhappy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ResultActivity extends AppCompatActivity {

    ImageView selfieView;
    ImageView aura;
    ImageView characterView;
    boolean isHappy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        selfieView = (ImageView) findViewById(R.id.selfieView);
        aura = (ImageView) findViewById(R.id.aura);
        characterView = (ImageView) findViewById(R.id.characterView);

        int width = aura.getWidth();
        int halfWidth = width/2;
        characterView.setMaxWidth(halfWidth);
        characterView.setMinimumWidth(halfWidth);


        if (isHappy) {
            Picasso.with(this)
                    .load(R.drawable.happy_selfie)
                    .into(selfieView);
            Picasso.with(this)
                    .load(R.drawable.cornersun)
                    .into(characterView);
        } else if (!isHappy){
            Picasso.with(this)
                    .load(R.drawable.sad_selfie)
                    .into(selfieView);
            aura.setImageResource(R.drawable.sad_gradient);
            Picasso.with(this)
                    .load(R.drawable.cornercloud)
                    .into(characterView);
        }
    }
}
