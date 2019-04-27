package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {


    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);

    List<CmsPage> findByPageNameOrSiteIdOrPageWebPath(String pageName,String siteId,String pageWebPath);
}
