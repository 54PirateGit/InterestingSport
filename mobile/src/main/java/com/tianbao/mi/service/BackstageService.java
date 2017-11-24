package com.tianbao.mi.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tianbao.mi.R;
import com.tianbao.mi.bean.BindingBean;
import com.tianbao.mi.bean.CurrencyBean;
import com.tianbao.mi.bean.FitUser;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.bean.UploadDataBean;
import com.tianbao.mi.bean.UserHeart;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.IntentConstant;
import com.tianbao.mi.constant.ReceiverConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SendBroadUtil;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.utils.T;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.tianbao.mi.constant.IntegerConstant.REFRESH_DATA_FREQUENCY;
import static com.tianbao.mi.constant.IntegerConstant.REFRESH_RELATION__FREQUENCY;

/**
 * 后台服务
 * Created by edianzu on 2017/11/24.
 */
public class BackstageService extends Service {

    private Context mContext;
    private Handler mHandler;

    private MediaPlayer mp;
    private List<FitUser> mList;// 保存数据
//    private List<Integer> ids;// id

    private Map<String, Map<String, Object>> rMap;
    private Map<String, Map<String, Object>> dMap;

    // 注册广播
    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverConstant.SERVICE_PLAY_SOUND);// 播放声音
        filter.addAction(ReceiverConstant.SERVICE_STOP_SOUND);// 停止播放声音
        filter.addAction(ReceiverConstant.BROAD_END_COURSE);// 课程结束
        filter.addAction(ReceiverConstant.BROAD_TO_SERVICE_BINDING_UPDATE);// 用户关系需要更新
        filter.addAction(ReceiverConstant.BROAD_START_UPDATE_DATA);// 需要开始更新数据
        filter.addAction(ReceiverConstant.BROAD_STOP_UPDATE_DATA);// 停止更新数据
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        mHandler = new Handler();
        initReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // 播放背景音乐 待机页
    private void playSoundStandby() {
        try {
            mp = MediaPlayer.create(mContext, R.raw.standby);// 重新设置要播放的音频
            mp.setLooping(true);
            mp.start();// 开始播放
        } catch (Exception e) {
            e.printStackTrace();// 输出异常信息
        }
    }

    // 播放背景音乐 课程结束页
    private void playSoundCourseEnd() {
        try {
            mp = MediaPlayer.create(mContext, R.raw.course_end1);// 重新设置要播放的音频
            mp.start();// 开始播放
            mp.setOnCompletionListener(m -> {
                mp = MediaPlayer.create(mContext, R.raw.course_end2);
                mp.start();
            });
        } catch (Exception e) {
            e.printStackTrace();// 输出异常信息
        }
    }

    // 播放声音
    private void playSound(int soundId) {
        if (soundId <= 0 || soundId > 7) return ;// 只有这些声音
        SoundPlayUtils.play(soundId);
    }

    // 停止播放任何声音
    private void stopPlaySound() {
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }

    // 获取用户绑定关系
    private void requestUserBinding(List<String> tempList) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<BindingBean> model;
        if (tempList == null || tempList.size() == 0) {// 获取某一个用户绑定关系
            Map<String, List<String>> param = new HashMap<>();
            param.put("headIds", tempList);

            model = service.getBinding(param);
        } else {// 获取全部绑定关系
            Map<String, String> param = new HashMap<>();
            param.put("storeId", String.valueOf(IntegerConstant.STORE_ID));

            model = service.getBindings(param);
        }

        model.enqueue(new Callback<BindingBean>() {
            @Override
            public void onResponse(Response<BindingBean> response, Retrofit retrofit) {
                BindingBean bean = response.body();
                if (bean == null) return ;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {// 获取数据成功
                    rMap = bean.getData();

                    handlerData(rMap, dMap);
                } else {// 获取数据失败

                }
            }

            @Override
            public void onFailure(Throwable t) {
                // 获取数据失败
            }
        });
    }

    // 处理数据
    private void handlerData(Map<String, Map<String, Object>> rMap, Map<String, Map<String, Object>> dMap) {
        if (rMap == null) return ;// 不存在没有用户的数据
        if (dMap == null) dMap = new HashMap<>();// 存在有用户没有数据

        LocalDateTime now = new LocalDateTime();
        for (Map.Entry<String, Map<String, Object>> entry : rMap.entrySet()) {
            Map<String, Object> map1 = entry.getValue();
            Map<String, Object> map2 = dMap.get(entry.getKey());

            setFitUserData(map1, map2, now);

//            int userId = (int) map1.get(StringConstant.KEY_USER_ID);
//            if (ids == null) {
//                ids = new ArrayList<>();
//                ids.add(userId);
//            } else if (!ids.contains(userId)) {
//                ids.add(userId);
//            }
        }

        if (mList != null && mList.size() > 0) {
            List<FitUser> list = new ArrayList<>();
            for (int i=0; i<mList.size(); i++) {
                if (mList.get(i).isNotOnline()) {
                    list.add(mList.get(i));
                }
            }
            if (list.size() > 0) SendBroadUtil.sendUpdate(mContext, list);

            requestUploadUser();// 将用户心率上传
        }
    }

    // 获取用户数据
    private void getUserData() {
        dMap = new HashMap<>();

        handlerData(rMap, dMap);
    }

    // 根据接收不同的广播消息处理不同的逻辑
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ReceiverConstant.SERVICE_PLAY_SOUND:// 播放声音
                    int soundId = intent.getIntExtra(IntentConstant.SOUND_ID, 0);
                    if (soundId == 0) return ;// == 0 不做任何处理
                    if (soundId == IntegerConstant.STANDBY_SOUND_ID) {// 待机页
                        playSoundStandby();
                    } else if (soundId == IntegerConstant.COURSE_END_SOUND_ID) {// 课程结束页
                        playSoundCourseEnd();
                    } else {// 各个界面
                        playSound(soundId);
                    }
                    break;
                case ReceiverConstant.SERVICE_STOP_SOUND:// 停止播放
                    stopPlaySound();
                    break;
                case ReceiverConstant.BROAD_TO_SERVICE_BINDING_UPDATE:// 有新的用户绑定关系
                    String key = intent.getStringExtra(StringConstant.BUILD_UPDATE_KEY);

                    List<String> tempList = new ArrayList<>();
                    tempList.add(0, "0");
                    tempList.add(key);
                    tempList.add("0");
                    requestUserBinding(tempList);

                    L.d("tempList", "tempList -> " + tempList.toString());
                    break;
                case ReceiverConstant.BROAD_ALL_BINDING:// 获取全部绑定关系
                    requestUserBinding(null);
                    break;
                case ReceiverConstant.BROAD_END_COURSE:// 课程结束
                    // 先将数据上传再清空
                    if (mList != null) {// 将数据清空
                        mList.clear();
                        mList = null;
                    }
                    break;
                case ReceiverConstant.BROAD_START_UPDATE_DATA:// 需要开始更新数据
                    mHandler.post(mLoopRaleRunnable);
                    mHandler.postDelayed(mLoopDataRunnable, REFRESH_DATA_FREQUENCY);
                    break;
                case ReceiverConstant.BROAD_STOP_UPDATE_DATA:// 停止更新数据
                    if (mLoopRaleRunnable != null) {
                        mHandler.removeCallbacks(mLoopRaleRunnable);
                    }
                    if (mLoopDataRunnable != null) {
                        mHandler.removeCallbacks(mLoopDataRunnable);
                    }
                    break;
            }
        }
    };

    // 循环获取用户关系
    private Runnable mLoopRaleRunnable = new Runnable() {
        @Override
        public void run() {
            requestUserBinding(null);
            mHandler.postDelayed(this, REFRESH_RELATION__FREQUENCY);
        }
    };

    // 循环获取数据
    private Runnable mLoopDataRunnable = new Runnable() {
        @Override
        public void run() {
            getUserData();
            mHandler.postDelayed(this, REFRESH_DATA_FREQUENCY);
        }
    };

    // 设置用户数据
    private void setFitUserData(Map<String, Object> rData, Map<String, Object> dData, LocalDateTime now) {
        if (rData == null) return ;
        if (dData == null) dData = new HashMap<>();
        FitUser user = null;

        int userId = (int) rData.get(StringConstant.KEY_USER_ID);// 用户 id
        if (mList != null && mList.size() > 0) {
            for (int i=0; i<mList.size(); i++) {
                if (mList.get(i).getUserId() == userId) {
                    user = mList.get(i);// 此用户已经存在
                    break;
                }
            }
            if (user == null) user = FitUser.build();// 此用户是新加入的
        } else {// 还没有任何用户的数据  即新用户加入
            user = FitUser.build();
            mList = new ArrayList<>();
        }
        String openId = (String) rData.get(StringConstant.KEY_OPEN_ID);// openId
        String avatar = (String) rData.get(StringConstant.KEY_AVATAR);//头像
        String nick = (String) rData.get(StringConstant.KEY_NICK);// 昵称
        String sex = (String) rData.get(StringConstant.KEY_SEX);// 性别
        String birthday = (String) rData.get(StringConstant.KEY_BIRTHDAY);// 生日 用于计算当前年龄
        String weight = (String) rData.get(StringConstant.KEY_WEIGHT);// 体重
        String height = (String) rData.get(StringConstant.KEY_HEIGHT);// 身高
        String restingHeart = (String) rData.get(StringConstant.KEY_RESTING_HEART);// 安静时心率

        String uuid = (String) rData.get(StringConstant.KEY_UUID);// 设备地址
        String supplierId = (String) rData.get(StringConstant.KEY_SUPPLIER_ID);// 厂商id
//        String showId = (String) rData.get(StringConstant.KEY_SHOW_ID);// 用户可视编号

        user.setOpenId(openId)
                .setUserId(userId)
                .setAvatar(avatar)
                .setNick(nick)
                .setSex(sex)
                .setAge(birthday, now)
                .setWeight(weight)
                .setHeight(height)
                .setHRrest(restingHeart);


        int type = (int) rData.get(StringConstant.KEY_TYPE);// 设备类型
        if (type == IntegerConstant.TYPE_CADENCE) {// 单车上的设备
            String heartRate = (String) dData.get(StringConstant.KEY_HEART_RATE);// 心率
            String cadence = (String) dData.get(StringConstant.KEY_CADENCE);// 踏频
            String interval4Cadence = (String) dData.get(StringConstant.KEY_INTERVAL_CADENCE);// 踏频间隔时间

            user.setbSupplierId(supplierId);
            user.setbUUID(uuid);
            user.setHeartRate(heartRate);
            user.setCadence(cadence);
            user.setInterval4Cadence(interval4Cadence);

            // 安静心率为 0  需要随机上传用户心率
            if (TextUtils.isEmpty(restingHeart) || restingHeart.equals("NaN")
                    || restingHeart.equals("0") || restingHeart.equals("null") || restingHeart.equals("NULL")) {
                UserHeart userHeart = new UserHeart();
                if (userId >= 0 && !TextUtils.isEmpty(heartRate) && !heartRate.equals("0") && !heartRate.equals("NaN")) {
                    try {
                        userHeart.setUserId(userId);
                        userHeart.setHeart(Integer.valueOf(heartRate));
                        mDataList.add(userHeart);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {// 获取心率的设备信息
            user.sethSupplierId(supplierId);
            user.sethUUID(uuid);
        }

        user.getFitInfo().cal4Cycle(IntegerConstant.GIRTH, user, now);

        if (user.isNotOnline()) {// 已经掉线
            // 将此信息发送到后台
            requestUnbinding(openId, uuid);
        }

        mList.add(user);
    }

    // 当接收的数据在 5 分钟以上都为空时调用此接口
    private void requestUnbinding(String openId, String uuid) {
        Map<String, String> param = new HashMap<>();
        param.put("openId", openId);
        param.put("uuid", uuid);
        param.put("storeId", String.valueOf(IntegerConstant.STORE_ID));
        param.put("type", String.valueOf(IntegerConstant.DYNAMIC_SYSTEM_TYPE));//

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<CurrencyBean> model = service.unbinding(param);
        model.enqueue(new Callback<CurrencyBean>() {
            @Override
            public void onResponse(Response<CurrencyBean> response, Retrofit retrofit) {
                CurrencyBean bean = response.body();
                if (bean == null) return;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    L.d("requestUnbinding success");
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private List<UserHeart> mDataList = new ArrayList<>();

    // 上传用户心率
    private void requestUploadUser() {
        if (mDataList == null || mDataList.size() <= 0) return ;
        UploadData uploadData = new UploadData();
        uploadData.setUserHeartList(mDataList);

        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.1.111:8080")
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<UploadDataBean> model = service.addHeart(uploadData);

        L.i("uploadData", "uploadData -> " + uploadData.toString());

        model.enqueue(new Callback<UploadDataBean>() {
            @Override
            public void onResponse(Response<UploadDataBean> response, Retrofit retrofit) {
                UploadDataBean bean = response.body();
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    L.i("mDataList", "数据上传成功");
                    mDataList.clear();

                } else {
                    L.i("mDataList", "数据上传失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                T.connectFailTip(mContext);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (mHandler != null) {
            if (mLoopRaleRunnable != null) {
                mHandler.removeCallbacks(mLoopRaleRunnable);
                mLoopRaleRunnable  =null;
            }
            if (mLoopDataRunnable != null) {
                mHandler.removeCallbacks(mLoopDataRunnable);
                mLoopDataRunnable = null;
            }
            mHandler = null;
        }
        mContext = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
