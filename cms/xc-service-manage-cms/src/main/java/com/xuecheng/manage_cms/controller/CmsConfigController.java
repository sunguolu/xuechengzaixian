package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsConfigControllerApi;
import com.xuecheng.framework.domain.BaseResult;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/CmsConfig")
public class CmsConfigController implements CmsConfigControllerApi {


    @Autowired
    private PageService pageService;

    @Override
    @RequestMapping("/getmodel/{id}")
    public BaseResult<CmsConfig> getmodel(@PathVariable("id") String id) {
        return pageService.getmodel(id);
    }
}
