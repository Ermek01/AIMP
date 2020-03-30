package com.example.aimp.music;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.aimp.MusicAIDL;
import com.example.aimp.utils.AxUtil;

import java.util.Arrays;
import java.util.WeakHashMap;

public class PlayerService {

    public static MusicAIDL mRemote = null;

    private static final WeakHashMap<Context, ServiceBinder> mHashMap;
    private static long[] emptyList = null;

    static {
        mHashMap = new WeakHashMap<>();
    }

    public static final ServiceToken bindToService(Context context, ServiceConnection serviceConnection){

        Activity realActvity = ((Activity) context).getParent();
        if (realActvity == null){
            realActvity = (Activity) context;
        }

        ContextWrapper mWrapper = new ContextWrapper(realActvity);
        mWrapper.startService(new Intent(mWrapper, MusicService.class));
        ServiceBinder binder = new ServiceBinder(serviceConnection, mWrapper.getApplicationContext());

        if (mWrapper.bindService(new Intent().setClass(mWrapper, MusicService.class), binder, 0)){
            mHashMap.put(mWrapper, binder);
            return new ServiceToken(mWrapper);
        }

        return null;
    }

    public static void unBindToService(ServiceToken token){
        if (token == null){
            return;
        }

        ContextWrapper mWrapper = token.contextWrapper;
        ServiceBinder binder = mHashMap.remove(mWrapper);
        if (binder == null){
            return;
        }

        mWrapper.unbindService(binder);
        if (mHashMap.isEmpty()){
            binder = null;
        }
    }

    public static void playAll(long[] list, int position, long sourceId, AxUtil.IdType type) throws RemoteException {

        if (list.length == 0 && list == null && mRemote == null){
            return;
        }

        long audioId = getAudioId();
        int currentPos = getCurrentPos();
        if (position == currentPos && audioId == list[position] && position != -1){
            long[] idList = getSaveIdList();
            if (Arrays.equals(idList, list)){
                play();
                return;
            }
        }

        if (position < 0){
            position = 0;
        }

        mRemote.open(list, position, sourceId, type.mId);
        play();
    }

    private static long[] getSaveIdList() throws RemoteException {
        if (mRemote != null){
            mRemote.getSaveIdList();
        }
        return emptyList;
    }

    private static void play() throws RemoteException {
        if (mRemote != null){
            mRemote.play();
        }
    }

    private static int getCurrentPos() throws RemoteException {
        if (mRemote != null){
            return mRemote.getCurrentPos();
        }
        return -1;
    }

    private static long getAudioId() throws RemoteException {
        if (mRemote != null){
            return mRemote.getAudioId();
        }
        return -1;
    }

    public static class ServiceToken{
        private ContextWrapper contextWrapper;

        public ServiceToken(ContextWrapper contextWrapper) {
            this.contextWrapper = contextWrapper;
        }
    }

    public static final class ServiceBinder implements ServiceConnection{

        private ServiceConnection mService;
        private Context mContext;

        public ServiceBinder(ServiceConnection mService, Context mContext) {
            this.mService = mService;
            this.mContext = mContext;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mRemote = MusicAIDL.Stub.asInterface(iBinder);
            if (mService != null){
                mService.onServiceConnected(componentName, iBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mService != null){
                mService.onServiceDisconnected(componentName);
            }

            mRemote = null;
        }
    }

}
