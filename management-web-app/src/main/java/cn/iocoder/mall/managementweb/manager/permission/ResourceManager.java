package cn.iocoder.mall.managementweb.manager.permission;

import cn.iocoder.common.framework.util.CollectionUtils;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.managementweb.controller.permission.dto.ResourceCreateDTO;
import cn.iocoder.mall.managementweb.controller.permission.dto.ResourceUpdateDTO;
import cn.iocoder.mall.managementweb.controller.permission.vo.AdminMenuTreeNodeVO;
import cn.iocoder.mall.managementweb.controller.permission.vo.ResourceVO;
import cn.iocoder.mall.managementweb.convert.permission.ResourceConvert;
import cn.iocoder.mall.systemservice.enums.permission.ResourceIdEnum;
import cn.iocoder.mall.systemservice.enums.permission.ResourceTypeEnum;
import cn.iocoder.mall.systemservice.rpc.permission.ResourceRpc;
import cn.iocoder.mall.systemservice.rpc.permission.RoleRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* 资源 Manager
*/
@Service
@Slf4j
public class ResourceManager {

    @Reference(version = "${dubbo.consumer.ResourceRpc.version}", validation = "false")
    private ResourceRpc resourceRpc;
    @Reference(version = "${dubbo.consumer.RoleRpc.version}", validation = "false")
    private RoleRpc roleRpc;

    /**
    * 创建资源
    *
    * @param createDTO 创建资源 DTO
    * @return 资源
    */
    public Integer createResource(ResourceCreateDTO createDTO, Integer createAdminId) {
        CommonResult<Integer> createResourceResult = resourceRpc.createResource(ResourceConvert.INSTANCE.convert(createDTO)
            .setCreateAdminId(createAdminId));
        createResourceResult.checkError();
        return createResourceResult.getData();
    }

    /**
    * 更新资源
    *
    * @param updateDTO 更新资源 DTO
    */
    public void updateResource(ResourceUpdateDTO updateDTO) {
        CommonResult<Boolean> updateResourceResult = resourceRpc.updateResource(ResourceConvert.INSTANCE.convert(updateDTO));
        updateResourceResult.checkError();
    }

    /**
    * 删除资源
    *
    * @param resourceId 资源编号
    */
    public void deleteResource(Integer resourceId) {
        CommonResult<Boolean> deleteResourceResult = resourceRpc.deleteResource(resourceId);
        deleteResourceResult.checkError();
    }

    /**
    * 获得资源
    *
    * @param resourceId 资源编号
    * @return 资源
    */
    public ResourceVO getResource(Integer resourceId) {
        CommonResult<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO> getResourceResult = resourceRpc.getResource(resourceId);
        getResourceResult.checkError();
        return ResourceConvert.INSTANCE.convert(getResourceResult.getData());
    }

    /**
    * 获得资源列表
    *
    * @param resourceIds 资源编号列表
    * @return 资源列表
    */
    public List<ResourceVO> listResource(List<Integer> resourceIds) {
        CommonResult<List<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO>> listResourceResult = resourceRpc.listResource(resourceIds);
        return ResourceConvert.INSTANCE.convertList(listResourceResult.getData());
    }

    /**
     * 获得管理员的菜单树
     *
     * @param adminId 管理员编号
     * @return 菜单树
     */
    public List<AdminMenuTreeNodeVO> treeAdminMenu(Integer adminId) {
        // 获得管理员拥有的角色编号列表
        CommonResult<Set<Integer>> listAdminRoleIdsResult = roleRpc.listAdminRoleIds(adminId);
        listAdminRoleIdsResult.checkError();
        if (CollectionUtils.isEmpty(listAdminRoleIdsResult.getData())) {
            return null;
        }
        // 获得角色拥有的资源列表
        CommonResult<List<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO>> resourceVOResult = resourceRpc.listRoleResource(
                listAdminRoleIdsResult.getData(), ResourceTypeEnum.MENU.getType());
        resourceVOResult.checkError();
        // 构建菜单树
        return this.buildAdminMenuTree(resourceVOResult.getData());
    }

    private List<AdminMenuTreeNodeVO> buildAdminMenuTree(List<cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO> resourceVOs) {
        // 排序，保证菜单的有序性
        resourceVOs.sort(Comparator.comparing(cn.iocoder.mall.systemservice.rpc.permission.vo.ResourceVO::getSort));
        // 构建菜单树
        // 使用 LinkedHashMap 的原因，是为了排序 。实际也可以用 Stream API ，就是太丑了。
        Map<Integer, AdminMenuTreeNodeVO> treeNodeMap = new LinkedHashMap<>();
        resourceVOs.forEach(resourceVO -> treeNodeMap.put(resourceVO.getId(), ResourceConvert.INSTANCE.convertTreeNode(resourceVO)));
        // 处理父子关系
        treeNodeMap.values().stream()
                .filter(node -> !node.getPid().equals(ResourceIdEnum.ROOT.getId()))
                .forEach((childNode) -> {
                    // 获得父节点
                    AdminMenuTreeNodeVO parentNode = treeNodeMap.get(childNode.getPid());
                    if (parentNode == null) {
                        log.error("[getResourceTree][resource({}) 找不到父资源({})]", childNode.getId(), childNode.getPid());
                        return;
                    }
                    if (parentNode.getChildren() == null) { // 初始化 children 数组
                        parentNode.setChildren(new ArrayList<>());
                    }
                    // 将自己添加到父节点中
                    parentNode.getChildren().add(childNode);
                });
        // 获得到所有的根节点
        return treeNodeMap.values().stream()
                .filter(node -> node.getPid().equals(ResourceIdEnum.ROOT.getId()))
                .collect(Collectors.toList());
    }

}
