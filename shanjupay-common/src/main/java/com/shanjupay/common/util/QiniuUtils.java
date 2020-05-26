package com.shanjupay.common.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;




/**
 * 七牛云上传工具
 */
public class QiniuUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);
    public static void   upload2qiniu(String accessKey,String secretKey,String bucket, byte[] bytes,String fileName) throws RuntimeException{
        //1.构造配置类
        Configuration cfg = new Configuration(Region.huadong());
        //2.获取上传manager
        UploadManager uploadManager = new UploadManager(cfg);
        //3.文件名key
        String key = fileName;
        try{
            //4.认证
            Auth auth = Auth.create(accessKey, secretKey);
            //5.认证通过后得到token（令牌）
            String upToken = auth.uploadToken(bucket);
            try{
                //6.上传文件,文件字节数组，key.token
                Response response = uploadManager.put(bytes, key, upToken);
                //7.解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            }catch (QiniuException ex){
                Response r = ex.response;
                System.err.println(r.toString());
                LOGGER.error("上传文件到七牛：{}",ex.getMessage());
                try {
                    LOGGER.error(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                throw new RuntimeException(r.bodyString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
