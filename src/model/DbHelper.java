package model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;

public abstract class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.db";
    protected SQLiteDatabase db;

    protected DbHelper() {
        super(null, null, null,0);
    }

    public DbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QuestionTableDbHelper.SQL_CREATE_QUESTIONTABLE);
        db.execSQL(MapTableDbHelper.SQL_CREATE_MAPTABLE);
        db.execSQL(GoogleAPITableDBHelper.SQL_CREATE_GOOGLE_API_TABLE);
    }

    public abstract List<TableItem> getTableItems();

    public abstract void removeTableItem(TableItem tableItem);

    public abstract void addTableItem(TableItem tableItem);


}
