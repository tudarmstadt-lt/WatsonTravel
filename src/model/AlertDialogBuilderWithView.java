package model;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;


public class AlertDialogBuilderWithView extends AlertDialog.Builder{

    View view;
    AlertDialog.Builder builder;

    public AlertDialogBuilderWithView(Context context, int theme, View v) {
        super(context, theme);
        view = v;
    }

    public View getView() {
        return view;
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }

    public void setBuilder(AlertDialog.Builder builder) {
        this.builder = builder;
    }

}
