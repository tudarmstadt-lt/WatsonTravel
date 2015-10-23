package model;

import jwatson.answer.Latlist;
import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class LocationLst
{
    List<Location> locationLst;


    public LocationLst()
    {
        locationLst = new ArrayList<Location>();
    }

    public LocationLst(List<Location> locationLst) {
        this.locationLst = locationLst;
    }

    public List<Location> getLocationLst() {
        return locationLst;
    }

    public Location getLocation(int index)
    {
        return this.locationLst.get(index);
    }


    /**
     * Add a new location to list
     * @param location
     */
    public void addLocation(Location location)
    {
        locationLst.add(location);
    }

    /**
     * Add a new LocationLst to LocationLst
     * @param listOfLocation
     */
    public void addLocations(List<Location> listOfLocation)
    {
        this.locationLst.addAll(listOfLocation);
    }

    /**
     * Remove item from list
     * @param index
     */
    public void removeLocation(int index)
    {
        this.locationLst.remove(index);
    }

    /**
     * Return a LocationLst filterd by the passing subCategory
     * @param subCategory
     * @return
     */
    public LocationLst filterBySubCategory(String subCategory)
    {
        LocationLst filteredLocationLst = new LocationLst();

        for (int i = 0; i < this.locationLst.size(); i++) {
            if(locationLst.get(i).getSubCategory() == subCategory)
                filteredLocationLst.addLocation(locationLst.get(i));
        }

        return  filteredLocationLst;
    }


    /**
     * Return a list of all sub categories which are in the LocationLst
     * @return
     */
    public List<String> getSubCategoryLst()
    {
        List<String> subCategories = new ArrayList<String>();

        for (int i = 0; i < locationLst.size(); i++) {
            String subCategory = locationLst.get(i).getSubCategory();

            if(subCategory != "" && !subCategories.contains(subCategory))
                subCategories.add(subCategory);
        }

        return  subCategories;
    }

    @Override
    public String toString() {
        return "LocationLst{" +
                "locationLst=" + locationLst +
                '}';
    }

    /**
     * Filter location list by the given parameters
     * @param ratingFrom location must be at least this rating couunt
     * @param choosenCategories location must be contains to choosen categories
     * @param startTime where the trinking tour starts
     * @param radius location must be
     * @return
     */
    public LocationLst filterWithConstraints(int ratingFrom, List<String> choosenCategories, Date startTime, LatLong startPos, int radius) {
        //TODO Sortierung nach Abstand
        //LatLong latLong = convertKmToLatLng(radius);
        LocationLst filteredList = new LocationLst();

        for (Location location : locationLst) {
            double rating   = location.getReviewList().getAverageRating();
            double distance = getDistanceInKM(startPos, location.getLatLong());
            //distance <= (double)radius

            if (ratingFrom <= rating && isInCategory(location,choosenCategories) &&
                    isInOpeningHours(location,startTime) && distance <= (double)radius) {
                filteredList.addLocation(location);
            }
        }

        filteredList = sortLstByDistance(filteredList, startPos);
        return filteredList;
    }

    /**
     *
     * @param filteredList
     * @return
     */
    private LocationLst sortLstByDistance(LocationLst filteredList, LatLong startPos) {
        LocationLst sortedLst = new LocationLst();
        Location location;

        while (filteredList.size() > 0)
        {
            int index = getNextLocation(filteredList, startPos);
            if(index == Integer.MIN_VALUE)
                break;
            else {
                location = filteredList.getLocation(index);
                startPos = location.getLatLong();
                sortedLst.addLocation(location);
                filteredList.removeLocation(index);
            }
        }

        return sortedLst;
    }

    /**
     * Return index of next location
     * @param filteredList
     * @param startPos
     * @return
     */
    private int getNextLocation(LocationLst filteredList, LatLong startPos)
    {
        int index = Integer.MIN_VALUE;
        double oldDistance = Double.MAX_VALUE;

        if (filteredList.size() == 0)
            return index;

        // get next location
        for (int i = 0; i < filteredList.size(); i++) {
            double newDistance = getDistanceInKM(startPos, filteredList.getLocation(i).getLatLong());

            if(oldDistance > newDistance) {
                oldDistance = newDistance;
                index = i;
            }
        }
        return index;
    }

    /**
     * Check, if location has the right category
     * @param location
     * @param choosenCategories
     * @return
     */
    private boolean isInCategory(Location location, List<String> choosenCategories) {
        for(String category : choosenCategories) {
            if(location.getSubCategory().equals(category) || location.getSubCategory() == category)
                return true;
        }
        return false;
    }

    /**
     * Calc the distance between to longitudes and latitudes
     * @param latLongA
     * @param latLongB
     * @return distance in km
     */
    private double getDistanceInKM(LatLong latLongA, LatLong latLongB)
    {
        double distance = 500;

        double lat;
        double dx;
        double dy;

        if(latLongA != null && latLongB != null){
            lat = (latLongA.latitude + latLongB.latitude) / 2 * 0.01745;
            dx  = 111.3 * Math.cos(lat) * (latLongA.longitude - latLongB.longitude);
            dy  = 111.3 * (latLongA.latitude - latLongB.latitude);
            distance = Math.sqrt(dx * dx + dy * dy);
        }

        return distance;
    }

    /**
     *
     * @param km
     * @return
     */
    private LatLong convertKmToLatLng(int km) {
        double lat = (1/110.574) * km;
        double lng = (1/(111.320*Math.cos(lat))) * km;
        return new LatLong(lat,lng);
    }

    /**
     *
     * @param location
     * @param startTime
     * @return
     */
    private boolean isInOpeningHours(Location location, Date startTime) {
        System.out.println(startTime.toString());
        if (location.getOpeningHours() == null)
            return true;
        OpeningHour currentOpeningHour = location.getOpeningHours().getCurrentOpeningHour();
        if(currentOpeningHour != null)
            System.out.println(currentOpeningHour.toString());
        else
            System.out.println("Current Hour is empty! DAFUQ ?");

        return currentOpeningHour != null && currentOpeningHour.isInOpeningHour(startTime);
    }

    /**
     * size of the location lst
     * @return
     */
    public int size() {
        return this.locationLst.size();
    }
}
