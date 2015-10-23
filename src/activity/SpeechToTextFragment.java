package activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.TravelCorpus_App.R;
import jwatson.answer.Evidencelist;
import model.DialogBuilder;
import model.Question;
import model.WatsonRequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechToTextFragment extends Fragment {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView givenQuestion;
    private TextView buttonAskWatson;
    private Dialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_speech_to_text, container, false);
        ImageButton buttonSpeak = (ImageButton)rootView.findViewById(R.id.btnSpeak);
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        givenQuestion = (TextView) rootView.findViewById(R.id.given_question);
        buttonAskWatson = (TextView) rootView.findViewById(R.id.btn_ask_watson);
        buttonAskWatson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Asking Watson");
                sendQuestionToWatson(givenQuestion.getText().toString());
            }
        });
        return rootView;
    }

    private void sendQuestionToWatson(String question) {
        AsyncTask<Void,Void,List<Evidencelist>> asyncTask = new AsyncTask<Void,Void,List<Evidencelist>>() {
            @Override
            protected List<Evidencelist> doInBackground(Void... v) {
                return WatsonRequestHandler.askWatson(question).getAnswerInformation().getEvidencelist();
            }
            @Override
            protected void onPostExecute(List<Evidencelist> evidencelists) {
                displayUncategorizedAnswersView(evidencelists);
                progressDialog.cancel();
            }
        }.execute();
    }

    private void displayUncategorizedAnswersView(List<Evidencelist> evidencelists) {
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

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Ask your question");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            DialogBuilder.buildAlertDialog(getActivity(),"Sorry! Your device doesn't support speech input");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    displayQuestion(result.get(0));
                }
                break;
            }
        }
    }

    private void displayQuestion(String question) {
        givenQuestion.setText(question + " ?");
        givenQuestion.setVisibility(View.VISIBLE);
        buttonAskWatson.setVisibility(View.VISIBLE);
    }
}
