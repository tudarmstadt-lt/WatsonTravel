package activity;

import adapter.LocationCategoryDrawerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.TravelCorpus_App.R;
import jwatson.answer.Evidencelist;
import java.util.ArrayList;
import java.util.List;

public class UncategorizedAnswers extends Fragment{

    private static List<Evidencelist> evidencelists;
    final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

        @Override public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

    });

    public UncategorizedAnswers() {

    }

    public static UncategorizedAnswers newInstance(List<Evidencelist> evidencelistList) {
        UncategorizedAnswers uncategorizedAnswers = new UncategorizedAnswers();
        evidencelists = evidencelistList;
        return uncategorizedAnswers;
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBackButton();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(this);
        fragmentTransaction.commit();
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        TextView title = (TextView) relativeLayout.findViewById(R.id.toolbar_title);
        title.setText("Results");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_answers, container, false);

        hideBackButton();
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.answerList);

        LocationCategoryDrawerAdapter locationDrawerAdapter = new LocationCategoryDrawerAdapter(getActivity(), buildTitleList());
        recyclerView.setAdapter(locationDrawerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    displayAnswer(evidencelists.get(recyclerView.getChildLayoutPosition(child)));
                    return true;

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    private void hideBackButton() {
        RelativeLayout toolbar = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        toolbar.findViewById(R.id.toolbar_icon_back).setVisibility(View.INVISIBLE);
    }

    private List<String> buildTitleList() {
        List<String> result = new ArrayList<>();
        for(Evidencelist evidencelist : evidencelists) {
            result.add(evidencelist.getTitle());
        }
        return result;
    }

    private void displayAnswer(Evidencelist evidencelist) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.toolbar_title);
        textView.setText("Answer");
        fragmentTransaction.hide(this);
        fragmentTransaction.add(R.id.overlap_container_body,
                UncategorizedAnswer.newInstance(evidencelist));
        fragmentTransaction.commit();
    }

}
