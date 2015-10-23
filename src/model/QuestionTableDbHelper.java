package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for manage Database access to table Question
 *
 * Table question includes the not defined question that the user
 * can add
 */
public class QuestionTableDbHelper extends DbHelper{

    private static final String DATABASE_QUESTIONSTABLE = "Questions";
    private static final String KEY_QUESTIONID = "questionId";
    private static final String KEY_QUESTIONTITLE = "title";
    private static final String KEY_ICONID = "iconId";
    private static final String KEY_QUESTION = "question";
    public static final String SQL_CREATE_QUESTIONTABLE = "CREATE TABLE " + DATABASE_QUESTIONSTABLE + " (" +
            KEY_QUESTIONID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_QUESTIONTITLE + " TEXT NOT NULL, " +
            KEY_QUESTION + " TEXT NOT NULL, " +
            KEY_ICONID + " INTEGER NOT NULL);";

    public QuestionTableDbHelper(Context context) {
        super(context);
    }

    @Override
    public List<TableItem> getTableItems() {
        List<TableItem> questionList = new ArrayList<>();
        String query = "SELECT * FROM " + DATABASE_QUESTIONSTABLE;
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do {
                Question question = new Question(cursor.getString(1), cursor.getString(2), cursor.getInt(3));
                question.setId(cursor.getInt(0));
                questionList.add(question);
            } while (cursor.moveToNext());
        }
        return questionList;
    }

    @Override
    public void removeTableItem(TableItem tableItem) {
        db.delete(DATABASE_QUESTIONSTABLE, KEY_QUESTIONID + "=" + tableItem.getId(), null);
    }

    @Override
    public void addTableItem(TableItem tableItem) {
        Question question = (Question) tableItem;
        ContentValues values = new ContentValues();
        values.put(KEY_QUESTIONTITLE, question.getTitle());
        values.put(KEY_QUESTION, question.getQuestion());
        values.put(KEY_ICONID, question.getIconId());
        db.insert(DATABASE_QUESTIONSTABLE, null, values);
    }
}
