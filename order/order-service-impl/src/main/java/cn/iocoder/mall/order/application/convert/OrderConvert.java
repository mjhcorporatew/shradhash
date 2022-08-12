package cn.iocoder.mall.order.application.convert;

import cn.iocoder.mall.order.api.dto.OrderBO;
import cn.iocoder.mall.order.dataobject.OrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 订单 convert
 *
 * @author Sin
 * @time 2019-03-17 10:14
 */
@Mapper
public interface OrderConvert {

    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    @Mappings({})
    List<OrderBO> convertPageBO(List<OrderDO> orderDOList);
}
