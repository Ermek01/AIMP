package com.example.aimp.dataloder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.aimp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class ArtistSongLoder {

    public static List<Song> getAllArtistSongs(Context context, long artist_id){

        List<Song> artistsongList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        String selection = "is_music=1 and title !='' and artist_id=" + artist_id;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()){
            do {
                int trackNumber = cursor.getInt(6);
                while (trackNumber >= 1000){
                    trackNumber -= 1000;
                }
                artistsongList.add(new Song(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                        artist_id, cursor.getString(4), cursor.getInt(5), trackNumber));
            }
            while (cursor.moveToNext());

            if (cursor != null){
                cursor.close();
            }
        }

        return artistsongList;
    }

}
