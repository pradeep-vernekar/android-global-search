package com.ltts.global_search;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ltts.global_search.model.Video;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * The purpose of this class is to make accessing the database
 * that you will create easier by using a Content Provider
 */

public class VideoDatabaseHandler {

    private static final String TAG = VideoDatabaseHandler.class.getName();
    private static final String DATABASE_NAME = "video_database_leanback";
    private static final int DATABASE_VERSION = 2;
    private static final String FTS_VIRTUAL_TABLE = "Leanback_table";

    public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;

    private static final HashMap<String,String> COLUMN_MAP = buildColumnMap();
    private final VideoDatabaseOpenHelper mDatabaseOpenHelper;

    // constructor function which takes context as argument
    public VideoDatabaseHandler(Context context) {
        this.mDatabaseOpenHelper = new VideoDatabaseOpenHelper(context);
    }

    //
    public Cursor getWordMatch(String query,String[] columns){
        String selection = KEY_NAME + " MATCH ?";
        String[] selectionArgs = new String[]{query + "*"};
        return query(selection,selectionArgs,columns);
    }

    private Cursor query(String selection,String[] selectionArgs,String[] columns){
        Log.e(TAG,"Searching...");
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(COLUMN_MAP);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),columns,selection,
                selectionArgs,null,null,null);
        if(cursor == null){
            Log.e(TAG,"not found...");
            return null;
        }else if (!cursor.moveToFirst()){
            Log.e(TAG,"not found...");
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static HashMap<String,String> buildColumnMap(){
        HashMap<String,String> map = new HashMap<>();
        map.put(KEY_NAME,KEY_NAME);
        map.put(KEY_DATA_TYPE,KEY_DATA_TYPE);
        map.put(KEY_PRODUCTION_YEAR,KEY_PRODUCTION_YEAR);
        map.put(KEY_COLUMN_DURATION,KEY_COLUMN_DURATION);
        map.put(KEY_ACTION,KEY_ACTION);
        map.put(BaseColumns._ID,"rowid As "+BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS "+SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid As "+SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }


    // inner database open helper class
    private static class VideoDatabaseOpenHelper extends SQLiteOpenHelper {

        private final WeakReference<Context> mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        KEY_NAME + ", " +
                        KEY_DATA_TYPE + "," +
                        KEY_ACTION + "," +
                        KEY_PRODUCTION_YEAR + "," +
                        KEY_COLUMN_DURATION + ");";


        public VideoDatabaseOpenHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = new WeakReference<>(context);

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.e(TAG,"On Create....");
            mDatabase = sqLiteDatabase;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDatabase();
        }

        // load database content from assets on worker thread
        private void loadDatabase(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadVideos();
                }
            }).start();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

        // code for loading and parsing assets
        private void loadVideos(){
                try {
                    InputStream inputStream = mHelperContext.get().getAssets().open("videos.json");
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    String jsonString = new String(buffer,"UTF-8");
                    Type videoListType = new TypeToken<List<Video>>(){}.getType();
                    List<Video> videos = new Gson().fromJson(jsonString,videoListType);
                    Log.e(TAG,"parse completed");
                    for (Video video:videos) {
                        addVideoForDeepLink(video);
                    }

                }catch (IOException e){
                    Log.e(TAG,e.getMessage());
                }
        }

        // add values to data base
        private void addVideoForDeepLink(Video video){
            Log.e(TAG,"adding..");
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_NAME,video.getTitle());
            contentValues.put(KEY_DATA_TYPE,video.getType());
            contentValues.put(KEY_PRODUCTION_YEAR,"2015");
            contentValues.put(KEY_COLUMN_DURATION,6400000);
            mDatabase.insert(FTS_VIRTUAL_TABLE,null,contentValues);
        }
    }



}
