package activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.TravelCorpus_App.R;
import model.*;
import org.mapsforge.core.model.LatLong;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DrinkingTourFragment extends Fragment {

    private View rootView;
    private int ratingFrom;
    private static LocationLst locationLst;
    private List<String> choosenCategories;
    private Date startTimeDate;
    private int radius;
    private static LatLong position;

    public DrinkingTourFragment() {

    }

    public static DrinkingTourFragment newInstance(LocationLst list, LatLong pos) {
        locationLst = list;
        position = pos;
        return new DrinkingTourFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ratingFrom = 1;
        radius = 1;
        choosenCategories = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        startTimeDate = cal.getTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_drinkingtour, container, false);
        setUpTimePickerDialog();
        setUpRatingRange();
        setUpCategories();
        setUpRadiusBar();
        TextView buttonCreateTour = (TextView)rootView.findViewById(R.id.button_create_tour);
        buttonCreateTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTour();
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setUpTimePickerDialog() {
        LinearLayout startTime = (LinearLayout) rootView.findViewById(R.id.tour_start_time);
        TextView startTimeValue = (TextView) rootView.findViewById(R.id.tour_start_time_value);
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        startTimeValue.setText(dateFormat.format(startTimeDate));
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String time;
                        if(hourOfDay >= 12)
                            time = "PM";
                        else
                            time = "AM";
                        if(hourOfDay >= 13)
                            hourOfDay = hourOfDay-12;
                        String timeString = hourOfDay + ":" + minute + " " + time;
                        startTimeValue.setText(timeString);
                        startTimeDate = parseStringToDate(timeString);
                    }
                };
                DialogBuilder.buildTimePickerDialog(getActivity(),timeSetListener);
            }
        });
    }

    private void setUpRatingRange() {
        View[] startRatingStars = {
                rootView.findViewById(R.id.from_rating_star_1),
                rootView.findViewById(R.id.from_rating_star_2),
                rootView.findViewById(R.id.from_rating_star_3),
                rootView.findViewById(R.id.from_rating_star_4),
                rootView.findViewById(R.id.from_rating_star_5)
        };
        for(int i = 0; i < 5; i++) {
            int amount = i+1;
            startRatingStars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ratingFrom = amount;
                    displayRatingStars(amount,startRatingStars);
                }
            });
        }
        for(View star : startRatingStars) {
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.from_rating_star_5: displayRatingStars(5, startRatingStars); break;
                        case R.id.from_rating_star_4: displayRatingStars(4, startRatingStars); break;
                        case R.id.from_rating_star_3: displayRatingStars(3, startRatingStars); break;
                        case R.id.from_rating_star_2: displayRatingStars(2, startRatingStars); break;
                        case R.id.from_rating_star_1: displayRatingStars(1, startRatingStars); break;
                    }
                }
            });
        }
    }

    private void setUpCategories() {
        GridLayout gridLayout = (GridLayout) rootView.findViewById(R.id.categories_container);
        List<String> categoriesList = locationLst.getSubCategoryLst();
        for (String category : categoriesList) {
            CheckBox categoryCheckBox = new CheckBox(getActivity());
            categoryCheckBox.setText(category);
            categoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        choosenCategories.add(category);
                    }
                    else
                        choosenCategories.remove(category);
                }
            });
            gridLayout.addView(categoryCheckBox);
        }
    }

    private void setUpRadiusBar() {
        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.tour_radius);
        TextView tourRadiusText = (TextView) rootView.findViewById(R.id.tour_radius_output);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress+1;
                tourRadiusText.setText(Integer.toString(progressChanged)+ " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                radius = progressChanged;
            }
        });
    }

    private void displayRatingStars(int amount, View[] stars) {
        for(int i = 0; i < stars.length; i++) {
            ImageView star = (ImageView)stars[i];
            if(i < amount)
                star.setImageResource(R.drawable.ic_action_star);
            else
                star.setImageResource(R.drawable.ic_action_star_border);
        }
    }

    private void createTour() {
        if(choosenCategories.size() <= 0) {
            DialogBuilder.buildAlertDialog(getActivity(),"No Categories selected");
        }
        else {
            LocationLst locationLstWithConstraints = locationLst.filterWithConstraints(
                    ratingFrom, choosenCategories, startTimeDate, position, radius
            );
            if(locationLstWithConstraints != null && locationLstWithConstraints.size() > 0) {
                displayLocation(locationLstWithConstraints);
            }
            else
                DialogBuilder.buildAlertDialog(getActivity(),"There are no Locations depending on your constraints");
        }
    }

    private Date parseStringToDate(String timeString) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        Date startTime = null;
        try {
            startTime = dateFormat.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startTime;
    }

    private void displayLocation(LocationLst locationLst) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, DrinkingTourLocationFragment.newInstance(locationLst));
        fragmentTransaction.commit();
    }

}
