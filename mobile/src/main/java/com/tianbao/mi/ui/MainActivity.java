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
import android.widget.Toast;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.CurrencyBean;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.bean.RecordBean;
import com.tianbao.mi.bean.UserDataBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.QrUtil;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.widget.AutoScrollListView;
import com.tianbao.mi.widget.AutoTypesettingLayout;
import com.tianbao.mi.widget.MemberView;
import com.tianbao.mi.widget.bdplayer.BDCloudVideoView;

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

    private List<InformationBean> iList = new ArrayList<>();

    private Context mContext;
    private BDCloudVideoView mVV = null;// 百度云播放器
    private Handler mHandler = new Handler();// 处理线程

    private boolean isFront = true;// 标记此时在前在后

    private List<UserDataBean> mList;// 保存用户数据
    private List<MemberView> vList = new ArrayList<>();// 保存 View

    private List<String> dKey = new ArrayList<>();// key

    private String playType;// 标识  点播 or 直播

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
        mVV.setOnCompletionListener(iMediaPlayer -> {
            mHandler.postDelayed(() -> {
                startActivity(new Intent(mContext, CourseEndActivity.class));// 跳转到课程结束界面
                finish();
            }, 5 * 1000L);
        });
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

        initPlayer();// 初始化播放器
        initView();

        Intent intent = getIntent();
        if (intent == null) return ;
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
        mHandler.postDelayed(mChangeViewRunnable, 7 * 1000L);// 前后面交换数据
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
                startActivity(new Intent(mContext, CourseEndActivity.class));// 跳转到课程结束界面
                finish();
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
                UserDataBean bean = new UserDataBean();
                bean.setKey(key);// 将 key 先加入  key 是识别用户数据与用户绑定关系的唯一标识
                bean.setSort(0);
                if (mList == null) mList = new ArrayList<>();
                mList.add(bean);
            }
        }
        for (String key : dKey) {
            for (int i = 0; i < mList.size(); i++) {
                if (key.equals(mList.get(i).getKey())) {
                    UserDataBean bean = mList.get(i);

                    Map<String, String> dData = dMap.get(key);
                    if (dData == null) dData = new HashMap<>();

                    bean.setHeartRate(dData.get("heartRate"));
                    String rate = dData.get("rate");
                    if (TextUtils.isEmpty(rate) || rate.equals("0") || rate.equals("--")) {// 获取空的数据
                        String oldRate = mList.get(i).getRate();// 取出上一次的数据
                        if (oldRate != null) {
                            if (!TextUtils.isEmpty(oldRate) && !oldRate.equals("0") && !rate.equals("--")) {// 上一次的数据不为空
                                bean.setRate(rate);
                                bean.setLastTime(System.currentTimeMillis());// 将空的数据填入并记录当前时间
                            } else {// 上一次的数据就已经为空 说明已经记录了第一次为空时的时间 这里就需要判断数据为空时长是否大于 5 分钟
                                long lastTime = bean.getLastTime();// 取出记录为空时的时间
                                if (lastTime != 0) {// 不为 0
                                    long time = System.currentTimeMillis();// 获取当前时间  用于比较
                                    if (time - lastTime >= 5 * 60 * 1000L) {// 数据空档期已经超过 5 分钟

                                        L.i("TimeDifference", "TimeDifference -> " + (time - lastTime));

                                        String openId = bean.getOpenId();
                                        String headId = bean.getKey();
                                        requestUnbinding(openId, headId);// 将此信息发送到后台
                                    }
                                } else {// 记录的时间为 0 说明是第一次没有数据的记录
                                    bean.setRate(rate);
                                    bean.setLastTime(System.currentTimeMillis());
                                }
                            }
                        }
                    } else {
                        bean.setRate(rate);
                    }

                    Map<String, String> rData = rMap.get(key);
                    if (rData == null) rData = new HashMap<>();

                    bean.setAvatar(rData.get("avatar"));
                    bean.setNick(rData.get("nick"));
                    bean.setSex(rData.get("sex"));
                    bean.setOpenId(rData.get("openId"));
                    bean.setUserId(rData.get("userId"));
                    break;
                }
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
        UserDataBean dataBean;
        if (mList == null || mList.size() <= 0) return ;
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

        for (int i=0; i<mList.size(); i++) {
            UserDataBean bean = mList.get(i);
            String hr = bean.getHeartRate();
            if (TextUtils.isEmpty(hr) || hr.equals("NULL") || hr.equals("null") || hr.equals("--")) return ;
            if (Integer.valueOf(hr) >= 180) {
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
                SoundPlayUtils.play(7);
            }
        }
    }

    // 排序  根据消耗卡路里由大到小
    private void sortData() {
        if (mList == null || mList.size() <= 3) return ;
        if (vList == null || vList.size() <= 3) return ;
        List<UserDataBean> tempList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) tempList.add(mList.get(i));
        List<String> resultList = sortResult(tempList);
        if (resultList == null || resultList.size() < 3) return ;

        InformationBean iBean = new InformationBean();
        iBean.setType(IntegerConstant.VIEW_TYPE_SORT);
        List<InformationBean.SortBean> sList = new ArrayList<>();

        InformationBean.SortBean sortBean;

        for (int i=0; i<mList.size(); i++) {
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
            SoundPlayUtils.play(6);
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
    private List<String> sortResult(List<UserDataBean> list) {
        if (list == null || list.size() <= 0) return null;
        if (list.size() <= 3) return null;
        UserDataBean[] arr = new UserDataBean[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        List<UserDataBean> result = new ArrayList<>();
        for (int x = 0; x < arr.length; x++) {
            UserDataBean a = arr[x];
            for (int y = x + 1; y < arr.length; y++) {
                UserDataBean b = arr[y];
                if ((float) a.getCalorie() >= (float) b.getCalorie()) continue;
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
            mHandler.postDelayed(this, 5 * 1000L);
        }
    };

    // 没有获取到数据或获取数据失败时重复获取数据
    private int count = 0;
    private Runnable reStartRequest =() -> {
        if (count < 5) {
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
                BuildBean buildBean = response.body();
                int code = buildBean.getCode();
                if (IntegerConstant.RESULT_OK == code) {
                    if (mHandler!= null && reStartRequest != null) {
                        mHandler.removeCallbacks(reStartRequest);
                    }
                    rMap = buildBean.getData();
                    List<String> tempList = new ArrayList<>();
                    for (Map.Entry<String, Map<String, String>> entry : rMap.entrySet()) {
                        tempList.add(entry.getKey());
                    }
                    assemblyData(tempList);
                } else {
                    mHandler.postDelayed(reStartRequest, 2000L);
                    L.w("requestUserInfo", "没有获取到用户绑定数据或没有用户绑定数据");
                }
            }

            @Override
            public void onFailure(Throwable t) {
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
                CourseInfoBean courseInfo = response.body();
                if (courseInfo == null) {
                    mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                    return ;
                }

                int code = courseInfo.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    CourseInfoBean.DataBean.CourseBean course = courseInfo.getData().getCourse();
                    if (course == null) {// 轮询获取数据线程开关由数据决定
                        mHandler.postDelayed(mLoopRequestRunnable, IntegerConstant.GET_LIVE_COURSE_STATUS);
                    } else {
                        String status = course.getLiveStatus();
                        if (status.equals(StringConstant.LIVE_STATU_OVER)) {
                            startActivity(new Intent(mContext, CourseEndActivity.class));// 跳转到课程结束界面
                            finish();
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
                CurrencyBean bean = response.body();
                if (bean == null) return ;
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

    // 获取课程直播状态 用于没有接收到课程结束推送消息时自行结束课程
    private Runnable mLoopRequestRunnable = ()-> requestCourseStatus();

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

    private long time;// 保存点击返回键的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (playType.equals(StringConstant.LIVE_PLAY_TYPE)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - time > 2000L) {
                    Toast.makeText(mContext, "再按一次返回键退出程序", Toast.LENGTH_LONG).show();
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
