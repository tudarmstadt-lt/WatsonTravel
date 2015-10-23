package model;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Martina on 05.08.2015.
 */
public class DefinedQuestions {

    public enum Category {
        DRINK   ("Drink"),
        EAT     ("Eat"),
        SLEEP   ("Sleep"),
        SEE     ("See"),
        GET_IN  ("Get In"),
        DO      ("Do"),
        BUY     ("Buy");

        private final String name;

        private Category(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

    // ======================================
    //  Question texts
    // ======================================
    private static final String QUESTION_DRINK      = "Where can I drink in {0}?";
    private static final String QUESTION_EAT        = "Where can I eat in {0}?";
    private static final String QUESTION_SLEEP      = "Where can I sleep in {0}?";
    private static final String QUESTION_SEE        = "What can I see in {0}?";
    private static final String QUESTION_TRAVEL_BY  = "Get in {0} by {1}";
    private static final String QUESTION_DO         = "What can I do in {0}";
    private static final String QUESTION_BUY        = "Where can I buy in {0}";


    private String questionText;
    private String displayText;
    private Category category;
    private String city;
    private ArrayList<String> subCategoryLst;

    public ArrayList<String> getSubCategoryLst() {
        return subCategoryLst;
    }

    public DefinedQuestions(Category category, String city) {
        this.category       = category;
        this.city           = city;

        switch(category)
        {
            case DRINK:
                questionText   = MessageFormat.format(QUESTION_DRINK, city);
                displayText    = "";
                subCategoryLst = getSubCategoryDrink();
                break;
            case EAT:
                questionText   = MessageFormat.format(QUESTION_EAT, city);
                displayText    = "";
                subCategoryLst = getSubCategoryEat();
                break;
            case SLEEP:
                questionText   = MessageFormat.format(QUESTION_SLEEP, city);
                displayText    = "";
                subCategoryLst = getSubCategorySleep();
                break;
            case DO:
                questionText   = MessageFormat.format(QUESTION_DO, city);
                displayText    = "";
                subCategoryLst = getSubCategoryDo();
                break;
            case BUY:
                questionText   = MessageFormat.format(QUESTION_BUY, city);
                displayText    = "";
                subCategoryLst = getSubCategoryBuy();
                break;
            case GET_IN:
                questionText   = MessageFormat.format(QUESTION_TRAVEL_BY, city);
                displayText    = "";
                subCategoryLst = getSubCategoryGetIn();
                break;
            case SEE:
                questionText   = MessageFormat.format(QUESTION_SEE, city);
                displayText    = "";
                subCategoryLst = getSubCategorySee();
                break;
        }
    }

    /**
     *
     * @return
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     *
     * @return
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     *
     * @return
     */
    public Category getCategory() {
        return category;
    }

    public String getCategoryName()
    {
        return category.toString();
    }

    /**
     *
     * @return
     */
    public String getCity() {
        return city;
    }



    // ========================================================
    // Sub category lists
    // ========================================================


    private static ArrayList<String> getSubCategorySee()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        return subCategory;
    }

    private static ArrayList<String> getSubCategoryDrink()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        subCategory.add("Bar");
        subCategory.add("Club");
        return subCategory;
    }

    private static ArrayList<String> getSubCategorySleep()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        subCategory.add("Hotel");
        subCategory.add("Hostel");
        subCategory.add("Budget");
        return subCategory;
    }

    private static ArrayList<String> getSubCategoryDo()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        return subCategory;
    }

    private static ArrayList<String> getSubCategoryEat()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        return subCategory;
    }

    private static ArrayList<String> getSubCategoryGetIn()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        subCategory.add("Train");
        subCategory.add("Plane");
        subCategory.add("Car");
        return subCategory;
    }

    private static ArrayList<String> getSubCategoryBuy()
    {
        ArrayList<String> subCategory = new ArrayList<String>();
        return subCategory;
    }
}
