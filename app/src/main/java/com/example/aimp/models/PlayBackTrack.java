package com.example.aimp.models;

import com.example.aimp.utils.AxUtil;

public class PlayBackTrack {

    public long mid;
    public long sourceId;
    public AxUtil.IdType mIdType;
    public int mCurrentPos;

    public PlayBackTrack(long mid, long sourceId, AxUtil.IdType mIdType, int mCurrentPos) {
        this.mid = mid;
        this.sourceId = sourceId;
        this.mIdType = mIdType;
        this.mCurrentPos = mCurrentPos;
    }
}
