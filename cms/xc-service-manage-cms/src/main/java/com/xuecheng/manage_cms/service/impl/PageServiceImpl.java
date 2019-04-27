package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.bean.utils.BeanCopyUtils;
import com.xuecheng.framework.domain.BaseResult;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class PageServiceImpl extends BaseServicImpl implements PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        queryPageRequest.setPage(page - 1 < 0 ? 0 : page - 1);
        queryPageRequest.setSize(size == 0 ? queryPageRequest.getSize() : size
        );
        CmsPage cmsPage = new CmsPage();
        String[] strs = new String[]{"siteId","pageName","pageAliase"};
        BeanCopyUtils.copyProperties(queryPageRequest, cmsPage,strs);

        /**定义条件查询器*/
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());

        /**定义example*/
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Pageable pageable = PageRequest.of(queryPageRequest.getPage(),queryPageRequest.getSize());
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    @Override
    public BaseResult<CmsPage> save(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        CommonCode code = CommonCode.FAIL;
        if(cmsPage1 == null){
            cmsPage.setPageId(null);
            CmsPage save = cmsPageRepository.save(cmsPage);
            code = CommonCode.SUCCESS;
        }else {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        return new BaseResult<CmsPage>(code,cmsPage);
    }

    @Override
    public BaseResult<CmsPage> getById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        CommonCode code = CommonCode.FAIL;
        CmsPage cmsPage = null;
        if(optional.isPresent()){
            cmsPage = optional.get();
            code = CommonCode.SUCCESS;
        }
        return new BaseResult<CmsPage>(code,cmsPage);
    }

    @Override
    public BaseResult<CmsPage> eidt(String id, CmsPage cmsPage) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            CmsPage cmsPage1 = optional.get();


        }
        return null;
    }

    @Override
    public BaseResult<CmsConfig> getmodel(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        CmsConfig cmsConfig = null;
        CommonCode code = CommonCode.FAIL;
        if(optional.isPresent()){
            cmsConfig = optional.get();
            code = CommonCode.SUCCESS;
        }
        return new BaseResult<CmsConfig>(code,cmsConfig);
    }


}
