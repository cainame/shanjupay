package com.shanjupay.transaction.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class PayChannelServiceImpl implements PayChannelService {
    @Autowired
    private PlatformChannelMapper platformChannelMapper;
    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;
    @Autowired
    PayChannelParamMapper payChannelParamMapper;

    /**
     * 获取平台服务类型
     *
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        //Entity->Dto
        List<PlatformChannelDTO> platformChannelDTOS = PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
        return platformChannelDTOS;
    }

    /**
     * 为某个应用绑定一个服务类型
     *
     * @param appId                应用id
     * @param platformChannelCodes 服务类型的code
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        //根据appId和平台服务类型code查询app_platform_channel
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new
                LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        //如果没有绑定则绑定
        if (appPlatformChannel == null) {
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    /**
     * 查询应用是否绑定了某个服务类型
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        int count = appPlatformChannelMapper.selectCount(
                new QueryWrapper<AppPlatformChannel>().lambda().eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        //已存在绑定关系返回1
        if (count > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 根据服务类型查询支付渠道
     * @param platformChannelCode 服务类型编码
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException {
        /**
         * 1.判单支付参数Dto是否为空
         * 2.判断appid是否为空
         * 3.判断服务类型code
         */
        if(payChannelParam == null || payChannelParam.getChannelName() == null || payChannelParam.getParam()== null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据应用、服务类型、支付渠道查询一条记录
        //根据应用、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParam.getAppId(), payChannelParam.getPlatformChannelCode());
        if(appPlatformChannelId == null){
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用与服务类型的绑定id和支付渠道查询PayChannelParam的一条记录
        PayChannelParam entity = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                .eq(PayChannelParam::getPayChannel, payChannelParam.getPayChannel()));
        //如果存在配置则更新
        if(entity != null){
            entity.setChannelName(payChannelParam.getChannelName());//配置名称
            entity.setParam(payChannelParam.getParam());//json格式的参数
            payChannelParamMapper.updateById(entity);
        }else{
            //否则添加配置
            PayChannelParam entityNew = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParam);
            entityNew.setId(null);
            entityNew.setAppPlatformChannelId(appPlatformChannelId);//应用与服务类型绑定关系id
            payChannelParamMapper.insert(entityNew);
        }
////        //保存到redis
//         updateCache(payChannelParam.getAppId(),payChannelParam.getPlatformChannelCode());

    }

    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) {
        return null;
    }

    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) {
        return null;
    }
    /**
     * 根据应用、服务类型查询应用与服务类型的绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if(appPlatformChannel!=null){
            return appPlatformChannel.getId();//应用与服务类型的绑定id
        }
        return null;
    }
}
