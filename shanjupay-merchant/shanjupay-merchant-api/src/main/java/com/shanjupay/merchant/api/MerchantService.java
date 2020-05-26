package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;

/**
 * 商户接口,对外暴露dubbo服务，供其它微服务调用
 */
public interface MerchantService {
    //根据商户id查询商户
    MerchantDTO queryMerchantById(Long merchantId);
    //商户注册
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;
    //资质申请
    void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;

}
