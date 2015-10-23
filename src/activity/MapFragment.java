package activity;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.example.TravelCorpus_App.R;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import model.*;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.*;
import java.util.List;


public class MapFragment extends AbstractFragment {

    private static MapView mapView;
    private static File map;
    private GraphHopper hopper;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private LocationListener locationListener;
    private static Marker myLocation;
    private Marker destinationLocation;
    private boolean showUIElements;
    private View rootView;
    private static String givenDestination;
    private static AbstractFragment fragment;
    private static boolean isDrinkingTour;
    private boolean useCurrentPosition;

    public MapFragment() {

    }

    public static MapFragment newInstance(File mapFile) {
        MapFragment mapFragment = new MapFragment();
        map = mapFile;
        return mapFragment;
    }

    public static MapFragment newInstance(String destination, File mapFile, boolean isTour) {
        MapFragment mapFragment = new MapFragment();
        map = mapFile;
        givenDestination = destination;
        fragment = mapFragment;
        isDrinkingTour = isTour;
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        useCurrentPosition = false;
        locationListener = new LocationListener(getActivity()) {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);
                updateMap(location);
            }
        };
        locationListener.initLoopRequest();
        AndroidGraphicFactory.createInstance(getActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.openmapview);
        if(locationListener.getLocation() != null)
            updateMap(locationListener.getLocation());

        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        mapView.getModel().mapViewPosition.setZoomLevel((byte) 17);

        this.tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor());
        MapFile mapFile;
        if(map.isDirectory())
            mapFile = new MapFile(new File(map.getAbsolutePath() + "/" + map.getName() + ".map"));
        else
            mapFile = new MapFile(new File(map.getAbsolutePath()+".map"));
        MapDataStore mapDataStore = mapFile;
        showUIElements = true;
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE){
            @Override
            public boolean onLongPress( LatLong tapLatLong, Point layerXY, Point tapXY ) {
                return toggleUIElements();
            }
        };
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        Layers layers = mapView.getLayerManager().getLayers();
        myLocation = createMarker(new LatLong(0,0),R.drawable.ic_action_location);
        destinationLocation = createMarker(new LatLong(0,0), R.drawable.ic_action_destination_location);
        layers.add(destinationLocation);
        if(map.isDirectory())
            loadGraphStorage();

        setUpInputFields();
        layers.add(tileRendererLayer);
        layers.add(myLocation);

        if(givenDestination != null) {
            setUpBackButton();
            useCurrentPosition = true;
            calcualteStartAndDestination("Current Position",givenDestination.replaceAll("\\s+","+"));
        }
        return rootView;

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.getLayerManager().getLayers().remove(this.tileRendererLayer);
        this.tileRendererLayer.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationListener.stopUpdates();
        mapView.getLayerManager().getLayers().remove(this.tileRendererLayer);
        this.tileRendererLayer.onDestroy();
        this.tileCache.destroy();
        this.locationListener = null;
        mapView.getModel().mapViewPosition.destroy();
        mapView.destroy();
        AndroidGraphicFactory.clearResourceMemoryCache();
    }

    private boolean toggleUIElements() {
        showUIElements = !showUIElements;
        if(rootView.findViewById(R.id.routingContainer).getVisibility() == View.VISIBLE) {
            rootView.findViewById(R.id.routingContainer).setVisibility(View.INVISIBLE);
            return true;
        }
        if(showUIElements) {
            rootView.findViewById(R.id.myPositionButton).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.routeContainerButton).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.myPositionButton).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.routeContainerButton).setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private void updateMap(Location location) {
        LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
        mapView.getModel().mapViewPosition.setCenter(latLong);
        myLocation.setLatLong(latLong);
    }

    private void setUpInputFields() {
        LinearLayout routingContainer = (LinearLayout)rootView.findViewById(R.id.routingContainer);
        routingContainer.bringToFront();
        EditText editText_input = (EditText)rootView.findViewById(R.id.start_address);
        EditText editTextDestinationAddress = (EditText)rootView.findViewById(R.id.destination_address);
        TextView buttonCalculateRoute = (TextView)rootView.findViewById(R.id.buttonCalculateRoute);
        buttonCalculateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startAddress = editText_input.getText().toString().replaceAll("\\s+","+");
                String destinationAddress = editTextDestinationAddress.getText().toString().replaceAll("\\s+","+");
                if(startAddress.length() > 0 && destinationAddress.length() > 0) {
                    calcualteStartAndDestination(startAddress,destinationAddress);
                    routingContainer.setVisibility(View.INVISIBLE);
                    hideSoftKeyboard();
                }
            }
        });
        ImageView closeButton = (ImageView)rootView.findViewById(R.id.close_routingContainer);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routingContainer.setVisibility(View.INVISIBLE);
                hideSoftKeyboard();
            }
        });
        ImageView useMyPosition = (ImageView)rootView.findViewById(R.id.use_current_position);
        useMyPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useCurrentPosition = true;
                editText_input.setText("Current Position");
            }
        });

        ImageButton openRoutingContainer = (ImageButton)rootView.findViewById(R.id.routeContainerButton);
        openRoutingContainer.bringToFront();
        openRoutingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routingContainer.setVisibility(View.VISIBLE);
            }
        });

        ImageButton centerOnMyPosition = (ImageButton) rootView.findViewById(R.id.myPositionButton);
        centerOnMyPosition.bringToFront();
        centerOnMyPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationListener.getLocation() == null)
                    DialogBuilder.buildAlertDialog(getActivity(),"Can't find your location, please try again!");
                else
                    updateMap(locationListener.getLocation());
            }
        });
    }

    private void setUpBackButton() {
        ImageButton backButton = (ImageButton) rootView.findViewById(R.id.back_button);
        backButton.setVisibility(View.VISIBLE);
        backButton.bringToFront();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDrinkingTour) {
                    changeVisibilityOfContainer(View.INVISIBLE, R.id.nov_container);
                    changeVisibilityOfContainer(View.VISIBLE, R.id.ov_container);
                }
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void changeVisibilityOfContainer(int visibility, int containerId) {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(containerId);
        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            view.setVisibility(visibility);
        }
    }

    private void calcualteStartAndDestination(String start, String destination) {
        new AsyncTask<Void,Void,LatLong[]>() {
            @Override
            protected LatLong[] doInBackground(Void... params) {

                if (Settings.API_Key != null && !Settings.API_Key.isEmpty())
                    return  null;

                MyGeocoder myGeocoder = new MyGeocoder(Settings.API_Key);
                LatLong startLocation;
                if(useCurrentPosition) {
                    startLocation = myLocation.getLatLong();
                    useCurrentPosition = false;
                }
                else
                    startLocation = myGeocoder.getLatLong(start);
                LatLong destinationLocation = myGeocoder.getLatLong(destination);
                return new LatLong[]{startLocation,destinationLocation};
            }
            @Override
            protected void onPostExecute(LatLong[] latLongs) {
                LatLong start = latLongs[0];
                LatLong destination = latLongs[1];
                if(myLocation.getLatLong().compareTo(start) == 0) {
                    myLocation.setLatLong(start);
                }
                mapView.getModel().mapViewPosition.setCenter(latLongs[1]);
                calcPath(start.latitude,start.longitude,destination.latitude,destination.longitude);
            }
        }.execute();
    }

    private Marker createMarker( LatLong p, int resource ) {
        Drawable drawable = getResources().getDrawable(resource,getActivity().getTheme());
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(p, bitmap, 0, -bitmap.getHeight() / 2);
    }

    private void loadGraphStorage() {
        new AsyncTask<Void, Void, Path>() {
            @Override
            protected Path doInBackground(Void... params) {
                GraphHopper tmpHopp = new GraphHopper().forMobile();
                //tmpHopp.load(map.getAbsolutePath());
                tmpHopp.load(new File(getActivity().getFilesDir(), "/maps/" + map.getName()).getAbsolutePath());
                hopper = tmpHopp;
                return null;
            }
        }.execute();
    }

    private Polyline createPolyline( GHResponse response ) {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(Color.argb(200, 0, 0xCC, 0x33));
        paintStroke.setDashPathEffect(new float[] {25, 15});
        paintStroke.setStrokeWidth(8);
        Polyline line = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<LatLong> geoPoints = line.getLatLongs();
        PointList tmp = response.getPoints();
        for (int i = 0; i < response.getPoints().getSize(); i++) {
            geoPoints.add(new LatLong(tmp.getLatitude(i), tmp.getLongitude(i)));
        }
        return line;
    }

    public void calcPath( final double fromLat, final double fromLon,
                          final double toLat, final double toLon ) {
        new AsyncTask<Void, Void, GHResponse>() {
            float time;
            protected GHResponse doInBackground( Void... v ) {
                StopWatch sw = new StopWatch().start();
                GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                        setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);
                req.getHints().
                        put("instructions", "false");
                GHResponse resp = hopper.route(req);
                time = sw.stop().getSeconds();
                return resp;
            }
            protected void onPostExecute( GHResponse resp ) {
                if (!resp.hasErrors()) {
                    destinationLocation.setLatLong(new LatLong(toLat,toLon));
                    removePolyLineLayer();
                    mapView.getLayerManager().getLayers().add(createPolyline(resp));
                }
            }
        }.execute();
    }

    private void removePolyLineLayer() {
        for(Layer layer: mapView.getLayerManager().getLayers())
            if(layer instanceof Polyline)
                mapView.getLayerManager().getLayers().remove(layer);
    }

}
