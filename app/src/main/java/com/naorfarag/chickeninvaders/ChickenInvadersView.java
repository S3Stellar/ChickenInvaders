package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

public class ChickenInvadersView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private PlayerShip player;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Invader[] enemies;

    // The score
    private int score = 0;

    // Lives
    private int lives = 3;

    // Game is paused at the start
    private boolean paused = true;

    // This variable tracks the game frame rate
    private long fps;
    // This is used to help calculate the fps
    private long timeThisFrame;

    private int enemyCount = 3;
    private int screenY;
    private int screenX;

    private ArrayList<Star> stars = new
            ArrayList<Star>();
    private Bitmap heart;
    private ImageView img;

    //defining a boom object to display blast
    private Boom boom;

    private Bitmap bg;
    Bitmap bgResized;

    public ChickenInvadersView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
        player = new PlayerShip(context, screenX, screenY);
        surfaceHolder = getHolder();
        paint = new Paint();

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }

        enemies = new Invader[enemyCount];
        for (int i = 0; i < enemyCount; i++) {
            enemies[i] = new Invader(context, screenX, screenY);
        }

        //initializing boom object
        boom = new Boom(context);
        activateScore();

        bg = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gamebackground);
        bgResized = Bitmap.createScaledBitmap(bg, screenX, screenY, true);
    }


    @Override
    public void run() {
        while (playing) {

            for (Star s : stars) {
                s.update(new Random().nextInt(5) + 1+score/10);
            }
            for (Invader e : enemies) {
                e.update(new Random().nextInt(4) + 1+score/10);
            }
            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }
            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1)
                fps = 1000 / timeThisFrame;
            //control(); // might remove
        }
    }


    /*@Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }*/

    private void update() {
        // Move the player's ship
        player.update(fps);

        // Has the player lost
        boolean lost = false;

        //setting boom outside the screen
        boom.setX(-650);
        boom.setY(-650);

        for (int i = 0; i < enemyCount; i++) {
            //if collision occurrs with player
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {
                lives--;
                //displaying boom at that location
                boom.setX(enemies[i].getX());
                boom.setY(enemies[i].getY());

                enemies[i].setY(screenY+enemies[i].getBitmap().getHeight());

            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            // Draw background image
            //canvas.drawBitmap(bgResized,0,0, paint);
            // Draw black background
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
            Bitmap resized = Bitmap.createScaledBitmap(heart, 50, 50, true);

            //draw hearts
            for (int i = 0; i < lives; i++) {
                canvas.drawBitmap(resized, screenX - 55*(i+1), 15, paint);
            }

            // draw score
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score, 10, 50, paint);

            for (int i = 0; i < enemyCount; i++) {
                canvas.drawBitmap(
                        enemies[i].getBitmap(),
                        enemies[i].getX(),
                        enemies[i].getY(),
                        paint
                );
            }

            //drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );

            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }

    /*private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;
                if (motionEvent.getX() > screenX / 2)
                    player.setMovementState(player.RIGHT);
                else
                    player.setMovementState(player.LEFT);
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                player.setMovementState(player.STOPPED);
                break;
        }
        return true;
    }

    private void activateScore() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (score < 9999) {
                    if(!paused&&lives>0)
                        score++;
                    handler.postDelayed(this, 1000L);
                    return;
                }
                handler.removeCallbacks(this);
            }
        }, 1000L);
    }
}
