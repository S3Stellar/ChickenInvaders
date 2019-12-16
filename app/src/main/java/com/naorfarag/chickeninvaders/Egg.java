package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

public class Egg {
    private Bitmap bitmap;
    private int x;
    private int y;
    private int speed;

    private int maxX;
    private int maxY;


    //creating a rect object for a friendly ship
    private Rect detectCollision;
    private Random rand = new Random();

    public Egg(Context context, int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.egg);
        speed = rand.nextInt(5) + 10;
        y = -bitmap.getHeight();
        raffleEggShow();

        // Initialize rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }


    public void update(int progressSpeed) {
        y += progressSpeed;
        y += speed;
        if (y > maxY - bitmap.getHeight()) {
            speed = rand.nextInt(10) + 10;
            y = -bitmap.getHeight();

            // Randomly show or not on lane
            raffleEggShow();
        }

        // Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y + bitmap.getHeight() / 4;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight() / 2;
    }

    private void raffleEggShow() {
        // Randomly show or not on lane
        if (rand.nextInt(100) > 85) {
            x = rand.nextInt(maxX - bitmap.getWidth()) + bitmap.getWidth() / 10;
        } else {
            x = -bitmap.getWidth();
        }
    }

    // One more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    // Getters
    public android.graphics.Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
}
