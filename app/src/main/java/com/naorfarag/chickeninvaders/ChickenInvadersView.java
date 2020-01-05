package com.naorfarag.chickeninvaders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TiltDirectionDetector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


public class ChickenInvadersView extends SurfaceView implements Runnable {

    private final Context context;
    volatile boolean playing;
    private Thread gameThread = null;

    private Paint paint;
    private SurfaceHolder surfaceHolder;

    private Boom boom;
    private Egg egg;
    private PlayerShip player;
    private Invader[] enemies;
    private ArrayList<Star> stars = new
            ArrayList<>();

    // Amount of enemies on screen at same time
    private int enemyCount;

    // The playerScore
    private int playerScore;

    // Lives
    private int lives = Finals.MAX_LIFE;

    // The high Scores Holder
    private ArrayList<PlayerAttributes> gameScores = new ArrayList<>();

    // Shared Preferences to store the High Scores
    private SharedPreferences sharedPreferences;

    // Game over
    volatile boolean gameOver = false;

    // Game is paused at the start
    volatile boolean paused = true;

    // Used to make stars move on first star (yet they pause if game paused after)
    volatile boolean firstStart = true;

    // Is game in tilt mode
    volatile boolean isTilt;

    // This variable tracks the game frame rate
    private long fps;

    // Screen resolution
    private int screenY;
    private int screenX;

    // Hearts bitmap
    private Bitmap heart;

    // Game over bitmap
    private Bitmap gameOverBitmap;

    // Background game play sound
    static MediaPlayer gameOnSound;

    // Killing enemy sound
    final MediaPlayer killedEnemySound;

    // Game over sound
    final MediaPlayer gameOverSound;

    final MediaPlayer hitAnEggSound;

    // Player's nickname
    private String playerNickname;

    // Used in various places rand generator
    private Random rand = new Random();

    // Tilt device listener
    private TiltDirectionDetector.TiltDirectionListener tiltDirectionListener;

    // Player's GPS location
    private double longitude;
    private double latitude;

    public ChickenInvadersView(Context context, String nickname, int lanes, boolean isTilt, double latitude, double longitude) {
        super(context);
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        this.playerNickname = nickname;
        this.enemyCount = lanes;
        this.isTilt = isTilt;

        // Set correct screenX, screenY values
        setScreenSize();

        // Activate tilt if in tilt mode
        deviceTiltManager();

        // Resize bitmaps
        resizeBitmaps(context);

        // Init game objects
        initializeGame(context);

        // Start playerScore counter (1sec)
        activateScore();

        // Initializing the media players for the game sounds
        gameOnSound = MediaPlayer.create(context, R.raw.chicksoundtrack);
        killedEnemySound = MediaPlayer.create(context, R.raw.killedenemy);
        gameOverSound = MediaPlayer.create(context, R.raw.gameover);
        hitAnEggSound = MediaPlayer.create(context, R.raw.pewpew);

        // Set sound listener onComplete, start gamesounds
        initGameSound();
    }

    private void initGameSound() {
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
        hitAnEggSound.setOnCompletionListener(onCompletionListener);

        // Start gameplay music
        if (gameOnSound != null && !gameOnSound.isPlaying())
            gameOnSound.start();

        // Set volumes
        hitAnEggSound.setVolume(Finals.MAX_VOLUME, Finals.MAX_VOLUME);
        killedEnemySound.setVolume(Finals.MAX_VOLUME, Finals.MAX_VOLUME);
        if (gameOnSound != null)
            gameOnSound.setVolume(0.65f, 0.65f);
    }

    private void initializeGame(Context context) {
        // Initializing
        sharedPreferences = context.getSharedPreferences(Finals.SHARED_PREF, Context.MODE_PRIVATE);
        player = new PlayerShip(context, screenX, screenY);
        surfaceHolder = getHolder();
        boom = new Boom(context);
        paint = new Paint();
        egg = new Egg(context, screenX, screenY);
        enemies = new Invader[enemyCount];

        // Initialize stars for background effect
        // Number of stars
        for (int i = 0; i < Finals.STARS_COUNT; i++)
            stars.add(new Star(screenX, screenY));

        // Initialize enemies
        for (int i = 0; i < enemyCount; i++) {
            enemies[i] = new Invader(context, screenX, screenY, screenX - (i + 1) * screenX / enemyCount + screenX / (enemyCount * 2) - getResources().getDrawable(R.drawable.chicken1).getIntrinsicWidth() / 2);
        }

        // Initialize existing highScore
        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            double longitude;
            double latitude;
            try {
                longitude = Double.parseDouble(sharedPreferences.getString(Finals.LONGITUDE + i, ""));
                latitude = Double.parseDouble(sharedPreferences.getString(Finals.LATITUDE + i, ""));
            } catch (Exception e) {
                longitude = 0;
                latitude = 0;
            }
            gameScores.add(new PlayerAttributes(sharedPreferences.getInt(Finals.SCORE + i, 0),
                    sharedPreferences.getString(Finals.NICKNAME + i, ""), longitude, latitude));
        }
    }


    @Override
    public void run() {
        while (playing) {
            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused || firstStart)
                update();

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            // This is used to help calculate the fps
            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame;
        }
    }


    private void update() {
        // Move the player's ship
        player.update(fps);

        // Increase stars & Invader & Egg speed as game progress
        for (Star s : stars)
            s.update(rand.nextInt(5) + 1 + playerScore / 7);

        // Delay start / wait for the player to start moving
        if (playerScore > 0.1) {
            for (Invader e : enemies)
                e.update(rand.nextInt(4) + 1 + playerScore / 7);
            egg.update(rand.nextInt(4) + 1 + playerScore / 7);
        }

        // Setting boom outside the screen
        boom.setX(Finals.OUT_OF_BOUNDS);
        boom.setY(Finals.OUT_OF_BOUNDS);

        checkEggIntersect();
        checkInvaderIntersect();

        if (lives <= 0) {
            gameOver = true;
            if (gameOverSound != null && !gameOverSound.isPlaying()) {
                gameOverSound.start();
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);

            // Draw stars
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }
            // Draw player
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);

            // Draw hearts
            for (int i = 0; i < lives; i++)
                canvas.drawBitmap(heart, screenX - (heart.getWidth() + 10) * (i + 1), 15, paint);

            // Draw playerScore
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + playerScore, 10, 50, paint);

            // Draw enemies
            for (int i = 0; i < enemyCount; i++)
                canvas.drawBitmap(enemies[i].getBitmap(), enemies[i].getX(), enemies[i].getY(), paint);

            // Drawing boom image
            canvas.drawBitmap(boom.getBitmap(), boom.getX(), boom.getY(), paint);

            // Draw egg (gives 1 life if below 3, else 10Points)
            canvas.drawBitmap(egg.getBitmap(), egg.getX(), egg.getY(), paint);

            // Game over - draw bitmap (need to fix gif)
            if (gameOver) {
                paused = true;
                playing = false;
                int yPos = (int) ((screenY / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawBitmap(gameOverBitmap, screenX / 2 - gameOverBitmap.getWidth() / 2, yPos / 2, paint);

                // Write "play again"
                paint.setTypeface(Typeface.create(Finals.ARIAL_FONT, Typeface.ITALIC));
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(35);
                canvas.drawText(Finals.PLAY_AGAIN, screenX / 2, yPos + gameOverBitmap.getHeight() / 4, paint);

                updateHighScoreTable();
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        paused = true;
        firstStart = false;

        if (isTilt) {
            Sensey.getInstance().stopTiltDirectionDetection(tiltDirectionListener);
        }

        if (gameOnSound != null && gameOnSound.isPlaying())
            gameOnSound.pause();
        try {
            if (gameThread != null)
                gameThread.join();
        } catch (InterruptedException e) {
            gameThread.interrupt();
            gameThread = null;
            Log.e(getClass().getSimpleName(), "ThreadException", e);
        }
    }

    public void resume() {
        playing = true;
        if (isTilt) {
            Sensey.getInstance().startTiltDirectionDetection(tiltDirectionListener);
        }
        if (gameOnSound != null && !gameOnSound.isPlaying())
            gameOnSound.start();
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void checkInvaderIntersect() {
        for (int i = 0; i < enemyCount; i++) {
            // If collision occurs with player
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {

                if (killedEnemySound != null && killedEnemySound.isPlaying())
                    killedEnemySound.pause();
                else if (killedEnemySound != null && !killedEnemySound.isPlaying())
                    killedEnemySound.start();

                vibrate();
                lives--;
                // Displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());
                enemies[i].setY(screenY + enemies[i].getBitmap().getHeight());
            }
        }
    }

    private void checkEggIntersect() {
        if (Rect.intersects(player.getDetectCollision(), egg.getDetectCollision())) {
            // If lives less than 3 -> add 1 life, else +10 points
            if (lives < Finals.MAX_LIFE) {
                lives++;
            } else {
                playerScore += Finals.EGG_BONUS;
            }
            egg.setY(Finals.OUT_OF_BOUNDS);
            egg.setX(Finals.OUT_OF_BOUNDS);
            vibrate();
            if (hitAnEggSound != null && hitAnEggSound.isPlaying())
                hitAnEggSound.pause();
            else if (hitAnEggSound != null && !hitAnEggSound.isPlaying())
                hitAnEggSound.start();
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v != null) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            Objects.requireNonNull(v).vibrate(500);
        }
    }

    private void resizeBitmaps(Context context) {
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        heart = Bitmap.createScaledBitmap(heart, screenX / 20, screenX / 20, true);

        // Game over bitmap
        gameOverBitmap = BitmapFactory.decodeResource(context.getResources(), R.raw.gameovergif);
        gameOverBitmap = Bitmap.createScaledBitmap(gameOverBitmap, screenX, screenY / 2, true);
    }

    private void deviceTiltManager() {
        if (isTilt) {
            Sensey.getInstance().init(context, Sensey.SAMPLING_PERIOD_GAME);
            paused = false;
            tiltDirectionListener = new TiltDirectionDetector.TiltDirectionListener() {
                @Override
                public void onTiltInAxisX(int direction) {
                    if (direction == TiltDirectionDetector.DIRECTION_CLOCKWISE) {
                        for (Invader e : enemies)
                            e.setSpeed(e.getSpeed() + 0.5f);
                    } else {
                        for (Invader e : enemies)
                            e.setSpeed(e.getSpeed() - 0.3f);
                    }
                }

                @Override
                public void onTiltInAxisY(int direction) {
                    if (direction == TiltDirectionDetector.DIRECTION_CLOCKWISE)
                        player.setMovementState(PlayerShip.LEFT);
                    else
                        player.setMovementState(PlayerShip.RIGHT);
                }

                @Override
                public void onTiltInAxisZ(int direction) {
                    if (direction == TiltDirectionDetector.DIRECTION_CLOCKWISE)
                        player.setMovementState(PlayerShip.LEFT);
                    else
                        player.setMovementState(PlayerShip.RIGHT);
                }
            };
        }
    }

    // Screen divided in half vertically to control movement
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isTilt) {
            // Get pointer index from the event object
            int pointerIndex = motionEvent.getActionIndex();
            // Get masked (not specific to a pointer) action
            int maskedAction = motionEvent.getActionMasked();
            switch (maskedAction) {
                // Player has removed finger from screen
                case MotionEvent.ACTION_UP:
                    player.setMovementState(PlayerShip.STOPPED);
                    break;

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    paused = false;
                    if (motionEvent.getX(pointerIndex) > (float) screenX / 2)
                        player.setMovementState(PlayerShip.RIGHT);
                    else
                        player.setMovementState(PlayerShip.LEFT);
                    break;
            }
        }
        if(isTilt && paused)
            paused=false;
        if (gameOver && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            stopMusic();
            pause();
            Objects.requireNonNull(getActivity()).recreate();
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

    private void updateHighScoreTable() {
        for (int i = Finals.HIGH_SCORE_COUNT - 1; i > 0; i--) {
            if (gameScores.get(i).getScore() < playerScore) {
                gameScores.get(i).setScore(playerScore);
                gameScores.get(i).setNickname(playerNickname);
                gameScores.get(i).setLatitude(latitude);
                gameScores.get(i).setLongitude(longitude);
                break;
            }
        }
        // Sort scores
        Collections.sort(gameScores);

        // Storing the scores through shared Preferences
        SharedPreferences.Editor e = sharedPreferences.edit();

        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            e.putInt(Finals.SCORE + i, gameScores.get(i).getScore());
            e.putString(Finals.NICKNAME + i, gameScores.get(i).getNickname());
            e.putString(Finals.LONGITUDE + i, gameScores.get(i).getLongitude() + "");
            e.putString(Finals.LATITUDE + i, gameScores.get(i).getLatitude() + "");
        }
        e.apply();
    }

    // Increase playerScore every 1 second
    private void activateScore() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (playerScore < Finals.MAX_SCORE) {
                    if (!paused && lives > 0 && playing)
                        playerScore++;
                    handler.postDelayed(this, 1000L);
                    return;
                }
                handler.removeCallbacks(this);
            }
        }, 1000L);
    }

    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Set screenX screenY according to navigation bar
    public void setScreenSize() {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager != null ? windowManager.getDefaultDisplay() : null;
        Point outPoint = new Point();
        // include navigation bar
        Objects.requireNonNull(display).getRealSize(outPoint);
        if (outPoint.y > outPoint.x) {
            screenY = outPoint.y;
            screenX = outPoint.x;
        } else {
            screenY = outPoint.x;
            screenX = outPoint.y;
        }
    }
}