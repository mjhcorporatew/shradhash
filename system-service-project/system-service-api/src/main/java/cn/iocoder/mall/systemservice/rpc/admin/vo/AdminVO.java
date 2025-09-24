package cn.iocoder.mall.systemservice.rpc.admin.vo;

import cn.iocoder.mall.systemservice.enums.admin.AdminStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 管理员 DO
 */
@Data
@Accessors(chain = true)
public class AdminVO {

    /**
     * 管理员编号
     */
    private Integer id;
    /**
     * 真实名字
     */
    private String name;
    /**
     * 部门编号
     */
    private Integer departmentId;
    /**
     * 在职状态
     *
     * 枚举 {@link AdminStatusEnum}
     */
    private Integer status;

    /**
     * 登陆账号
     */
    private String username;

}
