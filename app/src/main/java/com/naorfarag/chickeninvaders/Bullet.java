package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Bullet {
    private float x;
    private float y;
    private float speed;
    private boolean isActive;
    private Rect detectCollision;
    private int damage;
    private static Bitmap bulletBitmap;
    private static boolean bitmapLoaded = false;
    
    public Bullet(Context context) {
        // Load bitmap only once for performance optimization
        if (!bitmapLoaded) {
            bulletBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship);
            // Create a small bullet bitmap from spaceship bitmap (temporary solution)
            bulletBitmap = Bitmap.createScaledBitmap(bulletBitmap, 8, 16, true);
            bitmapLoaded = true;
        }
        
        speed = Finals.BULLET_SPEED;
        damage = Finals.BULLET_DAMAGE;
        isActive = false;
        detectCollision = new Rect();
    }
    
    public void fire(float startX, float startY) {
        x = startX;
        y = startY;
        isActive = true;
    }
    
    public void update(long fps) {
        if (!isActive) return;
        
        // Move bullet upward
        if (fps > 0) {
            y -= speed / fps;
        }
        
        // Deactivate if bullet goes off screen
        if (y < -bulletBitmap.getHeight()) {
            isActive = false;
        }
        
        // Update collision rectangle
        if (isActive) {
            detectCollision.left = (int) x;
            detectCollision.top = (int) y;
            detectCollision.right = (int) x + bulletBitmap.getWidth();
            detectCollision.bottom = (int) y + bulletBitmap.getHeight();
        }
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
    public Rect getDetectCollision() { return detectCollision; }
    public int getDamage() { return damage; }
    public Bitmap getBitmap() { return bulletBitmap; }
    
    // Setters
    public void setActive(boolean active) { isActive = active; }
    public void setDamage(int damage) { this.damage = damage; }
    public void setSpeed(float speed) { this.speed = speed; }
}