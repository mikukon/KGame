package com.yiqiding.ktvbox.ksong.dafen;

import com.yiqiding.ktvbox.rpc.socket.data.SendKGChanllengeResults;
import com.yiqiding.ktvbox.structure.KGameJiepaiEntity;

/**
 * Created by so898 on 14-7-22.
 */
public interface KGameResultListener {
    void showProgress(String show);
//    void KGameResult(float result);
    void KGameResult(SendKGChanllengeResults result);
    void allScoreWhenEnd(float[] scoreArr);
    void jiepaiWhenEnd(KGameJiepaiEntity jiepai);
}
