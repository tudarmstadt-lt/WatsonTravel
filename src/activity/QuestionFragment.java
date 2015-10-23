package activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.TravelCorpus_App.R;
import model.AbstractFragment;
import model.DbHelper;
import model.Question;

import java.util.List;


public class QuestionFragment extends AbstractFragment {

    private static FragmentDrawer fragmentDrawer;
    private static DbHelper dbHelper;
    private static ArrayAdapter<String> listAdapter;
    private static List<String> questions;
    private int selectedIconId;
    private ImageButton selectedIcon;

    public static QuestionFragment newInstance(FragmentDrawer drawer, DbHelper helper, List<String> data, ArrayAdapter<String> adapter) {
        QuestionFragment questionFragment = new QuestionFragment();
        fragmentDrawer = drawer;
        dbHelper = helper;
        listAdapter = adapter;
        questions = data;
        return questionFragment;
    }

    public QuestionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_question, container, false);

        TextView cancelButton = (TextView) rootView.findViewById(R.id.button_question_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewToSettingsView();
            }
        });
        TextView saveButton = (TextView) rootView.findViewById(R.id.button_question_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = ((EditText)rootView.findViewById(R.id.question_title)).getText().toString();
                String question = ((EditText)rootView.findViewById(R.id.question_question)).getText().toString();
                Question newQuestion = new Question(title, question,selectedIconId);
                dbHelper.addTableItem(newQuestion);
                fragmentDrawer.insert(newQuestion);
                questions.add(title);
                listAdapter.notifyDataSetChanged();
                changeViewToSettingsView();
            }
        });

        GridLayout iconLayout = (GridLayout) rootView.findViewById(R.id.layout_question_icon);
        TypedArray iconIds = getActivity().getResources().obtainTypedArray(R.array.question_icons);
        int rowCount = 1;
        iconLayout.setRowCount(rowCount);
        for(int i = 0; i < iconIds.length(); i++) {
            if(i == iconLayout.getColumnCount()) {
                rowCount++;
                iconLayout.setRowCount(rowCount);
            }
            int iconId = iconIds.getResourceId(i,-1);
            ImageButton imageButton = new ImageButton(getActivity());
            imageButton.setImageResource(iconId);
            imageButton.setBackground(getActivity().getDrawable(R.drawable.circle_button));

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedIcon != null)
                        selectedIcon.setBackground(getActivity().getDrawable(R.drawable.circle_button));
                    selectedIcon = (ImageButton) v;
                    selectedIconId = iconId;
                    v.setBackground(getActivity().getDrawable(R.drawable.circle_button_filled));
                }
            });
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.setMargins(0,0,35,15);
            iconLayout.addView(imageButton, layoutParams);
        }
        iconIds.recycle();
        return rootView;
    }

    private void changeViewToSettingsView() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body,SettingsFragment.newInstance(fragmentDrawer));
        fragmentTransaction.commit();
    }

}
