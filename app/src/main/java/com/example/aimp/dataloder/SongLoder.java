package com.example.aimp.dataloder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.aimp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SongLoder {

    public List<Song> getAllSongs(Context context){

        List<Song> songList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()){
            do {
                songList.add(new Song(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                        cursor.getLong(4), cursor.getString(5), cursor.getInt(6), cursor.getInt(7)));
            }
            while (cursor.moveToNext());

            if (cursor != null){
                cursor.close();
            }
        }

        return songList;
    }
}
