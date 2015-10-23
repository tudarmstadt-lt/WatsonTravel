package model;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsforgeMapParser {
    public static HashMap<String, Boolean> getCountryLst(String text)
    {
        HashMap<String, Boolean> countryLst = new HashMap<String, Boolean>();
        Pattern pattern = Pattern.compile("<tr>(.+)</tr>");
        Matcher matcher = pattern.matcher(text);

        //get each table row and extracts country name
        while (matcher.find())
        {
            //countryLst.add(matcher.group(2));
            String tmp = matcher.group(1);
            Boolean isDir;

            pattern = Pattern.compile("<a href=\".+>(.+)((.map)|(/))</a>");
            Matcher countryMatcher = pattern.matcher(tmp);
            if(countryMatcher.find()) {
                String country = countryMatcher.group(1);

                isDir = tmp.contains("[DIR]");
                if (countryLst.containsKey(country)) {
                    if (isDir)
                        countryLst.put(country, isDir);
                }
                else
                    countryLst.put(country, isDir);
            }
        }

        return countryLst;
    }
}
