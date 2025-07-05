package com.naorfarag.chickeninvaders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.TiltDirectionDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    
    // New game objects for enhanced features
    private Bullet[] bullets;
    private PowerUp powerUp;
    private Particle[] particles;
    private BossEnemy boss;
    
    // Shooting mechanics
    private boolean autoShoot = false;
    
    // Power-up system
    private boolean rapidFireActive = false;
    private boolean doubleScoreActive = false;
    private boolean spreadShotActive = false;
    private long rapidFireEndTime = 0;
    private long doubleScoreEndTime = 0;
    private long spreadShotEndTime = 0;
    
    // Combo system
    private int comboCount = 0;
    private long lastKillTime = 0;
    private int comboMultiplier = 1;
    
    // Achievement tracking
    private int enemiesKilled = 0;
    private boolean firstKillAchieved = false;
    private boolean combo5Achieved = false;
    private boolean score100Achieved = false;
    private boolean bossDefeatedAchieved = false;

    // The playerScore
    private int playerScore;

    // Lives
    private int lives = Finals.MAX_LIFE;

    // The high Scores Holder
    private ArrayList<PlayerAttributes> gameScores = new ArrayList<>();

    // Firebase database
    private DatabaseReference dbref;

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
        resizeBitmaps();

        // Init game objects
        initializeGame();

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

    private void initializeGame() {
        // Initializing
        dbref = FirebaseDatabase.getInstance().getReference();
        updateDataBase();

        player = new PlayerShip(context, screenX, screenY);
        surfaceHolder = getHolder();
        boom = new Boom(context);
        paint = new Paint();
        egg = new Egg(context, screenX, screenY);
        enemies = new Invader[enemyCount];

        // Initialize new game objects
        bullets = new Bullet[Finals.MAX_BULLETS];
        for (int i = 0; i < Finals.MAX_BULLETS; i++) {
            bullets[i] = new Bullet(context);
        }
        
        powerUp = new PowerUp(context, screenX, screenY);
        
        particles = new Particle[Finals.MAX_PARTICLES];
        for (int i = 0; i < Finals.MAX_PARTICLES; i++) {
            particles[i] = new Particle();
        }
        
        boss = new BossEnemy(context, screenX, screenY);

        // Initialize stars for background effect
        // Number of stars
        for (int i = 0; i < Finals.STARS_COUNT; i++)
            stars.add(new Star(screenX, screenY));

        // Initialize enemies
        for (int i = 0; i < enemyCount; i++) {
            enemies[i] = new Invader(context, screenX, screenY, screenX - (i + 1) * screenX / enemyCount + screenX / (enemyCount * 2) - getResources().getDrawable(R.drawable.chicken1).getIntrinsicWidth() / 2);
        }
    }

    public void updateDataBase() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude;
                double latitude;
                int score;
                String nickname = "";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        nickname = ds.child(Finals.NICKNAME).getValue(String.class);
                        score = ds.child(Finals.SCORE).getValue(Integer.class);
                        longitude = ds.child(Finals.LONGITUDE).getValue(Double.class);
                        latitude = ds.child(Finals.LATITUDE).getValue(Double.class);
                    } catch (Exception e) {
                        longitude = 0;
                        latitude = 0;
                        score = 0;
                    }
                    gameScores.add(new PlayerAttributes(score, nickname, longitude, latitude));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        dbref.addListenerForSingleValueEvent(valueEventListener);
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

        // Update bullets
        for (Bullet bullet : bullets) {
            bullet.update(fps);
        }
        
        // Update particles
        for (Particle particle : particles) {
            particle.update(fps);
        }
        
        // Update power-up
        powerUp.update(rand.nextInt(4) + 1 + playerScore / 7);
        
        // Update boss if active
        if (boss.shouldSpawn(playerScore)) {
            boss.spawn();
        }
        boss.update(fps);
        
        // Boss attacks
        if (boss.isActive() && boss.canAttack()) {
            boss.attack();
            // Create enemy bullets (using existing egg as projectile for now)
            // This could be expanded with dedicated enemy bullets
        }

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

        // Auto-shoot if enabled
        if (autoShoot && player.canShoot()) {
            shootBullet();
        }
        
        // Update power-ups
        updatePowerUps();
        
        // Update combo system
        updateComboSystem();

        // Check all collisions
        checkEggIntersect();
        checkPowerUpIntersect();
        checkInvaderIntersect();
        checkBulletCollisions();
        checkBossCollisions();

        if (lives <= 0) {
            gameOver = true;
            if (gameOverSound != null && !gameOverSound.isPlaying()) {
                gameOverSound.start();
            }
        }
        
        // Check achievements
        checkAchievements();
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
            
            // Draw particles first (behind everything)
            for (Particle particle : particles) {
                particle.draw(canvas, paint);
            }
            
            // Draw player with shield effect
            if (player.hasShield()) {
                // Draw shield as a blue circle around player
                paint.setColor(Color.argb(100, 0, 150, 255));
                canvas.drawCircle(player.getX() + player.getBitmap().getWidth() / 2f, 
                                player.getY() + player.getBitmap().getHeight() / 2f, 
                                player.getBitmap().getWidth() / 2f + 10, paint);
                paint.setColor(Color.WHITE);
            }
            canvas.drawBitmap(player.getBitmap(), player.getX(), player.getY(), paint);

            // Draw bullets
            paint.setColor(Color.YELLOW);
            for (Bullet bullet : bullets) {
                if (bullet.isActive()) {
                    canvas.drawBitmap(bullet.getBitmap(), bullet.getX(), bullet.getY(), paint);
                }
            }
            paint.setColor(Color.WHITE);

            // Draw hearts
            for (int i = 0; i < lives; i++)
                canvas.drawBitmap(heart, screenX - (heart.getWidth() + 10) * (i + 1), 15, paint);

            // Draw enhanced UI
            drawEnhancedUI(canvas);

            // Draw enemies
            for (int i = 0; i < enemyCount; i++) {
                if (!enemies[i].isDestroyed()) {
                    canvas.drawBitmap(enemies[i].getBitmap(), enemies[i].getX(), enemies[i].getY(), paint);
                }
            }
            
            // Draw boss
            if (boss.isActive()) {
                canvas.drawBitmap(boss.getBitmap(), boss.getX(), boss.getY(), paint);
                
                // Draw boss health bar
                drawBossHealthBar(canvas);
            }

            // Drawing boom image
            canvas.drawBitmap(boom.getBitmap(), boom.getX(), boom.getY(), paint);

            // Draw egg (gives 1 life if below 3, else 10Points)
            canvas.drawBitmap(egg.getBitmap(), egg.getX(), egg.getY(), paint);
            
            // Draw power-up
            if (powerUp.isActive()) {
                // Draw power-up with glow effect
                paint.setColor(Color.argb(150, 255, 255, 0));
                canvas.drawCircle(powerUp.getX() + powerUp.getBitmap().getWidth() / 2f, 
                                powerUp.getY() + powerUp.getBitmap().getHeight() / 2f, 
                                powerUp.getBitmap().getWidth() / 2f + 5, paint);
                paint.setColor(Color.WHITE);
                canvas.drawBitmap(powerUp.getBitmap(), powerUp.getX(), powerUp.getY(), paint);
            }

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
    
    private void drawEnhancedUI(Canvas canvas) {
        // Reset paint properties
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT);
        
        // Draw score with combo multiplier
        paint.setColor(Color.argb(255, 249, 129, 0));
        paint.setTextSize(40);
        String scoreText = "Score: " + playerScore;
        if (comboMultiplier > 1) {
            scoreText += " (x" + comboMultiplier + ")";
        }
        canvas.drawText(scoreText, 10, 50, paint);
        
        // Draw combo counter
        if (comboCount > 0) {
            paint.setColor(Color.argb(255, 255, 50, 50));
            paint.setTextSize(30);
            canvas.drawText("Combo: " + comboCount, 10, 90, paint);
        }
        
        // Draw enemies killed
        paint.setColor(Color.argb(255, 100, 255, 100));
        paint.setTextSize(25);
        canvas.drawText("Kills: " + enemiesKilled, 10, 130, paint);
        
        // Draw active power-ups
        int powerUpY = 170;
        paint.setTextSize(20);
        
        if (rapidFireActive) {
            paint.setColor(Color.RED);
            canvas.drawText("RAPID FIRE", 10, powerUpY);
            powerUpY += 25;
        }
        
        if (doubleScoreActive) {
            paint.setColor(Color.GOLD);
            canvas.drawText("DOUBLE SCORE", 10, powerUpY);
            powerUpY += 25;
        }
        
        if (spreadShotActive) {
            paint.setColor(Color.CYAN);
            canvas.drawText("SPREAD SHOT", 10, powerUpY);
            powerUpY += 25;
        }
        
        if (player.hasShield()) {
            paint.setColor(Color.BLUE);
            canvas.drawText("SHIELD", 10, powerUpY);
        }
        
        // Draw FPS counter (for performance monitoring)
        paint.setColor(Color.WHITE);
        paint.setTextSize(15);
        canvas.drawText("FPS: " + fps, screenX - 100, 30, paint);
    }
    
    private void drawBossHealthBar(Canvas canvas) {
        if (!boss.isActive()) return;
        
        int barWidth = screenX - 40;
        int barHeight = 20;
        int barX = 20;
        int barY = 100;
        
        // Background bar
        paint.setColor(Color.RED);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        
        // Health bar
        paint.setColor(Color.GREEN);
        float healthPercent = boss.getHealthPercentage();
        canvas.drawRect(barX, barY, barX + (barWidth * healthPercent), barY + barHeight, paint);
        
        // Border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Boss label
        paint.setTextSize(25);
        paint.setColor(Color.YELLOW);
        canvas.drawText("BOSS", barX, barY - 5, paint);
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
            // If collision occurs with player and no shield active
            if (!player.hasShield() && !enemies[i].isDestroyed() && 
                Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {

                if (killedEnemySound != null && killedEnemySound.isPlaying())
                    killedEnemySound.pause();
                else if (killedEnemySound != null && !killedEnemySound.isPlaying())
                    killedEnemySound.start();

                vibrate();
                lives--;
                
                // Create explosion particles
                Particle.createExplosion(particles, enemies[i].getX() + enemies[i].getBitmap().getWidth() / 2f, 
                                        enemies[i].getY() + enemies[i].getBitmap().getHeight() / 2f, 
                                        android.graphics.Color.RED, 10);
                
                // Displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());
                enemies[i].destroy();
                enemies[i].respawn();
            }
        }
    }

    private void checkEggIntersect() {
        if (Rect.intersects(player.getDetectCollision(), egg.getDetectCollision())) {
            // If lives less than 3 -> add 1 life, else +10 points
            if (lives < Finals.MAX_LIFE) {
                lives++;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(context, "+1 Life", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 50);
                        toast.show();
                    }
                });
            } else {
                playerScore += Finals.EGG_BONUS;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(context, "+" + Finals.EGG_BONUS + " Bonus points", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 50);
                        toast.show();
                    }
                });
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

    private void resizeBitmaps() {
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

    // Enhanced touch controls for movement and shooting
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
                    autoShoot = false;
                    break;

                // Player has touched the screen
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    paused = false;
                    
                    // Movement based on horizontal position
                    if (motionEvent.getX(pointerIndex) > (float) screenX / 2)
                        player.setMovementState(PlayerShip.RIGHT);
                    else
                        player.setMovementState(PlayerShip.LEFT);
                    
                    // Enable auto-shooting
                    autoShoot = true;
                    
                    // Immediate shot
                    shootBullet();
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    // Continue movement and shooting during drag
                    if (motionEvent.getX(pointerIndex) > (float) screenX / 2)
                        player.setMovementState(PlayerShip.RIGHT);
                    else
                        player.setMovementState(PlayerShip.LEFT);
                    break;
            }
        }
        if (isTilt && paused) {
            paused = false;
            autoShoot = true; // Enable auto-shoot in tilt mode too
        }
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

        for (int i = 0; i < Finals.HIGH_SCORE_COUNT; i++) {
            dbref.child(Finals.USER + i).child(Finals.NICKNAME).setValue(gameScores.get(i).getNickname());
            dbref.child(Finals.USER + i).child(Finals.SCORE).setValue(gameScores.get(i).getScore());
            dbref.child(Finals.USER + i).child(Finals.LONGITUDE).setValue(gameScores.get(i).getLongitude());
            dbref.child(Finals.USER + i).child(Finals.LATITUDE).setValue(gameScores.get(i).getLatitude());
        }
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
    
    // New shooting mechanics
    private void shootBullet() {
        if (!player.canShoot()) return;
        
        player.shoot();
        
        if (player.getWeaponType() == Finals.WeaponType.SPREAD || spreadShotActive) {
            // Spread shot - fire 3 bullets
            float[][] positions = player.getSpreadShotPositions();
            int bulletsCreated = 0;
            for (int i = 0; i < bullets.length && bulletsCreated < 3; i++) {
                if (!bullets[i].isActive()) {
                    bullets[i].fire(positions[bulletsCreated][0], positions[bulletsCreated][1]);
                    bulletsCreated++;
                }
            }
        } else {
            // Normal shot
            float[] pos = player.getBulletStartPosition();
            for (Bullet bullet : bullets) {
                if (!bullet.isActive()) {
                    bullet.fire(pos[0], pos[1]);
                    break;
                }
            }
        }
        
        // Play shooting sound
        if (hitAnEggSound != null && !hitAnEggSound.isPlaying()) {
            hitAnEggSound.start();
        }
    }
    
    // Power-up system
    private void updatePowerUps() {
        long currentTime = System.currentTimeMillis();
        
        // Check rapid fire
        if (rapidFireActive && currentTime > rapidFireEndTime) {
            rapidFireActive = false;
            player.resetWeapon();
        }
        
        // Check double score
        if (doubleScoreActive && currentTime > doubleScoreEndTime) {
            doubleScoreActive = false;
        }
        
        // Check spread shot
        if (spreadShotActive && currentTime > spreadShotEndTime) {
            spreadShotActive = false;
        }
    }
    
    private void checkPowerUpIntersect() {
        if (powerUp.isActive() && Rect.intersects(player.getDetectCollision(), powerUp.getDetectCollision())) {
            collectPowerUp(powerUp.getType());
            powerUp.collect();
            
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(context, powerUp.getDescription(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 100);
                    toast.show();
                }
            });
            
            vibrate();
        }
    }
    
    private void collectPowerUp(Finals.PowerUpType type) {
        long currentTime = System.currentTimeMillis();
        
        switch (type) {
            case RAPID_FIRE:
                rapidFireActive = true;
                rapidFireEndTime = currentTime + Finals.RAPID_FIRE_DURATION;
                player.activateRapidFire();
                break;
            case SHIELD:
                player.activateShield(Finals.SHIELD_DURATION);
                break;
            case DOUBLE_SCORE:
                doubleScoreActive = true;
                doubleScoreEndTime = currentTime + Finals.DOUBLE_SCORE_DURATION;
                break;
            case SPREAD_SHOT:
                spreadShotActive = true;
                spreadShotEndTime = currentTime + Finals.SPREAD_SHOT_DURATION;
                player.activateSpreadShot();
                break;
            case EXTRA_LIFE:
                if (lives < Finals.MAX_LIFE) {
                    lives++;
                }
                break;
        }
    }
    
    // Collision detection for bullets
    private void checkBulletCollisions() {
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) continue;
            
            // Check bullet vs enemies
            for (Invader enemy : enemies) {
                if (!enemy.isDestroyed() && Rect.intersects(bullet.getDetectCollision(), enemy.getDetectCollision())) {
                    // Hit enemy
                    enemy.takeDamage(bullet.getDamage());
                    bullet.setActive(false);
                    
                    if (enemy.isDestroyed()) {
                        // Enemy destroyed
                        addScore(enemy.getScoreValue());
                        enemiesKilled++;
                        updateCombo();
                        
                        // Create explosion particles
                        Particle.createExplosion(particles, enemy.getX() + enemy.getBitmap().getWidth() / 2f, 
                                                enemy.getY() + enemy.getBitmap().getHeight() / 2f, 
                                                android.graphics.Color.ORANGE, 8);
                        
                        // Play sound
                        if (killedEnemySound != null && !killedEnemySound.isPlaying()) {
                            killedEnemySound.start();
                        }
                        
                        enemy.respawn();
                    }
                    break;
                }
            }
        }
    }
    
    // Boss collision detection
    private void checkBossCollisions() {
        if (!boss.isActive()) return;
        
        // Check bullets vs boss
        for (Bullet bullet : bullets) {
            if (bullet.isActive() && Rect.intersects(bullet.getDetectCollision(), boss.getDetectCollision())) {
                boss.takeDamage(bullet.getDamage());
                bullet.setActive(false);
                
                // Create hit particles
                Particle.createExplosion(particles, bullet.getX(), bullet.getY(), 
                                        android.graphics.Color.RED, 5);
                
                if (!boss.isActive()) {
                    // Boss defeated
                    addScore(50); // Big score bonus
                    bossDefeatedAchieved = true;
                    
                    // Big explosion
                    Particle.createExplosion(particles, boss.getX() + boss.getBitmap().getWidth() / 2f, 
                                            boss.getY() + boss.getBitmap().getHeight() / 2f, 
                                            android.graphics.Color.YELLOW, 15);
                    
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(context, "BOSS DEFEATED! +50 Points!", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
                }
                break;
            }
        }
        
        // Check boss vs player (if no shield)
        if (!player.hasShield() && Rect.intersects(player.getDetectCollision(), boss.getDetectCollision())) {
            lives--;
            vibrate();
            
            // Create explosion at player position
            Particle.createExplosion(particles, player.getX() + player.getBitmap().getWidth() / 2f, 
                                    player.getY() + player.getBitmap().getHeight() / 2f, 
                                    android.graphics.Color.RED, 10);
            
            // Temporarily move boss away to prevent multiple hits
            boss.setActive(false);
        }
    }
    
    // Combo system
    private void updateCombo() {
        long currentTime = System.currentTimeMillis();
        lastKillTime = currentTime;
        comboCount++;
        
        // Update multiplier
        comboMultiplier = Math.min(1 + comboCount / 5, Finals.COMBO_MULTIPLIER_MAX);
    }
    
    private void updateComboSystem() {
        long currentTime = System.currentTimeMillis();
        
        // Reset combo if timeout
        if (currentTime - lastKillTime > Finals.COMBO_TIMEOUT) {
            comboCount = 0;
            comboMultiplier = 1;
        }
    }
    
    private void addScore(int baseScore) {
        int actualScore = baseScore * comboMultiplier;
        if (doubleScoreActive) {
            actualScore *= 2;
        }
        playerScore += actualScore;
    }
    
    // Achievement system
    private void checkAchievements() {
        if (!firstKillAchieved && enemiesKilled >= Finals.ACHIEVEMENT_FIRST_KILL) {
            firstKillAchieved = true;
            showAchievement("First Kill!");
        }
        
        if (!combo5Achieved && comboCount >= Finals.ACHIEVEMENT_COMBO_5) {
            combo5Achieved = true;
            showAchievement("Combo Master!");
        }
        
        if (!score100Achieved && playerScore >= Finals.ACHIEVEMENT_SCORE_100) {
            score100Achieved = true;
            showAchievement("Century Score!");
        }
    }
    
    private void showAchievement(final String achievement) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, "ACHIEVEMENT: " + achievement, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, -200);
                toast.show();
            }
        });
    }
}