package com.xuecheng.framework.domain;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;


@Data
public class BaseResult<T> extends ResponseResult {

    private T t;

    public BaseResult(ResultCode resultCode, T t) {
        super(resultCode);
        this.t = t;
    }
}
