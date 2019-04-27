package com.xuecheng.manage_cms.controller;


import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.BaseResult;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cmspage")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    private PageService pageService;

    @GetMapping("/list/{page}/{size}")
    @Override
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        return pageService.findList(page,size,queryPageRequest);
    }

    @PostMapping("/save")
    @Override
    public BaseResult<CmsPage> save(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }
}
