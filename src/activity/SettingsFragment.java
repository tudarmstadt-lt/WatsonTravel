package activity;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.example.TravelCorpus_App.R;
import model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private List<String> questions;
    private List<String> maps;
    private List<String> apiKeys;
    private ArrayAdapter<String> questionListAdapter;
    private ArrayAdapter<String> mapListAdapter;
    private ArrayAdapter<String> googleApiKeyAdapter;
    private static FragmentDrawer fragmentDrawer;
    private ListView listView;
    private DbHelper questionDbHelper;
    private DbHelper mapDbHelper;
    private DbHelper apiDBHelper;
    private List<TableItem> questionList;
    private List<TableItem> mapList;
    private List<TableItem> apiKeyLst;

    public static SettingsFragment newInstance(FragmentDrawer drawer) {
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentDrawer = drawer;
        return settingsFragment;
    }

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //question
        questionDbHelper = new QuestionTableDbHelper(getActivity());
        questionList     = questionDbHelper.getTableItems();
        questions        = new ArrayList<>();
        for(TableItem tableItem : questionList) {
            Question question = (Question) tableItem;
            questions.add(question.getTitle());
        }

        //maps
        mapDbHelper = new MapTableDbHelper(getActivity());
        mapList     = mapDbHelper.getTableItems();
        maps        = new ArrayList<>();
        for(TableItem tableItem : mapList) {
            Map map = (Map) tableItem;
            maps.add(map.getTitle() + "  |  " + map.getSize() + "MB");
        }

        //API key
        apiDBHelper = new GoogleAPITableDBHelper(getActivity());
        apiKeyLst   = apiDBHelper.getTableItems();
        apiKeys     = new ArrayList<>();
        for(TableItem tableItem : apiKeyLst) {
            GoogleAPI apiKey = (GoogleAPI) tableItem;
            apiKeys.add(apiKey.getApi_key());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        setUpQuestionList    (rootView);
        setUpMapsList        (rootView);
        setUpGoogleApiKeyBtn (rootView);
        setUpGoogleAPIList   (rootView);
        return rootView;
    }

    private void setUpQuestionList(View rootView) {
        // Inflate the layout for this fragment
        listView = (ListView) rootView.findViewById(R.id.listview_questions);
        questionListAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, questions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                int paddingLeft = item.getPaddingLeft();
                item.setOnTouchListener(new OnSwipeTouchListener(getActivity(),250) {
                    @Override
                    public void onAbortAction() {
                        item.setPadding(paddingLeft,0,0,0);
                    }

                    @Override
                    public void onSwipeRight(float offset) {
                        item.setPadding((int)offset, 0, 0, 0);
                    }

                    @Override
                    public void onSwipeLeft(float offset) {

                    }

                    @Override
                    public void onSwipeRightRelease() {
                        item.setPadding(paddingLeft,0,0,0);
                        questionDbHelper.removeTableItem(questionList.get(position));
                        removeListItem(item, position,questionListAdapter,questions,true);
                    }

                    @Override
                    public void onSwipeLeftRelease() {

                    }
                });
                return item;
            }
        };
        listView.setAdapter(questionListAdapter);
        Fragment fragment = this;
        ImageView addQuestionButton = (ImageView) rootView.findViewById(R.id.button_add_question);
        addQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body,QuestionFragment.newInstance(fragmentDrawer, questionDbHelper, questions, questionListAdapter));
                fragmentTransaction.commit();
            }
        });
    }

    private void setUpGoogleApiKeyBtn(View rootView) {
        ImageView addApiKeyButton = (ImageView) rootView.findViewById(R.id.button_add_api_key);
        addApiKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogBuilderWithView builderWithView = DialogBuilder.buildApiKeyDialog(getActivity());
                builderWithView.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(apiKeys.size() == 0)
                        {
                            EditText editText = (EditText) builderWithView.getView().findViewById(R.id.key);
                            new AsyncTask<String,Void,Boolean>() {
                                @Override
                                protected Boolean doInBackground(String... v) {
                                   return isApiKeyValid(v[0]);
                                }
                                @Override
                                protected void onPostExecute(Boolean valid) {
                                    dialog.cancel();
                                    if(valid) {
                                        GoogleAPITableDBHelper tableDBHelper = new GoogleAPITableDBHelper(getActivity());
                                        GoogleAPI googleAPI = new GoogleAPI(editText.getText().toString(), "User");
                                        tableDBHelper.addTableItem(googleAPI);
                                        Settings.API_Key = editText.getText().toString();
                                        apiKeys.add(Settings.API_Key);
                                        googleApiKeyAdapter.notifyDataSetChanged();
                                    }
                                    else
                                        DialogBuilder.buildAlertDialog(getActivity(),"Invalid API Key, please enter a new one.");
                                }
                            }.execute(editText.getText().toString());
                        }
                    }
                });
                builderWithView.create().show();
            }
        });
    }

    /**
     *
     * @param text
     * @return
     */
    private boolean isApiKeyValid(String text)
    {
       MyGeocoder geocoder = new MyGeocoder(text);
        return geocoder.validApiKey();
    }

    private void setUpMapsList(View rootView) {
        listView = (ListView) rootView.findViewById(R.id.listview_maps);
        mapListAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, maps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                int paddingLeft = item.getPaddingLeft();
                item.setOnTouchListener(new OnSwipeTouchListener(getActivity(),250) {
                    @Override
                    public void onAbortAction() {
                        item.setPadding(paddingLeft,0,0,0);
                    }

                    @Override
                    public void onSwipeRight(float offset) {
                        item.setPadding((int)offset, 0, 0, 0);
                    }

                    @Override
                    public void onSwipeLeft(float offset) {

                    }

                    @Override
                    public void onSwipeRightRelease() {
                        item.setPadding(paddingLeft,0,0,0);
                        File directory = new File(getActivity().getFilesDir()+"/maps",mapList.get(position).getTitle());
                        if(directory.exists() && directory.isDirectory())
                            deleteRecursive(directory);
                        else
                            deleteRecursive(new File(getActivity().getFilesDir()+"/maps/",mapList.get(position).getTitle()+".map"));
                        mapDbHelper.removeTableItem(mapList.get(position));
                        removeListItem(item, position, mapListAdapter, maps, false);
                    }

                    @Override
                    public void onSwipeLeftRelease() {

                    }
                });
                return item;
            }
        };
        listView.setAdapter(mapListAdapter);
    }

    private void setUpGoogleAPIList(View rootView) {
        listView = (ListView) rootView.findViewById(R.id.listview_api_keys);
        googleApiKeyAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, apiKeys) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                int paddingLeft = item.getPaddingLeft();
                item.setOnTouchListener(new OnSwipeTouchListener(getActivity(),250) {
                    @Override
                    public void onAbortAction() {
                        item.setPadding(paddingLeft,0,0,0);
                    }

                    @Override
                    public void onSwipeRight(float offset) {
                        item.setPadding((int)offset, 0, 0, 0);
                    }

                    @Override
                    public void onSwipeLeft(float offset) {

                    }

                    @Override
                    public void onSwipeRightRelease() {
                        item.setPadding(paddingLeft,0,0,0);

                        apiDBHelper.removeTableItem(apiKeyLst.get(position));
                        System.out.println(apiKeyLst.get(position));
                        removeListItem(item, position, googleApiKeyAdapter, apiKeys, false);
                    }

                    @Override
                    public void onSwipeLeftRelease() {

                    }
                });
                return item;
            }
        };
        listView.setAdapter(googleApiKeyAdapter);
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    protected void removeListItem(View rowView, final int positon, ArrayAdapter<String> arrayAdapter, List<String> list, boolean notifyFragmentDrawer) {
        final Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
        animation.setDuration(1000);
        rowView.startAnimation(animation);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.remove(positon);
                arrayAdapter.notifyDataSetChanged();
                if(notifyFragmentDrawer) fragmentDrawer.delete(positon);
                animation.cancel();
            }
        },1000);
    }

}
