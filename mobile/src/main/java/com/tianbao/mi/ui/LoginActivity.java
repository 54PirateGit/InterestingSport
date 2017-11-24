package com.tianbao.mi.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.tianbao.mi.R;
import com.tianbao.mi.app.MyApp;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.DialogUtils;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SPUtils;
import com.tianbao.mi.utils.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.tianbao.mi.constant.ConfigConstant.DEVICE_ID;
import static com.tianbao.mi.constant.ConfigConstant.REG_ID;

/**
 * 登录  只在安装之后第一次打开时会跳转到此界面  登录之后就不会再到此界面
 * 11/01
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.admin)
    EditText adminText;
    @BindView(R.id.password)
    EditText passwordText;

    private Context mContext;
    private Dialog dialogLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mContext = this;
    }

    // 发送请求 验证用户是否存在
    private void request() {
        String[] user = checkInputContent();
        if (user == null || user.length < 3) return ;

        dialogLoading = DialogUtils.dialogLoading(mContext);
        dialogLoading.show();

        Map<String, String> param = new HashMap<>();
        param.put("account", user[0]);
        param.put("password", user[1]);
        param.put("deviceId", user[2]);
        param.put("mipushId", user[3]);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service = retrofit.create(ApiService.class);
        Call<LoginBean> model = service.login(param);
        model.enqueue(new Callback<LoginBean>() {
            @Override
            public void onResponse(Response<LoginBean> response, Retrofit retrofit) {
                if (dialogLoading != null) dialogLoading.dismiss();
                L.v("response", "response == " + response.body().toString());

                List<String> bannerList = null;
                List<String> downList = null;

                LoginBean loginBean = response.body();
                int code = loginBean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    LoginBean.DataBean data = loginBean.getData();
                    if (data != null) {
                        int type = data.getType();
                        if (type != 0) SPUtils.put(mContext, StringConstant.DATA_TYPE_SP_KEY, type);// 账号类型

                        int storeId = data.getStoreId();
                        if (storeId != 0) SPUtils.put(mContext, StringConstant.STORE_ID_SP_KEY, storeId);// 店 ID

                        long refreshData = data.getRefreshDataFrequency();
                        if (refreshData > 0) IntegerConstant.REFRESH_DATA_FREQUENCY = refreshData;// 用户数据刷新时间;// 数据刷新时间

                        long refreshRela = data.getRefreshRelationFrequency();
                        if (refreshRela > 0) IntegerConstant.REFRESH_RELATION__FREQUENCY = refreshRela;// 用户关系刷新时间

                        long refreshSort = data.getSortFrequency();
                        if (refreshSort > 0) IntegerConstant.SORT_FREQUENCY = refreshSort;// 用户数据排序时间

                        int girth = data.getGirth();
                        if (girth > 0) IntegerConstant.GIRTH = (float) girth  / 100;// 动感单车周长

                        int ratio = data.getRatio();
                        if (ratio > 0) IntegerConstant.RATIO = (float)ratio / 100;// 踏频比例

                        // 将轮播图地址传递到待机页  如果有数据待机页就直接展示不需要重复获取
                        String adUrls = data.getStandbyUpAdUrl();
                        if (!TextUtils.isEmpty(adUrls)) {
                            if (adUrls.contains("，")) {
                                adUrls = adUrls.replace("，", ",");
                            }
                            String[] adUrlArr = adUrls.split(",");
                            for (String url : adUrlArr) {
                                if (bannerList == null) bannerList = new ArrayList<>();
                                bannerList.add(url);
                            }
                        }

                        // 待机页下面轮播图地址
                        String downUrl = data.getStandbyDownAdUrl();
                        if (!TextUtils.isEmpty(downUrl)) {
                            if (downUrl.contains("，")) {
                                downUrl = downUrl.replace("，", ",");
                            }
                            String[] downUrlArr = downUrl.split(",");
                            for (String url : downUrlArr) {
                                if (downList == null) downList = new ArrayList<>();
                                downList.add(url);
                            }
                        }
                        if (downList != null && downList.size() > 0) {
                            L.d("downList", "downList size - > " + downList.size());
                            MyApp.setDownUrl(downList);
                        }
                    } else {
                        T.alwaysShort(mContext, "data is null");
                    }

                    if (bannerList != null && bannerList.size() > 0) {
                        MyApp.setUpUrl(bannerList);
                    }
                    Intent intent = new Intent(mContext, StandbyActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String message = loginBean.getMessage();
                    if (TextUtils.isEmpty(message) || message.equals("null") || message.equals("NULL")) {
                        message = "账户或密码错误，请重新输入！";
                    }
                    T.alwaysShort(mContext, message);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (dialogLoading != null) dialogLoading.dismiss();
                T.connectFailTip(mContext);
            }
        });
    }

    // 检查输入内容  主要检查是否为空
    private String[] checkInputContent() {
        String[] user = new String[4];
        String admin = adminText.getText().toString();
        String password = passwordText.getText().toString();

        if (TextUtils.isEmpty(admin) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(admin)) {
                adminText.setError("请输入用户名");
            }
            if (TextUtils.isEmpty(password)) {
                passwordText.setError("请输入密码");
            }
            return null;
        }

        user[0] = admin;
        user[1] = password;
        user[2] = DEVICE_ID;
        user[3] = REG_ID;
        return user;
    }

    @OnClick(R.id.sign_in_button)
    public void onViewClick() {
        request();
    }

    private long time;

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - time >= IntegerConstant.APP_EXIT_TIME) {
            T.alwaysLong(mContext, "再按一次返回键退出程序！");
            time = curTime;
        } else {
            finish();
            MyApp.appExit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adminText = null;
        passwordText = null;
        if (dialogLoading != null) {
            dialogLoading.dismiss();
            dialogLoading = null;
        }
    }
}

