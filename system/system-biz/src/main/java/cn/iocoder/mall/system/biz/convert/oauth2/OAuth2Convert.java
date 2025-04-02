package cn.iocoder.mall.system.biz.convert.oauth2;

import cn.iocoder.mall.system.biz.bo.ouath2.OAuth2AuthenticateBO;
import cn.iocoder.mall.system.biz.dataobject.oauth2.OAuth2AccessTokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OAuth2Convert {

    OAuth2Convert INSTANCE = Mappers.getMapper(OAuth2Convert.class);

    @Mapping(source = "id", target = "accessToken")
    OAuth2AuthenticateBO convert(OAuth2AccessTokenDO bean);

}
