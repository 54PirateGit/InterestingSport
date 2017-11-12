package com.tianbao.mi.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.tianbao.mi.R;

/**
 * 背景音乐播放
 * Created by edianzu on 2017/11/10.
 */
public class SoundPlayUtils {

    // SoundPool对象
    public static SoundPool mSoundPlayer = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
    public static SoundPlayUtils soundPlayUtils;

    static Context mContext;

    /**
     * 初始化
     */
    public static SoundPlayUtils init(Context context) {
        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }

        // 初始化声音
        mContext = context;

        /**
         * 待机页有新人加入
         */
        mSoundPlayer.load(mContext, R.raw.partner_join, 1);// 1

        /**
         * 待机页进入开始转场页前播放，持续时间4秒。
           逻辑：
             1、服务端push课程开始消息或者轮循云服务判断课程是否开始。
             2、确认开课时，播放此音频，播放完后跳开始转场页
             3、注意前一页播放2秒，跳到下一页再播放2秒
         */
        mSoundPlayer.load(mContext, R.raw.start_load, 1);// 2

        /**
         * 卡路里增加了
         */
        mSoundPlayer.load(mContext, R.raw.calorie_add, 1);// 3

        /**
         * 开始转场页进入直播页前播放，持续时间2秒。
           逻辑：
             1、开始转场页展现后，先等待四秒，等进度条（4秒）跑完，然后播放此音频。
             2、注意前一页播放1秒，跳到下一页再播放一秒
         */
        mSoundPlayer.load(mContext, R.raw.start_course, 1);// 4

        /**
         * 直播页进入结束转场页前播放，持续时间2秒。
           逻辑：
             1、收到云服务的课程结束消息，或者30秒轮循一次确认课程结束，然后播放此音频。
             2、注意前一页播放1秒，跳到下一页再播放一秒。
         */
        mSoundPlayer.load(mContext, R.raw.course_end0, 1);// 5

        /**
         * 直播中-第一名发生变化
         */
        mSoundPlayer.load(mContext, R.raw.yes, 1);// 6

        /**
         * 直播中-心率过快
         */
        mSoundPlayer.load(mContext, R.raw.warm, 1);// 7

//        /**
//         * 页面打开后转着播  直播结束页  先播8再播9
//         */
//        mSoundPlayer.load(mContext, R.raw.course_end1, 1);// 8
//        mSoundPlayer.load(mContext, R.raw.course_end2, 1);// 9

        return soundPlayUtils;
    }

    /**
     * 播放声音
     *
     * @param soundID  要播放的声音
     */
    public static void play(int soundID) {
        mSoundPlayer.play(soundID, 1, 1, 1, 0, 1);
    }
}
