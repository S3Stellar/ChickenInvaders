package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;


public class Invader {

    private Bitmap bitmap;
    private float x;
    private float y;

    private float speed;

    private int maxX;
    private int maxY;

    private Rect detectCollision;

    private Random rand = new Random();
    private TypedArray chickenImages;
    private Context context;
    private int xCoordinate;
    
    // Health and scoring
    private int health;
    private int maxHealth;
    private int scoreValue;
    private boolean isDestroyed;

    public Invader(Context context, int screenX, int screenY, int xCoordinate) {
        this.context = context;
        this.xCoordinate = xCoordinate;
        chickenImages = context.getResources().obtainTypedArray(R.array.chickenimages);
        changeChickenImage();
        maxX = screenX;
        maxY = screenY;

        speed = rand.nextInt(5) + 8;
        y = -bitmap.getHeight();
        
        // Initialize health and scoring
        health = Finals.ENEMY_HEALTH;
        maxHealth = Finals.ENEMY_HEALTH;
        scoreValue = 10; // Base score value
        isDestroyed = false;

        // Randomly show or not on lane
        raffleInvaderShow();

        // Initialize rect object
        detectCollision = new Rect((int) x, (int) y, bitmap.getWidth(), bitmap.getHeight());
    }


    private void changeChickenImage() {
        final int rndInt = rand.nextInt(chickenImages.length());
        final int resID = chickenImages.getResourceId(rndInt, 0);
        bitmap = BitmapFactory.decodeResource(context.getResources(), resID);
    }

    public void update(int progressSpeed) {
        if (isDestroyed) return;
        
        y += progressSpeed;
        y += speed;
        if (y > maxY - bitmap.getHeight()) {
            respawn();
        }

        // Adding the top, left, bottom and right to the rect object
        detectCollision.left = (int) x;
        detectCollision.top = (int) y + bitmap.getHeight() / 4;
        detectCollision.right = (int) x + bitmap.getWidth();
        detectCollision.bottom = (int) y + bitmap.getHeight() / 2;
    }


    private void raffleInvaderShow() {
        if (rand.nextInt(5) > 2) {
            x = Finals.OUT_OF_BOUNDS;
        } else {
            x = xCoordinate;
        }
    }

    // One more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    // Getters
    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if (speed < rand.nextInt(5) + 8)
            this.speed = rand.nextInt(5) + 8;
        else
            this.speed = speed;
    }
    
    // Damage and health methods
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            destroy();
        }
    }
    
    public void destroy() {
        isDestroyed = true;
        x = Finals.OUT_OF_BOUNDS;
        y = Finals.OUT_OF_BOUNDS;
    }
    
    public void respawn() {
        health = maxHealth;
        isDestroyed = false;
        changeChickenImage();
        speed = rand.nextInt(10) + 10;
        y = -bitmap.getHeight();
        raffleInvaderShow();
    }
    
    // Getters for new properties
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getScoreValue() { return scoreValue; }
    public boolean isDestroyed() { return isDestroyed; }
    
    // Setters
    public void setHealth(int health) { this.health = health; }
    public void setScoreValue(int value) { this.scoreValue = value; }
}