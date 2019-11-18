package com.naorfarag.chickeninvaders;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


public class GameActivity extends AppCompatActivity {


    private Context context;

    // This is our thread
    private Thread gameThread = null;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    // The size of the screen in pixels
    private int screenX;
    private int screenY;

    // The players ship
    private PlayerShip playerShip;

    // Up to 60 invaders
    private Invader[] invaders = new Invader[60];
    private int numInvaders = 0;

    private int score = 0;

    // Lives
    private int lives = 3;

    private ViewGroup mainLayout;
    private ImageView image;

    private float xDelta, yDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.hide();

        // Set up full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Lock screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get username
        Intent intent = getIntent();

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);
        size.x = screenX;
        size.y = screenY;

        mainLayout = (FrameLayout) findViewById(R.id.frameLayout);
        image = findViewById(R.id.spaceship);
        image.setOnTouchListener(onTouchListener());
    }

    // Option 1 free move no boundaries
    /*private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        xDelta = view.getX() - event.getRawX();
                        yDelta = view.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        view.animate()
                                .x(event.getRawX() + xDelta)
                                .y(event.getRawY() + yDelta)
                                .setDuration(0)
                                .start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        };
    }*/

    // Option 2 only bottom no boundaries
   /* private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams)
                                view.getLayoutParams();

                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
                                .getLayoutParams();

                        layoutParams.leftMargin = (int) (x - xDelta);
                        layoutParams.topMargin = (int) (y - yDelta);
                        layoutParams.rightMargin = 0;
                        //layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;
                }
                mainLayout.invalidate();
                return true;
            }
        };
    }*/


   // Option 3 free move/left right move with boundaries
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

                        image.offsetLeftAndRight((int) (x - offsetPoint.x));

                        // Disable to make it move left right only
                        image.offsetTopAndBottom((int) (y - offsetPoint.y));


                        // check boundaries
                        if (image.getRight() > parentRect.right) {
                            image.offsetLeftAndRight(-(image.getRight() - parentRect.right));
                        } else if (image.getLeft() < parentRect.left) {
                            image.offsetLeftAndRight((parentRect.left - image.getLeft()));
                        }

                        if (image.getBottom() > parentRect.bottom) {
                            image.offsetTopAndBottom(-(image.getBottom() - parentRect.bottom));
                        } else if (image.getTop() < parentRect.top) {
                            image.offsetTopAndBottom((parentRect.top - image.getTop()));
                        }

                        break;
                }
                return true;
            }
        };
    }
}

