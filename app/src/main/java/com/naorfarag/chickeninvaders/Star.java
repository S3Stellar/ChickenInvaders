package com.naorfarag.chickeninvaders;

import java.util.Random;

public class Star {

    private int x;
    private int y;
    private int speed;
    private int maxX;
    private int maxY;
    private Random generator = new Random();


    public Star(int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        speed = generator.nextInt(10);

        //generating a random coordinate
        //but keeping the coordinate inside the screen size
        x = generator.nextInt(maxX);
        y = generator.nextInt(maxY);
    }

    public void update(int progressSpeed) {
        //animating the star horizontally left side
        //by decreasing x coordinate with player speed
        y += progressSpeed;
        y += speed;
        //if the star reached the left edge of the screen
        if (y > maxY) {
            //again starting the star from right edge
            //this will give a infinite scrolling background effect
            y = 0;
            x = generator.nextInt(maxX);
            speed = generator.nextInt(15);
        }
    }

    public float getStarWidth() {
        //Making the star width random so that
        //it will give a real look
        float minStarX = 1.0f;
        float maxStarX = 4.0f;
        return generator.nextFloat() * (maxStarX - minStarX) + minStarX;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
