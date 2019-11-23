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
    private int speed = 1;

    private int maxX;
    private int minX;

    private int maxY;
    private int minY;

    //creating a rect object for a friendly ship
    private Rect detectCollision;

    private Random rand = new Random();
    private TypedArray chicken_images;
    private Context context;

    public Invader(Context context, int screenX, int screenY) {
        this.context = context;
        chicken_images = context.getResources().obtainTypedArray(R.array.apptour);
        changeChickenImage();
        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;
        Random generator = new Random();
        speed = generator.nextInt(5) + 8;
        y =0;
        x = x = rand.nextInt(maxX-bitmap.getWidth()) + bitmap.getWidth()/10;

        //initializing rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    private void changeChickenImage() {
        final int rndInt = rand.nextInt(chicken_images.length());
        final int resID = chicken_images.getResourceId(rndInt, 0);
        bitmap = BitmapFactory.decodeResource(context.getResources(), resID);
    }

    public void update(int playerSpeed) {
        y += playerSpeed;
        y += speed;
        if (y > maxY - bitmap.getHeight()) {
            changeChickenImage();
            speed = rand.nextInt(10) + 10;
            y = 0;
            x = rand.nextInt(maxX-bitmap.getWidth()) + bitmap.getWidth()/10;
        }

        //Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y+bitmap.getHeight()/4;
        detectCollision.right = x + bitmap.getWidth()/(1+1/3);
        detectCollision.bottom = y + bitmap.getHeight()/2;
    }



    //one more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    //getters
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

/*
public class Invader {

    private Bitmap bitmap;
    private int x;
    private int y;
    private int speed = 1;

    private int maxX;
    private int minX;

    private int maxY;
    private int minY;

    //creating a rect object for a friendly ship
    private Rect detectCollision;


    public Invader(Context context, int screenX, int screenY) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.chicken2);
        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;
        Random generator = new Random();
        speed = generator.nextInt(6) + 10;
        x = screenX;
        y = generator.nextInt(maxY) - bitmap.getHeight();

        //initializing rect object
        detectCollision = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update(int playerSpeed) {
        x -= playerSpeed;
        x -= speed;
        if (x < minX - bitmap.getWidth()) {
            Random generator = new Random();
            speed = generator.nextInt(10) + 10;
            x = maxX;
            y = generator.nextInt(maxY) - bitmap.getHeight();
        }

        //Adding the top, left, bottom and right to the rect object
        detectCollision.left = x;
        detectCollision.top = y;
        detectCollision.right = x + bitmap.getWidth();
        detectCollision.bottom = y + bitmap.getHeight();
    }


    //one more getter for getting the rect object
    public Rect getDetectCollision() {
        return detectCollision;
    }

    //getters
    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}*/
