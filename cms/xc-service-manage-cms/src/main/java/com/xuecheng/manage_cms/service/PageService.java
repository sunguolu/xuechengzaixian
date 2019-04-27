package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.BaseResult;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;

public interface PageService {

    QueryResponseResult findList( int page, int size, QueryPageRequest queryPageRequest);

    BaseResult<CmsPage> save(CmsPage cmsPage);

    BaseResult<CmsPage> getById(String id);

    BaseResult<CmsPage> eidt(String id,CmsPage cmsPage);

    BaseResult<CmsConfig> getmodel(String id);



}