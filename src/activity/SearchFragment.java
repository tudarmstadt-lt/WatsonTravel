package activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.example.TravelCorpus_App.R;
import jwatson.answer.Evidencelist;
import jwatson.answer.WatsonAnswer;
import model.*;

import java.util.List;

public class SearchFragment extends AbstractFragment {

    private Dialog progressDialog;

    public SearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        setupInputControls(rootView);
        return rootView;
    }

    private void setupInputControls(View rootView) {
        LocationListener locationListener = new LocationListener(getActivity());
        EditText editText_search = (EditText) rootView.findViewById(R.id.editText_search);
        Switch switch_gps = (Switch) rootView.findViewById(R.id.switch1);
        switch_gps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !locationListener.isGPSEnabled()) {
                    DialogBuilder.buildAlertMessageNoGps(getActivity());
                    switch_gps.setChecked(false);
                }
                else
                    locationListener.initSingleRequest();
            }
        });
        editText_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH && editText_search.getText().length() > 0) {
                    String question = editText_search.getText().toString();
                    hideSoftKeyboard();
                    sendQuestionToWatson(question,switch_gps.isChecked(),locationListener);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void sendQuestionToWatson(String question, boolean cityEnabled, LocationListener locationListener) {
        AsyncTask<String,Void,List<Evidencelist>> asyncTask = new AsyncTask<String,Void,List<Evidencelist>>() {
            @Override
            protected List<Evidencelist> doInBackground(String... v) {
                if(!cityEnabled)
                    return WatsonRequestHandler.askWatson(question).getAnswerInformation().getEvidencelist();
                else if(v != null && v[0] != null)
                    return WatsonRequestHandler.askWatson(question + v[0]).getAnswerInformation().getEvidencelist();
                else
                    return WatsonRequestHandler.askWatson(question + getCurrentCity(locationListener)).getAnswerInformation().getEvidencelist();
            }
            @Override
            protected void onPostExecute(List<Evidencelist> evidencelistList) {
                displayResultView(evidencelistList);
                progressDialog.cancel();
            }
        };
        if(cityEnabled) {
            if (locationListener.isGPSEnabled())
                locationListener.initSingleRequest();
            if (locationListener.isGPSEnabled() || locationListener.getLocation() == null) {
                displayNoGPSDialog(asyncTask);
            }
            else {
                asyncTask.execute();
                progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Asking Watson");
            }
        }
        else {
            asyncTask.execute();
            progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Asking Watson");
        }
    }

    private void displayNoGPSDialog(AsyncTask<String,Void,List<Evidencelist>> asyncTask) {
        AlertDialogBuilderWithView dialogBuilder = DialogBuilder.buildAlertMessageNoGps(getActivity());
        dialogBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                EditText editText = (EditText) dialogBuilder.getView().findViewById(R.id.city);
                dialog.cancel();
                asyncTask.execute(editText.getText().toString());
                progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Asking Watson");
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void displayResultView(List<Evidencelist> evidencelists) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        changeVisibilityOfContainer(View.INVISIBLE, R.id.nov_container);
        changeVisibilityOfContainer(View.VISIBLE, R.id.ov_container);
        fragmentTransaction.replace(R.id.overlap_container_body, UncategorizedAnswers.newInstance(evidencelists));
        fragmentTransaction.commit();
    }

    private void changeVisibilityOfContainer(int visibility, int containerId) {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(containerId);
        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            view.setVisibility(visibility);
        }
    }

    /**
     * Return the current city depends on GPS data
     * @return
     */
    private String getCurrentCity(LocationListener locationListener) {

        if (Settings.API_Key == null || Settings.API_Key.isEmpty())
            return  "";

        MyGeocoder geocoder = new MyGeocoder(Settings.API_Key);
        return geocoder.getCityName(locationListener.getLocation());
    }

}
