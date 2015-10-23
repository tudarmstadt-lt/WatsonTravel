package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.TravelCorpus_App.R;
import jwatson.answer.Evidencelist;

import java.util.List;

public class UncategorizedAnswer extends Fragment{

    private static Evidencelist evidencelist;

    public static UncategorizedAnswer newInstance(Evidencelist evidence) {
        UncategorizedAnswer uncategorizedAnswer = new UncategorizedAnswer();
        evidencelist = evidence;
        return uncategorizedAnswer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_uncategorized_answer, container, false);
        setUpClickListenerOnBackIcon();
        TextView uncategorizedAnswerText = (TextView)rootView.findViewById(R.id.uncategorized_answer_text);
        uncategorizedAnswerText.setText(evidencelist.getText());
        TextView uncategorizedAnswerTitleValue = (TextView)rootView.findViewById(R.id.uncategorized_answer_title_value);
        uncategorizedAnswerTitleValue.setText(evidencelist.getTitle());
        return rootView;
    }

    private void setUpClickListenerOnBackIcon() {
        RelativeLayout toolbar = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        ImageView back = (ImageView) toolbar.findViewById(R.id.toolbar_icon_back);
        back.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                List<Fragment> fragmentList = fragmentManager.getFragments();
                int positionOfUncategorizedAnswersFragment = -1;
                for(int i = 0; i < fragmentList.size(); i++) {
                    if(fragmentList.get(i) instanceof UncategorizedAnswers) {
                        positionOfUncategorizedAnswersFragment = i;
                        break;
                    }
                }
                fragmentTransaction.remove(getFragment());
                fragmentList.get(positionOfUncategorizedAnswersFragment).onResume();
                fragmentTransaction.commit();
            }
        });
    }

    private Fragment getFragment() {
        return this;
    }
}
