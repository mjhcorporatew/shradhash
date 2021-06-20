package cn.iocoder.common.framework.config;

import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.common.framework.vo.RestResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true; // TODO 芋艿，未来，这里可以剔除掉一些，需要特殊返回的接口
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof RestResult) {
            return body;
        }
        if (body instanceof CommonResult) { // TODO 芋艿，后续要改下
            return body;
        }
        return RestResult.ok(body);
    }

}