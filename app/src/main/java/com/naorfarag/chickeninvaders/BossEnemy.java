package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class BossEnemy {
    private float x;
    private float y;
    private float speed;
    private float directionX;
    private int health;
    private int maxHealth;
    private boolean isActive;
    private boolean isMovingRight;
    private Rect detectCollision;
    private Bitmap bitmap;
    private int maxX;
    private int maxY;
    private Random rand = new Random();
    private long lastAttackTime;
    private static final long ATTACK_INTERVAL = 2000; // 2 seconds between attacks
    
    public BossEnemy(Context context, int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        
        // Load and scale boss bitmap (using larger chicken for now)
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.chicken1);
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 3, bitmap.getHeight() * 3, true);
        
        health = Finals.BOSS_HEALTH;
        maxHealth = Finals.BOSS_HEALTH;
        speed = 100; // Slower than regular enemies
        directionX = 1;
        isMovingRight = true;
        isActive = false;
        detectCollision = new Rect();
        lastAttackTime = 0;
        
        spawn();
    }
    
    public void spawn() {
        x = maxX / 2f - bitmap.getWidth() / 2f; // Center horizontally
        y = 50; // Near top of screen
        health = maxHealth;
        isActive = true;
        lastAttackTime = System.currentTimeMillis();
    }
    
    public void update(long fps) {
        if (!isActive) return;
        
        if (fps > 0) {
            // Horizontal movement pattern
            x += (directionX * speed) / fps;
            
            // Bounce off screen edges
            if (x <= 0) {
                x = 0;
                directionX = 1;
                isMovingRight = true;
            } else if (x >= maxX - bitmap.getWidth()) {
                x = maxX - bitmap.getWidth();
                directionX = -1;
                isMovingRight = false;
            }
            
            // Slow vertical descent
            y += 20 / fps;
        }
        
        // Update collision rectangle
        detectCollision.left = (int) x + 20;
        detectCollision.top = (int) y + 20;
        detectCollision.right = (int) x + bitmap.getWidth() - 20;
        detectCollision.bottom = (int) y + bitmap.getHeight() - 20;
    }
    
    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime > ATTACK_INTERVAL;
    }
    
    public void attack() {
        lastAttackTime = System.currentTimeMillis();
        // Boss attack logic will be handled in main game loop
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            defeat();
        }
    }
    
    public void defeat() {
        isActive = false;
        // Reset position off-screen
        x = Finals.OUT_OF_BOUNDS;
        y = Finals.OUT_OF_BOUNDS;
    }
    
    public float getHealthPercentage() {
        return (float) health / maxHealth;
    }
    
    public boolean shouldSpawn(int playerScore) {
        return !isActive && playerScore > 0 && playerScore % Finals.BOSS_SPAWN_SCORE == 0;
    }
    
    // Create multiple attack projectiles
    public float[][] getAttackPositions() {
        float[][] positions = new float[3][2]; // 3 projectiles, x and y coordinates
        
        // Center projectile
        positions[0][0] = x + bitmap.getWidth() / 2f;
        positions[0][1] = y + bitmap.getHeight();
        
        // Left projectile
        positions[1][0] = x + bitmap.getWidth() / 4f;
        positions[1][1] = y + bitmap.getHeight();
        
        // Right projectile
        positions[2][0] = x + 3 * bitmap.getWidth() / 4f;
        positions[2][1] = y + bitmap.getHeight();
        
        return positions;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
    public Rect getDetectCollision() { return detectCollision; }
    public Bitmap getBitmap() { return bitmap; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    
    // Setters
    public void setActive(boolean active) { isActive = active; }
    public void setHealth(int health) { this.health = health; }
}