package com.example.aimp.dataloder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.aimp.models.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoder {

    public List<Artist> getArtists(Cursor cursor){

        List<Artist> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()){
            do {
                list.add(new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3)));
            }while (cursor.moveToNext());
            if (cursor != null){
                cursor.close();
            }
        }

        return list;
    }

    public static Artist getArtist(Context context, long id){
        return artist(makeCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    private static Artist artist(Cursor cursor) {

        Artist artist = new Artist();
        if (cursor.moveToFirst() && cursor != null){
            artist = new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3));
        }
        if (cursor != null){
            cursor.close();
        }
        return artist;
    }

    public List<Artist> artistList(Context context){
        return getArtists(makeCursor(context, null, null));
    }

    public static Cursor makeCursor(Context context, String selection, String[] selectionArg){

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        };
        String sortOrder = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArg, sortOrder);

        return cursor;
    }
}
