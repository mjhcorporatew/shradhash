package cn.iocoder.mall.order.biz.dao;

import cn.iocoder.mall.order.biz.dataobject.CartItemDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CartMapper {

    CartItemDO selectById(@Param("id") Integer id);

    List<CartItemDO> selectByIds(@Param("ids") Collection<Integer> ids);

    CartItemDO selectByUserIdAndSkuIdAndStatus(@Param("userId") Integer userId,
                                               @Param("skuId") Integer skuId,
                                               @Param("status") Integer status);

    Integer selectQuantitySumByUserIdAndStatus(@Param("userId") Integer userId,
                                               @Param("status") Integer status);

//    List<CartItemDO> selectListByStatus(@Param("status") Integer status);
//
//    List<CartItemDO> selectListByTitleLike(@Param("title") String title,
//                                         @Param("offset") Integer offset,
//                                         @Param("limit") Integer limit);

//    Integer selectCountByTitleLike(@Param("title") String title);

    void insert(CartItemDO cartItemDO);

    int update(CartItemDO cartItemDO);

    int updateQuantity(@Param("id") Integer id,
                       @Param("quantityIncr") Integer quantityIncr);

}
