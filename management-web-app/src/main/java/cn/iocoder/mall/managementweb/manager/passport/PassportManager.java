package cn.iocoder.mall.managementweb.manager.passport;

import cn.iocoder.common.framework.enums.UserTypeEnum;
import cn.iocoder.common.framework.util.CollectionUtils;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.managementweb.controller.passport.dto.PassportLoginDTO;
import cn.iocoder.mall.managementweb.controller.passport.vo.PassportAccessTokenVO;
import cn.iocoder.mall.managementweb.controller.passport.vo.PassportAdminMenuTreeNodeVO;
import cn.iocoder.mall.managementweb.controller.passport.vo.PassportAdminVO;
import cn.iocoder.mall.managementweb.controller.permission.vo.ResourceTreeNodeVO;
import cn.iocoder.mall.managementweb.convert.passport.AdminPassportConvert;
import cn.iocoder.mall.managementweb.convert.permission.ResourceConvert;
import cn.iocoder.mall.managementweb.manager.permission.ResourceManager;
import cn.iocoder.mall.systemservice.enums.permission.ResourceTypeEnum;
import cn.iocoder.mall.systemservice.rpc.admin.AdminRpc;
import cn.iocoder.mall.systemservice.rpc.admin.vo.AdminVO;
import cn.iocoder.mall.systemservice.rpc.oauth.OAuth2Rpc;
import cn.iocoder.mall.systemservice.rpc.oauth.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.mall.systemservice.rpc.oauth.dto.OAuth2CreateAccessTokenReqDTO;
import cn.iocoder.mall.systemservice.rpc.oauth.dto.OAuth2RefreshAccessTokenReqDTO;
import cn.iocoder.mall.systemservice.rpc.permission.ResourceRpc;
import cn.iocoder.mall.systemservice.rpc.permission.RoleRpc;
import cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class PassportManager {

    @DubboReference(version = "${dubbo.consumer.AdminRpc.version}")
    private AdminRpc adminRpc;
    @DubboReference(version = "${dubbo.consumer.OAuth2Rpc.version}")
    private OAuth2Rpc oauth2Rpc;
    @DubboReference(version = "${dubbo.consumer.RoleRpc.version}")
    private RoleRpc roleRpc;
    @DubboReference(version = "${dubbo.consumer.ResourceRpc.version}")
    private ResourceRpc resourceRpc;

    public PassportAccessTokenVO login(PassportLoginDTO loginDTO, String ip) {
        // ?????????????????????
//        CommonResult<AdminVO> verifyPasswordResult = adminRpc.verifyPassword(AdminPassportConvert.INSTANCE.convert(loginDTO).setIp(ip));
        CommonResult<AdminVO> verifyPasswordResult = adminRpc.verifyPassword(AdminPassportConvert.INSTANCE.convert(loginDTO).setIp(ip));
        verifyPasswordResult.checkError();
        // ??????????????????
        CommonResult<OAuth2AccessTokenRespDTO> createAccessTokenResult = oauth2Rpc.createAccessToken(
                new OAuth2CreateAccessTokenReqDTO().setUserId(verifyPasswordResult.getData().getId())
                        .setUserType(UserTypeEnum.ADMIN.getValue()).setCreateIp(ip));
        createAccessTokenResult.checkError();
        // ??????
        return AdminPassportConvert.INSTANCE.convert(createAccessTokenResult.getData());
    }

    public PassportAdminVO getAdmin(Integer adminId) {
        CommonResult<AdminVO> getAdminResult = adminRpc.getAdmin(adminId);
        getAdminResult.checkError();
        return AdminPassportConvert.INSTANCE.convert(getAdminResult.getData());
    }

    public PassportAccessTokenVO refreshToken(String refreshToken, String ip) {
        CommonResult<OAuth2AccessTokenRespDTO> refreshAccessTokenResult = oauth2Rpc.refreshAccessToken(
                new OAuth2RefreshAccessTokenReqDTO().setRefreshToken(refreshToken).setCreateIp(ip));
        refreshAccessTokenResult.checkError();
        return AdminPassportConvert.INSTANCE.convert(refreshAccessTokenResult.getData());
    }

    /**
     * ????????????????????????????????????
     *
     * @param adminId ???????????????
     * @return ????????????
     */
    public Set<String> listAdminPermission(Integer adminId) {
        // ??????????????????????????????????????????
        CommonResult<Set<Integer>> listAdminRoleIdsResult = roleRpc.listAdminRoleIds(adminId);
        listAdminRoleIdsResult.checkError();
        if (CollectionUtils.isEmpty(listAdminRoleIdsResult.getData())) {
            return Collections.emptySet();
        }
        // ?????????????????????????????????
        CommonResult<List<ResourceVO>> resourceVOResult = resourceRpc.listRoleResource(
                listAdminRoleIdsResult.getData(), null);
        resourceVOResult.checkError();
        return CollectionUtils.convertSet(resourceVOResult.getData(), cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO::getPermission);
    }

    /**
     * ???????????????????????????
     *
     * @param adminId ???????????????
     * @return ?????????
     */
    public List<PassportAdminMenuTreeNodeVO> treeAdminMenu(Integer adminId) {
        // ??????????????????????????????????????????
        CommonResult<Set<Integer>> listAdminRoleIdsResult = roleRpc.listAdminRoleIds(adminId);
        listAdminRoleIdsResult.checkError();
        if (CollectionUtils.isEmpty(listAdminRoleIdsResult.getData())) {
            return Collections.emptyList();
        }
        // ?????????????????????????????????????????????
        CommonResult<List<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO>> resourceVOResult = resourceRpc.listRoleResource(
                listAdminRoleIdsResult.getData(), ResourceTypeEnum.MENU.getType());
        resourceVOResult.checkError();
        // ???????????????
        return this.buildAdminMenuTree(resourceVOResult.getData());
    }

    /**
     * ???????????????
     *
     * @param resourceVOs ??????????????????????????????
     * @return ?????????
     */
    private List<PassportAdminMenuTreeNodeVO> buildAdminMenuTree(List<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO> resourceVOs) {
        List<ResourceTreeNodeVO> treeNodeVOS = ResourceManager.buildResourceTree(resourceVOs);
        // ??????????????????????????????????????????????????????
        return ResourceConvert.INSTANCE.convert(treeNodeVOS);
    }

}
