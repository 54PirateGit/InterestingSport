package com.tianbao.mi.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * UploadData
 * Created by edianzu on 2017/11/14.
 */
@Data
public class UploadData implements Serializable {

    private List<MotionData> motionDataList;

    private List<UserHeart> userHeartList;
}
