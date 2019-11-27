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
    private int minY;
    private int maxX;
    private int minX;

    // This will hold the pixels per second that the spaceship will move
    private int shipSpeed;

    // Which ways can the paddle move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = STOPPED;

    private Rect detectCollision;

    public PlayerShip(Context context, int screenX, int screenY) {
        x = screenX / 2 - 100;
        y = screenY - 400;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.spaceship);

        maxY = screenY - bitmap.getHeight();
        minY = 0;
        maxX = screenX - bitmap.getWidth();
        minX = 0;

        // Initializing rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
        // How fast is the spaceship in pixels per second
        shipSpeed = 1000;
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


        //adding top, left, bottom and right to the rect object
        detectCollision.left = x + 40;
        detectCollision.top = y + 50;
        detectCollision.right = x + bitmap.getWidth() - 40;
        detectCollision.bottom = y + bitmap.getHeight() - 70;

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
}