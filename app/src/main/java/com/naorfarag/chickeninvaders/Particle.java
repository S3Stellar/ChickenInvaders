package com.naorfarag.chickeninvaders;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Particle {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private int color;
    private float size;
    private long lifeTime;
    private long maxLifeTime;
    private boolean isActive;
    private Random rand = new Random();
    
    public Particle() {
        maxLifeTime = Finals.PARTICLE_LIFE;
        isActive = false;
    }
    
    public void spawn(float startX, float startY, int particleColor) {
        x = startX;
        y = startY;
        
        // Random velocity for explosion effect
        velocityX = (rand.nextFloat() - 0.5f) * 400; // Random direction
        velocityY = (rand.nextFloat() - 0.5f) * 400;
        
        color = particleColor;
        size = rand.nextFloat() * 8 + 2; // Random size between 2-10
        lifeTime = maxLifeTime;
        isActive = true;
    }
    
    public void update(long fps) {
        if (!isActive) return;
        
        if (fps > 0) {
            // Update position
            x += velocityX / fps;
            y += velocityY / fps;
            
            // Apply gravity
            velocityY += 200 / fps;
            
            // Reduce life time
            lifeTime -= 1000 / fps;
            
            // Fade particle as it ages
            float alpha = (float) lifeTime / maxLifeTime;
            size *= 0.995f; // Shrink over time
            
            // Deactivate if lifetime is over or size too small
            if (lifeTime <= 0 || size < 0.5f) {
                isActive = false;
            }
        }
    }
    
    public void draw(android.graphics.Canvas canvas, Paint paint) {
        if (!isActive) return;
        
        // Calculate alpha based on remaining life
        float alpha = (float) lifeTime / maxLifeTime;
        
        // Set color with alpha
        paint.setColor(Color.argb((int)(alpha * 255), 
                                  Color.red(color), 
                                  Color.green(color), 
                                  Color.blue(color)));
        
        // Draw particle as a circle
        canvas.drawCircle(x, y, size, paint);
    }
    
    // Static method to create explosion particles
    public static void createExplosion(Particle[] particles, float x, float y, int color, int count) {
        int created = 0;
        for (int i = 0; i < particles.length && created < count; i++) {
            if (!particles[i].isActive()) {
                particles[i].spawn(x, y, color);
                created++;
            }
        }
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return isActive; }
    public float getSize() { return size; }
    
    // Setters
    public void setActive(boolean active) { isActive = active; }
}