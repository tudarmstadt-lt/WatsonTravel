package activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import model.Map;

import java.io.File;
import java.util.*;

public class DrinkingTourLocationFragment extends Fragment{

    private View rootView;
    private static LocationLst locations;
    private Location location;
    private int lastReviewIndex;
    private List<TextView> textViews;
    private int lastLocationIndex;
    private DbHelper dbHelper;
    private Dialog progressDialog;
    private String region = "baden-wuerttemberg";

    public DrinkingTourLocationFragment() {

    }

    public static DrinkingTourLocationFragment newInstance(LocationLst locationList) {
        locations = locationList;
        return new DrinkingTourLocationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new MapTableDbHelper(getActivity());
        lastLocationIndex = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_drinkingtour_location, container, false);
        location = getNextLocation();
        setUpLocationData();
        setUpNavigation();
        setUpNextLocation();
        setUpReviews();
        setUpOpeningHours();
        hideEmptyFields();
        return rootView;
    }

    private Location getNextLocation() {
        if(locations.size()!=0 && locations.size() > lastLocationIndex+1) {
            lastLocationIndex++;
            return locations.getLocationLst().get(lastLocationIndex);
        }
        else
            DialogBuilder.buildAlertDialog(getActivity(),"There are no more Locations to visit!");
        return null;
    }

    private void setUpNextLocation() {
        TextView nextLocationText = (TextView) rootView.findViewById(R.id.next_location_text);
        ImageView nextLocationImage = (ImageView) rootView.findViewById(R.id.next_location_arrow);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = getNextLocation();
                //TODO nachmal pruefen, ob das so in Ordnung ist, aber app stürzt nicht mehr ab
                if(location != null) {
                    setUpLocationData();
                    setUpNavigation();
                }
            }
        };
        nextLocationImage.setOnClickListener(onClickListener);
        nextLocationText.setOnClickListener(onClickListener);
    }

    private void setUpLocationData() {
        textViews = new ArrayList<>();
        TextView locationName = (TextView) rootView.findViewById(R.id.location_name_value);
        TextView locationAddress = (TextView) rootView.findViewById(R.id.location_address_value);
        TextView locationPhone = (TextView) rootView.findViewById(R.id.location_phone_value);
        TextView locationFax = (TextView) rootView.findViewById(R.id.location_fax_value);
        TextView locationWebsite = (TextView) rootView.findViewById(R.id.location_website_value);
        TextView locationInfo = (TextView) rootView.findViewById(R.id.location_info_value);
        textViews.add(locationName);
        textViews.add(locationAddress);
        textViews.add(locationPhone);
        textViews.add(locationFax);
        textViews.add(locationWebsite);
        textViews.add(locationInfo);
        locationName.setText(location.getName());
        locationAddress.setText(location.getAddress());
        locationPhone.setText(location.getPhone());
        locationFax.setText(location.getFax());
        String url = location.getWebsite();
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        locationWebsite.setText(url);
        locationInfo.setText(location.getAdditionalInfo());

        TextView locationRoute = (TextView) rootView.findViewById(R.id.location_route);
        locationRoute.setText("Navigate me to "+location.getName());
    }

    private void setUpReviews() {
        TextView locationRating = (TextView) rootView.findViewById(R.id.location_rating_value);
        locationRating.setText(Double.toString(location.getReviewList().getAverageRating()));
        int amountOfStars = (int) location.getReviewList().getAverageRating();
        ImageView[] stars = {
                (ImageView) rootView.findViewById(R.id.rating_star_1),
                (ImageView) rootView.findViewById(R.id.rating_star_2),
                (ImageView) rootView.findViewById(R.id.rating_star_3),
                (ImageView) rootView.findViewById(R.id.rating_star_4),
                (ImageView) rootView.findViewById(R.id.rating_star_5)};
        displayStars(amountOfStars,stars);
        if((location.getReviewList().getAverageRating() - amountOfStars) >= 0.5)
            stars[amountOfStars].setImageResource(R.drawable.ic_action_star_half);
        LinearLayout reviewContainer = (LinearLayout) rootView.findViewById(R.id.review_container);
        ScrollView reviewText = (ScrollView) rootView.findViewById(R.id.review_scrollview);
        reviewText.setOnTouchListener(new OnSwipeTouchListener(getActivity(), 300) {
            @Override
            public void onAbortAction() {}
            @Override
            public void onSwipeRight(float offset) {}
            @Override
            public void onSwipeLeft(float offset) {}
            @Override
            public void onSwipeRightRelease() {
                if (lastReviewIndex - 1 >= 0)
                    displayReview(lastReviewIndex - 1);
            }
            @Override
            public void onSwipeLeftRelease() {
                if (lastReviewIndex + 1 < location.getReviewList().size())
                    displayReview(lastReviewIndex + 1);
            }
        });
        TextView reviewContainerButton = (TextView) rootView.findViewById(R.id.review_container_button);
        reviewContainerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reviewContainer.getVisibility() == View.GONE) {
                    if(location.getReviewList().size() > 0) {
                        reviewContainer.setVisibility(View.VISIBLE);
                        displayReview(0);
                        reviewContainerButton.setText("Close Reviews");
                    }
                }
                else {
                    reviewContainer.setVisibility(View.GONE);
                    reviewContainerButton.setText("Open Reviews");
                }
            }
        });
    }

    private void setUpOpeningHours() {
        if(location.getOpeningHours() == null || location.getOpeningHours().getCurrentOpeningHour() == null)
            return;
        OpeningHour currentOppeningHour = location.getOpeningHours().getCurrentOpeningHour();
        TextView locationOpeningHour = (TextView) rootView.findViewById(R.id.location_opening_hour_value);
        textViews.add(locationOpeningHour);
        locationOpeningHour.setText(currentOppeningHour.toString());
    }

    private void displayReview(int position) {
        TextView reviewText = (TextView) rootView.findViewById(R.id.review_text);
        TextView reviewAuthor = (TextView) rootView.findViewById(R.id.review_author);
        Review review = location.getReviewList().get(position);
        reviewAuthor.setText(review.getAuthorName());
        reviewText.setText(review.getText());
        ImageView[] stars = {
                (ImageView) rootView.findViewById(R.id.review_rating_star_1),
                (ImageView) rootView.findViewById(R.id.review_rating_star_2),
                (ImageView) rootView.findViewById(R.id.review_rating_star_3),
                (ImageView) rootView.findViewById(R.id.review_rating_star_4),
                (ImageView) rootView.findViewById(R.id.review_rating_star_5)};
        displayStars(review.getRating(),stars);
        lastReviewIndex = position;
    }


    private void hideEmptyFields() {
        if(location.getAddress() == null)
            rootView.findViewById(R.id.location_address).setVisibility(View.GONE);
        if(location.getPhone() == null)
            rootView.findViewById(R.id.location_phone).setVisibility(View.GONE);
        if(location.getFax() == null)
            rootView.findViewById(R.id.location_fax).setVisibility(View.GONE);
        if(location.getWebsite() == null)
            rootView.findViewById(R.id.location_website).setVisibility(View.GONE);
        if(location.getAdditionalInfo() == null)
            rootView.findViewById(R.id.location_website).setVisibility(View.GONE);
        if(location.getOpeningHours() == null || location.getOpeningHours().getCurrentOpeningHour() == null)
            rootView.findViewById(R.id.location_opening_hour).setVisibility(View.GONE);
    }


    private void displayStars(int amount, ImageView[] stars) {
        switch(amount) {
            case 5: stars[4].setImageResource(R.drawable.ic_action_star);
            case 4: stars[3].setImageResource(R.drawable.ic_action_star);
            case 3: stars[2].setImageResource(R.drawable.ic_action_star);
            case 2: stars[1].setImageResource(R.drawable.ic_action_star);
            case 1: stars[0].setImageResource(R.drawable.ic_action_star);  break;
        }
    }

    private void setUpNavigation() {
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.location_navigate);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayRegionsDialog();
            }
        });
    }

    private void displayRegionsDialog() {
        AlertDialogBuilderWithView alertDialogBuilder = DialogBuilder.buildRegionPickerDialog(getActivity());
        Spinner regionSpinner = (Spinner) alertDialogBuilder.getView().findViewById(R.id.region_spinner);
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Resources res = getResources();
                String[] regions = res.getStringArray(R.array.region_spinner);
                region = regions[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        alertDialogBuilder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if (isAlreadyDownloaded(region))
                    changeToMapView(new File(getActivity().getFilesDir() + "/maps/" + region));
                else
                    downloadMap(region);
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean isAlreadyDownloaded(String name) {
        List<TableItem> tableItems = dbHelper.getTableItems();
        for(TableItem tableItem : tableItems) {
            if(tableItem.getTitle().equals(name))
                return true;
        }
        return false;
    }

    private void downloadMap(String name) {
        OSMDownloadHelper osmDownloadHelper = new OSMDownloadHelper() {
            @Override
            protected void onPostExecute(Void _nothing) {
                progressDialog.cancel();
                File map = new File(getActivity().getFilesDir()+"/maps/"+name);
                if(map.exists()) {
                    double size = (map.length()/1024)/1024;
                    dbHelper.addTableItem(new Map(name,size));
                    changeToMapView(map);
                }
                else
                    DialogBuilder.buildAlertDialog(getActivity(),"Something went wrong while downloading the Map");
            }
        };
        osmDownloadHelper.downloadRoutingMap(getActivity().getFilesDir(),name);
        progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Downloading Map");
    }

    private void changeToMapView(File map) {
        LocationListener locationListener = new LocationListener(getActivity());
        if(locationListener.isGPSEnabled()) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container_body, MapFragment.newInstance(location.getAddress()+","+location.getCity(),map,true));
            fragmentTransaction.commit();
        }
        else
            DialogBuilder.buildAlertDialog(getActivity(), "GPS disabled, please activate GPS");
    }
}
