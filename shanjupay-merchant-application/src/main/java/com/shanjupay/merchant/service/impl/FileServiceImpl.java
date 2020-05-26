package com.shanjupay.merchant.service.impl;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {

    @Value("${oss.qiniu.url}")
    private String qiniuUrl;
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;

    @Override
    public String upload(byte[] bytes, String fileName) throws BusinessException {
        try{
            //上传成功返回文件的访问地址（绝对路径）
           QiniuUtils.upload2qiniu(accessKey,secretKey,bucket,bytes,fileName);
        }catch (RuntimeException e){
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        return qiniuUrl+fileName;
    }
}
