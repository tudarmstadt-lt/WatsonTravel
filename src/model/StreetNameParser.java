package model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martina on 13.10.2015.
 */
public class StreetNameParser {

    public String getAddress(String text)
    {
        String address;

        //======================================================
//        String startWord = "</b>,";
//        int endIndex;
//
//        endIndex = text.indexOf("</b>,") + startWord.length();
//        endIndex = text.indexOf(",", endIndex)+1;
//        if(endIndex >= startWord.length()) {
//            String tmp = text.substring(0, endIndex);
//            address = addressWithPattern(tmp, Pattern.compile("</b>, (.+),", Pattern.CASE_INSENSITIVE));
//            if (address != "") {
//                return address.replace(startWord + " ", "");
//            }
//        }
        //======================================================


        //first of all check words with prefix
        address = addressWithPatternLst(text, getPrefixPattern());
        if(address != "")
            return address;

//        //Pattern xxx-xx 25 --> should checked
//        address = addressWithPattern(text, Pattern.compile("(([A-Z][\\-A-Za-z]+)\\s[-0-9]+)(\\.|,)?\\s?", Pattern.CASE_INSENSITIVE));
//        if(address != "")
//            return address;


        return address;
    }

    private String addressWithPatternLst(String text, ArrayList<Pattern> streetPattern)
    {
        String address = "";
        ArrayList<String> addressLst = new ArrayList<String>();
        for(Pattern pattern : streetPattern) {

            address = addressWithPattern(text, pattern);
            if (address != "")
                addressLst.add(address);

        }

        //use address with nummber
        for(String ad : addressLst)
            if(addressWithPattern(ad, Pattern.compile("[0-9]+")) != "")
                address = ad;


        if(address == "" && addressLst.size()>0)
            address = addressLst.get(0);

        return address;
    }

    private String addressWithPattern(String text, Pattern streetPattern)
    {
        Matcher matcher = streetPattern.matcher(text);
        if(matcher.find())
            return matcher.group(0);

        return "";
    }

    private ArrayList<Pattern> getPrefixPattern()
    {
        ArrayList<Pattern> streetPattern = new ArrayList<Pattern>();
        String pattern;
        String[] prefix = {"Strasse", "strasse", "street", "allee", "weg", "gasse", "platz",
                "Street", "Allee", "Platz", "str.", "Str."};

        pattern  = "(([A-Z][\\-A-Za-z]+)\\s?({0})\\s?[-0-9]*)(\\.|,)?\\s?";
        for (int i = 0; i < prefix.length; i++) {
            streetPattern.add(Pattern.compile(MessageFormat.format(pattern, prefix[i])));
        }

        pattern = "[A-Z][\\-A-Za-z]+\\sSt(\\.|,)\\s?";
        streetPattern.add(Pattern.compile(pattern));

        pattern = "[0-9]+\\s[\\-A-Za-z\\s]+\\sSt(\\.|,)\\s?";
        streetPattern.add(Pattern.compile(pattern));

        pattern = "[0-9]+\\s[\\-A-Za-z\\s]+\\sStreet(\\.|,)\\s?";
        streetPattern.add(Pattern.compile(pattern));


        //streetPattern.add(Pattern.compile("(([A-Z][\\-A-Za-z]+)\\s?(str.|strasse)\\s?[-0-9]+)(\\.|,)?\\s?", Pattern.CASE_INSENSITIVE));


        return streetPattern;
    }
}
