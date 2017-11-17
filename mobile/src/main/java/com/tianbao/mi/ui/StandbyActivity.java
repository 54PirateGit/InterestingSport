package com.tianbao.mi.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;
import com.tianbao.mi.R;
import com.tianbao.mi.adapter.BannerAdapter;
import com.tianbao.mi.adapter.LiveListAdapter;
import com.tianbao.mi.adapter.OnDemandAdapter;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.BuildBean;
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.CurrencyBean;
import com.tianbao.mi.bean.LiveCourseBean;
import com.tianbao.mi.bean.OnDemandCourseBean;
import com.tianbao.mi.bean.PartnerBean;
import com.tianbao.mi.bean.PartnerTipBean;
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
    @BindView(R.id.list_demand_course)
    ListView listDemand;// 点播课程列表
    @BindView(R.id.text_year)
    TextView textYear;// 当前年份
    @BindView(R.id.view_partner)
    PartnerLayout viewPartner;// 瘾伙伴
    @BindView(R.id.auto_scroll_list)
    AutoScrollListView scrollList;// 新加入瘾伙伴提示信息
    @BindView(R.id.image_more)
    ImageView imageMore;// 标识有更多直播数据
    @BindView(R.id.text_live)
    TextView textLive;// tab 直播
    @BindView(R.id.text_demand)
    TextView textDemand;// tab 点播

    private Dialog dialogLoading;
    private boolean isLoad;
    private boolean isSelectTab = false;// 焦点是否在 TAB 上  TAB 为选择直播或点播内容  默认焦点不是在 TAB 上  而是在直播列表中的第一条数据上
    private boolean isSelectLive = true;// 焦点是否直播列表中  数据刚加载默认是显示直播列表

    private Context mContext;
    private LiveListAdapter lAdapter;// 直播课程信息展示
    private OnDemandAdapter oAdapter;// 点播课程信息展示

    private Handler mHandler = new Handler();
    private boolean isLoop = true;// 没有课程信息时每隔一段时间去获取
    private final static long LOOP_TIME = 3 * 60 * 1000L;// 隔一定时间去获取课程信息

    private List<OnDemandCourseBean.DataBean> oList;// 保存点播课程信息
    private List<LiveCourseBean.DataBean> dList;// 保存直播课程信息
    private List<String> key = new ArrayList<>();
    private List<PartnerTipBean> pList;
    private List<String> ids = new ArrayList<>();

    private ArrayList<String> upList = new ArrayList<>();
    private ArrayList<String> downList = new ArrayList<>();

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

        setKey();// 全部 key
        initView();

//        isLoop = false;
//        Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
//        startActivity(intent);// 进入加载
//        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerBroad();// 注册广播
        mHandler.post(mLoopLiveListRunnable);
        if (isLoop) {
            mHandler.postDelayed(mLoopRequestRunnable, LOOP_TIME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mLoopLiveListRunnable != null) {
            mHandler.removeCallbacks(mLoopLiveListRunnable);
        }
        if (mLoopRequestRunnable != null) {
            mHandler.removeCallbacks(mLoopRequestRunnable);
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    // 初始化视图
    private void initView() {
        setFront();
        Picasso.with(mContext).load(R.drawable.kec).into(imageBackground);
        textName.setText(StringConstant.STORE_NAME);
        textYear.setText(StringConstant.TIME_YEAR);
        initBanner();

        requestUserInfo(key);
        scrollList.setAlpha(0.5f);

        mHandler.postDelayed(() -> requestOnDemandList(), 3000L);
    }

    // 初始化轮播图
    private void initBanner() {
        Intent intent = getIntent();
        if (intent != null) {
            upList = intent.getStringArrayListExtra(StringConstant.BANNER_LIST_UP);
            downList = intent.getStringArrayListExtra(StringConstant.BANNER_LIST_DOWN);
        }

        if (upList == null || upList.size() == 0) {
            banner.setAlpha(0.5f);
        } else {
            banner.setAlpha(1.0f);

            // 轮播图
            BannerAdapter bannerAdapter = new BannerAdapter(mContext, 200);
            bannerAdapter.setData(upList);
            banner.setDotGravity(Banner.CENTER).
                    setDot(R.drawable.no_selected_dot, R.drawable.selected_dot).
                    setAdapter(bannerAdapter);

            if (upList.size() > 1) {
                banner.startAutoPlay();//  自动播放轮播图
            } else {
                banner.stopAutoPlay();
            }
        }

        if (downList == null || downList.size() == 0) {
            advertisement.setAlpha(0.5f);
        } else {
            advertisement.setAlpha(1.0f);

            BannerAdapter bannerAdapter1 = new BannerAdapter(mContext, 200);
            bannerAdapter1.setData(downList);
            advertisement.setDotGravity(Banner.CENTER).
                    setDot(R.drawable.no_selected_dot, R.drawable.selected_dot).
                    setAdapter(bannerAdapter1);

            if (downList.size() > 1) {
                advertisement.startAutoPlay();//  自动播放轮播图
            } else {
                advertisement.stopAutoPlay();
            }
        }
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

    // 没有获取到数据或获取数据失败时重复请求
    private int count1 = 0;
    private Runnable reStartRequest = () -> {
        if (count1 < 5) {
            requestLiveList();
            count1++;
        }
    };

    // 没有获取到数据或获取数据失败时重复请求
    private int count2 = 0;
    private Runnable reStartRequestDemand = () -> {
        if (count2 < 5) {
            requestOnDemandList();
            count2++;
        }
    };

    // 获取点播课程列表
    private void requestOnDemandList() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        Call<OnDemandCourseBean> model = service.getOnDemandList();
        model.enqueue(new Callback<OnDemandCourseBean>() {
            @Override
            public void onResponse(Response<OnDemandCourseBean> response, Retrofit retrofit) {
                OnDemandCourseBean bean = response.body();
                if (bean != null) {
                    int code = bean.getCode();
                    if (code == IntegerConstant.RESULT_OK) {
                        oList = bean.getData();
                        if (oList == null || oList.size() <= 0) return;
                        oAdapter = new OnDemandAdapter(mContext, oList);
                        oAdapter.down(-1);
                        listDemand.setAdapter(oAdapter);
                        listDemand.setLayoutAnimation(getAnimationController());

                        ListViewUtils.setListHeight(listDemand);
                        oAdapter.notifyDataSetChanged();
                    } else {
                        L.w("requestOnDemandList", "data is error");
                        mHandler.postDelayed(reStartRequestDemand, 3000L);
                    }
                    if (isSelectLive) {
                        listDemand.setVisibility(View.GONE);
                    } else {
                        listDemand.setVisibility(View.VISIBLE);
                    }
                } else {
                    L.w("requestOnDemandList", "data is null");
                    mHandler.postDelayed(reStartRequestDemand, 3000L);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.w("requestOnDemandList", "连接服务器失败");
            }
        });
    }

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
                    if (reStartRequest != null) {
                        mHandler.removeCallbacks(reStartRequest);
                    }
                    dList = courseBean.getData();
                    if (dList == null || dList.size() <= 0) {
                        isSelectTab = true;
                        textLive.setBackground(getResources().getDrawable(R.drawable.tab_white_background));
                        textDemand.setBackground(null);
                        return;
                    }
                    isSelectTab = false;
                    if (dList.size() <= 3) imageMore.setVisibility(View.INVISIBLE);
                    else imageMore.setVisibility(View.VISIBLE);
                    if (lAdapter == null) {// 第一次进入界面时加载数据
                        lAdapter = new LiveListAdapter(mContext, dList);
                        listLive.setAdapter(lAdapter);
                        listLive.setLayoutAnimation(getAnimationController());

                        ListViewUtils.setListHeight(listLive);
                        lAdapter.notifyDataSetChanged();

                        mHandler.postDelayed(() -> {
                            if (MyApp.getCourseId() != 0) request();
                        }, 15000L);
                    } else {// 界面有更新
                        lAdapter.notifyDataSetChanged();
                    }

                    if (isSelectLive) {
                        listLive.setVisibility(View.VISIBLE);
                    } else {
                        listLive.setVisibility(View.GONE);
                    }
                } else {
                    mHandler.postDelayed(reStartRequest, 2000L);

                    L.d("requestLiveList", "requestLiveList 获取课程信息失败，请稍后重试");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.d("requestLiveList", "requestLiveList 连接服务器失败");

                mHandler.postDelayed(reStartRequest, 2000L);
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
        Call<CurrencyBean> model = service.selectCourseLive(param);
        model.enqueue(new Callback<CurrencyBean>() {
            @Override
            public void onResponse(Response<CurrencyBean> response, Retrofit retrofit) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                CurrencyBean bean = response.body();
                if (bean == null) return;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    if (lAdapter != null) {
                        for (int i = 0; i < dList.size(); i++) {
                            if (i == position) {
                                dList.get(i).setSelect(true);
                            } else {
                                dList.get(i).setSelect(false);
                            }
                        }
                        lAdapter.setList(dList);
                        mHandler.postDelayed(() -> request(), 15000L);
                    }
                } else {
                    L.d("selectLiveList", "selectLiveList 获取课程信息失败，请重试");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                L.d("selectLiveList", "selectLiveList 连接服务器失败");
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
        Call<CurrencyBean> model = service.changeCourseLive(param);
        model.enqueue(new Callback<CurrencyBean>() {
            @Override
            public void onResponse(Response<CurrencyBean> response, Retrofit retrofit) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                CurrencyBean bean = response.body();
                if (bean == null) return;
                int code = bean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    if (mIntoLiveRunnable != null) {
                        mHandler.removeCallbacks(mIntoLiveRunnable);
                    }
                    if (lAdapter != null) {
                        for (int i = 0; i < dList.size(); i++) {
                            if (i == position) {
                                dList.get(i).setSelect(true);
                            } else {
                                dList.get(i).setSelect(false);
                            }
                        }
                        lAdapter.setList(dList);
                        mHandler.postDelayed(() -> request(), 15000L);
                    }
                } else {
                    L.d("changeLiveList", "changeLiveList 获取课程信息失败，请重试");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (dialogLoading != null) dialogLoading.dismiss();
                isLoad = false;
                L.d("changeLiveList", "changeLiveList 连接服务器失败");
            }
        });
    }

    // 获取课程信息
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
                    return;
                }

                int code = courseInfo.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    L.v("courseInfo -> > > " + courseInfo.toString());

                    CourseInfoBean.DataBean.CourseBean course = courseInfo.getData().getCourse();
                    if (course == null) {// 轮询获取数据线程开关由数据决定

                        isLoop = true;
                        mHandler.postDelayed(mLoopRequestRunnable, LOOP_TIME);
                    } else {
                        if (course.getLiveStatus().equals(StringConstant.LIVE_STATU_ING)) {
                            SoundPlayUtils.play(2);// 播放背景音乐

                            isLoop = false;
                            Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
                            intent.putExtra(StringConstant.PLAY_TYPE, StringConstant.LIVE_PLAY_TYPE);
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

                L.d("tempList", "tempList -> " + tempList.toString());
            } else if (action.equals(StringConstant.BROAD_START_COURSE)) {// 课程开始
                mHandler.postDelayed(mIntoLiveRunnable, 5 * 1000L);
            }
        }
    };

    // 进入直播
    private Runnable mIntoLiveRunnable = () -> {
        isLoop = false;
        Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
        intent.putExtra(StringConstant.PLAY_TYPE, StringConstant.LIVE_PLAY_TYPE);
        startActivity(intent);// 进入加载
        finish();
    };

    // 获取用户的绑定关系
    private void requestUserInfo(List<String> tempList) {
        tempList.add(0, "0");
        tempList.add("0");

        Map<String, List<String>> param = new HashMap<>();
        param.put("headIds", tempList);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        L.d("tempList", "tempList -> " + tempList.toString());

        ApiService service = retrofit.create(ApiService.class);
        Call<BuildBean> model = service.getBuild(param);
        model.enqueue(new Callback<BuildBean>() {
            @Override
            public void onResponse(Response<BuildBean> response, Retrofit retrofit) {
                BuildBean buildBean = response.body();
                if (buildBean == null) return;

                int code = buildBean.getCode();
                if (IntegerConstant.RESULT_OK == code) {
                    L.v("response", buildBean.toString());

                    Map<String, Map<String, String>> rMap = buildBean.getData();

                    for (Map.Entry<String, Map<String, String>> entry : rMap.entrySet()) {
                        System.out.println("key = " + entry.getKey() + " and value = " + entry.getValue());

                        Map<String, String> map = entry.getValue();
                        String id = map.get("userId");
                        if (!TextUtils.isEmpty(id) && !ids.contains(id)) {
                            ids.add(id);

                            // 提示有新的瘾伙伴加入
                            PartnerTipBean bean = new PartnerTipBean();
                            String name = map.get("nick");
                            if (TextUtils.isEmpty(name)) name = "NULL";
                            bean.setName(name);
                            if (scrollList.isJoinRun()) {
                                scrollList.updatePartnerData(bean);
                            } else {
                                if (pList == null) pList = new ArrayList<>();
                                pList.add(bean);
                                scrollList.setPartnerList(pList);
                            }

                            // 加入的瘾伙伴展示
                            PartnerBean pBean = new PartnerBean();
                            pBean.setNick(name);
                            pBean.setHead(map.get("avatar"));
                            PartnerView view = new PartnerView(mContext);
                            view.updateView(pBean);
                            viewPartner.setPartnerView(view);

                            SoundPlayUtils.play(1);// 播放背景音乐  有新的瘾伙伴加入
                        }
                    }
                } else {
                    L.d("requestUserInfo", "requestUserInfo 没有获取到数据或获取数据失败");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                L.d("requestUserInfo", "requestUserInfo 连接服务器失败");
            }
        });
    }

    private int position = 0;// 当前位置

    // 处理遥控器按键事件
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isLoad || lAdapter == null) return super.dispatchKeyEvent(event);
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            // up 事件,这里多数情况不需要处理
        } else {
            // down 事件或许可以直接覆盖 onKeyDown 方法, 而不是这个
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {// 上
                if (isSelectTab) return true;
                if (position - 1 < 0) {// 焦点转移到 TAB 上
                    isSelectTab = true;
                    if (isSelectLive) {
                        textLive.setBackground(getResources().getDrawable(R.drawable.tab_white_background));
                        textDemand.setBackground(null);
                        if (lAdapter != null) lAdapter.up(-1);
                    } else {
                        textDemand.setBackground(getResources().getDrawable(R.drawable.tab_white_background));
                        textLive.setBackground(null);
                        if (oAdapter != null) oAdapter.up(-1);
                    }
                    return true;
                }
                position--;
                if (isSelectLive) {
                    lAdapter.up(position);
                    if (listLive.getLastVisiblePosition() != position + 1) {
                        listLive.setSelection(position);
                    }
                } else {
                    oAdapter.up(position);
                    if (listDemand.getLastVisiblePosition() != position + 1) {
                        listDemand.setSelection(position);
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {// 左
                if (isSelectTab) {
                    if (isSelectLive) {// 此时选中的是直播列表
                        // 直播列表已是最左边的 TAB  不做处理
                    } else {// 此时选中的是点播课程列表  按左键回到直播列表
                        textDemand.setBackground(null);
                        textLive.setBackground(getDrawable(R.drawable.tab_white_background));
                        isSelectLive = true;

                        listDemand.setVisibility(View.GONE);
                        listLive.setVisibility(View.VISIBLE);

                        if (dList != null && dList.size() > 3) {
                            if (imageMore.getVisibility() == View.GONE) imageMore.setVisibility(View.VISIBLE);
                        } else {
                            if (imageMore.getVisibility() == View.VISIBLE) imageMore.setVisibility(View.GONE);
                        }
                    }
                }

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {// 右
                if (isSelectTab) {
                    if (isSelectLive) {// 此时选中的是直播列表  按右键回到点播列表
                        textLive.setBackground(null);
                        textDemand.setBackground(getDrawable(R.drawable.tab_white_background));
                        isSelectLive = false;

                        listLive.setVisibility(View.GONE);
                        listDemand.setVisibility(View.VISIBLE);

                        if (oList != null && oList.size() > 3) {
                            if (imageMore.getVisibility() == View.GONE) imageMore.setVisibility(View.VISIBLE);
                        } else {
                            if (imageMore.getVisibility() == View.VISIBLE) imageMore.setVisibility(View.GONE);
                        }
                    } else {// 此时选中的是点播课程列表
                        // 点播列表已是最右边的 TAB  不做处理
                    }
                }

            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {// 下
                if (isSelectTab) {
                    isSelectTab = false;
                    if (isSelectLive) {
                        if (dList != null && dList.size() > 0) {// 有数据才能往下
                            textLive.setBackground(getResources().getDrawable(R.drawable.tab_background));
                            textDemand.setBackground(null);
                            lAdapter.down(position);
                        }
                    } else {
                        if (oList != null && oList.size() > 0) {// 有数据才能往下
                            textDemand.setBackground(getResources().getDrawable(R.drawable.tab_background));
                            textLive.setBackground(null);
                            oAdapter.down(position);
                        }
                    }
                    return true;
                }
                if (isSelectLive) {
                    if (dList == null || dList.size() <= 0) return true;
                    if (position + 1 >= dList.size()) return true;
                    position++;
                    lAdapter.down(position);
                    if (listLive.getLastVisiblePosition() < position) {
                        listLive.setSelection(position - 2);
                    }
                } else {
                    if (oList == null || oList.size() == 0) return true;
                    if (position + 1 >= oList.size()) return true;
                    position++;
                    oAdapter.down(position);
                    if (listDemand.getLastVisiblePosition() < position) {
                        listDemand.setSelection(position - 2);
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {// 确认
                if (isSelectTab) return true;
                if (isSelectLive) {// 焦点在直播列表中
                    if (!dList.get(position).isSelect()) {
                        dialogLoading = DialogUtils.dialogLoading(mContext);
                        dialogLoading.show();
                        isLoad = true;
                        if (MyApp.getCourseId() == 0) {
                            selectLiveList(lAdapter.getCourseId(position));
                        } else {
                            changeLiveList(lAdapter.getCourseId(position), MyApp.getCourseId());
                        }
                    }
                } else {// 焦点在点播列表中
                    Intent intent = new Intent(StandbyActivity.this, LoadActivity.class);
                    intent.putExtra(StringConstant.PLAY_TYPE, StringConstant.DEMAND_PLAY_TYPE);
                    startActivity(intent);// 进入加载
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
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler = null;
        }
        if (dialogLoading != null) {
            dialogLoading.dismiss();
            dialogLoading = null;
        }
        isLoad = false;
    }
}
