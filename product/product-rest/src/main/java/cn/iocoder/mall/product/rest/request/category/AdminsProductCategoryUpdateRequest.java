package cn.iocoder.mall.product.rest.request.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @Author: jiangweifan
 * @Date: 2020/5/6
 * @Description: 商品分类 - 更新商品分类Request
 */
@ApiModel("更新商品分类Request")
@Data
@Accessors(chain = true)
public class AdminsProductCategoryUpdateRequest {

    @ApiModelProperty(name = "id", value = "分类编号", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(name = "pid", value = "父级分类编号", required = true, example = "1")
    private Integer pid;

    @ApiModelProperty(name = "name", value = "分类名字（标识）", required = true, example = "admin/info")
    private String name;

    @ApiModelProperty(name = "description", value = "描述", required = true, example = "1")
    private String description;

    @ApiModelProperty(name = "picUrl", value = "分类图片", example = "http://www.iocoder.cn/images/common/wechat_mp_2017_07_31_bak.jpg/")
    private String picUrl;

    @ApiModelProperty(name = "sort", value = "排序", required = true, example = "1")
    private Integer sort;
}
