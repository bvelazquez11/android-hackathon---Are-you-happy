package com.example.areyouhappy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observer;


import hod.api.hodclient.HODApps;
import hod.api.hodclient.HODClient;
import hod.api.hodclient.IHODClientCallback;
import hod.response.parser.HODResponseParser;
import hod.response.parser.RecognizeSpeechResponse;
import hod.response.parser.SentimentAnalysisResponse;
import hod.response.parser.SpeechRecognitionResponse;

public class MainActivity extends AppCompatActivity implements IHODClientCallback {

    HODClient hodClient = new HODClient("b7799240-2c3c-4ea8-8381-efcdcc72456b",this);
    HODResponseParser hodResponseParser = new HODResponseParser();
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    ImageView star;
    ImageView dogee;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    int counter = 1;
    int async_counter = 0;
    int rounded;

    private MediaPlayer mPlayer = null;

    Animation rotate;
    TextView mTextView;

    boolean mStartPlaying = true;
    boolean mStartRecording = false;
    String hodApp = "";
    boolean isHappy;
    int maxQuestion = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecordButton = (Button) findViewById(R.id.RecordButton);
        mTextView = (TextView) findViewById(R.id.textView);
        star = (ImageView) findViewById(R.id.star);
        dogee = (ImageView) findViewById(R.id.dogee);
        BitmapDrawable starDrawable = (BitmapDrawable) this.getResources().getDrawable(R.drawable.star);

        Picasso.with(this)
                .load(R.drawable.star)
                .into(star);

        Picasso.with(this)
                .load(R.drawable.dogee)
                .into(dogee);

        star.setMinimumWidth(starDrawable.getBitmap().getWidth());
        star.setMinimumHeight(starDrawable.getBitmap().getWidth());

        rotate = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //counter += 1 ;
                Log.d(LOG_TAG, "onClick: counter = " + counter);
                mStartRecording = !mStartRecording;
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop recording");

                    if (counter < maxQuestion) {
                        mTextView.setText(String.format("question %d", counter));
                        star.startAnimation(rotate);
                    } else {
                        //counter = 0;
                    }
                    counter++;
                } else {
                    mRecordButton.setText("Start recording");
                    mRecordButton.setEnabled(false);
                    useHODClient();
                }

                switch (counter) {
                    case 2 :
                        mTextView.setText("How was your past week?");
                        break;
                    case 3 :
                        mTextView.setText("Done!");
                        break;
                    case 4 :
                        mTextView.setText("question 4");
                        break;
                    case 5 :
                        mTextView.setText("question 5");
                        break;
                    case 6 :
                        mTextView.setText("done!");
                        break;
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();


            Intent intent = new Intent(MainActivity.this,ResultActivity.class);
            intent.putExtra("ishappy",isHappy);
            intent.putExtra("byteArray", byteArray);
            intent.putExtra("percent",rounded);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void useHODClient() {

        hodApp = HODApps.RECOGNIZE_SPEECH;
        Map<String,Object> params =  new HashMap<String,Object>();
        params.put("file", mFileName);
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }
    List<String> textArray = new ArrayList<>();
    // HOD methods.
    @Override
    public void requestCompletedWithContent(String response) {
        if (hodApp.equals(HODApps.RECOGNIZE_SPEECH)) {
            SpeechRecognitionResponse resp = hodResponseParser.ParseSpeechRecognitionResponse(response);
            if (resp != null) {
                for (SpeechRecognitionResponse.Document doc : resp.document) {
                    mRecordButton.setEnabled(true);
                    String text = doc.content;
                    Log.d(LOG_TAG, "requestCompletedWithContent: " + text);
                    if (text.length() == 0) {
                        // please speak again.
                    } else {
                        async_counter += 1 ;
                        textArray.add(text);
                        if (counter > maxQuestion) {
                            Log.d(LOG_TAG, "requestCompletedWithContent: " + text);
                            hodApp = HODApps.ANALYZE_SENTIMENT;
                            Map<String, Object> params = new HashMap<>();
                            params.put("text", textArray);
                            hodClient.GetRequest(params, hodApp, HODClient.REQ_MODE.SYNC);
                        }
                    }

                }
            }
        }
        else if (hodApp.equals(HODApps.ANALYZE_SENTIMENT)) {
            SentimentAnalysisResponse resp = hodResponseParser.ParseSentimentAnalysisResponse(response);
            async_counter += 1 ;
            mRecordButton.setEnabled(true);
            star.clearAnimation();
            if (resp != null) {
                double agg = resp.aggregate.score;
                if (counter >= maxQuestion){
                    if (agg >= 0.0) {
                        isHappy = true;
                    } else if (agg < 0.0) {
                        isHappy = false;
                    }

                    double percent = Math.abs(agg) * 100;
                    rounded = (int) Math.round(percent);
                    Log.d(LOG_TAG, "requestCompletedWithContent: percent= " + rounded);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                Log.d(LOG_TAG, "requestCompletedWithContent: " + isHappy);
            }

        }
    }

    @Override
    public void requestCompletedWithJobID(String response) {
        String jobID = hodResponseParser.ParseJobID(response);
        if (jobID.length() > 0)
            hodClient.GetJobResult(jobID);
    }

    @Override
    public void onErrorOccurred(String errorMessage) {
        mRecordButton.setEnabled(true);
        Log.i(LOG_TAG, "onErrorOccurred: " + errorMessage);
    }

    //Recorder methods
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public MainActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecord.mp4";
    }

}
