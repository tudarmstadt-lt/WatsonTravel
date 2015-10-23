package activity;

import android.app.Dialog;
import android.os.AsyncTask;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapSettingsFragment extends Fragment {

    private Dialog progressDialog;
    private HashMap<String, Boolean> availableCountries;
    private HashMap<String, Boolean> availableContinents;
    private HashMap<String, Boolean> availableRegions;
    private String continent;
    private String country;
    private DbHelper dbHelper;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new MapTableDbHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map_settings, container, false);
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                availableContinents = MapsforgeMapParser.getCountryLst(HttpRequestHandler.execute("http://download.mapsforge.org/maps/"));
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                setUpContinentSpinner();
                progressDialog.cancel();
            }
        }.execute();
        progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Getting available Continents");
        return rootView;
    }

    private void setUpContinentSpinner() {
        Spinner continentSpinner = (Spinner) rootView.findViewById(R.id.spinner_continent);
        String[] continents = getArrayWithPlaceholder("Choose a continent", availableContinents);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_item, continents);
        continentSpinner.setAdapter(adapter);
        continentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) {
                    continent = continents[position];
                    if(!availableContinents.get(continent)) {
                        if(isAlreadyDownloaded(continent))
                            changeToMapView(new File(getActivity().getFilesDir()+"/maps/"+continent));
                        else
                            downloadMap(continent,continent,false);
                    }
                    else
                        requestAvailableCountries(continent);
                }
                else
                    changeVisibilityOfSpinner(R.id.spinner_country,View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpCountrySpinner() {
        Spinner countrySpinner = (Spinner) rootView.findViewById(R.id.spinner_country);
        String[] countries = getArrayWithPlaceholder("Choose a country",availableCountries);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_item, countries);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) {
                    country = countries[position];
                    if(!availableCountries.get(country)) {
                        if(isAlreadyDownloaded(country))
                            changeToMapView(new File(getActivity().getFilesDir()+"/maps/"+country));
                        else
                            downloadMap(continent+"/"+country,country,false);
                    }
                    else
                        requestAvailableRegions(country);
                }
                else
                    changeVisibilityOfSpinner(R.id.spinner_region,View.INVISIBLE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpRegionSpinner() {
        Spinner regionSpinner = (Spinner)rootView.findViewById(R.id.spinner_region);
        String[] regions = getArrayWithPlaceholder("Choose a region",availableRegions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_item, regions);
        regionSpinner.setAdapter(adapter);
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) {
                    String region = regions[position];
                    if(isAlreadyDownloaded(region))
                        changeToMapView(new File(getActivity().getFilesDir()+"/maps/"+region));
                    else if(country.equals("germany"))
                        downloadMap(continent+"/"+country,region,true);
                    else
                        downloadMap(continent+"/"+country+"/"+region, region,false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean isAlreadyDownloaded(String name) {
        List<TableItem> tableItems = dbHelper.getTableItems();
        for(TableItem tableItem : tableItems) {
            if(tableItem.getTitle().equals(name))
                return true;
        }
        return false;
    }

    private void changeVisibilityOfSpinner(int id, int visibility) {
        rootView.findViewById(id).setVisibility(visibility);
    }

    private void requestAvailableCountries(String continent) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                availableCountries = MapsforgeMapParser.getCountryLst(HttpRequestHandler.execute("http://download.mapsforge.org/maps/" + continent));
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                setUpCountrySpinner();
                changeVisibilityOfSpinner(R.id.spinner_country,View.VISIBLE);
                progressDialog.cancel();
            }
        }.execute();
        progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Getting available Countries");
    }

    private void requestAvailableRegions(String country) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... v) {
                availableRegions = MapsforgeMapParser.getCountryLst(HttpRequestHandler.execute("http://download.mapsforge.org/maps/" + continent + "/" + country));
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                setUpRegionSpinner();
                changeVisibilityOfSpinner(R.id.spinner_region,View.VISIBLE);
                progressDialog.cancel();
            }
        }.execute();
        progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Getting available Regions");
    }

    private void downloadMap(String path, String name, boolean isRoutingMap) {
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
        if(isRoutingMap)
            osmDownloadHelper.downloadRoutingMap(getActivity().getFilesDir(),name);
        else
            osmDownloadHelper.downloadMap(getActivity().getFilesDir(), path, name);
        progressDialog = DialogBuilder.buildProcessDialog(getActivity(), "Downloading Map");
    }

    private void changeToMapView(File map) {
        LocationListener locationListener = new LocationListener(getActivity());
        if(locationListener.isGPSEnabled()) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, MapFragment.newInstance(map));
            fragmentTransaction.commit();
        }
        else
            DialogBuilder.buildAlertMessageNoGps(getActivity());
    }

    private String[] getArrayWithPlaceholder(String placeholder, HashMap<String, Boolean> hashMap) {
        String[] strings = new String[hashMap.size()];
        hashMap.keySet().toArray(strings);
        Arrays.sort(strings);
        String[] newStrings = new String[strings.length+1];
        for(int i = newStrings.length-1; i > 0; i--) {
            newStrings[i] = strings[i-1];
        }
        newStrings[0] = placeholder;
        return newStrings;
    }


}
