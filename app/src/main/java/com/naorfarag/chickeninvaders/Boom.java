package com.naorfarag.chickeninvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Boom {

    //bitmap object
    private Bitmap bitmap;

    //coordinate variables
    private float x;
    private float y;

    //constructor
    public Boom(Context context) {
        //getting boom image from drawable resource
        bitmap = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.boom);

        //setting the coordinate outside the screen
        //so that it won't shown up in the screen
        //it will be only visible for a fraction of second
        //after collision
        x = Finals.OUT_OF_BOUNDS;
        y = Finals.OUT_OF_BOUNDS;
    }

    // Setters for x and y to make it visible at the place of collision
    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
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

}