package com.ltts.global_search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VideoContentProvider extends ContentProvider {


    private VideoDatabaseHandler mVideoDatabase;

    @Override
    public boolean onCreate() {
        mVideoDatabase = new VideoDatabaseHandler(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String query =selectionArgs[0];
        query = query.toLowerCase();
        String[] columns = new String[]{
                BaseColumns._ID,
                VideoDatabaseHandler.KEY_NAME,
                VideoDatabaseHandler.KEY_DATA_TYPE,
                VideoDatabaseHandler.KEY_PRODUCTION_YEAR,
                VideoDatabaseHandler.KEY_COLUMN_DURATION,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        return mVideoDatabase.getWordMatch(query,columns);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
