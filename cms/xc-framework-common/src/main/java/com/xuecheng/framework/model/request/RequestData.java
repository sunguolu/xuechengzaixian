package com.xuecheng.framework.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Created by mrt on 2018/3/5.
 */
@Data
@ToString
public class RequestData {


    @ApiModelProperty("页码")
    private  Integer page = 1;
    @ApiModelProperty("每页记录数")
    private  Integer size = 10;

}
