package activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.example.TravelCorpus_App.R;
import jwatson.answer.Evidencelist;
import jwatson.answer.WatsonAnswer;
import model.*;
import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private Dialog progressDialog;
    private DbHelper dbHelper;
    private GoogleAPITableDBHelper googleAPITableDBHelper;
    private FragmentDrawer drawerFragment;
    protected LocationListener locationListener;
    private  LatLong position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHelper = new QuestionTableDbHelper(this);
        googleAPITableDBHelper = new GoogleAPITableDBHelper(this);
        List<TableItem> apiKeyLst = googleAPITableDBHelper.getTableItems();

        if(apiKeyLst.size() <= 0)
            DialogBuilder.buildAlertDialog(this,"No API Key found!\nPlease enter a key in the settings menu.");
        else
        {
            GoogleAPI apiKey = (GoogleAPI) apiKeyLst.get(0);
            Settings.API_Key = apiKey.getApi_key();
        }

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar, dbHelper.getTableItems());
        drawerFragment.setDrawerListener(this);

        changeVisibilityOfContainer(View.INVISIBLE, R.id.nov_container);

        setListenerOnCloseIcon();
        setUpNavItems();

        showFragmentItem(new HomeFragment(), getString(R.string.title_home));

        locationListener = new LocationListener(this);
        if(locationListener.isGPSEnabled())
            locationListener.initSingleRequest();
    }

    private void setListenerOnCloseIcon() {
        ImageView close = (ImageView) findViewById(R.id.toolbar_icon_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVisibilityOfContainer(View.VISIBLE,R.id.nov_container);
                changeVisibilityOfContainer(View.INVISIBLE,R.id.ov_container);
                removeOverlayContainerFragments();
            }
        });
    }

    private void removeOverlayContainerFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for(Fragment fragment : fragmentManager.getFragments()) {
            if(fragment instanceof LocationsCategoriesFragment ||
                    fragment instanceof LocationFragment ||
                    fragment instanceof LocationsFragment ||
                    fragment instanceof UncategorizedAnswer ||
                    fragment instanceof UncategorizedAnswers)
                fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showFragmentItem(SettingsFragment.newInstance(drawerFragment), getString(R.string.title_settings));
            return true;
        }
        if(id == R.id.action_search){
            showFragmentItem(new SearchFragment(), getString(R.string.title_search));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Activity main = this;
        AsyncTask<String,Void,List<Evidencelist>> asyncTask = new AsyncTask<String,Void,List<Evidencelist>>() {
            @Override
            protected List<Evidencelist> doInBackground(String... v) {
                Question question = (Question)dbHelper.getTableItems().get(position);
                if(v != null && v[0] != null)
                    return WatsonRequestHandler.askWatson(question.getQuestion() + " in " + v[0]).getAnswerInformation().getEvidencelist();
                else
                    return WatsonRequestHandler.askWatson(question.getQuestion() + " in " +getCurrentCity()).getAnswerInformation().getEvidencelist();
            }
            @Override
            protected void onPostExecute(List<Evidencelist> evidencelists) {
                displayUncategorizedAnswersView(evidencelists);
                progressDialog.cancel();
            }
        };
        if(locationListener.isGPSEnabled())
            locationListener.initSingleRequest();
        if(!locationListener.isGPSEnabled() || locationListener.getLocation() == null) {
            displayNoGPSDialogEvidencelist(asyncTask);
        }
        else {
            asyncTask.execute();
            progressDialog = DialogBuilder.buildProcessDialog(main, "Asking Watson");
        }
    }

    private void setUpNavItems() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View view = findViewById(R.id.fragment_navigation_drawer);
        RelativeLayout homeItem = (RelativeLayout) findViewById(R.id.nav_home);
        homeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragmentItem(new HomeFragment(), getString(R.string.title_home));
                drawerLayout.closeDrawer(view);
            }
        });
        RelativeLayout mapItem = (RelativeLayout) findViewById(R.id.nav_map);
        mapItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapSettingsFragment mapSettingsFragment = new MapSettingsFragment();
                showFragmentItem(mapSettingsFragment, getString(R.string.title_map));
                drawerLayout.closeDrawer(view);
            }
        });
        RelativeLayout tourItem = (RelativeLayout) findViewById(R.id.nav_drinkingtour);
        Activity activity = this;
        tourItem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!Settings.API_Key.equals(""))
                {
                    removeOverlayContainerFragments();
                    setDrinkingTourLocations();
                    drawerLayout.closeDrawer(view);
                }
                else
                {
                    DialogBuilder.buildAlertDialog(activity,"No API Key found!\n" +
                            "Please enter a key in the settings menu.");
                }
            }
        });
        RelativeLayout speechToTextItem = (RelativeLayout) findViewById(R.id.nav_speech_to_text);
        speechToTextItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragmentItem(new SpeechToTextFragment(), "Speech To Text");
                drawerLayout.closeDrawer(view);
            }
        });

        RelativeLayout definedQuestion = (RelativeLayout) findViewById(R.id.nav_drink);
        definedQuestion.setOnClickListener(createDefinedQuestionListener(DefinedQuestions.Category.DRINK,drawerLayout));
        definedQuestion = (RelativeLayout) findViewById(R.id.nav_eat);
        definedQuestion.setOnClickListener(createDefinedQuestionListener(DefinedQuestions.Category.EAT,drawerLayout));
        definedQuestion = (RelativeLayout) findViewById(R.id.nav_sleep);
        definedQuestion.setOnClickListener(createDefinedQuestionListener(DefinedQuestions.Category.SLEEP,drawerLayout));
        definedQuestion = (RelativeLayout) findViewById(R.id.nav_see);
        definedQuestion.setOnClickListener(createDefinedQuestionListener(DefinedQuestions.Category.SEE,drawerLayout));
        definedQuestion = (RelativeLayout) findViewById(R.id.nav_do);
        definedQuestion.setOnClickListener(createDefinedQuestionListener(DefinedQuestions.Category.DO,drawerLayout));
    }

    private View.OnClickListener createDefinedQuestionListener(DefinedQuestions.Category category, DrawerLayout drawerLayout) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleQuestionRequest(category);
            }
        };
    }

    /**
     *
     * @param category
     */
    private void handleQuestionRequest(DefinedQuestions.Category category) {
        Activity main = this;
        AsyncTask<String,Void,LocationLst> asyncTask = new AsyncTask<String,Void,LocationLst>() {
            @Override
            protected LocationLst doInBackground(String... v) {
                if(v != null && v.length > 0 && v[0] != null)
                    return getLocationLst(category, v[0],false);
                else
                    return getLocationLst(category, getCurrentCity(),false);
            }
            @Override
            protected void onPostExecute(LocationLst locationLst) {
                displayResultView(locationLst);
                progressDialog.cancel();
            }
        };
        if(locationListener.isGPSEnabled())
            locationListener.initSingleRequest();
        if(!locationListener.isGPSEnabled() || locationListener.getLocation() == null)
            displayNoGPSDialog(asyncTask);
        else {
            asyncTask.execute();
            progressDialog = DialogBuilder.buildProcessDialog(main, "Asking Watson");
        }
    }

    /**
     *
     */
    private void setDrinkingTourLocations() {
        Activity main = this;
        AsyncTask<String,Void,LocationLst> asyncTask = new AsyncTask<String,Void,LocationLst>() {
            @Override
            protected LocationLst doInBackground(String... v) {
                String city;
                if(v != null && v.length > 0 && v[0] != null)
                    city = v[0];
                else
                    city = getCurrentCity();

                position = getCurrentLatLong(city);
                return getLocationLst(DefinedQuestions.Category.DRINK,city,true);
            }
            @Override
            protected void onPostExecute(LocationLst locationLst) {
                showFragmentItem(DrinkingTourFragment.newInstance(locationLst, position), getString(R.string.title_drinkingtour));
                progressDialog.cancel();
            }
        };
        if(locationListener.isGPSEnabled())
            locationListener.initSingleRequest();
        if(!locationListener.isGPSEnabled() || locationListener.getLocation() == null)
                displayNoGPSDialog(asyncTask);
        else {
            asyncTask.execute();
            progressDialog = DialogBuilder.buildProcessDialog(main, "Getting Locations");
        }
    }

    /**
     *
     * @param category
     * @param cityName
     * @param makeGoogleRequests
     * @return
     */
    private LocationLst getLocationLst(DefinedQuestions.Category category, String cityName, boolean makeGoogleRequests) {
        DefinedQuestions definedQuestions = new DefinedQuestions(category,cityName);
        WatsonAnswer answer = WatsonRequestHandler.askWatson(definedQuestions.getQuestionText());

        if (answer != null) {
            JWatsonAnswerParser jWatsonAnswerParser = new JWatsonAnswerParser(definedQuestions, answer);
            LocationLst locationLst = jWatsonAnswerParser.parseAnswer();
            if (makeGoogleRequests) {
                if (Settings.API_Key != null && !Settings.API_Key.isEmpty()) {
                    MyGeocoder geocoder = new MyGeocoder(Settings.API_Key);
                    for (int i = 0; i < locationLst.size(); i++) {
                        geocoder.setAdditionalInformationsToLocation(locationLst.getLocationLst().get(i));
                    }
                }
            }
            return locationLst;
        }
        else{
            return new LocationLst();
        }

    }


    /**
     * Return the current city depends on GPS data
     * @return
     */
    private String getCurrentCity() {
        String currentCity = "";

        if(Settings.API_Key != null && !Settings.API_Key.isEmpty()) {
            MyGeocoder geocoder = new MyGeocoder(Settings.API_Key);
            currentCity = geocoder.getCityName(locationListener.getLocation());
        }
        return currentCity;
    }


    /**
     * Return the current Latitude and Longatude
     * @param city
     * @return
     */
    private LatLong getCurrentLatLong(String city)
    {
        double latitude;
        double longatude;

        latitude  = locationListener.getLatitude();
        longatude = locationListener.getLongitude();

        if(latitude == Double.MAX_VALUE || longatude == Double.MAX_VALUE) {
            MyGeocoder geocoder = new MyGeocoder(Settings.API_Key);
            return geocoder.getLatLong(city);
        }
        else
            return new LatLong(latitude, longatude);


    }

    private void displayNoGPSDialog(AsyncTask<String,Void,LocationLst> asyncTask) {
        Activity main = this;
        AlertDialogBuilderWithView dialogBuilder = DialogBuilder.buildAlertMessageNoGps(this);
        dialogBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                EditText editText = (EditText) dialogBuilder.getView().findViewById(R.id.city);
                dialog.cancel();
                asyncTask.execute(editText.getText().toString());
                progressDialog = DialogBuilder.buildProcessDialog(main, "Asking Watson");
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void displayNoGPSDialogEvidencelist(AsyncTask<String,Void,List<Evidencelist>> asyncTask) {
        Activity main = this;
        AlertDialogBuilderWithView dialogBuilder = DialogBuilder.buildAlertMessageNoGps(this);
        dialogBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                EditText editText = (EditText) dialogBuilder.getView().findViewById(R.id.city);
                dialog.cancel();
                asyncTask.execute(editText.getText().toString());
                progressDialog = DialogBuilder.buildProcessDialog(main, "Asking Watson");
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void displayUncategorizedAnswersView(List<Evidencelist> evidencelists) {
        removeOverlayContainerFragments();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        changeVisibilityOfContainer(View.INVISIBLE, R.id.nov_container);
        changeVisibilityOfContainer(View.VISIBLE, R.id.ov_container);
        fragmentTransaction.replace(R.id.overlap_container_body, UncategorizedAnswers.newInstance(evidencelists));
        fragmentTransaction.commit();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(findViewById(R.id.fragment_navigation_drawer));
    }

    private void displayResultView(LocationLst locationLst) {
        removeOverlayContainerFragments();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        changeVisibilityOfContainer(View.INVISIBLE, R.id.nov_container);
        changeVisibilityOfContainer(View.VISIBLE, R.id.ov_container);
        fragmentTransaction.replace(R.id.overlap_container_body, LocationsCategoriesFragment.newInstance(locationLst));
        fragmentTransaction.commit();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(findViewById(R.id.fragment_navigation_drawer));
    }

    private void changeVisibilityOfContainer(int visibility, int containerId) {
        LinearLayout linearLayout = (LinearLayout) findViewById(containerId);
        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            view.setVisibility(visibility);
        }
    }

    private void showFragmentItem(Fragment fragment, String title) {
        removeOverlayContainerFragments();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        changeVisibilityOfContainer(View.VISIBLE, R.id.nov_container);
        changeVisibilityOfContainer(View.INVISIBLE, R.id.ov_container);

        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();
        getSupportActionBar().setTitle(title);
    }

}

