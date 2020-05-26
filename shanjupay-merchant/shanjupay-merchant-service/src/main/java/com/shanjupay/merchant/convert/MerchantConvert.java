package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * VO:用于应用层接收前端请求及响应前端数据
 * DTO:用于服务层传入及响应数据
 * entity:用于持久层传入及响应数据
 * VO/DTO->DTO->ENTITY
 * 定义dto和entity之间的转换规则
 * Created by Administrator.
 */
@Mapper //对象属性的映射
public interface MerchantConvert {

    //转换类实例
    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    //把dto转换成entity
    Merchant dto2entity(MerchantDTO merchantDTO);

    //把entity转换成dto
    MerchantDTO entity2dto(Merchant merchant);

    //list之间也可以转换，很entity的List转成MerchantDTO list
    List<MerchantDTO> entityList2dtoList(List<Merchant> merchants);

}
