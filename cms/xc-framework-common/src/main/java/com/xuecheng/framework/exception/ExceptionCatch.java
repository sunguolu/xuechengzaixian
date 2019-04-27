package com.xuecheng.framework.exception;


import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 控制器增强
 * 可以将对于控制器的全局配置放在同一个位置
 * 并应用到所有@RequestMapping中
 * 可以使用@ExceptionHandler、@InitBinder、@ModelAttribute注解到方法上
 */
@RestControllerAdvice
public class ExceptionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    /**
     * 使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
     */
    private static  ImmutableMap<Class<? extends Throwable>, ResultCode> Exception;

    /**
     * 使用builder来构建一个异常类型和错误代码的异常
     */
    private static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();

    static {
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }


    @ExceptionHandler(CustomException.class)
    public ResponseResult catchCustomException(CustomException e){
        LOGGER.error("catch exception : {}\\r\\nexception:",e.getMessage(),e);
        return new ResponseResult(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult catchException(Exception e){
        if(Exception == null){
            Exception = builder.build();
        }
        ResultCode resultCode = Exception.get(e.getClass());

        return new ResponseResult(resultCode == null ? CommonCode.SERVER_ERROR : resultCode);
    }

}
