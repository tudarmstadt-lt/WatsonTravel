package model;



import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
import com.example.TravelCorpus_App.R;

import java.util.Calendar;


public class DialogBuilder {
    public static AlertDialogBuilderWithView buildAlertMessageNoGps(Activity context) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_gps,null);
        AlertDialogBuilderWithView alertDialog = new AlertDialogBuilderWithView(context,R.style.MyMaterialTheme_Dialog,view);
        alertDialog.setView(view);
        alertDialog.setTitle("GPS won't work");
        alertDialog.setMessage("Your GPS seems to be disabled, please type in the city:");
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        return alertDialog;
    }

    public static AlertDialogBuilderWithView buildApiKeyDialog(Activity context) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        //View view = layoutInflater.inflate(R.layout.alert_dialog_region_picker,null);
        View view = layoutInflater.inflate(R.layout.alert_dialog_api_key,null);
        AlertDialogBuilderWithView alertDialog = new AlertDialogBuilderWithView(context,R.style.MyMaterialTheme_Dialog,view);
        alertDialog.setView(view);
        alertDialog.setTitle("Add API Key");
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        return alertDialog;
    }

    public static AlertDialogBuilderWithView buildRegionPickerDialog(Activity context) {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.alert_dialog_region_picker,null);
        AlertDialogBuilderWithView alertDialog = new AlertDialogBuilderWithView(context,R.style.MyMaterialTheme_Dialog,view);
        alertDialog.setView(view);
        alertDialog.setTitle("Choose your region");
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        return alertDialog;
    }

    public static Dialog buildProcessDialog(Activity context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.MyMaterialTheme_Dialog);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    public static void buildAlertDialog(Activity context, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.MyMaterialTheme_Dialog);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public static void buildTimePickerDialog(Activity context, TimePickerDialog.OnTimeSetListener timeSetListener) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(context,R.style.MyMaterialTheme_Dialog,timeSetListener,Calendar.HOUR,Calendar.MINUTE,false);
        timePickerDialog.show();
    }

}
