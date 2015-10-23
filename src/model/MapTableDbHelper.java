package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapTableDbHelper extends DbHelper {

    private static final String DATABASE_MAPTABLE = "Maps";
    private static final String KEY_MAPID= "mapId";
    private static final String KEY_MAPNAME = "name";
    private static final String KEY_SIZE = "size";
    public static final String SQL_CREATE_MAPTABLE = "CREATE TABLE " + DATABASE_MAPTABLE + " (" +
            KEY_MAPID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_MAPNAME + " TEXT NOT NULL, " +
            KEY_SIZE + " DOUBLE NOT NULL);";


    public MapTableDbHelper(Context context) {
        super(context);
    }

    @Override
    public List<TableItem> getTableItems() {
        List<TableItem> mapList = new ArrayList<>();
        String query = "SELECT * FROM " + DATABASE_MAPTABLE;
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do {
                Map map = new Map(cursor.getString(1), cursor.getDouble(2));
                map.setId(cursor.getInt(0));
                mapList.add(map);
            } while (cursor.moveToNext());
        }
        return mapList;
    }

    @Override
    public void removeTableItem(TableItem tableItem) {
        db.delete(DATABASE_MAPTABLE, KEY_MAPID + "=" + tableItem.getId(), null);
    }

    @Override
    public void addTableItem(TableItem tableItem) {
        Map map = (Map) tableItem;
        ContentValues values = new ContentValues();
        values.put(KEY_MAPNAME, map.getTitle());
        values.put(KEY_SIZE, map.getSize());
        db.insert(DATABASE_MAPTABLE, null, values);
    }
}
