package cn.iocoder.mall.admin.api.bo.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@ApiModel("管理员授权 BO")
@Data
@Accessors(chain = true)
public class AdminAuthorizationBO {

    @ApiModelProperty(value = "管理员编号", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "角色编号数组", required = true, example = "1")
    private Set<Integer> roleIds;

}
