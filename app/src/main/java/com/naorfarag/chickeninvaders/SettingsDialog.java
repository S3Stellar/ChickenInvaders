package com.naorfarag.chickeninvaders;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.github.florent37.androidslidr.Slidr;

import java.util.Objects;

import hari.bounceview.BounceView;

public class SettingsDialog extends AppCompatDialogFragment {

    private DialogListener listener;
    private boolean isTilt = false;
    private CheckBox checkBox;
    private int progress;
    private Slidr slidr;
    private Dialog dialog;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_layout, null);

        builder.setView(view)
                .setPositiveButton(Finals.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progress = (int) slidr.getCurrentValue();
                        isTilt = checkBox.isChecked();
                        listener.applySettings(progress, isTilt);
                    }
                });

        checkBox = view.findViewById(R.id.checkBox);
        slidr = view.findViewById(R.id.seekbarSlider);
        createSeekBar();

        dialog = builder.create();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            BounceView.addAnimTo(dialog);

            dialog.getWindow().
                    setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            // Set full-sreen mode (immersive sticky):
            dialog.getWindow().getDecorView().setSystemUiVisibility(Finals.UI_FLAGS);

            // Show the alertDialog:
            dialog.show();

            // Set dialog focusable so we can avoid touching outside:
            dialog.getWindow().
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        }
        return dialog;
    }


    private void createSeekBar() {
        slidr.setTextFormatter(new Slidr.TextFormatter() {
            @Override
            public String format(float value) {
                return String.format("  %d", (int) value);
            }
        });

        slidr.setMax(Finals.MAX_LANES);
        slidr.setMin(Finals.MIN_LANES);
        slidr.setTextMax(Finals.INSANE);
        slidr.setTextMin(Finals.EASY);
        slidr.setCurrentValue(Finals.DEFAULT_LANES);
        slidr.setListener(new Slidr.Listener() {
            @Override
            public void valueChanged(Slidr slidr, float currentValue) {
                progress = (int) slidr.getCurrentValue();
            }

            @Override
            public void bubbleClicked(Slidr slidr) {
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DialogListener");
        }
    }

    public interface DialogListener {
        void applySettings(int lanesAmount, boolean isTilt);
    }
}
