package com.tianbao.mi.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.bean.RecordBean;
import com.tianbao.mi.bean.UserDataBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.widget.AutoScrollListView;
import com.tianbao.mi.widget.AutoTypesettingLayout;
import com.tianbao.mi.widget.MemberView;
import com.tianbao.mi.widget.bdplayer.BDCloudVideoView;

import java.util.ArrayList;
import java.util.Collections;
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
 * 加载成员信息和展示数据
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

    @BindView(R.id.auto_scroll_list)
    AutoScrollListView autoScrollListView;

    private Context mContext;
    private BDCloudVideoView mVV = null;// 百度云播放器
    private Handler mHandler = new Handler();// 处理线程

    private boolean isFront = true;// 标记此时在前在后

    private List<UserDataBean> mList;// 保存用户数据
    private List<MemberView> vList = new ArrayList<>();// 保存 View

    private List<String> dKey;// key

    private String key1;// No.1
    private String key2;// No.2
    private String key3;// No.3

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
        mVV.start();
    }

    private List<InformationBean> list = new ArrayList<>();

    private void initScrollView() {
        InformationBean bean;

        bean = new InformationBean();
        bean.setType(IntegerConstant.VIEW_TYPE_SORT);
        List<InformationBean.SortBean> sList = new ArrayList<>();

        InformationBean.SortBean sortBean = new InformationBean.SortBean();
        sortBean.setSort(1);
        sortBean.setName("zhangsan1");
        sList.add(sortBean);

        sortBean = new InformationBean.SortBean();
        sortBean.setSort(2);
        sortBean.setName("zhangsan2");
        sList.add(sortBean);

        sortBean = new InformationBean.SortBean();
        sortBean.setSort(3);
        sortBean.setName("zhangsan3");
        sList.add(sortBean);

        bean.setSortList(sList);
        list.add(bean);

        bean = new InformationBean();
        bean.setType(IntegerConstant.VIEW_TYPE_TIP);
        bean.setName("lisi1");
        bean.setTip("心率过快！请注意训练强度！");
        list.add(bean);

        bean = new InformationBean();
        bean.setType(IntegerConstant.VIEW_TYPE_TIP);
        bean.setName("lisi2");
        bean.setTip("速度过慢！请加油！");
        list.add(bean);

        bean = new InformationBean();
        bean.setType(IntegerConstant.VIEW_TYPE_TIP);
        bean.setName("lisi3");
        bean.setTip("心率过快！请注意训练强度！");
        list.add(bean);

        bean = new InformationBean();
        bean.setType(IntegerConstant.VIEW_TYPE_TIP);
        bean.setName("lisi4");
        bean.setTip("心率过快！请注意训练强度！");
        list.add(bean);

        autoScrollListView.setInfoList(list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startActivity(new Intent(this, CourseEndActivity.class));
//        finish();

        ButterKnife.bind(this);
        mContext = this;

        registerBroad();// 注册广播
        initPlayer();// 初始化播放器
        initData();

//        initScrollView();
    }

    // 初始化数据
    @SuppressWarnings("unchecked")
    private void initData() {
        Intent intent = getIntent();
        if (intent == null) return;// 没有获取到数据
        mList = (List<UserDataBean>) intent.getSerializableExtra(StringConstant.USER_DATA_LIST);
        if (mList == null) mList = new ArrayList<>();// 没有数据

        // 取出 Key
        for (UserDataBean bean : mList) {
            String key = bean.getKey();
            if (dKey == null) dKey = new ArrayList<>();
            dKey.add(key);
        }

        initView();
    }

    // 初始化视图
    private void initView() {
        viewInfo.setAlpha(0.5f);
        invalidateView();

        // 添加动画效果
        if (viewLeftFront.childCount() > 0) viewLeftFront.setLayoutAnimation(getAnimationController());
        if (viewRightFront.childCount() > 0) viewRightFront.setLayoutAnimation(getAnimationController());

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
            sort();
            mHandler.postDelayed(this, IntegerConstant.SORT_FREQUENCY);
        }
    };

    // 刷新界面
    private void invalidateView() {
        if (viewLeftFront != null) viewLeftFront.removeAllViews();
        if (viewRightFront != null) viewRightFront.removeAllViews();
        if (vList != null) vList.clear();

        MemberView childView;
        UserDataBean dataBean;
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

        // 加载
        updateView();
    }

    // 取出前五名的数据  根据卡路里
    private void sort() {
        if (mList == null || mList.size() <= 0) return;// 没有数据
        if (vList == null || vList.size() <= 0) return;// 没有数据
        Collections.sort(mList);

        if (mList != null && mList.size() > 3) {
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    key1 = mList.get(i).getKey();// 获取第一名
                } else if (i == 1) {
                    key2 = mList.get(i).getKey();// 获取第二名
                } else if (i == 2) {
                    key3 = mList.get(i).getKey();// 获取第三名
                }
            }

            for (MemberView view : vList) {
                if (view.getKey().equals(key1)) {
                    view.dataSort(1);
                } else if (view.getKey().equals(key2)) {
                    view.dataSort(2);
                } else if (view.getKey().equals(key3)) {
                    view.dataSort(3);
                } else {
                    view.dataSort(0);
                }
            }
        }
//        mHandler.postDelayed(() -> invalidateView(), 5000L);
    }

    // 接收广播 更新用户绑定关系
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(StringConstant.BROAD_BUILD_UPDATE)) {// 有新的用户绑定关系需要处理
                String key = intent.getStringExtra(StringConstant.BUILD_UPDATE_KEY);
                if (dKey == null) dKey = new ArrayList<>();
                if (!dKey.contains(key)) {
                    dKey.add(key);
                }
                requestUserInfo();
            } else if (action.equals(StringConstant.BROAD_END_COURSE)) {// 课程结束

                startActivity(new Intent(mContext, CourseEndActivity.class));// 跳转到课程结束界面
                finish();
            }
        }
    };

    private Map<String, Map<String, String>> dMap;// 用户数据
    private Map<String, Map<String, String>> rMap;// 用户绑定关系

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

                    updateData(tempList);
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

    // 更新数据
    private void updateData(List<String> tempList) {
        if (tempList == null || tempList.size() <= 0) return;// 没有数据
        if (dMap == null || dMap.size() <= 0) return;// 没有数据
        if (dKey == null) dKey = new ArrayList<>();

        for (String key : tempList) {
            if (dKey.contains(key)) {// 没有新增数据
                Map<String, String> dData = dMap.get(key);
                for (MemberView view : vList) {
                    if (key.equals(view.getKey())) {
                        view.setUserData(dData);
                        break;
                    }
                }
            } else {// 有数据新增
                dKey.add(key);
                MemberView view = new MemberView(mContext);
                UserDataBean bean = new UserDataBean();
                Map<String, String> dData = dMap.get(key);
                bean.setKey(key);
                bean.setHeartRate(dData.get("heartRate"));
                bean.setRate(dData.get("rate"));
                view.setData(bean);
                mList.add(bean);

                if (viewLeftFront.childCount() < 7) {
                    viewLeftFront.addView(view);
                } else if (viewRightFront.childCount() < 7) {
                    viewRightFront.addView(view);
                } else if (viewLeftBack.childCount() < 7) {
                    viewLeftBack.addView(view);
                } else if (viewRightBack.childCount() < 7) {
                    viewRightBack.addView(view);
                }
            }
        }
        updateView();
    }

    // 前后面交换数据
    private Runnable mChangeViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mList.size() > 14) {// 后面有数据
                viewLeftFront.startAnim(isFront);
                viewRightFront.startAnim(isFront);
                viewLeftBack.startAnim(!isFront);
                viewRightBack.startAnim(!isFront);

                isFront = !isFront;
            }

            mHandler.postDelayed(this, 5 * 1000L);
        }
    };

    // 获取用户的绑定关系
    private void requestUserInfo() {
        Map<String, List<String>> param = new HashMap<>();
        param.put("headIds", dKey);
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
                    rMap = buildBean.getData();

                    updateRela();
                } else {
                    L.w("LoadFail", "requestUserInfo 获取数据失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.w("LoadFail", "requestUserInfo 连接服务器失败");
            }
        });
    }

    // 更新用户关系
    private void updateRela() {
        if (rMap == null || rMap.size() <= 0) return ;// 没有数据
        mList = assemblyData();
        setView();
    }

    public void setView() {
        if (mList == null || mList.size() <= 0) return ;// 没有数据
        for (UserDataBean bean : mList) {
            String key = bean.getKey();
            if (dKey.contains(key)) {// 没有新用户加入
                Map<String, String> dData = rMap.get(key);
                for (MemberView view : vList) {
                    if (key.equals(view.getKey())) {
                        view.setRelaData(dData);
                        break;
                    }
                }
            } else {// 有新的用户加入
                dKey.add(key);
                MemberView view = new MemberView(mContext);
                view.setData(bean);

                if (viewLeftFront.childCount() < 7) {
                    viewLeftFront.addView(view);
                } else if (viewRightFront.childCount() < 7) {
                    viewRightFront.addView(view);
                } else if (viewLeftBack.childCount() < 7) {
                    viewLeftBack.addView(view);
                } else if (viewRightBack.childCount() < 7) {
                    viewRightBack.addView(view);
                }
            }
        }
        updateView();
    }

    // 组装数据 树莓派 + 服务器
    private List<UserDataBean> assemblyData() {
        if (dKey == null || dKey.size() <= 0) return null;
        List<UserDataBean> data = new ArrayList<>();
        UserDataBean userDataBean;
        for (String key : dKey) {
            userDataBean = new UserDataBean();
            Map<String, String> dData = dMap.get(key);
            Map<String, String> rData = rMap.get(key);

            userDataBean.setKey(key);

            if (rData == null) rData = new HashMap<>();
            if (dData == null) dData = new HashMap<>();

            userDataBean.setAvatar(rData.get("avatar"));
            userDataBean.setNick(rData.get("nick"));
            userDataBean.setSex(rData.get("sex"));
            userDataBean.setOpenId(rData.get("openId"));
            userDataBean.setUserId(rData.get("userId"));

            userDataBean.setHeartRate(dData.get("heartRate"));
            userDataBean.setRate(dData.get("rate"));

            data.add(userDataBean);
        }
        return data;
    }

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

    // Layout 动画
    private LayoutAnimationController getAnimationController() {
        int duration = 300;
        AnimationSet set = new AnimationSet(true);

        // 透明动画  View 越来越清晰
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        // 位移动画
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);// 0.5f 动画播放的延迟时间

        /*
         * 子 view 播放动画的顺序
         * ORDER_NORMAL     正序加载
         * ORDER_REVERSE    倒序加载
         * ORDER_RANDOM     随机加载
         */
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    private long time;// 保存点击返回键的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            long currentTime = System.currentTimeMillis();
            if (currentTime - time > 2000L) {
                Toast.makeText(mContext, "再按一次返回键退出程序", Toast.LENGTH_LONG).show();
                time = currentTime;
            } else {
                finish();
                MyApp.appExit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
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

        if(mChangeViewRunnable != null) {
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

        key1 = null;
        key2 = null;
        key3 = null;

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
