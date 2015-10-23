package model;

import jwatson.answer.Answer;
import jwatson.answer.Evidencelist;
import jwatson.answer.WatsonAnswer;
import model.DefinedQuestions;
import model.LocationLst;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martina on 30.05.2015.
 */
public class JWatsonAnswerParser
{
    private DefinedQuestions question;
    private WatsonAnswer watsonAnswer;

    //=======================================
    // Getter and setter
    //=======================================

    public WatsonAnswer getWatsonAnswer() {
        return watsonAnswer;
    }

    public void setWatsonAnswer(WatsonAnswer watsonAnswer) {
        this.watsonAnswer = watsonAnswer;
    }

    //=======================================
    // Constructor
    //=======================================


    /**
     *
     * @param question
     * @param watsonAnswer
     */
    public JWatsonAnswerParser(DefinedQuestions question, WatsonAnswer watsonAnswer)
    {
        this.question       = question;
        this.watsonAnswer   = watsonAnswer;
    }

    //=======================================
    // Functions
    //=======================================s

    /**
     * Deletes all items without the right city, category, subcategory
     * and create a list of all subcategories
     */
    public LocationLst parseAnswer()
    {
        List<String> subCategoryLst = getSubCategoryLstByEvidenceLst();
        return parseHtmlAnswer(subCategoryLst);
    }

    /**
     * Return a list of all sub catageories that inculdes category and city
     * @return
     */
    public List<String> getSubCategoryLstByEvidenceLst()
    {
        String title;
        List<String> subCategoryLst = new ArrayList<String>();
        List<Evidencelist> tmpEvidenceLst = this.watsonAnswer.getAnswerInformation().getEvidencelist();

        for (int i = 0; i < tmpEvidenceLst.size(); i++)
        {
            title   = tmpEvidenceLst.get(i).getTitle();
            String subCategory = getSubCategoryFromTitle(title);

            if(subCategory != null && !subCategoryLst.contains(subCategory))
                subCategoryLst.add(subCategory);
        }

        return subCategoryLst;
    }

    /**
     * Extract sub  category from title
     * @param text
     * @return
     */
    private String getSubCategoryFromTitle(String text)
    {

        Matcher matcher;
        String subCategory = null;
        String sPattern    = String.format("(%s\\s:\\s){1,2}%s\\s:\\s(.+)", this.question.getCity(), this.question.getCategory());
        Pattern pattern    = Pattern.compile(sPattern, Pattern.CASE_INSENSITIVE);

        matcher = pattern.matcher(text);

        if(matcher.find()){
            subCategory    = matcher.group(2);
            int colonIndex = subCategory.indexOf(":");

            if(colonIndex >= 0)
                subCategory = subCategory.substring(0, colonIndex).trim();
        }

        return  subCategory;
    }

    /**
     *
     */
    public LocationLst parseHtmlAnswer(List<String> subCategoryLst)
    {
        String title;
        List<Answer> answerLst = this.watsonAnswer.getAnswerInformation().getAnswers();
        LocationLst result     = new LocationLst();

        for (int i = 0; i < answerLst.size(); i++)
        {
            String answer = answerLst.get(i).getFormattedText().replace("\n","");

            //get sub category
            Pattern pattern = Pattern.compile("<h1 class=\"topicTitle\">(.+?)</h1>");
            Matcher matcher = pattern.matcher(answer);

            if(matcher.find()) {
                Boolean containsToCategory = false;
                title = matcher.group(1);
                String tmp = getSubCategoryFromTitle(title);

                // City:City:Category:SubCategory:
                if(tmp != null)
                    title = tmp;

                if (subCategoryLst.contains(title))
                    containsToCategory = true;
                else if(title.toUpperCase().contains(this.question.getCategoryName().toUpperCase()))
                {
                    containsToCategory = true;
                    title = null;
                }

                if(containsToCategory)
                {
                    List<Location> locations = extractLocationsInfo(answer, title);

                    if (locations.size() > 0) {
                        result.addLocations(locations);
                    }
                }

            }
        }

        return result;
    }

    /**
     * Extract location information from html text
     * @param answer
     * @param subCategory
     * @return
     */
    private List<Location> extractLocationsInfo(String answer, String subCategory)
    {
        List<Location> locationLst = new ArrayList<Location>();
        Pattern pattern = Pattern.compile("<li>(.+?)</li>");
        Matcher matcher = pattern.matcher(answer);

        //extract information for all list items
        while (matcher.find())
        {

            Location location = new Location();
            String tmp;
            String item;
            Matcher itemMatcher;
            String originalItem;

            item = matcher.group(1);
            item = PrepareFormattedText.replaceHtmlInUnicode(item);
            item = PrepareFormattedText.replaceMutatedVowel(item);
            originalItem = item;

            location.setCity    (this.question.getCity());
            location.setCategory(this.question.getCategoryName());

            if(subCategory != null)
                location.setSubCategory(subCategory);
            else
                location.setSubCategory(useExistingSubCategory(item));


            //===================================================================

            // als location name wird der erste text zwischen <b></b> genommen
            int index = item.indexOf("</b>");

            if(item.length() < index + 6)
                index +=4; //index + lengthOf(</b>)
            else
                index +=6;

            tmp = item.substring(0, index);
            pattern = Pattern.compile("<b>(<a\\shref=\"(.+)\">)(.+)</a></b>(\\.|,)?\\s?");
            itemMatcher = pattern.matcher(tmp);
            if (itemMatcher.find()) {
                if(item.indexOf(itemMatcher.group(0)) < 10) {
                    location.setName(PrepareFormattedText.replaceMutatedVowel(itemMatcher.group(3)));
                    location.setWebsite(itemMatcher.group(2));
                }
                // delete link from text
                item = item.replace(itemMatcher.group(0), "");
            }
            else
            {
                pattern     = Pattern.compile("<b>(.+)</b>(\\.|,)?\\s?");
                itemMatcher = pattern.matcher(tmp);

                if (itemMatcher.find()) {
                    location.setName(itemMatcher.group(1));
                    item = item.replace(itemMatcher.group(0), "");
                }
            }

            //get location name and website
//            if (index > 0) {
//                pattern = Pattern.compile("<b>(<a\\shref=\"(.+)\">)(.+)</a></b>(\\.|,)?\\s?");
//                tmp     = item.substring(0, index);
//                itemMatcher = pattern.matcher(tmp);
//
//                if (itemMatcher.find()) {
//                    if(item.indexOf(itemMatcher.group(0)) < 10) {
//                        location.setName(PrepareFormattedText.replaceMutatedVowel(itemMatcher.group(3)));
//                        location.setWebsite(itemMatcher.group(2));
//                    }
//                    // delete link from text
//                    item = item.replace(itemMatcher.group(0), "");
//                }
//            }
//            else
//            {
//                pattern     = Pattern.compile("<b>(.+)</b>(\\.|,)?\\s?");
//                tmp         = item.substring(0, index);
//                itemMatcher = pattern.matcher(tmp);
//
//                if (itemMatcher.find()) {
//                    location.setName(itemMatcher.group(1));
//                    item = item.replace(itemMatcher.group(0), "");
//                }
//
//            }
            //===================================================================

            item = extractAddress    (item, originalItem, location);
            item = extractPhoneNumber(item, location);
            item = extractFaxNumber  (item, location);
            extractAdditionalInfo    (item, location);

            if (location.getName() != null)
                locationLst.add(location);

        }

        return locationLst;
    }

    private void extractAdditionalInfo(String item, Location location) {
        String additionalText = "";
        int position;

        position = item.indexOf(".");
        if(position + 2 < item.length())
            additionalText = item.substring(position + 2);

        location.setAdditionalInfo(PrepareFormattedText.prepareText(item));
    }


    /**
     *
     * @param item
     * @param location
     * @return
     */
    private String extractAddress(String item, String originalItem, Location location) {

        StreetNameParser streetParser = new StreetNameParser();
        String streetName;


        streetName = streetParser.getAddress(originalItem);
        streetName = streetName.replace("</b>", "");
        item       = item.replace(streetName, "");
        streetName = streetName.replace(",", "").trim();

        if(streetName.endsWith("."))
            streetName = streetName.substring(0, streetName.length()-1);

        location.setAddress(streetName);
        return  item;
    }

    /**
     * If no sub category is available use the pre-define sub categories
     * @param item
     * @return
     */
    private String useExistingSubCategory(String item) {

        String subCategory = "Other";
        int count = 0;
        Pattern pattern;
        Matcher matcher;
        ArrayList<String> subCategoryLst = question.getSubCategoryLst();

        for (int i = 0; i < subCategoryLst.size()-1; i++) {
            int actualCount = 0;
            pattern = Pattern.compile(subCategoryLst.get(i),Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(item);

            while (matcher.find())
            {
                actualCount++;
            }

            if(actualCount > count)
                subCategory = subCategoryLst.get(i);
        }

        return subCategory;
    }

    public String extractPhoneNumber(String text, Location location)
    {
        //number with tel symbol prefix
        Pattern pattern = Pattern.compile("(&#9742;)\\s(\\+([0-9]|\\s|-)+)(\\.|,)?\\s?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            location.setPhone (matcher.group(2));
            return  text.replace(matcher.group(0), "");
        }

        pattern = Pattern.compile("Tel:([/\\s\\-\\+0-9]+)(\\.|,)?\\s");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            location.setPhone(matcher.group(1));
            return  text.replace(matcher.group(0), "");
        }

        return  text;
    }

    public String extractFaxNumber(String text, Location location)
    {
        Pattern pattern = Pattern.compile("fax:([/\\s\\-\\+0-9]+)(\\.|,)?\\s");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            location.setFax(matcher.group(1));
            return  text.replace(matcher.group(0), "");
        }

        return  text;
    }

}
