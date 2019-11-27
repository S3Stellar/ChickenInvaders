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
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ChickenInvadersView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Star s;
    private Boom boom;
    private PlayerShip player;
    private Invader[] enemies;
    private ArrayList<Star> stars = new
            ArrayList<>();

    // Amount of enemies on screen at same time
    private int enemyCount = 3;

    // Number of stars
    private int stars_num = 100;

    // The score
    private int score = 0;

    // Lives
    private int lives = 3;

    // The high Scores Holder
    int highScore[] = new int[4];
    String highScoreNames[] = new String[4];

    // Shared Prefernces to store the High Scores
    SharedPreferences sharedPreferences;

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

    // Background bitmap
    private Bitmap gameBackground;

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
        setScreenSize();
        this.nickname = nickname;
        // Resize heart bitmap
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        heart = Bitmap.createScaledBitmap(heart, 50, 50, true);

        player = new PlayerShip(context, screenX, screenY);
        surfaceHolder = getHolder();
        paint = new Paint();

        // Initialize stars for background effect
        for (int i = 0; i < stars_num; i++)
            stars.add(new Star(screenX, screenY));

        // Initialize enemies
        enemies = new Invader[enemyCount];
        for (int i = 0; i < enemyCount; i++)
            enemies[i] = new Invader(context, screenX, screenY);


        // Initialize boom object
        boom = new Boom(context);

        // Start score counter (1sec)
        activateScore();

        // In case of using background instead black color
        gameBackground = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gamebackground);
        gameBackground = Bitmap.createScaledBitmap(gameBackground, screenX, screenY + 150, true);

        //initializing the media players for the game sounds
        gameOnSound = MediaPlayer.create(context, R.raw.chicksoundtrack);
        killedEnemySound = MediaPlayer.create(context, R.raw.killedenemy);
        gameOverSound = MediaPlayer.create(context, R.raw.gameover);

        // Start gameplay music
        gameOnSound.start();

        //initializing shared Preferences
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME", Context.MODE_PRIVATE);

        //initializing the array high scores with the previous values
        highScore[0] = sharedPreferences.getInt("score1", 0);
        highScore[1] = sharedPreferences.getInt("score2", 0);
        highScore[2] = sharedPreferences.getInt("score3", 0);
        highScore[3] = sharedPreferences.getInt("score4", 0);
        highScoreNames[0] = sharedPreferences.getString("nickname1","");
        highScoreNames[1] = sharedPreferences.getString("nickname2","");
        highScoreNames[2] = sharedPreferences.getString("nickname3","");
        highScoreNames[3] = sharedPreferences.getString("nickname4","");

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
        boom.setX(-650);
        boom.setY(-650);

        for (int i = 0; i < enemyCount; i++) {
            // If collision occurs with player
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {
                killedEnemySound.start();
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
            gameOnSound.stop();
            gameOverSound.start();
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
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                FutureTarget<Bitmap> futureTarget =
                        Glide.with(getContext())
                                .asBitmap()
                                .load(R.raw.gameovergif)
                                .submit(screenX / 2, screenY / 2);
                try {
                    Bitmap gameOverBitmap = futureTarget.get();
                    canvas.drawBitmap(gameOverBitmap, screenX / 2 - gameOverBitmap.getWidth() / 2, yPos / 2, paint);
                    Glide.with(getContext()).clear(futureTarget);
                    paint.setTypeface(Typeface.create("Arial", Typeface.ITALIC));
                    paint.setTextSize(35);
                    canvas.drawText("Click to play again!", canvas.getWidth() / 2, yPos + gameOverBitmap.getHeight() / 4, paint);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 4; i++) {
                    if (highScore[i] < score) {
                       // final int finalI = i;
                        highScore[i] = score;
                        highScoreNames[i] = nickname;
                        break;
                    }
                }

                // Storing the scores through shared Preferences
                SharedPreferences.Editor e = sharedPreferences.edit();

                for (int i = 0; i < 4; i++) {
                    int j = i + 1;
                    e.putInt("score" + j, highScore[i]);
                    e.putString("nickname"+j,highScoreNames[i]);
                }
                e.apply();
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void pause() {
        playing = false;
        if (gameOnSound != null)
            gameOnSound.pause();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
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
        if (gameOver)
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                getContext().startActivity(new Intent(getContext(), GameActivity.class));
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
                if (score < 9999) {
                    if (!paused && lives > 0)
                        if (playing)
                            score++;
                    handler.postDelayed(this, 1000L);
                    return;
                }
                handler.removeCallbacks(this);
            }
        }, 1000L);
    }

    public void setScreenSize() {
        WindowManager windowManager =
                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // include navigation bar
            display.getRealSize(outPoint);
        } else {
            // exclude navigation bar
            display.getSize(outPoint);
        }
        if (outPoint.y > outPoint.x) {
            screenY = outPoint.y;
            screenX = outPoint.x;
        } else {
            screenY = outPoint.x;
            screenX = outPoint.y;
        }
    }
}