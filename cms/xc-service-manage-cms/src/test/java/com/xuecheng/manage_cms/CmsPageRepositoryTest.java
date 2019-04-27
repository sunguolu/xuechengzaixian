package com.xuecheng.manage_cms;


import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import sun.misc.IOUtils;

import java.io.*;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    @Test
    public void cmsPageMongoTest() {
        List<CmsPage> all = cmsPageRepository.findAll();
        String s = JSON.toJSONString(all);
        System.out.println(s);
    }


    @Test
    public void cmsPageMongoPageTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        String s = JSON.toJSONString(all);
        System.out.println(s);
    }

    @Test
    public void cmsPageFindAllbyTest() {
        List<CmsPage> all = cmsPageRepository.findByPageNameOrSiteIdOrPageWebPath("轮播图", "5a751fab6abb5044e0d19ea1", "/include/index_banner.html");
        String s = JSON.toJSONString(all);
        System.out.println(s);
    }


    @Test
    public void gridFsTest() throws FileNotFoundException {
        /**存储文件信息*/

        File file = new File("G:\\程序员的数学.pdf");
        FileInputStream inputStream = new FileInputStream(file);

        ObjectId objectId = gridFsTemplate.store(inputStream, "程序员数学");
        System.out.println(objectId.toString());

    }


    @Test
    public void queryFile() throws IOException {
        String fileId = "5cbec12f34ee3c0d74778b36";
        //根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        //获取流中的数据
        File file = new File("D:\\" + gridFsResource.getFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        InputStream inputStream = gridFsResource.getInputStream();
//        bufferedOutputStream.write(gridFsResource.getInputStream());

    }

}
