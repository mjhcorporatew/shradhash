package cn.iocoder.mall.system.rest.controller.users;

import cn.iocoder.common.framework.constant.MallConstants;
import cn.iocoder.common.framework.util.HttpUtil;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.system.biz.dto.oatuh2.OAuth2MobileCodeSendDTO;
import cn.iocoder.mall.system.biz.service.oauth2.OAuth2MobileCodeService;
import cn.iocoder.mall.system.biz.service.oauth2.OAuth2Service;
import cn.iocoder.mall.system.biz.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(MallConstants.ROOT_PATH_USER + "/oauth2")
@Api(tags = "用户 - OAuth2 API")
public class UsersOAuth2Controller {

    @Autowired
    private OAuth2Service oauth2Service;
    @Autowired
    private UserService userService;
    @Autowired
    private OAuth2MobileCodeService oauth2MobileCodeService;

    @PostMapping("/send_mobile_code")
    @ApiOperation("发送手机验证码")
    @ApiImplicitParam(name = "mobile", value = "手机号", required = true, example = "15601691234")
    public CommonResult<Boolean> sendMobileCode(@RequestParam("mobile") String mobile,
                                                HttpServletRequest request) {
        // 执行发送验证码
        OAuth2MobileCodeSendDTO sendDTO = new OAuth2MobileCodeSendDTO()
                .setMobile(mobile).setIp(HttpUtil.getIp(request));
        oauth2MobileCodeService.sendMobileCode(sendDTO);
        // 返回成功
        return CommonResult.success(true);
    }

}
