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
import com.tianbao.mi.bean.CourseInfoBean;
import com.tianbao.mi.bean.FitUser;
import com.tianbao.mi.bean.GymData;
import com.tianbao.mi.bean.InformationBean;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.IntentConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.QrUtil;
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

import static com.tianbao.mi.constant.ReceiverConstant.BROAD_BINDING_UPDATE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_END_COURSE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_TO_SERVICE_BINDING_UPDATE;
import static com.tianbao.mi.constant.ReceiverConstant.BROAD_UPDATE_DATA;
import static com.tianbao.mi.constant.StringConstant.BUILD_UPDATE_KEY;

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
    @BindView(R.id.view_record)
    View viewRecord;// 录播中

    @BindView(R.id.auto_scroll_list)
    AutoScrollListView autoScrollListView;

    @BindView(R.id.image_qr)
    ImageView imageQr;// 二维码
    @BindView(R.id.view_qr)
    View viewQr;

    @BindView(R.id.image_qr_code)
    ImageView imageQrCode;
    @BindView(R.id.view_qr_code)
    View viewQrCode;

    private Context mContext;
    private BDCloudVideoView mVV = null;// 百度云播放器
    private Handler mHandler = new Handler();// 处理线程

    private List<InformationBean> iList = new ArrayList<>();
    private List<FitUser> mList;// 保存用户数据
    private List<MemberView> vList = new ArrayList<>();// 保存 View

    private boolean isFront = true;// 标记此时在前在后
    private String playType;// 标识  点播 or 直播

    private boolean isCancelRequest = false;// 取消所有网络请求

    // 注册广播
    private void registerBroad() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROAD_END_COURSE);
        registerReceiver(mReceiver, filter);
    }

    // 初始化播放器
    private void initPlayer() {
        mVV = (BDCloudVideoView) findViewById(R.id.bd_player);
        if (playType.equals(StringConstant.LIVE_PLAY_TYPE)) {
            mVV.setVideoPath(StringConstant.DEMAND_URL);

            L.i("playUrl", "playUrl -> " + StringConstant.DEMAND_URL);
        } else {
            mVV.setVideoPath(StringConstant.LIVE_URL);

            L.i("playUrl", "playUrl -> " + StringConstant.LIVE_URL);
        }
        mVV.setVideoScalingMode(BDCloudVideoView.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        mVV.setOnCompletionListener(iMediaPlayer -> mHandler.postDelayed(() -> courseEnd(), IntegerConstant.INTO_COURSE_END));
        mVV.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mContext = this;

        Intent intent = getIntent();
        if (intent == null) return;
        playType = intent.getStringExtra(StringConstant.PLAY_TYPE);

        if (playType.equals(StringConstant.DEMAND_PLAY_TYPE)) {
            imageLiving.setVisibility(View.GONE);
            viewRecord.setVisibility(View.VISIBLE);
        } else {
            imageLiving.setVisibility(View.VISIBLE);
            viewRecord.setVisibility(View.GONE);
        }

        initPlayer();// 初始化播放器
        initView();

        initQr();// 初始化二维码
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroad();// 注册广播

        if (mVV != null && !mVV.isPlaying() && !TextUtils.isEmpty(StringConstant.LIVE_URL) && !StringConstant.LIVE_URL.equals("-1")) {
            mVV.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mVV != null && mVV.isPlaying()) {
            mVV.stopPlayback();
        }
    }

    // 初始化视图
    private void initView() {
        viewInfo.setAlpha(0.5f);

        mHandler.postDelayed(mLoopSort, IntegerConstant.SORT_FREQUENCY);// 隔一定时间去根据用户数据排序
        mHandler.postDelayed(mChangeViewRunnable, IntegerConstant.FRONT_BACK_DATA_CHANGE_FIRST);// 前后面交换数据
    }

    // 初始化二维码
    private void initQr() {
        Bitmap qrBitmap = QrUtil.generateBitmap(mContext, "hello world", 240, 240, true);
        if (qrBitmap != null) {
            if (mList != null && mList.size() > IntegerConstant.MAIN_QR_CODE_COUNT) {
                imageQr.setImageBitmap(qrBitmap);
                viewQr.setVisibility(View.VISIBLE);
                viewQrCode.setVisibility(View.GONE);

                IntegerConstant.MAIN_RIGHT_COUNT = 7;
            } else {
                imageQrCode.setImageBitmap(qrBitmap);
                viewQrCode.setAlpha(0.5f);
                viewQrCode.setVisibility(View.VISIBLE);

                viewQr.setVisibility(View.GONE);
                IntegerConstant.MAIN_RIGHT_COUNT = 5;
            }
        } else {
            viewQr.setVisibility(View.GONE);
            viewQrCode.setVisibility(View.GONE);
        }
    }

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
           if (action.equals(BROAD_END_COURSE)) {// 课程结束
                if (playType.equals(StringConstant.LIVE_PLAY_TYPE)) {
                    courseEnd();
                }
            } else if (action.equals(BROAD_UPDATE_DATA)) {// 请更新数据
                mList = (List<FitUser>) intent.getSerializableExtra(IntentConstant.USER_DATA);
               updateDataToView();
            } else if (action.equals(BROAD_BINDING_UPDATE)) {// 有新的用户关系
               String uuid = intent.getStringExtra(BUILD_UPDATE_KEY);
               Intent intentUpdate = new Intent();
               intentUpdate.setAction(BROAD_TO_SERVICE_BINDING_UPDATE);
               intentUpdate.putExtra(BUILD_UPDATE_KEY, uuid);
               sendBroadcast(intentUpdate);
           }
        }
    };

    // 根据最新数据更新界面
    private void updateDataToView() {
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
        initQr();// 初始化二维码

        for (int i = 0; i < mList.size(); i++) {
            childView = new MemberView(mContext);
            dataBean = mList.get(i);
            childView.setData(dataBean);

            // 目前只能排版 28 个
            if (i < IntegerConstant.MAIN_LEFT_COUNT) {// 左排前面
                viewLeftFront.addView(childView);
            } else if (i < IntegerConstant.MAIN_LEFT_COUNT + IntegerConstant.MAIN_RIGHT_COUNT) {// 右排前面
                viewRightFront.addView(childView);
            } else if (i < IntegerConstant.MAIN_LEFT_COUNT * 2 + IntegerConstant.MAIN_RIGHT_COUNT) {// 左排后面
                viewLeftBack.addView(childView);
            } else if (i < IntegerConstant.MAIN_LEFT_COUNT * 2 + IntegerConstant.MAIN_RIGHT_COUNT * 2) {// 右排后面
                viewRightBack.addView(childView);
            }
            vList.add(childView);// 保存
        }
        updateView();// 加载

        for (int i = 0; i < mList.size(); i++) {
            FitUser bean = mList.get(i);
            int level = bean.getHearRateLevel();
            if (level == 5) {// 心率范围在 5 档的位置需要提示用户
                String name = bean.getNick(true);
                if (name.equals(StringConstant.NO_LONGER_PROMPTED)) continue;// 跳过

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

                String name = mList.get(i).getNick(false);
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(1);
                sortBean.setName(name);
                sList.add(sortBean);
            } else if (mList.get(i).getKey().equals(resultList.get(1))) {
                mList.get(i).setSort(2);

                String name = mList.get(i).getNick(false);
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(2);
                sortBean.setName(name);
                sList.add(sortBean);
            } else if (mList.get(i).getKey().equals(resultList.get(2))) {
                mList.get(i).setSort(3);

                String name = mList.get(i).getNick(false);
                if (TextUtils.isEmpty(name)) name = "用户_" + key.split("_")[1];
                sortBean = new InformationBean.SortBean();
                sortBean.setSort(3);
                sortBean.setName(name);
                sList.add(sortBean);
            } else {
                mList.get(i).setSort(0);
            }
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
