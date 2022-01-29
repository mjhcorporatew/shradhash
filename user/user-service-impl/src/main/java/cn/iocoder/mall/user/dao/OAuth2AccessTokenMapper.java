package cn.iocoder.mall.user.dao;

import cn.iocoder.mall.user.dataobject.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2AccessTokenMapper {

    void insert(OAuth2AccessTokenDO entity);

    OAuth2AccessTokenDO selectByTokenId(String tokenId);

    void updateToInvalidByUserId(@Param("userId") Integer userId);

}