// IMyAidlInterface.aidl
package com.example.aimp;

interface MusicAIDL {

    void open(in long[] list, int position, long sourceId, int type);
    void play();
    void stop();
    long getAudioId();
    int getCurrentPos();
    long[] getSaveIdList();
}
