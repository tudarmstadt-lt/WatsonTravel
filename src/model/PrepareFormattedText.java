package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martina on 31.08.2015.
 */
public class PrepareFormattedText {

    /**
     * Delete html tags and prepare text
     * @param text
     * @return
     */
    public static String prepareText(String text)
    {
        Pattern pattern;
        Matcher matcher;

        //delete links
        pattern = Pattern.compile("<a href=\"(.+)\">(.*)</a>");
        matcher = pattern.matcher(text);

        while (matcher.find())
        {
            text = text.replace(matcher.group(0), matcher.group(2) + " (" + matcher.group(1) + ")");
        }

        pattern = Pattern.compile("(<[a-z]+>)|(</[a-z]+>)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(text);

        // delete html tags
        while (matcher.find())
        {
            text = text.replace(matcher.group(0), "");
        }

        text = text.trim();

        if(text.length()>=1) {
            if (text.substring(0, 1).equals("."))
                text = text.replace(".", "");

            else if (text.substring(0, 1).equals("-"))
                text = text.replace("-", "");

            else if (text.substring(0, 1).equals(","))
                text = text.replace(",", "");
        }

        if(text.equals("."))
            text = "";
        return text.trim();
    }

    /**
     *
     * @param text
     * @return
     */
    public static String replaceHtmlInUnicode(String text)
    {
        text = text.replace("&quot;","\"");
        text = text.replace("&amp;","&");
        text = text.replace("&lt;","<");
        text = text.replace("&gt;",">");
        text = text.replace("&apos;","'");


        return text.replaceAll("\\s&[a-z]+;\\s"," ");
    }

    /**
     *
     * @param item
     * @return
     */
    public static String replaceMutatedVowel(String item) {
        item = item.replace("AE","Ae");
        item = item.replace("UE","Ue");
        item = item.replace("OE","Oe");
        /*item = item.replace("ae","\u00e4");
        item = item.replace("ue","\u00fc");
        item = item.replace("oe","\u00f6");*/
        item = item.replace("SS","ss");

        return item;
    }
}

