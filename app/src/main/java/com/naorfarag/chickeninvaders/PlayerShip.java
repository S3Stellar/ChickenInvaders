package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class PlayerShip {
    private Bitmap bitmap;
    private int x;
    private int y;

    private int maxY;
    private int maxX;
    private int minX;

    // This will hold the pixels per second that the spaceship will move
    private int shipSpeed;

    // Which ways can the paddle move
    public static final int STOPPED = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = STOPPED;

    private Rect detectCollision;
    
    // Shooting capabilities
    private long lastShotTime;
    private int fireRate; // Time between shots in milliseconds
    private Finals.WeaponType weaponType;
    private boolean hasShield;
    private long shieldEndTime;

    public PlayerShip(Context context, int screenX, int screenY) {

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship);
        x = screenX / 2 - bitmap.getWidth() / 2;
        y = screenY - bitmap.getHeight() * 2;

        maxY = screenY - bitmap.getHeight();
        maxX = screenX - bitmap.getWidth();
        minX = 0;

        // Initializing rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
        // How fast is the spaceship in pixels per second
        shipSpeed = Finals.SHIP_SPEED;
        
        // Initialize shooting capabilities
        lastShotTime = 0;
        fireRate = 500; // Default 500ms between shots
        weaponType = Finals.WeaponType.NORMAL;
        hasShield = false;
        shieldEndTime = 0;
    }

    // This method will be used to change/set if the paddle is going left, right or nowhere
    public void setMovementState(int state) {
        shipMoving = state;
    }

    public void update(long fps) {
        if (shipMoving == LEFT)
            x = (int) (x - shipSpeed / fps);

        if (shipMoving == RIGHT)
            x = (int) (x + shipSpeed / fps);

        if (y > maxY) {
            y = maxY;
        }
        if (x > maxX) {
            x = maxX;
        }
        if (x < minX) {
            x = minX;
        }

        // Adding top, left, bottom and right to the rect object
        detectCollision.left = x + 26;
        detectCollision.top = y + 50;
        detectCollision.right = x + bitmap.getWidth() - 26;
        detectCollision.bottom = y + bitmap.getHeight() - 70;
        
        // Update power-ups
        updatePowerUps();
    }

    //one more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    // Shooting methods
    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime > fireRate;
    }
    
    public void shoot() {
        lastShotTime = System.currentTimeMillis();
    }
    
    public float[] getBulletStartPosition() {
        return new float[]{x + bitmap.getWidth() / 2f, y};
    }
    
    public float[][] getSpreadShotPositions() {
        float[][] positions = new float[3][2];
        // Center
        positions[0][0] = x + bitmap.getWidth() / 2f;
        positions[0][1] = y;
        // Left
        positions[1][0] = x + bitmap.getWidth() / 4f;
        positions[1][1] = y;
        // Right
        positions[2][0] = x + 3 * bitmap.getWidth() / 4f;
        positions[2][1] = y;
        return positions;
    }
    
    // Power-up methods
    public void activateRapidFire() {
        weaponType = Finals.WeaponType.RAPID;
        fireRate = 150; // Much faster shooting
    }
    
    public void activateSpreadShot() {
        weaponType = Finals.WeaponType.SPREAD;
        fireRate = 300;
    }
    
    public void activateShield(long duration) {
        hasShield = true;
        shieldEndTime = System.currentTimeMillis() + duration;
    }
    
    public void resetWeapon() {
        weaponType = Finals.WeaponType.NORMAL;
        fireRate = 500;
    }
    
    public void updatePowerUps() {
        // Check if shield expired
        if (hasShield && System.currentTimeMillis() > shieldEndTime) {
            hasShield = false;
        }
    }
    
    // Getters for new properties
    public Finals.WeaponType getWeaponType() { return weaponType; }
    public boolean hasShield() { return hasShield; }
    public int getFireRate() { return fireRate; }
    
    // Setters
    public void setWeaponType(Finals.WeaponType type) { weaponType = type; }
    public void setFireRate(int rate) { fireRate = rate; }
}