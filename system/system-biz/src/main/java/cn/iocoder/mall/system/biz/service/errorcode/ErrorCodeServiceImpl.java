package cn.iocoder.mall.system.biz.service.errorcode;

import cn.iocoder.common.framework.util.ServiceExceptionUtil;
import cn.iocoder.common.framework.vo.PageResult;
import cn.iocoder.mall.mybatis.enums.DeletedStatusEnum;
import cn.iocoder.mall.mybatis.query.QueryWrapperX;
import cn.iocoder.mall.system.biz.bo.errorcode.ErrorCodeBO;
import cn.iocoder.mall.system.biz.convert.errorcode.ErrorCodeConvert;
import cn.iocoder.mall.system.biz.dao.errorcode.ErrorCodeMapper;
import cn.iocoder.mall.system.biz.dataobject.errorcode.ErrorCodeDO;
import cn.iocoder.mall.system.biz.dto.errorcode.*;
import cn.iocoder.mall.system.biz.enums.SystemErrorCodeEnum;
import cn.iocoder.mall.system.biz.enums.errorcode.ErrorCodeTypeEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ding
 */
@Service
public class ErrorCodeServiceImpl extends ServiceImpl<ErrorCodeMapper, ErrorCodeDO> implements ErrorCodeService {

    @Autowired
    private ErrorCodeMapper errorCodeMapper;


    @Override
    public ErrorCodeBO getErrorCode(Integer code) {
        return ErrorCodeConvert.INSTANCE.convert(errorCodeMapper.selectByCode(code));
    }

    @Override
    public List<ErrorCodeBO> getErrorCodeList(ErrorCodeGetListDTO errorCodeGetListDTO) {
        // TODO FROM 芋艿 to 鱿鱼丝：QueryWrapperX 只存在 mapper 里，不直接体现在 Service
        return ErrorCodeConvert.INSTANCE.convertList(errorCodeMapper.selectListByIds(errorCodeGetListDTO.getIds()));
    }

    @Override
    public List<ErrorCodeBO> getErrorCodeByGroup(Integer group) {
        List<ErrorCodeDO> list = errorCodeMapper.selectByGroup(group);
        // TODO FROM 芋艿 to 鱿鱼丝：这块微信交流一波哈。
        return ErrorCodeConvert.INSTANCE.convertList(list);
    }


    @Override
    public PageResult<ErrorCodeBO> getErrorCodePage(ErrorCodePageDTO pageDTO) {
        IPage<ErrorCodeDO> list = errorCodeMapper.selectPage(pageDTO);
        return ErrorCodeConvert.INSTANCE.convertPage(list);
    }

    @Override
    public Integer addErrorCode(ErrorCodeAddDTO errorCodeAddDTO) {
        // 校验错误码
        checkDuplicateErrorCode(errorCodeAddDTO.getCode(), null);
        // 保存到数据库
        ErrorCodeDO errorCode = ErrorCodeConvert.INSTANCE.convert(errorCodeAddDTO);
        errorCode.setCreateTime(new Date());
        errorCode.setDeleted(DeletedStatusEnum.DELETED_NO.getValue());
        errorCodeMapper.insert(errorCode);
        // TODO 插入操作日志
        // 返回成功
        return errorCode.getId();
    }

    @Override
    public Boolean addErrorCodeList(List<ErrorCodeAddDTO> list) {
        List<ErrorCodeDO> doList = new ArrayList<>();
        for (ErrorCodeAddDTO errorCodeAddDTO:list
             ) {
            // 校验错误码
            checkDuplicateErrorCode(errorCodeAddDTO.getCode(), null);
            ErrorCodeDO errorCode = ErrorCodeConvert.INSTANCE.convert(errorCodeAddDTO);
            errorCode.setCreateTime(new Date());
            errorCode.setDeleted(DeletedStatusEnum.DELETED_NO.getValue());
            doList.add(errorCode);
        }
        // TODO 插入操作日志
        return this.saveBatch(doList);
    }

    @Override
    public void updateErrorCode(ErrorCodeUpdateDTO errorCodeUpdateDTO) {
        // 校验错误码是否存在
        ErrorCodeDO errorCodeDO = errorCodeMapper.selectByCode(errorCodeUpdateDTO.getCode());
        if (errorCodeDO == null) {
            throw ServiceExceptionUtil.exception(SystemErrorCodeEnum.ERROR_CODE_NOT_EXISTS);
        }
        // 内置错误码，写死在枚举类中，不允许修改
        if (ErrorCodeTypeEnum.SYSTEM.getType().equals(errorCodeDO.getType())) {
            throw ServiceExceptionUtil.exception(SystemErrorCodeEnum.ERROR_CAN_NOT_UPDATE_SYSTEM_TYPE_ERROR);
        }
        // 校验角色的唯一字段是否重复
        checkDuplicateErrorCode(errorCodeDO.getCode(), errorCodeDO.getId());
        // 更新到数据库
        ErrorCodeDO updateRole = ErrorCodeConvert.INSTANCE.convert(errorCodeUpdateDTO);
        errorCodeMapper.updateById(updateRole);
        // TODO 插入操作日志
    }

    @Override
    public void deleteErrorCode(ErrorCodeDeleteDTO errorCodeDeleteDTO) {
        // 校验错误码是否存在
        ErrorCodeDO errorCodeDO = errorCodeMapper.selectById(errorCodeDeleteDTO.getId());
        if (errorCodeDO == null) {
            throw ServiceExceptionUtil.exception(SystemErrorCodeEnum.ERROR_CODE_NOT_EXISTS);
        }
        // 内置错误码，不允许删除
        if (ErrorCodeTypeEnum.SYSTEM.getType().equals(errorCodeDO.getType())) {
            throw ServiceExceptionUtil.exception(SystemErrorCodeEnum.ERROR_CAN_NOT_UPDATE_SYSTEM_TYPE_ERROR);
        }
        // TODO FROM 芋艿 to 鱿鱼丝：不能删除内置错误码
        // 更新到数据库，标记删除
        errorCodeMapper.deleteById(errorCodeDO.getId());
        // TODO: 2020-05-10 刷新对外提供的错误码列表
        // TODO: 2020-05-10 ServiceExceptionUtil中未提供去除错误码操作，后续新增此接口是否影响？
    }

    /**
     * 校验错误码的唯一字段是否重复
     *
     * 是否存在相同编码的错误码
     *
     * @param code 错误码编码
     * @param id 错误码编号
     */
    private void checkDuplicateErrorCode(Integer code, Integer id) {
        ErrorCodeDO errorCodeDO = errorCodeMapper.selectByCode(code);
        if (errorCodeDO != null && !errorCodeDO.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(SystemErrorCodeEnum.ERROR_CODE_DUPLICATE, errorCodeDO.getCode());
        }
    }
}
