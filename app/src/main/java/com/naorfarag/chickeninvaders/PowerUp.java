package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class PowerUp {
    private float x;
    private float y;
    private float speed;
    private boolean isActive;
    private Rect detectCollision;
    private Finals.PowerUpType type;
    private Bitmap bitmap;
    private int maxX;
    private int maxY;
    private Random rand = new Random();
    
    public PowerUp(Context context, int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        
        // Randomly select power-up type
        Finals.PowerUpType[] types = Finals.PowerUpType.values();
        type = types[rand.nextInt(types.length)];
        
        // Load appropriate bitmap based on type (using egg as base for now)
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.egg);
        // Scale bitmap appropriately
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
        
        speed = rand.nextInt(3) + 2; // Slower than enemies
        y = -bitmap.getHeight();
        isActive = false;
        detectCollision = new Rect();
        
        spawn();
    }
    
    public void spawn() {
        // Randomly decide whether to spawn (15% chance)
        if (rand.nextInt(100) < 15) {
            x = rand.nextInt(maxX - bitmap.getWidth());
            y = -bitmap.getHeight();
            isActive = true;
        } else {
            isActive = false;
            x = Finals.OUT_OF_BOUNDS;
        }
    }
    
    public void update(int progressSpeed) {
        if (!isActive) return;
        
        y += progressSpeed;
        y += speed;
        
        // Respawn if goes off screen
        if (y > maxY) {
            spawn();
        }
        
        // Update collision rectangle
        if (isActive) {
            detectCollision.left = (int) x;
            detectCollision.top = (int) y;
            detectCollision.right = (int) x + bitmap.getWidth();
            detectCollision.bottom = (int) y + bitmap.getHeight();
        }
    }
    
    public void collect() {
        isActive = false;
        x = Finals.OUT_OF_BOUNDS;
        y = Finals.OUT_OF_BOUNDS;
    }
    
    public String getDescription() {
        switch (type) {
            case RAPID_FIRE:
                return "Rapid Fire!";
            case SHIELD:
                return "Shield Activated!";
            case DOUBLE_SCORE:
                return "Double Score!";
            case SPREAD_SHOT:
                return "Spread Shot!";
            case EXTRA_LIFE:
                return "+1 Life!";
            default:
                return "Power Up!";
        }
    }
    
    public int getDuration() {
        switch (type) {
            case RAPID_FIRE:
                return Finals.RAPID_FIRE_DURATION;
            case SHIELD:
                return Finals.SHIELD_DURATION;
            case DOUBLE_SCORE:
                return Finals.DOUBLE_SCORE_DURATION;
            case SPREAD_SHOT:
                return Finals.SPREAD_SHOT_DURATION;
            case EXTRA_LIFE:
                return 0; // Instant effect
            default:
                return 0;
        }
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
    public Rect getDetectCollision() { return detectCollision; }
    public Finals.PowerUpType getType() { return type; }
    public Bitmap getBitmap() { return bitmap; }
    
    // Setters
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setActive(boolean active) { isActive = active; }
}