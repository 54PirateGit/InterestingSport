package com.tianbao.mi.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.CurrencyBean;
import com.tianbao.mi.bean.FitUser;
import com.tianbao.mi.bean.GymData;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.bean.RecordBean;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.bean.UploadDataBean;
import com.tianbao.mi.bean.UserHeart;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.QrUtil;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.utils.T;
import com.tianbao.mi.widget.AutoScrollListView;
import com.tianbao.mi.widget.AutoTypesettingLayout;
import com.tianbao.mi.widget.MemberView;
import com.tianbao.mi.widget.bdplayer.BDCloudVideoView;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 动感单车数据展示界面
 * 10/23
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.view_left_front)
    AutoTypesettingLayout viewLeftFront;// 左边前面
    @BindView(R.id.view_right_front)
    AutoTypesettingLayout viewRightFront;// 右边前面

    @BindView(R.id.view_left_back)
    AutoTypesettingLayout viewLeftBack;// 左边后面
    @BindView(R.id.view_right_back)
    AutoTypesettingLayout viewRightBack;// 右边后面

    @BindView(R.id.view_info)
    RelativeLayout viewInfo;// 底部信息板
    @BindView(R.id.living)
    ImageView imageLiving;// 直播中  点播进来时不需要显示

    @BindView(R.id.auto_scroll_list)
    AutoScrollListView autoScrollListView;

    @BindView(R.id.image_qr)
    ImageView imageQr;// 二维码
    @BindView(R.id.view_qr)
    View viewQr;

    private Context mContext;
    private BDCloudVideoView mVV = null;// 百度云播放器
    private Handler mHandler = new Handler();// 处理线程

    private List<InformationBean> iList = new ArrayList<>();
    //    private List<UserDataBean> mList;// 保存用户数据
    private List<FitUser> mList;// 保存用户数据
    private List<MemberView> vList = new ArrayList<>();// 保存 View
    private List<String> dKey = new ArrayList<>();// key
    private List<UserHeart> mDataList = new ArrayList<>();

    private boolean isFront = true;// 标记此时在前在后
    private String playType;// 标识  点播 or 直播

    private boolean isCancelRequest = false;// 取消所有网络请求

    // 注册广播
    private void registerBroad() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(StringConstant.BROAD_BUILD_UPDATE);
        filter.addAction(StringConstant.BROAD_END_COURSE);
        registerReceiver(mReceiver, filter);
    }

    // 初始化播放器
    private void initPlayer() {
        mVV = (BDCloudVideoView) findViewById(R.id.bd_player);
        mVV.setVideoPath(StringConstant.LIVE_URL);
        mVV.setVideoScalingMode(BDCloudVideoView.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mVV.setOnCompletionListener(iMediaPlayer -> mHandler.postDelayed(() -> courseEnd(), IntegerConstant.INTO_COURSE_END));
        mVV.start();
    }

    private List<String> key = new ArrayList<>();// 获取所有用户绑定数据需要

    private void setKey() {
        int storeId = (int) SPUtils.get(mContext, StringConstant.STORE_ID_SP_KEY, 1);
        String kString;
        for (int i = 0; i < 30; i++) {
            kString = storeId + "_" + i;
            key.add(kString);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mContext = this;

        setKey();// 所有 key

//        initPlayer();// 初始化播放器
        initView();

        Intent intent = getIntent();
        if (intent == null) return;
        playType = intent.getStringExtra(StringConstant.PLAY_TYPE);

        if (playType.equals(StringConstant.DEMAND_PLAY_TYPE)) {
            imageLiving.setVisibility(View.GONE);
        } else {
            imageLiving.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroad();// 注册广播

//        if (mVV != null && !mVV.isPlaying() && !TextUtils.isEmpty(StringConstant.LIVE_URL) && !StringConstant.LIVE_URL.equals("-1")) {
//            mVV.start();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
//        if (mVV != null && mVV.isPlaying()) {
//            mVV.pause();
//        }
    }

    // 初始化视图
    private void initView() {
        viewInfo.setAlpha(0.5f);
        Bitmap qrBitmap = QrUtil.generateBitmap("hello world", 240, 240);
        if (qrBitmap != null) {
            viewQr.setVisibility(View.VISIBLE);
            imageQr.setImageBitmap(qrBitmap);
        } else {
            viewQr.setVisibility(View.GONE);
        }

        mHandler.postDelayed(mLoopUserDataRunnable, IntegerConstant.REFRESH_DATA_FREQUENCY);// 隔一定时间去获取用户数据
        mHandler.post(mLoopUserRelaRunnable);// 隔一定时间去获取用户绑定关系
        mHandler.postDelayed(mLoopSort, IntegerConstant.SORT_FREQUENCY);// 隔一定时间去根据用户数据排序
        mHandler.postDelayed(mChangeViewRunnable, IntegerConstant.FRONT_BACK_DATA_CHANGE_FIRST);// 前后面交换数据
    }

    // 轮询去获取用户数据
    private Runnable mLoopUserDataRunnable = new Runnable() {
        @Override
        public void run() {
            requestUserData();
            mHandler.postDelayed(this, IntegerConstant.REFRESH_DATA_FREQUENCY);
        }
    };

    // 轮询去获取用户关系
    private Runnable mLoopUserRelaRunnable = new Runnable() {
        @Override
        public void run() {
            requestUserInfo();
            mHandler.postDelayed(this, IntegerConstant.REFRESH_RELATION__FREQUENCY);
        }
    };

    // 轮询去根据用户数据排序
    private Runnable mLoopSort = new Runnable() {
        @Override
        public void run() {
            sortData();
            mHandler.postDelayed(this, IntegerConstant.SORT_FREQUENCY);
        }
    };

    // 接收广播 更新用户绑定关系
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(StringConstant.BROAD_BUILD_UPDATE)) {// 有新的用户绑定关系需要处理
                requestUserInfo();
            } else if (action.equals(StringConstant.BROAD_END_COURSE)) {// 课程结束
                courseEnd();
            }
        }
    };

    private Map<String, Map<String, String>> dMap = new HashMap<>();// 用户数据
    private Map<String, Map<String, String>> rMap = new HashMap<>();// 用户绑定关系

    // 网络请求获取网络数据  获取用户数据
    private void requestUserData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL_PI)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<RecordBean> model = service.requestRecord();
        model.enqueue(new Callback<RecordBean>() {
            @Override
            public void onResponse(Response<RecordBean> response, Retrofit retrofit) {
                if (isCancelRequest) return;
                RecordBean recordBean = response.body();
                int code = recordBean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    dMap = recordBean.getData();
                    List<String> tempList = new ArrayList<>();
                    for (Map.Entry<String, Map<String, String>> entry : dMap.entrySet()) {
                        tempList.add(entry.getKey());
                    }
                    assemblyData(tempList);
                } else {
                    L.w("LoadFail", "requestUserData 加载失败，等待重新加载");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.w("LoadFail", "requestUserData 连接服务器失败");
            }
        });
    }

    // 组装用户数据
    private void assemblyData(List<String> tList) {
        for (String key : tList) {
            if (!dKey.contains(key)) {// 有新的数据
                dKey.add(key);
                FitUser bean = FitUser.build();
                bean.setKey(key);// 将 key 先加入  key 是识别用户数据与用户绑定关系的唯一标识
                bean.setSort(0);
                if (mList == null) mList = new ArrayList<>();
                mList.add(bean);
            }
        }
        for (String key : dKey) {
            for (int i = 0; i < mList.size(); i++) {
                if (key.equals(mList.get(i).getKey())) {
                    FitUser bean = mList.get(i);
                    Map<String, String> dData = dMap.get(key);
                    Map<String, String> rData = rMap.get(key);
                    LocalDateTime now = new LocalDateTime();
                    bean = setFitUserData(bean, dData, rData, now);
                    bean.getFitInfo().cal4Cycle(IntegerConstant.GIRTH, bean, now);

                    if (bean.isNotOnline()) {// 已经掉线
                        String openId = bean.getOpenId();
                        String headId = bean.getKey();
                        requestUnbinding(openId, headId);// 将此信息发送到后台
                    }
                    break;
                }
            }
        }

        if (mDataList != null && mDataList.size() > 0) {
            for (int i=0; i<mDataList.size(); i++) {
                if (mDataList.get(i).getHeart() == 0) {
                    mDataList.remove(i);
                }
            }

            if (mDataList.size() > 0) {
                requestUploadUser();
            }
        }

        // 先移除全部视图
        if (viewLeftFront != null) viewLeftFront.removeAllViews();
        if (viewRightFront != null) viewRightFront.removeAllViews();
        if (viewLeftBack != null) viewLeftBack.removeAllViews();
        if (viewRightBack != null) viewRightBack.removeAllViews();
        if (vList != null) vList.clear();

        // 然后重新添加  因为刷新界面的方法不是很管用 所以只能这样手动刷新界面
        MemberView childView;
        FitUser dataBean;
        if (mList == null || mList.size() <= 0) return;
        for (int i = 0; i < mList.size(); i++) {
            childView = new MemberView(mContext);
            dataBean = mList.get(i);
            childView.setData(dataBean);

            // 目前只能排版 28 个
            if (i < 7) {// 左排前面
                viewLeftFront.addView(childView);
            } else if (i < 14) {// 右排前面
                viewRightFront.addView(childView);
            } else if (i < 21) {// 左排后面
                viewLeftBack.addView(childView);
            } else if (i < 28) {// 右排后面
                viewRightBack.addView(childView);
            }
            vList.add(childView);// 保存
        }
        updateView();// 加载

        for (int i = 0; i < mList.size(); i++) {
            FitUser bean = mList.get(i);
            int level = bean.getHearRateLevel();
            if (level == 5) {// 心率范围在 5 档的位置需要提示用户
                String name = bean.getNick();

                InformationBean iBean = new InformationBean();
                iBean.setType(IntegerConstant.VIEW_TYPE_TIP);
                iBean.setName(name);
                iBean.setTip("心率过快！请注意训练强度！");

                if (autoScrollListView.isTipRun()) {
                    autoScrollListView.updateData(iBean);
                } else {
                    iList.add(iBean);
                    autoScrollListView.setInfoList(iList);
                }
                SoundPlayUtils.play(IntegerConstant.SOUND_WARM);
            }
        }
    }

    // 排序  根据消耗卡路里由大到小
    private void sortData() {
        if (mList == null || mList.size() <= 3) return;
        if (vList == null || vList.size() <= 3) return;
        List<FitUser> tempList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) tempList.add(mList.get(i));
        List<String> resultList = sortResult(tempList);
        if (resultList == null || resultList.size() < 3) return;

        InformationBean iBean = new InformationBean();
        iBean.setType(IntegerConstant.VIEW_TYPE_SORT);
        List<InformationBean.SortBean> sList = new ArrayList<>();

        InformationBean.SortBean sortBean;

        for (int i = 0; i < mList.size(); i++) {
            String key = mList.get(i).getKey();
            if (key.equals(resultList.get(0))) {
                mList.get(i).setSort(1);

                String name = mList.get(i).getNick();
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(1);
                sortBean.setName(name);
                sList.add(sortBean);
            } else if (mList.get(i).getKey().equals(resultList.get(1))) {
                mList.get(i).setSort(2);

                String name = mList.get(i).getNick();
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(2);
                sortBean.setName(name);
                sList.add(sortBean);
            } else if (mList.get(i).getKey().equals(resultList.get(2))) {
                mList.get(i).setSort(3);

                String name = mList.get(i).getNick();
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(3);
                sortBean.setName(name);
                sList.add(sortBean);
            } else {
                mList.get(i).setSort(0);
            }
            SoundPlayUtils.play(IntegerConstant.SOUND_YES);
        }

        iBean.setSortList(sList);
        if (autoScrollListView.isTipRun()) {
            autoScrollListView.updateData(iBean);
        } else {
            iList.add(iBean);
            autoScrollListView.setInfoList(iList);
        }
    }

    // 排序  大 - > 小
    private List<String> sortResult(List<FitUser> list) {
        if (list == null || list.size() <= 0) return null;
        if (list.size() <= 3) return null;
        FitUser[] arr = new FitUser[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        List<FitUser> result = new ArrayList<>();
        for (int x = 0; x < arr.length; x++) {
            FitUser a = arr[x];
            for (int y = x + 1; y < arr.length; y++) {
                FitUser b = arr[y];
                if (a.getKcal() >= b.getKcal()) continue;
                else {
                    arr[x] = b;
                    arr[y] = a;
                    a = arr[x];
                }
            }
            result.add(a);
        }
        List<String> resultList = new ArrayList<>();
        resultList.add(result.get(0).getKey());
        resultList.add(result.get(1).getKey());
        resultList.add(result.get(2).getKey());
        return resultList;
    }

    // 前后面交换数据
    private Runnable mChangeViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mList != null && mList.size() > 14) {// 后面有数据
                viewLeftFront.startAnim(isFront);
                viewRightFront.startAnim(isFront);
                viewLeftBack.startAnim(!isFront);
                viewRightBack.startAnim(!isFront);
                isFront = !isFront;
            }
            mHandler.postDelayed(this, IntegerConstant.FRONT_BACK_DATA_CHANGE);
        }
    };

    // 没有获取到数据或获取数据失败时重复获取数据
    private int count = 0;
    private Runnable reStartRequestRunnable = () -> {
        if (count < IntegerConstant.RESTART_REQUEST_COUNT) {
            requestUserInfo();
            count++;
        }
    };

    // 获取用户的绑定关系
    private void requestUserInfo() {
        Map<String, List<String>> param = new HashMap<>();
        param.put("headIds", key);// 获取全部
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<BuildBean> model = service.getBuild(param);
        model.enqueue(new Callback<BuildBean>() {
            @Override
            public void onResponse(Response<BuildBean> response, Retrofit retrofit) {
                if (isCancelRequest) return;
                BuildBean buildBean = response.body();
                int code = buildBean.getCode();
                if (IntegerConstant.RESULT_OK == code) {
                    if (mHandler != null && reStartRequestRunnable != null) {
                        mHandler.removeCallbacks(reStartRequestRunnable);
                    }
                    rMap = buildBean.getData();
                    List<String> tempList = new ArrayList<>();
                    for (Map.Entry<String, Map<String, String>> entry : rMap.entrySet()) {
                        tempList.add(entry.getKey());

                        Map<String, String> map = entry.getValue();
                        String restingHeart = map.get(StringConstant.KEY_RESTING_HEART);
                        if (TextUtils.isEmpty(restingHeart) || restingHeart.equals("NaN")
                                || restingHeart.equals("0") || restingHeart.equals("null") || restingHeart.equals("NULL")) {
                            UserHeart userHeart = new UserHeart();
                            String userId = map.get(StringConstant.KEY_USER_ID);
                            if (!TextUtils.isEmpty(userId)) {
                                userHeart.setUserId(Integer.valueOf(userId));
                                mDataList.add(userHeart);
                            }
                        }
                    }
                    assemblyData(tempList);
                } else {
                    mHandler.postDelayed(reStartRequestRunnable, IntegerConstant.RESTART_REQUEST_TIME);
                    L.w("requestUserInfo", "没有获取到用户绑定数据或没有用户绑定数据");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                mHandler.postDelayed(reStartRequestRunnable, IntegerConstant.RESTART_REQUEST_TIME);
                L.w("requestUserInfo", "连接服务器失败");
            }
        });
    }

    // 获取课程信息
    private void requestCourseStatus() {
        Map<String, String> param = new HashMap<>();
        param.put("storeId", String.valueOf(IntegerConstant.STORE_ID));
        param.put("courseId", String.valueOf(MyApp.getCourseId()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<CourseInfoBean> model = service.getCourse(param);

        model.enqueue(new Callback<CourseInfoBean>() {
            @Override
            public void onResponse(Response<CourseInfoBean> response, Retrofit retrofit) {
                if (isCancelRequest) return;
                CourseInfoBean courseInfo = response.body();
                if (courseInfo == null) {
                    mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                    return;
                }

                int code = courseInfo.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    CourseInfoBean.DataBean.CourseBean course = courseInfo.getData().getCourse();
                    if (course == null) {// 轮询获取数据线程开关由数据决定
                        mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                    } else {
                        String status = course.getLiveStatus();
                        if (status.equals(StringConstant.LIVE_STATU_OVER)) {
                            courseEnd();
                        } else {
                            mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                        }
                    }
                } else {
                    L.w("data is null");
                    mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.w("连接服务器失败");
            }
        });
    }

    // 当接收的数据在 5 分钟以上都为空时调用此接口
    private void requestUnbinding(String openId, String headId) {
        Map<String, String> param = new HashMap<>();
        param.put("openId", openId);
        param.put("headId", headId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<CurrencyBean> model = service.unbinding(param);
        model.enqueue(new Callback<CurrencyBean>() {
            @Override
            public void onResponse(Response<CurrencyBean> response, Retrofit retrofit) {
                if (isCancelRequest) return;
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

    // 课程结束
    private void courseEnd() {
        SoundPlayUtils.play(IntegerConstant.SOUND_COURSE_END);

        UploadData uploadData = new UploadData();
        List<GymData> data = uploadData();
        uploadData.setGymDataList(data);

        Intent intent = new Intent(mContext, CourseEndActivity.class);
        intent.putExtra(StringConstant.UPLOAD_DATA_KEY, uploadData);
        startActivity(intent);// 跳转到课程结束界面
        finish();
    }

    // 上传用户心率
    private void requestUploadUser() {
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
                    T.showShort(mContext, "数据上传成功");

                    mDataList.clear();

                } else {
                    T.showShort(mContext, "数据上传失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                T.connectFailTip(mContext);
            }
        });
    }


    // 需要上传的用户数据
    private List<GymData> uploadData() {
        if (mList == null || mList.size() <= 0) return null;
        LocalDateTime now = new LocalDateTime();
        List<GymData> gList = new ArrayList<>();

        GymData gData;
        for (FitUser user : mList) {
            gData = new GymData();

            gData.setCourseId(MyApp.getCourseId());
            gData.setAverageHeartRate(user.getAverageHeartRate(now));// 平均心率

            String calorie = user.getKcalStr();
            if (TextUtils.isEmpty(calorie) || calorie.equals("--") || calorie.equals("NaN")) {
                calorie = "0";
            }
            gData.setCalorie(Float.valueOf(calorie));
            gData.setStatus(1);
            gData.setMaximumHeartRate(200);// 最大心率 待修改
            gData.setExerciseDuration(user.getDuration(now));

            String mileage = user.getDistanceStr();
            if (TextUtils.isEmpty(mileage) || mileage.equals("--") || mileage.equals("NaN")) {
                mileage = "0";
            }
            gData.setMileage(Float.valueOf(mileage));
            gData.setUserId(user.getUserId());
            gData.setTopSpeed(user.getPACE());
            gData.setAverageVelocity(30);// 平均速度  待修改

            gData.setMaximum((int)user.getHrLevel5duration());
            gData.setAccumulation((int)user.getHrLevel4duration());
            gData.setConsume((int)user.getHrLevel3duration());
            gData.setBurning((int)user.getHrLevel2duration());
            gData.setRelax((int)user.getHrLevel1duration());

            gData.setMaximumPct(user.getHrLevelRate(5, now));
            gData.setAccumulationPct(user.getHrLevelRate(4, now));
            gData.setConsumePct(user.getHrLevelRate(3, now));
            gData.setBurningPct(user.getHrLevelRate(2, now));
            gData.setRelaxPct(user.getHrLevelRate(1, now));

            gData.setType(IntegerConstant.DYNAMIC_SYSTEM_TYPE);

            gList.add(gData);
        }

        return gList;
    }

    // 获取课程直播状态 用于没有接收到课程结束推送消息时自行结束课程
    private Runnable mLoopRequestRunnable = () -> requestCourseStatus();

    // 更新界面排版
    private void updateView() {
        viewLeftFront.updateView();
        viewRightFront.updateView();
        viewLeftBack.updateView();
        viewRightBack.updateView();

        viewLeftFront.setAlpha(isFront);
        viewRightFront.setAlpha(isFront);
        viewLeftBack.setAlpha(!isFront);
        viewRightBack.setAlpha(!isFront);
    }

    // 设置用户数据
    private FitUser setFitUserData(FitUser user, Map<String, String> data1, Map<String, String> data2, LocalDateTime now) {
        if (data1 == null) data1 = new HashMap<>();
        if (data2 == null) data2 = new HashMap<>();
        if (user == null) user = FitUser.build();

        String openId = data2.get(StringConstant.KEY_OPEN_ID);// openId
        String userId = data2.get(StringConstant.KEY_USER_ID);// 用户 id
        String avatar = data2.get(StringConstant.KEY_AVATAR);//头像
        String nick = data2.get(StringConstant.KEY_NICK);// 昵称
        String sex = data2.get(StringConstant.KEY_SEX);// 性别
        String birthday = data2.get(StringConstant.KEY_BIRTHDAY);// 生日 用于计算当前年龄
        String weight = data2.get(StringConstant.KEY_WEIGHT);// 体重
        String height = data2.get(StringConstant.KEY_HEIGHT);// 身高
        String restingHeart = data2.get(StringConstant.KEY_RESTING_HEART);// 安静时心率

        String heartRate = data1.get(StringConstant.KEY_HEART_RATE);// 心率
        String cadence = data1.get(StringConstant.KEY_CADENCE);// 踏频
        String interval4Cadence = data1.get(StringConstant.KEY_INTERVAL_CADENCE);// 踏频间隔时间

        if (!TextUtils.isEmpty(userId) || !userId.equals("NaN") || userId.equals("null")) {
            if (mDataList != null && mDataList.size() > 0) {
                for (int i=0; i<mDataList.size(); i++) {
                    UserHeart userHeart = mDataList.get(i);
                    if (userHeart.getUserId() == Integer.valueOf(userId)) {
                        if (TextUtils.isEmpty(heartRate) || heartRate.equals("NaN")) {
                            heartRate = "0";
                        }
                        userHeart.setHeart(Integer.valueOf(heartRate));
                        break;
                    }
                }
            }
        }

        user.setOpenId(openId)
                .setUserId(userId)
                .setAvatar(avatar)
                .setNick(nick)
                .setSex(sex)
                .setHeartRate(heartRate)
                .setAge(birthday, now)
                .setWeight(weight)
                .setHeight(height)
                .setHRrest(restingHeart)
                .setCadence(cadence)
                .setInterval4Cadence(interval4Cadence);
        return user;
    }

    private long time;// 保存点击返回键的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (playType.equals(StringConstant.LIVE_PLAY_TYPE)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - time > IntegerConstant.APP_EXIT_TIME) {
                    T.alwaysLong(mContext, "再按一次返回键退出程序");
                    time = currentTime;
                } else {
                    finish();
                    MyApp.appExit();
                }
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = true;

        if (mVV != null) {
            mVV.release();
            mVV = null;
        }

        if (mLoopUserDataRunnable != null) {
            mHandler.removeCallbacks(mLoopUserDataRunnable);
            mLoopUserDataRunnable = null;
        }

        if (mLoopUserRelaRunnable != null) {
            mHandler.removeCallbacks(mLoopUserRelaRunnable);
            mLoopUserRelaRunnable = null;
        }

        if (mLoopSort != null) {
            mHandler.removeCallbacks(mLoopSort);
            mLoopSort = null;
        }

        if (mChangeViewRunnable != null) {
            mHandler.removeCallbacks(mChangeViewRunnable);
            mChangeViewRunnable = null;
        }

        if (reStartRequestRunnable != null) {
            mHandler.removeCallbacks(reStartRequestRunnable);
            reStartRequestRunnable = null;
        }

        if (mList != null) {
            mList.clear();
            mList = null;
        }

        if (vList != null) {
            vList.clear();
            vList = null;
        }

        if (dKey != null) {
            dKey.clear();
            dKey = null;
        }

        viewLeftFront = null;
        viewRightFront = null;
        viewLeftBack = null;
        viewRightBack = null;
        viewInfo = null;
        autoScrollListView = null;

        mHandler = null;
        mContext = null;
    }
}
