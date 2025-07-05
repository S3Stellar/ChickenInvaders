package com.naorfarag.chickeninvaders;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PauseMenu {
    private Dialog dialog;
    private Context context;
    private PauseMenuListener listener;
    
    public interface PauseMenuListener {
        void onResume();
        void onRestart();
        void onExit();
        void onVolumeChanged(float volume);
    }
    
    public PauseMenu(Context context, PauseMenuListener listener) {
        this.context = context;
        this.listener = listener;
        createDialog();
    }
    
    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settings_layout); // Reuse existing settings layout
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(200, 0, 0, 0)));
        dialog.setCancelable(false);
        
        setupButtons();
    }
    
    private void setupButtons() {
        Button resumeButton = dialog.findViewById(R.id.button);
        Button restartButton = dialog.findViewById(R.id.button2);
        Button exitButton = dialog.findViewById(R.id.button3);
        
        if (resumeButton != null) {
            resumeButton.setText("Resume");
            resumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    listener.onResume();
                }
            });
        }
        
        if (restartButton != null) {
            restartButton.setText("Restart");
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    listener.onRestart();
                }
            });
        }
        
        if (exitButton != null) {
            exitButton.setText("Exit");
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    listener.onExit();
                }
            });
        }
    }
    
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
    
    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}