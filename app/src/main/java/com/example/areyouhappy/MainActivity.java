package com.example.areyouhappy;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hod.api.hodclient.HODApps;
import hod.api.hodclient.HODClient;
import hod.api.hodclient.IHODClientCallback;
import hod.response.parser.HODResponseParser;
import hod.response.parser.RecognizeSpeechResponse;
import hod.response.parser.SpeechRecognitionResponse;

public class MainActivity extends AppCompatActivity implements IHODClientCallback {

    HODClient hodClient = new HODClient("b7799240-2c3c-4ea8-8381-efcdcc72456b",this);
    HODResponseParser hodResponseParser = new HODResponseParser();
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private Button   mPlayButton;
    private MediaPlayer mPlayer = null;

    Button mHODbutton;
    TextView mTextView;

    boolean mStartPlaying = true;
    boolean mStartRecording = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecordButton = (Button) findViewById(R.id.RecordButton);
        mPlayButton = (Button) findViewById(R.id.PlayButton);
        mHODbutton = (Button) findViewById(R.id.HODbutton);
        mTextView = (TextView) findViewById(R.id.textView);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop recording");
                } else {
                    mRecordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mStartPlaying) {
                    mPlayButton.setText("Stop playing");
                } else {
                    mPlayButton.setText("Start playing");
                }

                onPlay(mStartPlaying);
                mStartPlaying = !mStartPlaying;
            }
        });

        mHODbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useHODClient();
            }
        });
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

        String hodApp = HODApps.RECOGNIZE_SPEECH;
        Map<String,Object> params =  new HashMap<String,Object>();
        params.put("file", mFileName);
        hodClient.PostRequest(params, hodApp, HODClient.REQ_MODE.ASYNC);
    }

    // HOD methods.
    @Override
    public void requestCompletedWithContent(String response) {
        SpeechRecognitionResponse resp = hodResponseParser.ParseSpeechRecognitionResponse(response);
        if(resp != null){
            String text = resp.getDocument().get(0).getContent();
            mTextView.setText(text);
        }
    }

    @Override
    public void requestCompletedWithJobID(String response) {
        String jobID = hodResponseParser.ParseJobID(response);
        if (jobID.length() > 0)
            hodClient.GetJobStatus(jobID);
    }

    @Override
    public void onErrorOccurred(String errorMessage) {

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
