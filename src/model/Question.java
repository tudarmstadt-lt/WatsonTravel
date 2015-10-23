package model;

/**
 * Data class for not defined questions
 */
public class Question extends TableItem {

    private int iconId;
    private String question;

    public Question(String title, String question, int iconId) {
        this.title = title;
        this.iconId = iconId;
        this.question = question;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", iconId='" + iconId + '\'' +
                ", question='" + question + '\'' +
                '}';
    }

}
