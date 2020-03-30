package com.example.aimp.dataloder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.aimp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class AlbumSongLoder {

    public static List<Song> getAllAlbumSongs(Context context, long _id){

        List<Song> albumsongList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK
        };

        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        String selection = "is_music=1 and title !='' and album_id=" + _id;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()){
            do {
                int trackNumber = cursor.getInt(6);
                while (trackNumber >= 1000){
                    trackNumber -= 1000;
                }
                albumsongList.add(new Song(cursor.getLong(0), cursor.getString(1), _id ,cursor.getString(2), cursor.getLong(3),
                        cursor.getString(4), cursor.getInt(5), trackNumber));
            }
            while (cursor.moveToNext());

            if (cursor != null){
                cursor.close();
            }
        }

        return albumsongList;
    }

}
