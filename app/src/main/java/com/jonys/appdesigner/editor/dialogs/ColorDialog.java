package com.jonys.appdesigner.editor.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jonys.appdesigner.R;
import com.jonys.appdesigner.tools.ColorView;

import java.util.regex.Pattern;

public class ColorDialog extends AttributeDialog implements SeekBar.OnSeekBarChangeListener {

    private ColorView colorPreview;
    private TextView textColorPreview;

    private SeekBar seekAlpha;
    private SeekBar seekRed;
    private SeekBar seekGreen;
    private SeekBar seekBlue;

    private TextInputLayout inputLayout;
    private TextInputEditText editText;

    public ColorDialog(Context context, String savedValue) {
        super(context);

        final View dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_attribute_color, null, false);

        colorPreview = dialogView.findViewById(R.id.color_preview);
        textColorPreview = dialogView.findViewById(R.id.text_color_preview);

        seekAlpha = dialogView.findViewById(R.id.seek_alpha);
        seekAlpha.setOnSeekBarChangeListener(this);
        seekAlpha.setMax(255);
        seekAlpha.setProgress(255);

        seekRed = dialogView.findViewById(R.id.seek_red);
        seekRed.setOnSeekBarChangeListener(this);
        seekRed.setMax(255);
        seekRed.setProgress(255);

        seekGreen = dialogView.findViewById(R.id.seek_green);
        seekGreen.setOnSeekBarChangeListener(this);
        seekGreen.setMax(255);
        seekGreen.setProgress(255);

        seekBlue = dialogView.findViewById(R.id.seek_blue);
        seekBlue.setOnSeekBarChangeListener(this);
        seekBlue.setMax(255);
        seekBlue.setProgress(255);

        inputLayout = dialogView.findViewById(R.id.textinput_layout);
        inputLayout.setHint("Enter custom HEX code");

        editText = dialogView.findViewById(R.id.textinput_edittext);

        if (!savedValue.equals("")) {
            colorPreview.setColor(Color.parseColor(savedValue));
            updateText(colorPreview.getColor());
            updateSeekbars(colorPreview.getColor());
            updateEditText();
        }

        editText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}

                    @Override
                    public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {}

                    @Override
                    public void afterTextChanged(Editable p1) {
                        checkHexErrors(editText.getText().toString());
                    }
                });

        setView(dialogView, 10);
    }

    @Override
    public void onClickSave() {
        listener.onSave(colorPreview.getHexColor());
    }

    private void checkHexErrors(String hex) {
        if (Pattern.matches("#[a-fA-F0-9]{6}", hex) || Pattern.matches("#[a-fA-F0-9]{8}", hex)) {
            colorPreview.setColor(Color.parseColor(hex));
            updateSeekbars(colorPreview.getColor());
            updateText(colorPreview.getColor());

            inputLayout.setErrorEnabled(false);
            inputLayout.setError("");
            setEnabled(true);
            return;
        }

        inputLayout.setErrorEnabled(true);
        inputLayout.setError("Invalid HEX value");
        setEnabled(false);
    }

    private void updateText(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        textColorPreview.setText(a + ", " + r + ", " + g + ", " + b);
        textColorPreview.setTextColor(Color.luminance(color) < 0.5f ? Color.WHITE : Color.DKGRAY);
    }

    private void updateSeekbars(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        seekAlpha.setProgress(a);
        seekRed.setProgress(r);
        seekGreen.setProgress(g);
        seekBlue.setProgress(b);
    }

    private void updateEditText() {
        editText.setText(colorPreview.getHexColor());
    }

    @Override
    public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seek.getId()) {
                case R.id.seek_alpha:
                    colorPreview.setAlpha(progress);
                    updateText(colorPreview.getColor());
                    updateEditText();
                    break;

                case R.id.seek_red:
                    colorPreview.setRed(progress);
                    updateText(colorPreview.getColor());
                    updateEditText();
                    break;

                case R.id.seek_green:
                    colorPreview.setGreen(progress);
                    updateText(colorPreview.getColor());
                    updateEditText();
                    break;

                case R.id.seek_blue:
                    colorPreview.setBlue(progress);
                    updateText(colorPreview.getColor());
                    updateEditText();
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar p1) {}

    @Override
    public void onStopTrackingTouch(SeekBar p1) {}
}
