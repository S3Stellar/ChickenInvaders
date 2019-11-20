package com.naorfarag.chickeninvaders;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


import java.util.ArrayList;


public class GameActivity extends AppCompatActivity {

    //private ImageView spaceship;
    private ChickenInvadersView chickenInvadersView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Get username
        Intent intent = getIntent();

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        chickenInvadersView = new ChickenInvadersView(this, size.x,size.y);
        if(chickenInvadersView.getParent() != null) {
            ((ViewGroup)chickenInvadersView.getParent()).removeView(chickenInvadersView); // <- fix
        }
        setContentView(chickenInvadersView);
  /*      spaceship = findViewById(R.id.spaceship);
        spaceship.setOnTouchListener(onTouchListener());*/
    }


    //pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        chickenInvadersView.pause();
    }

    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        chickenInvadersView.resume();
    }

    /*// Option 3 free move/left right move with boundaries
    private View.OnTouchListener onTouchListener() {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        // Display Rect Boundaries
        final Rect parentRect = new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);

        // original down point
        final PointF offsetPoint = new PointF();
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                final int action = motionEvent.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        offsetPoint.x = motionEvent.getX();
                        offsetPoint.y = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();

                        spaceship.offsetLeftAndRight((int) (x - offsetPoint.x));

                        // Disable to make it move left right only
                        spaceship.offsetTopAndBottom((int) (y - offsetPoint.y));


                        // check boundaries
                        if (spaceship.getRight() > parentRect.right) {
                            spaceship.offsetLeftAndRight(-(spaceship.getRight() - parentRect.right));
                        } else if (spaceship.getLeft() < parentRect.left) {
                            spaceship.offsetLeftAndRight((parentRect.left - spaceship.getLeft()));
                        }

                        if (spaceship.getBottom() > parentRect.bottom) {
                            spaceship.offsetTopAndBottom(-(spaceship.getBottom() - parentRect.bottom));
                        } else if (spaceship.getTop() < parentRect.top) {
                            spaceship.offsetTopAndBottom((parentRect.top - spaceship.getTop()));
                        }

                        break;
                }
                return true;
            }
        };
    }*/
}

