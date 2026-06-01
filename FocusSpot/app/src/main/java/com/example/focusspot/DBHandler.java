package com.example.focusspot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "focus_spot_db";
    private static final int DATABASE_VERSION = 7;

    private static final String TABLE_PLACES = "places";
    private static final String TABLE_SUGGESTIONS = "suggestions";
    
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_NOISE = "noise";
    private static final String COLUMN_CROWD = "crowd";
    private static final String COLUMN_SPACE = "space";
    private static final String COLUMN_WIFI = "wifi";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE = "imageResId";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLACES_TABLE = "CREATE TABLE " + TABLE_PLACES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_NOISE + " TEXT,"
                + COLUMN_CROWD + " TEXT,"
                + COLUMN_SPACE + " TEXT,"
                + COLUMN_WIFI + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_IMAGE + " INTEGER" + ")";
        db.execSQL(CREATE_PLACES_TABLE);

        String CREATE_SUGGESTIONS_TABLE = "CREATE TABLE " + TABLE_SUGGESTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_NOISE + " TEXT,"
                + COLUMN_CROWD + " TEXT,"
                + COLUMN_SPACE + " TEXT,"
                + COLUMN_WIFI + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_IMAGE + " INTEGER" + ")";
        db.execSQL(CREATE_SUGGESTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUGGESTIONS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addPlace(Place place) {
        insertToTable(TABLE_PLACES, place);
    }

    public void addSuggestion(Place place) {
        insertToTable(TABLE_SUGGESTIONS, place);
    }

    private void insertToTable(String tableName, Place place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, place.getName());
        values.put(COLUMN_NOISE, place.getNoise());
        values.put(COLUMN_CROWD, place.getCrowd());
        values.put(COLUMN_SPACE, place.getSpace());
        values.put(COLUMN_WIFI, place.getWifi());
        values.put(COLUMN_DESCRIPTION, place.getDescription());
        values.put(COLUMN_LOCATION, place.getLocation());
        values.put(COLUMN_CATEGORY, place.getCategory());
        values.put(COLUMN_IMAGE, place.getImageResId());

        db.insert(tableName, null, values);
        db.close();
    }

    public List<Place> getAllPlaces() {
        return fetchFromTable(TABLE_PLACES);
    }

    public List<Place> getPendingPlaces() {
        return fetchFromTable(TABLE_SUGGESTIONS);
    }

    private List<Place> fetchFromTable(String tableName) {
        List<Place> placeList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + tableName;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Place place = new Place(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getInt(9)
                );
                place.setId(cursor.getInt(0));
                placeList.add(place);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return placeList;
    }

    public void updatePlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, place.getName());
        values.put(COLUMN_NOISE, place.getNoise());
        values.put(COLUMN_CROWD, place.getCrowd());
        values.put(COLUMN_SPACE, place.getSpace());
        values.put(COLUMN_WIFI, place.getWifi());
        values.put(COLUMN_DESCRIPTION, place.getDescription());
        values.put(COLUMN_LOCATION, place.getLocation());
        values.put(COLUMN_CATEGORY, place.getCategory());
        values.put(COLUMN_IMAGE, place.getImageResId());

        db.update(TABLE_PLACES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(place.getId())});
        db.close();
    }

    public void updatePlaceImage(String placeName, int newImageResId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE, newImageResId);
        db.update(TABLE_PLACES, values, COLUMN_NAME + " = ?", new String[]{placeName});
        db.close();
    }

    public void deletePlace(int id) {
        deleteFromTable(TABLE_PLACES, id);
    }

    public void deleteSuggestion(int id) {
        deleteFromTable(TABLE_SUGGESTIONS, id);
    }

    public void approveSuggestion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        Cursor cursor = db.query(TABLE_SUGGESTIONS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, cursor.getString(1));
            values.put(COLUMN_NOISE, cursor.getString(2));
            values.put(COLUMN_CROWD, cursor.getString(3));
            values.put(COLUMN_SPACE, cursor.getString(4));
            values.put(COLUMN_WIFI, cursor.getString(5));
            values.put(COLUMN_DESCRIPTION, cursor.getString(6));
            values.put(COLUMN_LOCATION, cursor.getString(7));
            values.put(COLUMN_CATEGORY, cursor.getString(8));
            values.put(COLUMN_IMAGE, cursor.getInt(9));

            db.insert(TABLE_PLACES, null, values);
            
            db.delete(TABLE_SUGGESTIONS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            
            cursor.close();
        }
        db.close();
    }

    private void deleteFromTable(String tableName, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Place getPlaceByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLACES, null, COLUMN_NAME + "=?", new String[]{name}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Place place = new Place(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getInt(9)
            );
            place.setId(cursor.getInt(0));
            cursor.close();
            return place;
        }
        if (cursor != null) cursor.close();
        return null;
    }
}
