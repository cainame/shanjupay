package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;

import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
public class MerchantController {

    @Reference
    private MerchantService merchantService;//注入远程的接口

    @Autowired //注入本地的bean
    private SmsService smsService;

    @Autowired
    private FileService fileService;

    @ApiOperation(value="根据id查询商户信息")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取手机验证码")
    @GetMapping("/sms")
    @ApiImplicitParam(value = "手机号",name = "phone",required = true,dataType = "string",paramType = "query")
    public String getSMSCode(@RequestParam("phone") String phone){
        //向验证码服务请求发送验证码
        return smsService.sendMsg(phone);
    }

    @ApiOperation("商户注册")
    @ApiImplicitParam(value = "商户注册信息",name = "merchantRegisterVO",required = true,dataType = "MerchantRegisterVO",paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO){
        //1.校验参数的合法性
        if(merchantRegisterVO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }

        if(StringUtils.isBlank(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        //手机号格式校验
        if(!PhoneUtil.isMatches(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //2.校验验证码
        smsService.checkVerifiyCode(merchantRegisterVO.getVerifiykey(),merchantRegisterVO.getVerifiyCode());

        //3.调用dubbo服务接口,调用商户微服务
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @ApiOperation("证件上传")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file) throws IOException {
        //原始文件名称
        String originalFilename = file.getOriginalFilename();
        //文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")-1);
        //文件名称
        String fileName = UUID.randomUUID().toString()+suffix;
        String fileurl = fileService.upload(file.getBytes(), fileName);
        return fileurl ;
    }

    @ApiOperation("商户资质申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")})
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantInfo) {
        //解析token得到商户id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantInfo);
        //资质申请
        merchantService.applyMerchant(merchantId,merchantDTO);
    }
}
