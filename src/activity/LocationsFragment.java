package activity;

import adapter.LocationCategoryDrawerAdapter;
import adapter.LocationDrawerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.TravelCorpus_App.R;
import model.LocationLst;
import model.ResultFragment;

import java.util.List;

public class LocationsFragment extends ResultFragment {

    private static LocationLst locationLst;
    final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

        @Override public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

    });

    public LocationsFragment() {

    }

    public static LocationsFragment newInstance(LocationLst locations) {
        LocationsFragment locationsFragment = new LocationsFragment();
        locationLst = locations;
        return locationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void displayPreviousFragment() {
        setUpClickListenerOnBackIcon();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(this);
        fragmentTransaction.commit();
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        TextView title = (TextView) relativeLayout.findViewById(R.id.toolbar_title);
        title.setText(locationLst.getLocationLst().get(0).getSubCategory());
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
                int positionOfCategoriesFragment = -1;
                for(int i = 0; i < fragmentList.size(); i++) {
                    if(fragmentList.get(i) instanceof LocationsCategoriesFragment) {
                        positionOfCategoriesFragment = i;
                        break;
                    }
                }
                fragmentTransaction.remove(getFragment());
                ((ResultFragment)fragmentList.get(positionOfCategoriesFragment)).displayPreviousFragment();
                fragmentTransaction.commit();
            }
        });
    }

    private Fragment getFragment() {
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_answers, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.answerList);
        LocationDrawerAdapter locationDrawerAdapter = new LocationDrawerAdapter(getActivity(), locationLst.getLocationLst());
        recyclerView.setAdapter(locationDrawerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    showLocation(recyclerView.getChildLayoutPosition(child));
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });
        setUpClickListenerOnBackIcon();
        return rootView;
    }

    private void showLocation(int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.answerToolbar);
        TextView textView = (TextView) relativeLayout.findViewById(R.id.toolbar_title);
        textView.setText(locationLst.getLocationLst().get(position).getName());
        fragmentTransaction.hide(this);
        fragmentTransaction.add(R.id.overlap_container_body, LocationFragment.newInstance(locationLst.getLocationLst().get(position)));
        fragmentTransaction.commit();
    }
}
