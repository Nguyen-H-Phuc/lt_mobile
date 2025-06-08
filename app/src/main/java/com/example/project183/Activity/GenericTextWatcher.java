package com.example.project183.Activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class GenericTextWatcher implements TextWatcher {

    private final EditText nextView;
    private final EditText previousView;

    public GenericTextWatcher(EditText nextView, EditText previousView) {
        this.nextView = nextView;
        this.previousView = previousView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 1 && nextView != null) {
            nextView.requestFocus();
        } else if (s.length() == 0 && previousView != null) {
            previousView.requestFocus(); // Lùi lại khi xóa
        }
    }
}
