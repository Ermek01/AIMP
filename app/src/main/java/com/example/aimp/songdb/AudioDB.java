package com.example.aimp.songdb;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AudioDB extends SQLiteOpenHelper {

    private static final String DATABASENAME = "songs.db";
    private static final int VERSION = 4;
    public static AudioDB instance = null;
    private Context context;

    public static AudioDB getInstance(Context context){
        if (instance == null){
            instance = new AudioDB(context.getApplicationContext());
        }
        return instance;
    }

    public AudioDB(@Nullable Context context) {
        super(context, DATABASENAME, null, VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SongPlayStatus.getInstance(context).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SongPlayStatus.getInstance(context).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SongPlayStatus.getInstance(context).onDowngrade(db, oldVersion, newVersion);
    }
}
