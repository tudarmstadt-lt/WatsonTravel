package model;

import android.location.Address;
import android.location.Location;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.model.LatLong;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyGeocoder {

    //private final String API_KEY = "AIzaSyBFDjNfBXoZw31PZ3MnYcdvqelu8s5GBFU";

    private  String apiKey;
    public MyGeocoder(String apiKey)
    {
        this.apiKey = apiKey;
    }

    /**
     * Send a request to google an check if the api key is valid
     * @return
     */
    public boolean validApiKey()
    {
        String requestAddress;
        String content;
        boolean keyIsValid = false;

        // send google request to to check if api key is valid
        requestAddress = "https://maps.googleapis.com/maps/api/geocode/json?address=" + "" + "&key=" + apiKey;
        content        = HttpsRequestHandler.execute(requestAddress);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(content);
            if ("REQUEST_DENIED".equalsIgnoreCase(jsonObject.getString("status")))
                keyIsValid = false;
            else
                keyIsValid = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return keyIsValid;
    }

    /**
     * Send a google request with lat/lng to get the city name
     * @param location
     * @return
     */
    public String getCityName(Location location) {
        List<Address> addresses = getAddressFromContent(HttpsRequestHandler.execute("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&key=" + apiKey));
        try {
            if (addresses.size() > 0) {
                return addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get lat, lng depends on address
     * @param address
     * @return
     */
    public LatLong getLatLong(String address) {
        return getLatLongFromContent(HttpsRequestHandler.execute("https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey));
    }

    /**
     *
     * @param location
     */
    public void setAdditionalInformationsToLocation(model.Location location) {
        String parameters = location.getName()+"+"+location.getCity();
        String placeId = getPlaceIdFromContent(HttpsRequestHandler.execute("https://maps.googleapis.com/maps/api/place/textsearch/json?query="+parameters.replaceAll("\\s+", "+")+"&key="+apiKey));
        if(placeId != null) {
            String content = HttpsRequestHandler.execute("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&language=en&key=" + apiKey);
            setAdditionalInformations(location,content);
        }
    }

    /**
     * Extract Address from content
     * @param content
     * @return
     */
    private List<Address> getAddressFromContent(String content) {
        List<Address> addresses = new ArrayList<>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(content);
            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    JSONArray address_components = result.getJSONArray("address_components");
                    Address address = new Address(Locale.getDefault());
                    for(int x = 0; x < address_components.length(); x++) {
                        JSONObject addressComponent = address_components.getJSONObject(x);
                        String type = addressComponent.getJSONArray("types").getString(0);
                        String longName = addressComponent.getString("long_name");
                        switch (type) {
                            case "locality": address.setLocality(longName); break;
                            case "administrative_area_level_1": address.setAdminArea(longName); break;
                            case "country": address.setCountryName(longName); break;
                            case "postal_code": address.setPostalCode(longName); break;
                        }
                    }
                    address.setAddressLine(0, result.getString("formatted_address"));
                    addresses.add(address);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private String getPlaceIdFromContent(String content) {
        if(content == null)
            return null;
        JSONObject jsonObject;
        String placeId = null;
        try {
            jsonObject = new JSONObject(content);
            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
                placeId = result.getString("place_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeId;
    }

    /**
     * Complete location with google data
      * @param location
     * @param content
     */
    private void setAdditionalInformations(model.Location location, String content) {
        JSONObject jsonObject;
        ReviewList reviewList = new ReviewList();
        OpeningHours locationOpeningHours = new OpeningHours();
        String phoneNumber = null;
        String address = null;
        LatLong latLong = null;

        try {
            jsonObject = new JSONObject(content);
            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONObject result    = jsonObject.getJSONObject("result");
                reviewList           = getRatings(result);
                locationOpeningHours = getOpeningHours(result);
                phoneNumber          = getPhoneNumber(result);
                address              = getAddress(result);
                latLong              = getLatLongFromContent(result);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        location.setReviewList  (reviewList);
        location.setOpeningHours(locationOpeningHours);
        location.setLatLong     (latLong);
        if(location.getAddress() == null || location.getAddress().length() == 0)
            location.setAddress(address);
        if(location.getPhone() == null || location.getPhone().length() == 0)
            location.setPhone(phoneNumber);
    }

     /**
     * Extract rating information from JSON Objekct
     * @param result
     * @return
     * @throws JSONException
     */
    private ReviewList getRatings(JSONObject result) throws JSONException {
        ReviewList reviewList = new ReviewList();
        reviewList.setAverageRating(result.getDouble("rating"));
        JSONArray reviews = result.getJSONArray("reviews");
        for(int i = 0; i < reviews.length(); i++) {
            Review review = new Review();
            JSONObject reviewObject = reviews.getJSONObject(i);
            review.setAuthorName(reviewObject.getString("author_name"));
            review.setDate(new java.util.Date((long)reviewObject.getLong("time")*1000));
            review.setRating(reviewObject.getInt("rating"));
            review.setText(reviewObject.getString("text"));
            reviewList.add(review);
        }
        return reviewList;
    }

    /**
     * Extract Opening Hours from JSON object
     * @param result
     * @return
     * @throws JSONException
     */
    private OpeningHours getOpeningHours(JSONObject result) throws JSONException {
        OpeningHours locationOpeningHours = new OpeningHours();
        if(result.isNull("opening_hours"))
            return null;
        JSONObject openingHours = result.getJSONObject("opening_hours");
        if(openingHours.isNull("weekday_text"))
            return null;
        JSONArray weekday = openingHours.getJSONArray("weekday_text");
        for(int i = 0; i < weekday.length(); i++) {
            OpeningHour openingHour = getOpeningHour(weekday.getString(i));
            switch (i) {
                case 0: locationOpeningHours.setMonday(openingHour); break;
                case 1: locationOpeningHours.setTuesday(openingHour); break;
                case 2: locationOpeningHours.setWednesday(openingHour); break;
                case 3: locationOpeningHours.setThursday(openingHour); break;
                case 4: locationOpeningHours.setFriday(openingHour); break;
                case 5: locationOpeningHours.setSaturday(openingHour); break;
                case 6: locationOpeningHours.setSunday(openingHour); break;
            }
        }
        return locationOpeningHours;
    }

    /**
     * Extract address from JSON Object
     * @param result
     * @return
     * @throws JSONException
     */
    private String getAddress(JSONObject result) throws JSONException {
        if(result.isNull("formatted_address"))
            return null;
        String formattedAddress = result.getString("formatted_address");
        return formattedAddress.substring(0,formattedAddress.indexOf(','));
    }

    /**
     * Extract phone number form JSON Object
     * @param result
     * @return
     * @throws JSONException
     */
    private String getPhoneNumber(JSONObject result) throws JSONException {
        if(result.isNull("formatted_phone_number"))
            return null;
        return result.getString("formatted_phone_number");
    }

    //TODO kann man beide funktionen zusammenfassen???
    private LatLong getLatLongFromContent(JSONObject result) {
        LatLong latLong = null;

        JSONObject geometry = null;
        try {
            geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            latLong = new LatLong(lat,lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return latLong;
    }


    /**
     * Extract latitude and longatude from content
     * @param content
     * @return
     */
    private LatLong getLatLongFromContent(String content) {
        JSONObject jsonObject;
        LatLong latLong = null;
        try {
            jsonObject = new JSONObject(content);
            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                latLong = new LatLong(lat,lng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return latLong;
    }

    /**
     * Extract openening hour from string
     * @param text
     * @return
     */
    private OpeningHour getOpeningHour(String text) {
        //Pattern pattern = Pattern.compile("([0-9]{1,2}:[0-9]{1,2}\\sam|pm)\\s.\\s([0-9]{1,2}:[0-9]{1,2}\\sam|pm)");
        Pattern pattern = Pattern.compile("[0-9]{1,2}:[0-9]{1,2}\\s(am|pm)");
        Matcher matcher = pattern.matcher(text);
        String from = null;
        String until = null;
        int index =0;
        /*if(matcher.find()) {
            from = matcher.group(1);
            until = matcher.group(2);
        }*/
        while(matcher.find()) {
            if(index == 0)
                from = matcher.group(0);
            if(index == 1)
                until = matcher.group(0);
            index++;
            if(index == 2)
                break;
        }
        if(from == null || until == null)
            return null;
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        Date fromDate = null;
        Date untilDate = null;
        Calendar currentCal = Calendar.getInstance();
        try {
            Calendar runCal = Calendar.getInstance();
            fromDate = dateFormat.parse(from);
            runCal.setTime(fromDate);
            runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
            runCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
            runCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
            fromDate = runCal.getTime();
            untilDate = dateFormat.parse(until);
            runCal.setTime(untilDate);
            if(until.contains("am") || until.contains("AM"))
                runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH)+1);
            else
                runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
            runCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
            runCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
            untilDate = runCal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new OpeningHour(fromDate,untilDate);
    }
}
