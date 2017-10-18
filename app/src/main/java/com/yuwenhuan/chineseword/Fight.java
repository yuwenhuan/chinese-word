package com.yuwenhuan.chineseword;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Fight extends AppCompatActivity implements View.OnClickListener{
    private static final int WORD_LIST_NUM_PER_LESSON = 4; // words per lesson
    private static final int[] BUTTON_ID = {R.id.word1, R.id.word2, R.id.word3, R.id.word4};
    private static final String INDEX_FILENAME = "/index.txt"; // words per lesson
    private static final String TAG = "Fight Activity";
    private AssetManager assetManager;
    private MediaPlayer player;
    private Timer playWordTimer;
    private int currentLesson;
    private int currentWordNum;
    private TextView bullet;
    private Animation animationBulletToRight;
    private Animation animationBulletToLeft;
    private String[] wordList;
    private ImageView goodGuy;
    private ImageView badGuy;
    private int goodGuyBlood, badGuyBlood;
    private int goodGuyPivotX, goodGuyPivotY, badGuyPivotX, badGuyPivotY;
    private TimerTask playWordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight);

        // initialize
        goodGuy = (ImageView) findViewById(R.id.goodGuy);
        goodGuy.setImageResource(R.drawable.optimus);
        badGuy = (ImageView) findViewById(R.id.badGuy);
        badGuy.setImageResource(R.drawable.ultron);
        bullet = (TextView) findViewById(R.id.bullet);
        animationBulletToRight = AnimationUtils.loadAnimation(this, R.anim.bullet_to_right);
        animationBulletToLeft = AnimationUtils.loadAnimation(this, R.anim.bullet_to_left);
        currentLesson = 1;
        currentWordNum = (int) (Math.random() * 4) + 1;
        goodGuyBlood = 90;
        badGuyBlood = 90;
        goodGuyPivotX = goodGuy.getDrawable().getBounds().width() / 2;
        goodGuyPivotY = goodGuy.getDrawable().getBounds().height() / 2;
        badGuyPivotX = badGuy.getDrawable().getBounds().width() / 2;
        badGuyPivotY = badGuy.getDrawable().getBounds().height() / 2;
        player = new MediaPlayer();


        // get asset manager
        assetManager = getAssets();

        // get word List
        wordList = getWordList(1);

        // set buttons
        Button[] buttons = new Button[WORD_LIST_NUM_PER_LESSON];
        for(int i = 0; i < 4; i++){
            buttons[i] = (Button) findViewById(BUTTON_ID[i]);
            buttons[i].setText(wordList[i]);
            //buttons[i].setTextSize(30);
            buttons[i].setOnClickListener(this);
        }

        // create play word task
        playWordTask = new TimerTask() {
            @Override
            public void run() {
                playWord(Fight.this.currentLesson, Fight.this.currentWordNum); // But to start with just had this (no cleanup or prepare)
            }
        };

        // play word
        startPlayWord();

        // animation listener
        animationBulletToRight.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bullet.setVisibility(View.INVISIBLE);
            }
        });
        animationBulletToLeft.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bullet.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fight, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        playWordTimer.cancel();
        player.stop();
        player.release();
    }

    private void playWord(int lesson, int num) {
        // play word mp3
        try {
            String filename = Integer.toString(lesson) + "/" + Integer.toString(num) + ".mp3";
            AssetFileDescriptor afd = assetManager.openFd(filename);
            player.reset();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
            afd.close();
        } catch (IOException e) {
            Log.e(TAG, "Play mp3 error.", e);
        }
    }

    private void playSound(String soundName) {
        // play word mp3
        try {
            String filename = "sound/" + soundName + ".wav";
            AssetFileDescriptor afd = assetManager.openFd(filename);
            player.reset();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
            afd.close();
        } catch (IOException e) {
            Log.e(TAG, "Play sound error.", e);
        }
    }

    private String[] getWordList(int num) {
        // get word list
        String[] wordList = new String[WORD_LIST_NUM_PER_LESSON];
        Arrays.fill(wordList, "");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(assetManager.open(Integer.toString(num) + INDEX_FILENAME)));
            String mLine = reader.readLine();
            int word_idx = 0;
            while (mLine != null) {
                wordList[word_idx % 4] = mLine;
                mLine = reader.readLine();
                word_idx = word_idx + 1;
            }
        } catch (IOException e) {
            Log.e(TAG, "Asset read error. num = " + num, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Asset file close error.", e);
                }
            }
        }
        return wordList;
    }

    private void startPlayWord() {
        playWordTimer = new Timer();
        playWordTimer.schedule(playWordTask, 0, 4000);
    }

    private void stopPlayWord() {
        playWordTimer.cancel();
    }

    @Override
    public void onClick(View v) {
        for (int i=0; i<WORD_LIST_NUM_PER_LESSON; i++) {
            if (v.getId() == BUTTON_ID[i]) {
                //Log.d(TAG, "button " + Integer.toString(i));
                if (currentWordNum == i + 1) {
                    // answer is correct
                    player.stop();
                    playSound("correct");
                    // fire bullet_to_right
                    bullet.setText(wordList[i]);
                    bullet.setVisibility(View.VISIBLE);
                    bullet.startAnimation(animationBulletToRight);
                    // reduce bad guy's blood
                    badGuyBlood -= 10;
                    // rotate bad guy
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) 90 - badGuyBlood, badGuyPivotX, badGuyPivotY);
                    badGuy.setImageMatrix(matrix);
                    // choose a new word
                    currentWordNum = (int) (Math.random() * 4) + 1;
                } else {
                    // answer is wrong
                    player.stop();
                    playSound("wrong");
                    // fire bullet_to_left
                    bullet.setText(wordList[i]);
                    bullet.setVisibility(View.VISIBLE);
                    bullet.startAnimation(animationBulletToLeft);
                    // reduce good guy's blood
                    goodGuyBlood -= 10;
                    // rotate good guy
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) 90 - goodGuyBlood, goodGuyPivotX, goodGuyPivotY);
                    goodGuy.setImageMatrix(matrix);
                }
            }
        }
    }
}
