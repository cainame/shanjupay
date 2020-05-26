package com.shanjupay.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.entity.PlatformChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author author
 * @since 2019-11-15
 */
@Repository
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {

    /**
     * 根据服务类型code查询对应的支付渠道
     * @param platformChannelCode
     * @return
     * 查询语句1   select * from pay_channel where CHANNEL_CODE in (select PAY_CHANNEL from platform_pay_channel where PLATFORM_CHANNEL ='shanju_b2c')
     */
//    @Select("SELECT " +
//            "  pc.* " +
//            "FROM" +
//            "  platform_pay_channel ppc," +
//            "  pay_channel pc," +
//            "  platform_channel pla " +
//            "WHERE ppc.PAY_CHANNEL = pc.CHANNEL_CODE " +
//            "  AND ppc.PLATFORM_CHANNEL = pla.CHANNEL_CODE " +
//            "  AND pla.CHANNEL_CODE = #{platformChannelCode}  ")
    @Select("select a.* from pay_channel a" +
            "    left join platform_pay_channel b on a.CHANNEL_CODE=b.PAY_CHANNEL" +
            "    left join  platform_channel c on b.PLATFORM_CHANNEL = c.CHANNEL_CODE where c.CHANNEL_CODE=#{platformChannelCode}")
    List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannelCode);
}
