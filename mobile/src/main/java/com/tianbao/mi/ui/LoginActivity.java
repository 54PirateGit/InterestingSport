package com.tianbao.mi.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.tianbao.mi.R;
import com.tianbao.mi.bean.LoginBean;
import com.tianbao.mi.constant.IntegerConstant;
import com.tianbao.mi.constant.StringConstant;
import com.tianbao.mi.net.Api;
import com.tianbao.mi.net.ApiService;
import com.tianbao.mi.utils.L;
import com.tianbao.mi.utils.SPUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.admin)
    EditText adminText;
    @BindView(R.id.password)
    EditText passwordText;

    private Context mContext;

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
                L.v("response", "response == " + response.body().toString());

                Intent intent = new Intent(mContext, StandbyActivity.class);
                ArrayList<String> bannerList = null;

                LoginBean loginBean = response.body();
                int code = loginBean.getCode();
                if (code == IntegerConstant.RESULT_OK) {
                    LoginBean.DataBean data = loginBean.getData();
                    if (data != null) {
                        int type = data.getType();
                        if (type != 0) SPUtils.put(mContext, StringConstant.DATA_TYPE_SP_KEY, type);

                        int storeId = data.getStoreId();
                        if (storeId != 0) SPUtils.put(mContext, StringConstant.STORE_ID_SP_KEY, storeId);

                        long refreshData = data.getRefreshDataFrequency();
                        if (refreshData > 0) SPUtils.put(mContext, StringConstant.REFRESH_DATA_FREQUENCY, refreshData);

                        long refreshRela = data.getRefreshRelationFrequency();
                        if (refreshRela > 0) SPUtils.put(mContext, StringConstant.REFRESH_RELATION__FREQUENCY, refreshRela);

                        long refreshSort = data.getSortFrequency();
                        if (refreshSort > 0) SPUtils.put(mContext, StringConstant.SORT_FREQUENCY, refreshSort);

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
                    } else {
                        Toast.makeText(mContext, "data is null", Toast.LENGTH_SHORT).show();
                    }

                    if (bannerList != null && bannerList.size() > 0) {
                        intent.putStringArrayListExtra(StringConstant.BANNER_LIST_UP, bannerList);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, "code == " + code, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(mContext, "发生错误，请确认后重试", Toast.LENGTH_LONG).show();
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
        user[2] = StringConstant.DEVICE_ID;
        user[3] = StringConstant.REG_ID;
        return user;
    }

    @OnClick(R.id.sign_in_button)
    public void onViewClick() {
        request();
    }
}

