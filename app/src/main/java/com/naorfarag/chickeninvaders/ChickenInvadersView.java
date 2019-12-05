package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChickenInvadersView extends SurfaceView implements Runnable, Finals {

    volatile boolean playing;
    private Thread gameThread = null;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Boom boom;
    private PlayerShip player;
    private Invader[] enemies;
    private ArrayList<Star> stars = new
            ArrayList<>();

    // Amount of enemies on screen at same time
    private int enemyCount = ENEMIES_COUNT;

    // Number of stars
    private int stars_num = STARS_COUNT;

    // The score
    private int score;

    // Lives
    private int lives = MAX_LIFE;

    // The high Scores Holder
    private ArrayList<PlayerNickScore> gameScores = new ArrayList<>();

    // Shared Preferences to store the High Scores
    private SharedPreferences sharedPreferences;

    // Game over
    private boolean gameOver = false;

    // Game is paused at the start
    private boolean paused = true;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    // Screen resolution
    private int screenY;
    private int screenX;

    // Hearts bitmap
    private Bitmap heart;

    // Background bitmap (if chosen to use)
    //private Bitmap gameBackground;

    // Game over bitmap
    private Bitmap gameOverBitmap;

    // Background game play sound
    static MediaPlayer gameOnSound;

    // Killing enemy sound
    final MediaPlayer killedEnemySound;

    // Game over sound
    final MediaPlayer gameOverSound;

    // Player's nickname
    private String nickname;

    public ChickenInvadersView(Context context, String nickname) {
        super(context);
        this.nickname = nickname;
        setScreenSize();

        // Resize heart bitmap
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        heart = Bitmap.createScaledBitmap(heart, 50, 50, true);

        // In case of using background instead black color
        /*gameBackground = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gamebackground);
        gameBackground = Bitmap.createScaledBitmap(gameBackground, screenX, screenY, true);*/

        // Game over bitmap
        gameOverBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.raw.gameovergif);
        gameOverBitmap = Bitmap.createScaledBitmap(gameOverBitmap, screenX, screenY / 2, true);

        // Initializing
        sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        player = new PlayerShip(context, screenX, screenY);
        surfaceHolder = getHolder();
        boom = new Boom(context);
        paint = new Paint();

        // Initialize stars for background effect
        for (int i = 0; i < stars_num; i++)
            stars.add(new Star(screenX, screenY));

        // Initialize enemies
        enemies = new Invader[enemyCount];
        for (int i = 0; i < enemyCount; i++)
            enemies[i] = new Invader(context, screenX, screenY);

        for (int i = 0; i < HIGH_SCORE_COUNT; i++) {
            gameScores.add(new PlayerNickScore(sharedPreferences.getInt(SCORE + i, 0)
                    , sharedPreferences.getString(NICKNAME + i, "")));
        }

        // Start score counter (1sec)
        activateScore();

        //initializing the media players for the game sounds
        gameOnSound = MediaPlayer.create(context, R.raw.chicksoundtrack);
        killedEnemySound = MediaPlayer.create(context, R.raw.killedenemy);
        gameOverSound = MediaPlayer.create(context, R.raw.gameover);

        MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null && mp.isPlaying()) {
                    mp.pause();
                    mp.seekTo(0);
                }
            }
        };
        gameOverSound.setOnCompletionListener(onCompletionListener);
        killedEnemySound.setOnCompletionListener(onCompletionListener);

        // Start gameplay music
        if (gameOnSound != null && !gameOnSound.isPlaying())
            gameOnSound.start();
    }


    @Override
    public void run() {
        while (playing) {
            // Increase stars & Invader speed as game progress
            for (Star s : stars)
                s.update(new Random().nextInt(5) + 1 + score / 7);
            for (Invader e : enemies)
                e.update(new Random().nextInt(4) + 1 + score / 7);

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused)
                update();

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame;
        }
    }


    private void update() {
        // Move the player's ship
        player.update(fps);

        // Setting boom outside the screen
        boom.setX(OUT_OF_BOUNDS);
        boom.setY(OUT_OF_BOUNDS);

        for (int i = 0; i < enemyCount; i++) {
            // If collision occurs with player
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {
                if (killedEnemySound != null && killedEnemySound.isPlaying())
                    killedEnemySound.pause();
                else if (killedEnemySound != null && !killedEnemySound.isPlaying()) {
                    killedEnemySound.start();
                }
                lives--;
                // Displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());
                enemies[i].setY(screenY + enemies[i].getBitmap().getHeight());
            }
        }
        if (lives == 0) {
            playing = false;
            gameOver = true;
            if (gameOverSound != null && !gameOverSound.isPlaying()) {
                gameOverSound.start();
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            // Draw background image OR black background
            //canvas.drawBitmap(gameBackground,0,0, paint);
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);

            // Draw stars
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }
            // Draw player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);

            // Draw hearts
            for (int i = 0; i < lives; i++)
                canvas.drawBitmap(heart, screenX - 55 * (i + 1), 15, paint);

            // Draw score
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score, 10, 50, paint);

            // Draw enemies
            for (int i = 0; i < enemyCount; i++)
                canvas.drawBitmap(
                        enemies[i].getBitmap(),
                        enemies[i].getX(),
                        enemies[i].getY(),
                        paint
                );

            // Drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );

            // Game over - draw bitmap (need to fix gif)
            if (gameOver) {
                int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawBitmap(gameOverBitmap, screenX / 2 - gameOverBitmap.getWidth() / 2, yPos / 2, paint);

                // Write "play again"
                paint.setTypeface(Typeface.create(ARIAL_FONT, Typeface.ITALIC));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(35);
                canvas.drawText(PLAY_AGAIN, canvas.getWidth() / 2, yPos + gameOverBitmap.getHeight() / 4, paint);

                for (int i = HIGH_SCORE_COUNT - 1; i > 0; i--) {
                    if (gameScores.get(i).getScore() < score) {
                        gameScores.get(i).setScore(score);
                        gameScores.get(i).setNickname(nickname);
                        break;
                    }
                }
                // Sort scores
                Collections.sort(gameScores);

                // Storing the scores through shared Preferences
                SharedPreferences.Editor e = sharedPreferences.edit();

                for (int i = 0; i < HIGH_SCORE_COUNT; i++) {
                    e.putInt("score" + i, gameScores.get(i).getScore());
                    e.putString("nickname" + i, gameScores.get(i).getNickname());
                }
                e.apply();
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void pause() {
        playing = false;
        if (gameOnSound != null && gameOnSound.isPlaying())
            gameOnSound.pause();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            gameThread.interrupt();
            Log.e(getClass().getSimpleName(), "ThreadException", e);
        }
    }

    public void resume() {
        playing = true;
        if (gameOnSound != null && !gameOnSound.isPlaying())
            gameOnSound.start();
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Screen divided in half vertically to control movement
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Get pointer index from the event object
        int pointerIndex = motionEvent.getActionIndex();
        // Get masked (not specific to a pointer) action
        int maskedAction = motionEvent.getActionMasked();
        switch (maskedAction) {
            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                player.setMovementState(player.STOPPED);
                break;

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                paused = false;
                if (motionEvent.getX(pointerIndex) > screenX / 2)
                    player.setMovementState(player.RIGHT);
                else
                    player.setMovementState(player.LEFT);
                break;
        }
        if (gameOver && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            stopMusic();
            Intent intent = new Intent(getContext(), GameActivity.class);
            intent.putExtra(NICKNAME, nickname);
            getContext().startActivity(intent);
        }
        return true;
    }

    // Stop & release music
    public static void stopMusic() {
        if (gameOnSound != null) {
            gameOnSound.stop();
            gameOnSound.reset();
            gameOnSound.release();
            gameOnSound = null;
        }
    }

    // Increase score every 1 second
    private void activateScore() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (score < MAX_SCORE) {
                    if (!paused && lives > 0 && playing)
                        score++;
                    handler.postDelayed(this, 1000L);
                    return;
                }
                handler.removeCallbacks(this);
            }
        }, 1000L);
    }

    // Set screenX screenY according to navigation bar
    public void setScreenSize() {
        WindowManager windowManager =
                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        // include navigation bar
        display.getRealSize(outPoint);
        if (outPoint.y > outPoint.x) {
            screenY = outPoint.y;
            screenX = outPoint.x;
        } else {
            screenY = outPoint.x;
            screenX = outPoint.y;
        }
    }
}