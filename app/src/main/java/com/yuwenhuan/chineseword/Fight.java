package com.yuwenhuan.chineseword;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.media.AudioAttributes;
import android.media.SoundPool;
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


public class Fight extends AppCompatActivity implements View.OnClickListener{
    private static final int WORD_LIST_NUM_PER_LESSON = 4; // words per lesson
    private static final int[] BUTTON_ID = {R.id.word1, R.id.word2, R.id.word3, R.id.word4};
    private static final String INDEX_FILENAME = "/index.txt"; // words per lesson
    private static final String TAG = "Fight Activity";
    private static final String CURRENT_LESSON = "com.yuwenhuan.CURRENT_LESSON";
    private AssetManager assetManager;
    private int currentLesson;
    private int currentWordNum;
    private TextView bullet;
    private Animation animationBulletToRight;
    private Animation animationBulletToLeft;
    private String[] wordList;
    private ImageView goodGuy;
    private ImageView badGuy;
    private int goodGuyBlood, badGuyBlood;
    private SoundPool sp;
    private int[] wordSoundPoolIds;
    private int currentWordStreamId;
    private boolean spLoadReady;
    private int correctSoundId;
    private int wrongSoundId;
    private int explodeSoundId;
    private int winSoundId;
    private int looseSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fight);

        // get current lesson
        Intent intent = getIntent();
        currentLesson = intent.getIntExtra(CURRENT_LESSON, 1);

        // initialize
        goodGuy = (ImageView) findViewById(R.id.goodGuy);
        goodGuy.setImageResource(R.drawable.optimus);
        badGuy = (ImageView) findViewById(R.id.badGuy);
        badGuy.setImageResource(R.drawable.ultron);
        bullet = (TextView) findViewById(R.id.bullet);
        animationBulletToRight = AnimationUtils.loadAnimation(this, R.anim.bullet_to_right);
        animationBulletToLeft = AnimationUtils.loadAnimation(this, R.anim.bullet_to_left);
        goodGuyBlood = 90;
        badGuyBlood = 90;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sp = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();
        spLoadReady = false;
        wordSoundPoolIds = new int[WORD_LIST_NUM_PER_LESSON];
        assetManager = getAssets();

        // get word List
        wordList = getWordList(currentLesson);

        // set buttons
        Button[] buttons = new Button[WORD_LIST_NUM_PER_LESSON];
        for(int i = 0; i < 4; i++){
            buttons[i] = (Button) findViewById(BUTTON_ID[i]);
            buttons[i].setText(wordList[i]);
            //buttons[i].setTextSize(30);
            buttons[i].setOnClickListener(this);
        }

        // select a random word
        currentWordNum = (int) (Math.random() * WORD_LIST_NUM_PER_LESSON);

        // play word
        loadSound(1);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Fight.this.spLoadReady = true;
                Fight.this.playWord(currentWordNum);
            }
        });

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
                sp.play(explodeSoundId, 1, 1, 1, 0, 1);
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
                sp.play(explodeSoundId, 1, 1, 1, 0, 1);
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
    protected void onDestroy() {
        super.onDestroy();
        sp.release();
    }

    private void loadSound(int lesson) {
        for (int i=0; i<WORD_LIST_NUM_PER_LESSON; i++) {
            String filename = Integer.toString(lesson) + "/" + Integer.toString(i + 1) + ".mp3";
            try {
                AssetFileDescriptor afd = assetManager.openFd(filename);
                wordSoundPoolIds[i] = sp.load(afd, 1);
                afd.close();
            } catch (IOException e) {
                Log.e(TAG, "Load word mp3 error.", e);
            }
        }
        correctSoundId = sp.load(this, R.raw.correct, 1);
        wrongSoundId = sp.load(this, R.raw.wrong, 1);
        explodeSoundId = sp.load(this, R.raw.explode, 1);
        winSoundId = sp.load(this, R.raw.win, 1);
        looseSoundId = sp.load(this, R.raw.loose, 1);
    }

    private void playWord(int num) {
        // play word mp3
        currentWordStreamId = sp.play(wordSoundPoolIds[num], 1, 1, 1, 0, 1);
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

    private void showGameEndDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goodGuyBlood = 90;
                        badGuyBlood = 90;
                        goodGuy.setRotation(0);
                        badGuy.setRotation(0);
                        // select a random word
                        currentWordNum = (int) (Math.random() * WORD_LIST_NUM_PER_LESSON);
                        // play word
                        Fight.this.playWord(currentWordNum);
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (spLoadReady) {
            for (int i = 0; i < WORD_LIST_NUM_PER_LESSON; i++) {
                if (v.getId() == BUTTON_ID[i]) {
                    //Log.d(TAG, "button " + Integer.toString(i));
                    if (currentWordNum == i) {
                        // answer is correct
                        // play correct sound
                        sp.play(correctSoundId, 1, 1, 1, 0, 1);
                        // fire bullet_to_right
                        bullet.setText(wordList[i]);
                        bullet.setVisibility(View.VISIBLE);
                        bullet.startAnimation(animationBulletToRight);
                        // reduce bad guy's blood
                        badGuyBlood -= 10;
                        // rotate bad guy
                        int badGuyPivotX = badGuy.getDrawable().getBounds().width() / 2;
                        int badGuyPivotY = badGuy.getDrawable().getBounds().height() / 2;
                        badGuy.setPivotX(badGuyPivotX);
                        badGuy.setPivotY(badGuyPivotY);
                        badGuy.setRotation(-(90 - badGuyBlood));
                        if (badGuyBlood > 0) {
                            // choose a new word
                            currentWordNum = (currentWordNum + (int) (Math.random() * (WORD_LIST_NUM_PER_LESSON - 1)) + 1) % WORD_LIST_NUM_PER_LESSON;
                            playWord(currentWordNum);
                        } else {
                            // win the game
                            showGameEndDialog("你赢了", "你赢了，再来一次？");
                        }
                    } else {
                        // answer is wrong
                        // play wrong sound
                        sp.play(wrongSoundId, 1, 1, 1, 0, 1);
                        // fire bullet_to_left
                        bullet.setText(wordList[i]);
                        bullet.setVisibility(View.VISIBLE);
                        bullet.startAnimation(animationBulletToLeft);
                        // reduce good guy's blood
                        goodGuyBlood -= 10;
                        // rotate good guy
                        int goodGuyPivotX = goodGuy.getDrawable().getBounds().width() / 2;
                        int goodGuyPivotY = goodGuy.getDrawable().getBounds().height() / 2;
                        goodGuy.setPivotX(goodGuyPivotX);
                        goodGuy.setPivotY(goodGuyPivotY);
                        goodGuy.setRotation(90 - goodGuyBlood);
                        if (goodGuyBlood > 0) {
                            // play word again
                            playWord(currentWordNum);
                        } else {
                            // loose the game
                            showGameEndDialog("你输了", "你输了，再来一次？");
                        }
                    }
                }
            }
        }
    }
}
