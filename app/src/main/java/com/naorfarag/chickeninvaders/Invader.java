package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;


public class Invader {

    private Bitmap bitmap;
    private int x;
    private int y;
    private int speed;

    private int maxX;
    private int maxY;


    //creating a rect object for a friendly ship
    private Rect detectCollision;

    private Random rand = new Random();
    private TypedArray chickenImages;
    private Context context;

    public Invader(Context context, int screenX, int screenY) {
        this.context = context;
        chickenImages = context.getResources().obtainTypedArray(R.array.chickenimages);
        changeChickenImage();
        maxX = screenX;
        maxY = screenY;

        Random generator = new Random();
        speed = generator.nextInt(5) + 8;
        y =0;
        x = rand.nextInt(maxX-bitmap.getWidth()) + bitmap.getWidth()/10;

        // Initialize rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    private void changeChickenImage() {
        final int rndInt = rand.nextInt(chickenImages.length());
        final int resID = chickenImages.getResourceId(rndInt, 0);
        bitmap = BitmapFactory.decodeResource(context.getResources(), resID);
    }

    public void update(int progressSpeed) {
        y += progressSpeed;
        y += speed;
        if (y > maxY - bitmap.getHeight()) {
            changeChickenImage();
            speed = rand.nextInt(10) + 10;
            y = 0;
            x = rand.nextInt(maxX-bitmap.getWidth()) + bitmap.getWidth()/10;
        }

        // Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y+bitmap.getHeight()/4;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight()/2;
    }


    // One more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    // Getters
    public Bitmap getBitmap() {
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
}