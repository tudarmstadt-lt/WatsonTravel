package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martina on 21.10.2015.
 */
public class GoogleAPITableDBHelper extends DbHelper {

    private static final String DATABASE_MAPTABLE  = "GoogleAPI";
    private static final String API_KEY            = "api_key";
    private static final String NAME               = "name";

    public static final String SQL_CREATE_GOOGLE_API_TABLE = "CREATE TABLE " + DATABASE_MAPTABLE + " (" +
            API_KEY + " TEXT PRIMARY KEY NOT NULL, " +
            NAME + " TEXT NOT NULL );";


    public GoogleAPITableDBHelper(Context context) {
        super(context);
    }

    @Override
    public List<TableItem> getTableItems() {
        List<TableItem> googleApiList = new ArrayList<>();
        String query = "SELECT * FROM " + DATABASE_MAPTABLE;
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do {
                GoogleAPI googleAPI = new GoogleAPI(cursor.getString(0), cursor.getString(1));
                googleApiList.add(googleAPI);
            } while (cursor.moveToNext());
        }
        return googleApiList;
    }

    @Override
    public void removeTableItem(TableItem tableItem) {
        db.delete(DATABASE_MAPTABLE, API_KEY + "='" + ((GoogleAPI)tableItem).getApi_key() + "'", null);
    }

    @Override
    public void addTableItem(TableItem tableItem) {
        GoogleAPI googleAPI  = (GoogleAPI) tableItem;
        ContentValues values = new ContentValues();

        values.put(NAME   , googleAPI.getName());
        values.put(API_KEY, googleAPI.getApi_key());

        db.insert(DATABASE_MAPTABLE, null, values);
    }
}
