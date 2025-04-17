package cn.iocoder.mall.product.rest.controller.admins;

import cn.iocoder.common.framework.constant.MallConstants;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.product.biz.bo.category.ProductCategoryAddBO;
import cn.iocoder.mall.product.biz.bo.category.ProductCategoryAllListBO;
import cn.iocoder.mall.product.biz.dto.category.ProductCategoryAddDTO;
import cn.iocoder.mall.product.biz.enums.product.ProductCategoryConstants;
import cn.iocoder.mall.product.biz.service.product.ProductCategoryService;
import cn.iocoder.mall.product.rest.convert.category.ProductCategoryConvert;
import cn.iocoder.mall.product.rest.request.category.AdminsProductCategoryAddRequest;
import cn.iocoder.mall.product.rest.response.category.AdminsProductCategoryAddResponse;
import cn.iocoder.mall.product.rest.response.category.AdminsProductCategoryTreeNodeResponse;
import cn.iocoder.mall.security.core.context.AdminSecurityContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.common.framework.vo.CommonResult.success;

/**
 * @Author: jiangweifan
 * @Date: 2020/5/6
 * @Description: 商品分类 - API
 */
@RestController
@RequestMapping(MallConstants.ROOT_PATH_ADMIN + "/category")
@Api(tags = "商品分类 API")
public class AdminsProductCategoryController {

    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping("/tree")
    @ApiOperation("获取分类树结构")
    public CommonResult<List<AdminsProductCategoryTreeNodeResponse>> tree() {
        List<ProductCategoryAllListBO> productCategories = productCategoryService.getAllProductCategory();
        // 创建 ProductCategoryTreeNodeVO Map
        Map<Integer, AdminsProductCategoryTreeNodeResponse> treeNodeMap = productCategories.stream().collect(Collectors.toMap(ProductCategoryAllListBO::getId, ProductCategoryConvert.INSTANCE::convertToTreeNodeResponse));
        // 处理父子关系
        treeNodeMap.values().stream()
                .filter(node -> !node.getPid().equals(ProductCategoryConstants.PID_ROOT))
                .forEach((childNode) -> {
                    // 获得父节点
                    AdminsProductCategoryTreeNodeResponse parentNode = treeNodeMap.get(childNode.getPid());
                    if (parentNode.getChildren() == null) { // 初始化 children 数组
                        parentNode.setChildren(new ArrayList<>());
                    }
                    // 将自己添加到父节点中
                    parentNode.getChildren().add(childNode);
                });
        // 获得到所有的根节点
        List<AdminsProductCategoryTreeNodeResponse> rootNodes = treeNodeMap.values().stream()
                .filter(node -> node.getPid().equals(ProductCategoryConstants.PID_ROOT))
                .sorted(Comparator.comparing(AdminsProductCategoryTreeNodeResponse::getSort))
                .collect(Collectors.toList());
        return success(rootNodes);
    }

    @PostMapping("/add")
    @ApiOperation(value = "创建商品分类")
    public CommonResult<AdminsProductCategoryAddResponse> add(@RequestBody AdminsProductCategoryAddRequest adminsProductCategoryAddRequest) {
        // 转换 ProductCategoryAddDTO 对象
        ProductCategoryAddDTO productCategoryAddDTO = ProductCategoryConvert.INSTANCE.convertToAddDTO(AdminSecurityContextHolder.getContext().getAdminId(), adminsProductCategoryAddRequest);
        // 创建商品分类
        ProductCategoryAddBO addProductCategoryBO = productCategoryService.addProductCategory(productCategoryAddDTO);
        // 返回结果
        return success(ProductCategoryConvert.INSTANCE.convertToAddResponse(addProductCategoryBO));
    }

}
