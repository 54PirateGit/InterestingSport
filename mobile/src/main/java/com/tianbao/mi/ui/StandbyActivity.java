package com.tianbao.mi.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.adapter.BannerAdapter;
import com.tianbao.mi.adapter.LiveListAdapter;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.LiveCourseBean;
import com.tianbao.mi.bean.PartnerBean;
import com.tianbao.mi.bean.PartnerTipBean;
import com.tianbao.mi.bean.SelectCourseBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.DialogUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.ListViewUtils;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.SoundPlayUtils;
import com.tianbao.mi.widget.AutoScrollListView;
import com.tianbao.mi.widget.PartnerLayout;
import com.tianbao.mi.widget.PartnerView;
import com.tianbao.mi.widget.banner.Banner;

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
 * 待机界面
 * 10/30
 */
public class StandbyActivity extends Activity {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.advertisement)
    Banner advertisement;
    @BindView(R.id.text_name)
    TextView textName;// 店名
    @BindView(R.id.list_live_course)
    ListView listLive;// 直播课程列表
    @BindView(R.id.text_year)
    TextView textYear;// 当前年份
    @BindView(R.id.view_partner)
    PartnerLayout viewPartner;// 瘾伙伴
    @BindView(R.id.auto_scroll_list)
    AutoScrollListView scrollList;// 新加入瘾伙伴提示信息
    @BindView(R.id.image_more)
    ImageView imageMore;// 标识有更多直播数据

    private Dialog dialogLoading;
    private boolean isLoad;

    private Context mContext;
    private LiveListAdapter adapter;

    private Handler mHandler = new Handler();
    private boolean isLoop = true;// 没有课程信息时每隔一段时间去获取
    private final static long LOOP_TIME = 3 * 60 * 1000L;// 隔一定时间去获取课程信息

    private List<LiveCourseBean.DataBean> dList;// 保存直播课程信息
    private ArrayList<String> kList = new ArrayList<>();
    private List<String> key = new ArrayList<>();
    private List<PartnerTipBean> pList;

    private void setKey() {
        int storeId = (int) SPUtils.get(mContext, StringConstant.STORE_ID_SP_KEY, 1);
        String kString;
        for (int i = 1; i < 29; i++) {
            kString = storeId + "_" + i;
            key.add(kString);
        }
    }

    // 注册广播
    private void registerBroad() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(StringConstant.BROAD_BUILD_UPDATE);
        filter.addAction(StringConstant.BROAD_START_COURSE);
        registerReceiver(mReceiver, filter);
    }

    // 设置字体
    private void setFront() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "font/FZKTJT.ttf");
        textName.setTypeface(tf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standby);
        mContext = this;
        ButterKnife.bind(this);

        registerBroad();// 注册广播
        setKey();// 全部 key
        initView();
    }

    // 初始化视图
    private void initView() {
        setFront();
        Picasso.with(mContext).load(R.drawable.kec).into(imageBackground);
        textName.setText(StringConstant.STORE_NAME);
        textYear.setText(StringConstant.TIME_YEAR);
        initBanner();

        mHandler.post(mLoopLiveListRunnable);

        requestUserInfo(key);
//        viewPartner();

        scrollList.setAlpha(0.5f);
    }

    // 初始化轮播图
    private void initBanner() {
        // 轮播图数据  测试
        List<Integer> bannerData = new ArrayList<>();
        bannerData.add(R.drawable.jingp);
//        bannerData.add(R.mipmap.a2);
//        bannerData.add(R.mipmap.a3);
//        bannerData.add(R.mipmap.a4);

        // 轮播图
        BannerAdapter bannerAdapter = new BannerAdapter(mContext, 400);
        bannerAdapter.setData(bannerData);
        banner.setDotGravity(Banner.CENTER).
                setDot(R.drawable.no_selected_dot, R.drawable.selected_dot).
                setAdapter(bannerAdapter);

        if (bannerData.size() > 1) {
            banner.startAutoPlay();//  自动播放轮播图
        } else {
            banner.stopAutoPlay();
        }

        if (bannerData.size() == 0) banner.setAlpha(0.5f);
        else banner.setAlpha(1.0f);

        advertisement.setAlpha(0.5f);
    }

    // 轮询获取课程信息
    private Runnable mLoopRequestRunnable = new Runnable() {
        @Override
        public void run() {
            if (isLoop) {
                request();
                mHandler.postDelayed(this, LOOP_TIME);
            }
        }
    };

    // 轮询获取直播课程列表
    private Runnable mLoopLiveListRunnable = new Runnable() {
        @Override
        public void run() {
            requestLiveList();
            mHandler.postDelayed(this, 5 * 60 * 1000L);
        }
    };

    // 获取直播课程列表
    private void requestLiveList() {
        Map<String, String> param = new HashMap<>();
        param.put("deviceId", StringConstant.DEVICE_ID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<LiveCourseBean> model = service.getLiveList(param);
        model.enqueue(new Callback<LiveCourseBean>() {
            @Override
            public void onResponse(Response<LiveCourseBean> response, Retrofit retrofit) {
                LiveCourseBean courseBean = response.body();
                if (courseBean == null) return;
                L.v("courseBean", courseBean.toString());

                int code = courseBean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    if (dList == null) dList = new ArrayList<>();
                    dList = courseBean.getData();
                    if (dList.size() <= 3) imageMore.setVisibility(View.INVISIBLE);
                    else imageMore.setVisibility(View.VISIBLE);
                    if (adapter == null) {// 第一次进入界面时加载数据
                        adapter = new LiveListAdapter(mContext, dList);
                        listLive.setAdapter(adapter);
                        listLive.setLayoutAnimation(getAnimationController());

                        ListViewUtils.setListHeight(listLive);
                        adapter.notifyDataSetChanged();

                        mHandler.postDelayed(() -> {
                            if (MyApp.getCourseId() != 0) request();
                        }, 5000L);
                    } else {// 界面有更新
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(mContext, "获取课程信息失败，请稍后重试", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "连接服务器失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 选择直播课程
    private void selectLiveList(int id) {
        Map<String, String> param = new HashMap<>();
        param.put("courseId", String.valueOf(id));
        param.put("deviceId", StringConstant.DEVICE_ID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<SelectCourseBean> model = service.selectCourseLive(param);
        model.enqueue(new Callback<SelectCourseBean>() {
            @Override
            public void onResponse(Response<SelectCourseBean> response, Retrofit retrofit) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                SelectCourseBean bean = response.body();
                if (bean == null) return;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    if (adapter != null) {
                        for (int i=0; i<dList.size(); i++) {
                            if (i == position) {
                                dList.get(i).setSelect(true);
                            } else {
                                dList.get(i).setSelect(false);
                            }
                        }
                        adapter.setList(dList);
//                        courseId = adapter.ok(position);
                        mHandler.postDelayed(() -> request(), 5000L);
                    }
                } else {
                    Toast.makeText(mContext, "获取课程信息失败，请重试", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                Toast.makeText(mContext, "连接服务器失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 选择直播课程
    private void changeLiveList(int id, int oId) {
        Map<String, String> param = new HashMap<>();
        param.put("courseId", String.valueOf(id));
        param.put("oldCourseId", String.valueOf(oId));
        param.put("deviceId", StringConstant.DEVICE_ID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<SelectCourseBean> model = service.changeCourseLive(param);
        model.enqueue(new Callback<SelectCourseBean>() {
            @Override
            public void onResponse(Response<SelectCourseBean> response, Retrofit retrofit) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                SelectCourseBean bean = response.body();
                if (bean == null) return;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    if (mIntoLiveRunnable != null) {
                        mHandler.removeCallbacks(mIntoLiveRunnable);
                    }

                    if (adapter != null) {
                        for (int i=0; i<dList.size(); i++) {
                            if (i == position) {
                                dList.get(i).setSelect(true);
                            } else {
                                dList.get(i).setSelect(false);
                            }
                        }
                        adapter.setList(dList);

//                        courseId = adapter.ok(position);

                        mHandler.postDelayed(() -> request(), 5000L);
                    }
                } else {
                    Toast.makeText(mContext, "获取课程信息失败，请重试", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                Toast.makeText(mContext, "连接服务器失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 获取一次课程信息
    private void request() {
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
                    isLoop = true;
                    mHandler.postDelayed(mLoopRequestRunnable, LOOP_TIME);
                    return ;
                }

                int code = courseInfo.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    L.v("courseInfo -> > > " + courseInfo.toString());

                    CourseInfoBean.DataBean.CourseBean course = courseInfo.getData().getCourse();
                    if (course == null) {// 轮询获取数据线程开关由数据决定

                        isLoop = true;
                        mHandler.postDelayed(mLoopRequestRunnable, LOOP_TIME);
                    } else {

                        L.v("status -> > > " + course.getLiveStatus());

                        if (course.getLiveStatus().equals(StringConstant.LIVE_STATU_ING)) {
//                            mHandler.postDelayed(mIntoLiveRunnable, 5 * 1000L);
                            SoundPlayUtils.play(2);// 播放背景音乐

                            isLoop = false;
                            Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
                            intent.putStringArrayListExtra(StringConstant.USER_KEY, kList);
                            startActivity(intent);// 进入加载
                            finish();
                        }
                    }
                } else {
                    L.w("data is null");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.w("连接服务器失败");
            }
        });
    }

    // 接收广播 更新用户绑定关系
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(StringConstant.BROAD_BUILD_UPDATE)) {// 有新的用户绑定关系需要处理
                String key = intent.getStringExtra(StringConstant.BUILD_UPDATE_KEY);

                List<String> tempList = new ArrayList<>();
                tempList.add(key);
                requestUserInfo(tempList);

                SoundPlayUtils.play(1);// 播放背景音乐  有新的瘾伙伴加入

//                if (kList == null) kList = new ArrayList<>();
//                else if (!kList.contains(key)) {// 有新的用户加入
//                    kList.add(key);
//                    List<String> tempList = new ArrayList<>();
//                    tempList.add(key);
//                    requestUserInfo(tempList);
//                }
            } else if (action.equals(StringConstant.BROAD_START_COURSE)) {// 课程开始
                mHandler.postDelayed(mIntoLiveRunnable, 5 * 1000L);
            }
        }
    };

    // 进入直播
    private Runnable mIntoLiveRunnable = () -> {
        isLoop = false;
        Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
        intent.putStringArrayListExtra(StringConstant.USER_KEY, kList);
        startActivity(intent);// 进入加载
        finish();
    };

    // 获取用户的绑定关系
    private void requestUserInfo(List<String> tempList) {
        Map<String, List<String>> param = new HashMap<>();
        param.put("headIds", tempList);
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
                if (buildBean == null) return ;

                int code = buildBean.getCode();
                if (IntegerConstant.RESULT_OK == code) {
                    L.v("response", buildBean.toString());

                    Map<String, Map<String, String>> rMap = buildBean.getData();
                    if (kList == null || kList.size() <= 0) {
                        if (kList == null) kList = new ArrayList<>();

                        List<PartnerBean> list = new ArrayList<>();
                        PartnerBean pBean;

                        for (Map.Entry<String, Map<String, String>> entry : rMap.entrySet()) {
                            System.out.println("key = " + entry.getKey() + " and value = " + entry.getValue());
                            kList.add(entry.getKey());

                            // 提示有新的瘾伙伴加入
                            PartnerTipBean bean = new PartnerTipBean();
                            Map<String, String> map = entry.getValue();
                            String name = map.get("nick");
                            if (TextUtils.isEmpty(name)) name = "NULL";
                            bean.setName(name);
                            if (pList == null) pList = new ArrayList<>();
                            pList.add(bean);

                            // 加入的瘾伙伴展示
                            pBean = new PartnerBean();
                            pBean.setNick(name);
                            pBean.setHead(map.get("avatar"));
                            list.add(pBean);
                        }
                        if (pList == null) pList = new ArrayList<>();
                        scrollList.setPartnerList(pList);

                        List<PartnerView> vList = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            PartnerView view = new PartnerView(mContext);
                            view.updateView(list.get(i));
                            vList.add(view);
                        }
                        viewPartner.setList(vList);
                    } else {
                        for (Map.Entry<String, Map<String, String>> entry : rMap.entrySet()) {
                            if (!kList.contains(entry.getKey())) {
                                kList.add(entry.getKey());

                                // 提示有新的瘾伙伴加入
                                PartnerTipBean bean = new PartnerTipBean();
                                Map<String, String> map = entry.getValue();
                                String name = map.get("nick");
                                if (TextUtils.isEmpty(name)) name = "NULL";
                                bean.setName(name);
                                scrollList.updatePartnerData(bean);

                                // 加入的瘾伙伴展示
                                PartnerBean pBean = new PartnerBean();
                                pBean.setNick(name);
                                pBean.setHead(map.get("avatar"));

                                PartnerView view = new PartnerView(mContext);
                                view.updateView(pBean);
                                viewPartner.setPartnerView(view);
                            }
                        }
                    }

                } else {
                    Toast.makeText(mContext, "code == " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "errorCode != 0", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private List<PartnerBean> getData() {
//        List<PartnerBean> list = new ArrayList<>();
//        PartnerBean bean;
//        for (int i = 0; i < 20; i++) {
//            bean = new PartnerBean();
//            bean.setNick("用户_" + i);
//            bean.setHead("");
//            list.add(bean);
//        }
//        return list;
//    }

    // 加载瘾伙伴
//    private void viewPartner() {
//        List<PartnerView> vList = new ArrayList<>();
//        List<PartnerBean> list = getData();
//        for (int i = 0; i < list.size(); i++) {
//            PartnerView view = new PartnerView(mContext);
//            view.updateView(list.get(i));
//            vList.add(view);
//        }
//        viewPartner.setList(vList);
//    }

    private int position = 0;// 当前位置
//    private int courseId = 0;// 课程 ID

    // 处理遥控器按键事件
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isLoad || adapter == null) return super.dispatchKeyEvent(event);
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            // up 事件,这里多数情况不需要处理
        } else {
            // down 事件或许可以直接覆盖 onKeyDown 方法, 而不是这个
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {// 上
                position--;
                if (position < 0) position = dList.size() - 1;
                adapter.up(position);
                listLive.setSelection(position);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {// 左
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {// 右
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {// 下
                position++;
                if (position == dList.size()) position = 0;
                adapter.down(position);
//                if (position >= 3) listLive.setSelection(position - 2);
//                else if (position == 0) listLive.setSelection(position);
                listLive.setSelection(position);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {// 确认
                if (!dList.get(position).isSelect()) {
                    dialogLoading = DialogUtils.dialogLoading(mContext);
                    dialogLoading.show();
                    isLoad = true;
                    if (MyApp.getCourseId() == 0) {
                        selectLiveList(adapter.getCourseId(position));
                    } else {
                        changeLiveList(adapter.getCourseId(position), MyApp.getCourseId());
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 监控返回键
                long currentTime = System.currentTimeMillis();
                if (currentTime - time > 2000L) {
                    Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_LONG).show();
                    time = currentTime;
                } else {
                    finish();
                    MyApp.appExit();
                }
            }
        }
        return true;
    }

    private long time;// 保存点击返回键的时间

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            if (mLoopRequestRunnable != null) {
                mHandler.removeCallbacks(mLoopRequestRunnable);
                mLoopRequestRunnable = null;
            }
            if (mLoopLiveListRunnable != null) {
                mHandler.removeCallbacks(mLoopLiveListRunnable);
                mLoopLiveListRunnable = null;
            }
            mHandler = null;
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (dialogLoading != null) {
            dialogLoading.dismiss();
            dialogLoading = null;
        }
        isLoad = false;
    }
}
