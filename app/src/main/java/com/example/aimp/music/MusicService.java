package com.example.aimp.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.INotificationSideChannel;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.List;

import com.example.aimp.R;
import com.example.aimp.models.Song;
import android.support.v4.os.IResultReceiver;
import android.util.Log;
import android.webkit.PluginStub;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aimp.MusicAIDL;
import com.example.aimp.models.PlayBackTrack;
import com.example.aimp.songdb.SongPlayStatus;
import com.example.aimp.utils.AxUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.aimp.adapters.SongAdapter.songList;
import static com.example.aimp.music.MediaStyleHelper.NOTIFICATION_ID;
import static com.example.aimp.music.MediaStyleHelper.getActionIntent;


public class MusicService extends Service {

    private static final String TAG = "MusicService";

    private static final String TOGGLEPAUSE_ACTION = "com.example.aimp.togglepause";
    private static final String PLAY_ACTION = "com.example.aimp.ACTION_PLAY";
    private static final String PAUSE_ACTION = "com.example.aimp.ACTION_PAUSE";
    private static final String STOP_ACTION = "com.example.aimp.ACTION_STOP";
    private static final String NEXT_ACTION = "com.example.aimp.ACTION_NEXT";
    private static final String PREVIOUS_ACTION = "com.example.aimp.ACTION_PREVIOUS";

    private static final int SERVICE_DIED = 10;
    private static final int FADE_UP = 11;
    private static final int FADE_DOWN = 12;
    private static final int FADE_CHANGE = 13;
    private static final int GO_TO_NEXT_TRACK = 22;

    private static final int NOTIFICATION_MODE_NON = 0;
    private static final int NOTIFICATION_MODE_FOREGROUND = 1;
    private static final int NOTIFICATION_MODE_BACKGROUND = 2;
    private int mNotify = NOTIFICATION_MODE_NON;

    private final IBinder I_BINDER = new SubStub(this);
    public static ArrayList<PlayBackTrack> mPlayList = new ArrayList<>(100);
    private SongPlayStatus mSongPlayStatus;
    private int mPlayPos = -1;
    private SharedPreferences preferences;
    private MyMedia mPlayer;
    private boolean isSupposedToPlayng = false;
    private boolean mPauseByTransientLossOfFocus = false;
    private AudioManager mAudioManager;
    private MyPlayerHandler myPlayerHandler;
    private HandlerThread mHandlerThread;

    private int notiId;
    private MediaSessionCompat mSession;
    private NotificationManagerCompat mNotificationManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            commandHandler(intent);
        }
    };

    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            myPlayerHandler.obtainMessage(FADE_CHANGE, i, 0).sendToTarget();
        }
    };



    @Override
    public boolean onUnbind(Intent intent) {
        mSongPlayStatus.saveSongInDb(mPlayList);
        if (isSupposedToPlayng || mPauseByTransientLossOfFocus){
            return true;
        }
        stopSelf();
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel();
        }
        mSongPlayStatus = SongPlayStatus.getInstance(this);
        mPlayList = mSongPlayStatus.getSongToDb();
        preferences = getSharedPreferences("musicservice", 0);

        mPlayPos = preferences.getInt("pos", 0);
        mHandlerThread = new HandlerThread("MyPlayerHandler", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        myPlayerHandler = new MyPlayerHandler(mHandlerThread.getLooper(), this);

        mPlayer = new MyMedia(this);
        mPlayer.setHandler(myPlayerHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PLAY_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(STOP_ACTION);

        registerReceiver(receiver, filter);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setupMediaSession();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return I_BINDER;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            commandHandler(intent);
        }
        return START_NOT_STICKY;
    }

    private boolean isPlaying(){
        return isSupposedToPlayng;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mPlayer = null;

        mAudioManager.abandonAudioFocus(focusChangeListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationChannel(){

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, "Musics", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
    }

    private void commandHandler(Intent intent) {

        String action = intent.getAction();

        if (TOGGLEPAUSE_ACTION.equals(action)){
            if (isPlaying()){
                pause();
                mPauseByTransientLossOfFocus = false;
                mNotificationManager.notify(notiId, createNotification());
            }
            else {
                play();
            }
        }
        else if (PLAY_ACTION.equals(action)){
            play();
        }
        else if (PAUSE_ACTION.equals(action)){
            pause();
            mPauseByTransientLossOfFocus = false;
        }
        else if (NEXT_ACTION.equals(action)){
            goToNext();
        }
        else if (PREVIOUS_ACTION.equals(action)){
            previous_track();
        }
    }

    private void setupMediaSession(){
        mSession = new MediaSessionCompat(this, "musics");

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                play();
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                goToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                previous_track();
            }

            @Override
            public void onStop() {
                super.onStop();
                stop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                mPlayer.seek(pos);
            }
        });
    }

    private void updateMediaSession(){
        int playPauseState = isSupposedToPlayng ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        mSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songList.get(mPlayPos).title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songList.get(mPlayPos).artistName)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, getBitmap(this, songList.get(mPlayPos).albumId))
                .build());

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
        .setState(playPauseState, position(), 1.0f)
        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
        | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
    }

    private Notification createNotification(){

        int playPauseButton = isPlaying() ? R.drawable.ic_pause_circle_outline : R.drawable.ic_play_circle_outline;

        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mSession);

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
        .setShowActionsInCompactView(0,1,2,3)
        .setMediaSession(mSession.getSessionToken()));

        builder.setSmallIcon(R.mipmap.icon)
                .setColor(getResources().getColor(R.color.colorPrimary));

        builder.addAction(R.drawable.ic_skip_previous, getString(R.string.previous), getActionIntent(this, PREVIOUS_ACTION))
        .addAction(playPauseButton, getString(R.string.previous), getActionIntent(this, TOGGLEPAUSE_ACTION))
        .addAction(R.drawable.ic_skip_next_black, getString(R.string.previous), getActionIntent(this, NEXT_ACTION));

        Notification notification = builder.build();

        return notification;
    }

    private void previous_track(){
        if (mPlayPos <= mPlayList.size() && mPlayPos >= 0){

            if(mPlayPos == 0){
                mPlayPos = mPlayList.size() -1;
            }
            mPlayPos--;
        }
        stop();
        play();
    }

    private void goToNext(){
        if (mPlayPos <= mPlayList.size() && mPlayPos >= 0){

            if(mPlayPos == mPlayList.size()-1){
                mPlayPos = 0;
            }
            mPlayPos++;
        }
        stop();
        play();
    }

    private long position(){
        if (mPlayer.mIsInitialized){
            return mPlayer.position();
        }
        return -1;
    }

    private void open(long[] list, int position, long sourceId, AxUtil.IdType idType) {


        synchronized (this){

            int mLength = list.length;
            boolean newList = true;
            if (mPlayList.size() == mLength){
                newList = false;

                for (int i = 0; i < mLength; i++){

                    if (list[i] != mPlayList.get(i).mid){
                        newList = true;
                        break;
                    }
                }
            }
            if (newList){
                addToPlayList(list, -1, sourceId, idType);
                mSongPlayStatus.saveSongInDb(mPlayList);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("pos", position);
            }

            if (position >= 0){
                mPlayPos = position;
            }
        }
    }

    private void addToPlayList(long[] list, int position, long sourceId, AxUtil.IdType idType) {

        int addLength = list.length;
        if (position < 0){
            mPlayList.clear();
            position = 0;
        }
        mPlayList.ensureCapacity(mPlayList.size() + addLength);

        if (position > mPlayList.size()){
            position = mPlayList.size();
        }

        ArrayList<PlayBackTrack> mList = new ArrayList<>(addLength);

        for (int i = 0; i < addLength; i++){
            mList.add(new PlayBackTrack(list[i], sourceId, idType, i));
        }

        mPlayList.addAll(position, mList);
    }

    public long getAudioId() {
        PlayBackTrack track = getCurrentTrack();
        if (track != null){
            return track.mid;
        }
        return -1;
    }

    public PlayBackTrack getCurrentTrack() {
        return getTrack(mPlayPos);
    }

    public synchronized PlayBackTrack getTrack(int index) {
        if (index != -1 && index < mPlayList.size()){
            return mPlayList.get(index);
        }
        return null;
    }

    private int getQueuePosition() {
        synchronized (this){
            return mPlayPos;
        }
    }

    public long[] getSaveIdList() {
        synchronized (this){
            int length = mPlayList.size();
            long[] idL = new long[length];
            for (int i = 0; i < length; i++){
                idL[i] = mPlayList.get(i).mid;
            }
            return idL;
        }
    }

    private void pause() {
        if (isPlaying()){
            mPlayer.pause();
            isSupposedToPlayng = false;
        }
    }

    private void play() {
        mPlayer.setDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + mPlayList.get(mPlayPos).mid);
        mAudioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mSession.setActive(true);
        mPlayer.start();
        myPlayerHandler.removeMessages(FADE_DOWN);
        myPlayerHandler.sendEmptyMessage(FADE_UP);
        isSupposedToPlayng = true;
        mPauseByTransientLossOfFocus = true;

        updateMediaSession();
        notiId = hashCode();
        startForeground(notiId, createNotification());
    }

    private int getAudioSessionId(){
        synchronized (this){
            return mPlayer.getAudioSessionId();
        }
    }

    private void stop() {
        if (mPlayer.mIsInitialized){
            mPlayer.stop();
        }
    }

    private Bitmap getBitmap(Context context, long id){
        Bitmap albumArt = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);
            ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (fileDescriptor != null){
                FileDescriptor descriptor = fileDescriptor.getFileDescriptor();
                albumArt = BitmapFactory.decodeFileDescriptor(descriptor, null, options);
                fileDescriptor = null;
                descriptor = null;
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

        if (albumArt != null){
            return albumArt;
        }
        else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.note);
        }
    }

    public class MyMedia implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{
        
        private WeakReference<MusicService> mService;
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private boolean mIsInitialized = false;
        private Handler mHandler;
        private float mVolume;


        public MyMedia(MusicService service) {
            this.mService = new WeakReference<>(service);
        }

        public void setDataSource(String path){
            mIsInitialized = setDataPath(mMediaPlayer, path);
        }

        private boolean setDataPath(MediaPlayer mMediaPlayer, String path) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")){
                    mMediaPlayer.setDataSource(mService.get(), Uri.parse(path));
                }
                else {
                    mMediaPlayer.setDataSource(path);
                }

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return true;
        }

        public boolean mInitialized(){
            return mIsInitialized;
        }

        public void setHandler(Handler handler){
            mHandler = handler;
        }

        public void start(){
            mMediaPlayer.start();
        }

        public void stop(){
            mMediaPlayer.stop();
            mIsInitialized = false;
        }

        public void pause(){
            mMediaPlayer.pause();
        }

        public void release(){
            stop();
            mMediaPlayer.release();
        }

        public long duration(){
            if (mMediaPlayer != null && mInitialized()){
                return mMediaPlayer.getDuration();
            }
            return -1;
        }

        public long position(){
            if (mMediaPlayer != null && mInitialized()){
                return mMediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        public void setVolume(float vol){
            mMediaPlayer.setVolume(vol, vol);
            mVolume = vol;
        }

        public long seek(long whereTo){
            mMediaPlayer.seekTo((int) whereTo);
            return whereTo;
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if (mediaPlayer == mMediaPlayer){
                mHandler.sendEmptyMessage(GO_TO_NEXT_TRACK);
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            switch (what){
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mMediaPlayer.release();
                    mMediaPlayer = new MediaPlayer();
                    Message message = mHandler.obtainMessage(SERVICE_DIED);
                    mHandler.sendMessageDelayed(message, 2000);
                    break;
                default:
                    break;
            }
            return false;
        }

        public int getAudioSessionId(){
            return mMediaPlayer.getAudioSessionId();
        }
    }

    public class MyPlayerHandler extends Handler{

        private WeakReference<MusicService> mService;
        private float mVolume = 1.0f;

        public MyPlayerHandler(@NonNull Looper looper, MusicService service) {
            super(looper);
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            MusicService service = mService.get();
            if (service == null) {
                return;
            }

            synchronized (service){
                switch (msg.what){
                    case FADE_UP:
                        mVolume += 0.1f;
                        if (mVolume < 1.0f){
                            sendEmptyMessageDelayed(FADE_UP, 10);
                        }
                        else {
                            mVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mVolume);
                        break;
                    case FADE_DOWN:
                        mVolume -= 0.5f;
                        if (mVolume < 0.2f){
                            sendEmptyMessageDelayed(FADE_DOWN, 10);
                        }
                        else {
                            mVolume = 0.2f;
                        }
                        service.mPlayer.setVolume(mVolume);
                        break;
                    case GO_TO_NEXT_TRACK:
                        goToNext();
                        break;

                    case FADE_CHANGE:
                        switch (msg.arg1){
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                removeMessages(FADE_UP);
                                sendEmptyMessage(FADE_DOWN);
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS:
                                if (service.isSupposedToPlayng){
                                    service.mPauseByTransientLossOfFocus = false;
                                }
                                service.pause();
                                break;

                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (service.isSupposedToPlayng){
                                    service.mPauseByTransientLossOfFocus = true;
                                }
                                service.pause();
                                break;

                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!service.isSupposedToPlayng && service.mPauseByTransientLossOfFocus){
                                    service.mPauseByTransientLossOfFocus = false;
                                    mVolume = 0.0f;
                                    service.mPlayer.setVolume(mVolume);
                                    service.play();
                                }
                                else {
                                    removeMessages(FADE_DOWN);
                                    sendEmptyMessage(FADE_UP);
                                }
                                break;
                        }
                        break;
                }
            }

            super.handleMessage(msg);
        }
    }



    public static final class SubStub extends MusicAIDL.Stub{

        private WeakReference<MusicService> mService;

        public SubStub(MusicService service) {
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void open(long[] list, int position, long sourceId, int type) throws RemoteException {
            mService.get().open(list, position, sourceId, AxUtil.IdType.getInstance(type));
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public long getAudioId() throws RemoteException {
            return mService.get().getAudioId();
        }

        @Override
        public int getCurrentPos() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public long[] getSaveIdList() throws RemoteException {
            return mService.get().getSaveIdList();
        }
    }

}
