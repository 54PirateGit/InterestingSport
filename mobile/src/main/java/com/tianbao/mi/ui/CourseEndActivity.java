package com.tianbao.mi.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.UploadData;
import com.tianbao.mi.bean.UploadDataBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.BitmapUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.QrUtil;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.SendBroadUtil;
import com.tianbao.mi.utils.T;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 课程结束跳转至此
 * 11/06
 */
public class CourseEndActivity extends Activity {

    @BindView(R.id.image_background)
    ImageView imageBackground;
    @BindView(R.id.text_title)
    TextView textTitle;
    @BindView(R.id.text_year)
    TextView textYear;
    @BindView(R.id.text_time)
    TextView textTime;
    @BindView(R.id.view_save)
    View viewSave;// 数据正在保存
    @BindView(R.id.view_qr)
    View viewQr;// 数据保存成功展示数据分享二维码
    @BindView(R.id.image)
    ImageView imageQr;// 二维码

    private Context mContext;
    private Handler mHandler = new Handler();

    private MediaPlayer mp;

    private int timer = 180;// 此界面停留 3 分钟

    private void setFront() {
        Typeface tf = Typeface.createFromAsset(getAssets(), "font/FZKTJT.ttf");
        textTitle.setTypeface(tf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_end);
        mContext = this;
        ButterKnife.bind(this);
        setFront();

        initView();
        initData();
    }

    // 初始化视图
    private void initView() {
        Bitmap bitmapBackground = BitmapUtils.readBitMap(mContext, R.drawable.end_background);
        imageBackground.setImageBitmap(bitmapBackground);

        String string = (String) SPUtils.get(mContext, StringConstant.TIME_YEAR, "2017");
        textYear.setText(string);

        mHandler.postDelayed(mCountDownRunnable, 1000L);// 倒计时 三分钟后结束

        Intent intent = getIntent();
        if (intent == null) return ;
        UploadData uploadData = (UploadData) intent.getSerializableExtra(StringConstant.UPLOAD_DATA_KEY);
        if (uploadData == null) return ;

//        for (int i=0; i<uploadData.getGymDataList().size(); i++) {
//            L.i("GymData", "GymData -> " + uploadData.getGymDataList().get(i).toString());
//        }

        requestUploadData(uploadData);
    }

    // 初始化数据
    private void initData() {
        SendBroadUtil.sendPlayToService(mContext, IntegerConstant.COURSE_END_SOUND_ID);
    }

    // 课程完结上传数据
    private void requestUploadData(UploadData uploadData) {
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.2.58:8080")
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<UploadDataBean> model = service.saveGymData(uploadData);
        model.enqueue(new Callback<UploadDataBean>() {
            @Override
            public void onResponse(Response<UploadDataBean> response, Retrofit retrofit) {
                UploadDataBean bean = response.body();
                int code = bean.getCode();
                L.i("GymData", "message == " + bean.getMessage());

                if (code == IntegerConstant.RESULT_OK) {// 数据上传成功
                    try {
                        Bitmap qrBitmap = QrUtil.makeQRImage(mContext, "hello world", 270, 270);
                        imageQr.setImageBitmap(qrBitmap);
                        viewQr.setVisibility(View.VISIBLE);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    viewSave.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // 连接服务器失败 检查网络设置或服务器问题 如果是网络问题 及时连接上网络之后数据重新上传 断网时间较长则数据可能会丢失
                L.i("GymData", "onFailure");
            }
        });
    }

    // 倒计时
    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            timer--;
            textTime.setText(String.valueOf(timer));
            if (timer > 0) {
                mHandler.postDelayed(this, 1000L);
            } else {
                startActivity(new Intent(mContext, StandbyActivity.class));
                finish();
            }
        }
    };

    private long time;// 保存点击返回键的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            long currentTime = System.currentTimeMillis();
            if (currentTime - time > IntegerConstant.APP_EXIT_TIME) {
                T.alwaysLong(mContext, "再按一次返回键退出程序");
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
    protected void onStop() {
        super.onStop();
        SendBroadUtil.sendStopToService(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownRunnable != null) {
            mHandler.removeCallbacks(mCountDownRunnable);
            mCountDownRunnable = null;
        }
        imageBackground = null;
        textTitle = null;
        textYear = null;
        textTime = null;
        viewSave = null;
        viewQr = null;

        mHandler = null;
        mContext = null;
    }
}
