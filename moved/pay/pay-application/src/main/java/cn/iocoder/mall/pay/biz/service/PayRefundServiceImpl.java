package cn.iocoder.mall.pay.biz.service;

import cn.iocoder.common.framework.util.DateUtil;
import cn.iocoder.common.framework.util.MathUtil;
import cn.iocoder.common.framework.util.ServiceExceptionUtil;
import cn.iocoder.common.framework.vo.CommonResult;
import cn.iocoder.mall.pay.api.PayRefundService;
import cn.iocoder.mall.pay.api.bo.refund.PayRefundPageBO;
import cn.iocoder.mall.pay.api.bo.refund.PayRefundSubmitBO;
import cn.iocoder.mall.pay.api.constant.PayErrorCodeEnum;
import cn.iocoder.mall.pay.api.constant.PayRefundStatus;
import cn.iocoder.mall.pay.api.constant.PayTransactionStatusEnum;
import cn.iocoder.mall.pay.api.dto.refund.PayRefundPageDTO;
import cn.iocoder.mall.pay.api.dto.refund.PayRefundSubmitDTO;
import cn.iocoder.mall.pay.biz.client.AbstractPaySDK;
import cn.iocoder.mall.pay.biz.client.PaySDKFactory;
import cn.iocoder.mall.pay.biz.client.RefundSuccessBO;
import cn.iocoder.mall.pay.biz.convert.PayRefundConvert;
import cn.iocoder.mall.pay.biz.dao.PayRefundMapper;
import cn.iocoder.mall.pay.biz.dataobject.PayAppDO;
import cn.iocoder.mall.pay.biz.dataobject.PayRefundDO;
import cn.iocoder.mall.pay.biz.dataobject.PayTransactionDO;
import cn.iocoder.mall.pay.biz.dataobject.PayTransactionExtensionDO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@org.apache.dubbo.config.annotation.Service(validation = "true", version = "${dubbo.provider.PayRefundService.version}")
public class PayRefundServiceImpl implements PayRefundService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PayRefundMapper payRefundMapper;

    @Autowired
    private PayAppServiceImpl payAppService;
    @Autowired
    private PayNotifyServiceImpl payNotifyService;
    @Autowired
    private PayTransactionServiceImpl payTransactionService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public CommonResult<PayRefundSubmitBO> submitRefund(PayRefundSubmitDTO payRefundSubmitDTO) {
        // ?????? App ????????????
        PayAppDO payAppDO = payAppService.validPayApp(payRefundSubmitDTO.getAppId());
        // ?????? PayTransactionDO ???????????????????????????
        PayTransactionDO payTransaction = payTransactionService.getTransaction(payRefundSubmitDTO.getAppId(), payRefundSubmitDTO.getOrderId());
        if (payTransaction == null) { // ????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_TRANSACTION_NOT_FOUND.getCode());
        }
        if (!PayTransactionStatusEnum.SUCCESS.getValue().equals(payTransaction.getStatus())) { // ?????????????????????????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_TRANSACTION_STATUS_IS_NOT_SUCCESS.getCode());
        }
        if (payRefundSubmitDTO.getPrice() > payTransaction.getPrice() - payTransaction.getRefundTotal()) { // ????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_REFUND_PRICE_EXCEED.getCode());
        }
        // ?????? PayTransactionExtensionDO ???????????????????????????
        PayTransactionExtensionDO payTransactionExtension = payTransactionService.getPayTransactionExtension(payTransaction.getExtensionId());
        if (payTransactionExtension == null) { // ????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_TRANSACTION_EXTENSION_NOT_FOUND.getCode());
        }
        if (!PayTransactionStatusEnum.SUCCESS.getValue().equals(payTransactionExtension.getStatus())) { // ?????????????????????????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_TRANSACTION_EXTENSION_STATUS_IS_NOT_SUCCESS.getCode());
        }
        // ?????? PayTransactionExtensionDO
        PayRefundDO payRefundDO = PayRefundConvert.INSTANCE.convert(payRefundSubmitDTO)
                .setTransactionId(payTransaction.getId())
                .setRefundCode(generateTransactionCode()) // TODO ?????????????????????
                .setStatus(PayRefundStatus.WAITING.getValue())
                .setNotifyUrl(payAppDO.getRefundNotifyUrl())
                .setRefundChannel(payTransaction.getPayChannel());
        payRefundDO.setCreateTime(new Date());
        payRefundMapper.insert(payRefundDO);
        // ??????????????????
        AbstractPaySDK paySDK = PaySDKFactory.getSDK(payTransaction.getPayChannel());
        CommonResult<String> invokeResult = paySDK.submitRefund(payRefundDO, payTransactionExtension, null); // TODO ???????????? extra = null
        if (invokeResult.isError()) {
            return CommonResult.error(invokeResult);
        }
        // ????????????
        PayRefundSubmitBO payRefundSubmitBO = new PayRefundSubmitBO()
                .setId(payRefundDO.getId());
        return CommonResult.success(payRefundSubmitBO);
    }

    @Override
    @Transactional
    public CommonResult<Boolean> updateRefundSuccess(Integer payChannel, String params) {
        // TODO ???????????????????????????
        // ??????????????????????????? TransactionSuccessBO ??????
        AbstractPaySDK paySDK = PaySDKFactory.getSDK(payChannel);
        CommonResult<RefundSuccessBO> paySuccessResult = paySDK.parseRefundSuccessParams(params);
        if (paySuccessResult.isError()) {
            return CommonResult.error(paySuccessResult);
        }
        // TODO ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? false ??????????????????????????????????????????????????????
        // 1.1 ?????? PayRefundDO
        PayRefundDO payRefund = payRefundMapper.selectByRefundCode(paySuccessResult.getData().getRefundCode());
        if (payRefund == null) {
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_REFUND_NOT_FOUND.getCode());
        }
        if (!PayRefundStatus.WAITING.getValue().equals(payRefund.getStatus())) { // ?????????????????????????????????
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_REFUND_STATUS_NOT_WAITING.getCode());
        }
        // 1.2 ?????? PayRefundDO
        Integer status = paySuccessResult.getData().getSuccess() ? PayRefundStatus.SUCCESS.getValue() : PayRefundStatus.FAILURE.getValue();
        PayRefundDO updatePayRefundDO = new PayRefundDO()
                .setId(payRefund.getId())
                .setStatus(status)
                .setTradeNo(paySuccessResult.getData().getTradeNo())
                .setExtensionData(params);
        int updateCounts = payRefundMapper.update(updatePayRefundDO, PayRefundStatus.WAITING.getValue());
        if (updateCounts == 0) { // ?????????????????????????????????
            throw ServiceExceptionUtil.exception(PayErrorCodeEnum.PAY_REFUND_STATUS_NOT_WAITING.getCode());
        }
        // 2.1 ?????? PayTransactionDO ????????????????????????
        PayTransactionDO payTransaction = payTransactionService.getTransaction(payRefund.getTransactionId());
        if (payTransaction == null) {
            return ServiceExceptionUtil.error(PayErrorCodeEnum.PAY_TRANSACTION_NOT_FOUND.getCode());
        }
        if (!PayTransactionStatusEnum.SUCCESS.getValue().equals(payTransaction.getStatus())) { // ?????????????????????????????????
            throw ServiceExceptionUtil.exception(PayErrorCodeEnum.PAY_TRANSACTION_STATUS_IS_NOT_SUCCESS.getCode());
        }
        if (payRefund.getPrice() + payTransaction.getRefundTotal() > payTransaction.getPrice()) {
            throw ServiceExceptionUtil.exception(PayErrorCodeEnum.PAY_REFUND_PRICE_EXCEED.getCode());
        }
        // 2.2 ?????? PayTransactionDO
        updateCounts = payTransactionService.updateTransactionPriceTotalIncr(payRefund.getTransactionId(), payRefund.getPrice());
        if (updateCounts == 0) { // ??????????????? TODO ????????????????????????????????????????????????????????????????????????????????????
            throw ServiceExceptionUtil.exception(PayErrorCodeEnum.PAY_REFUND_PRICE_EXCEED.getCode());
        }
        // 3 ?????? PayNotifyTaskDO
        payNotifyService.addRefundNotifyTask(payRefund);
        // ????????????
        return CommonResult.success(true);
    }

    @Override
    public PayRefundPageBO getRefundPage(PayRefundPageDTO payRefundPageDTO) {
        PayRefundPageBO payRefundPageBO = new PayRefundPageBO();
        // ??????????????????
        int offset = (payRefundPageDTO.getPageNo() - 1) * payRefundPageDTO.getPageSize();
        payRefundPageBO.setList(PayRefundConvert.INSTANCE.convertList(payRefundMapper.selectListByPage(
                payRefundPageDTO.getCreateBeginTime(), payRefundPageDTO.getCreateEndTime(),
                payRefundPageDTO.getFinishBeginTime(), payRefundPageDTO.getFinishEndTime(),
                payRefundPageDTO.getStatus(), payRefundPageDTO.getPayChannel(),
                offset, payRefundPageDTO.getPageSize())));
        // ??????????????????
        payRefundPageBO.setTotal(payRefundMapper.selectCountByPage(
                payRefundPageDTO.getCreateBeginTime(), payRefundPageDTO.getCreateEndTime(),
                payRefundPageDTO.getFinishBeginTime(), payRefundPageDTO.getFinishEndTime(),
                payRefundPageDTO.getStatus(), payRefundPageDTO.getPayChannel()));
        return payRefundPageBO;
    }

    private String generateTransactionCode() {
//    wx
//    2014
//    10
//    27
//    20
//    09
//    39
//    5522657
//    a690389285100
        // ???????????????
        // ????????????????????????????????? 14 ???
        // ????????????6 ??? TODO ????????????????????????????????????????????????
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + // ????????????
                MathUtil.random(100000, 999999) // ????????????????????????????????????????????????
                ;
    }

}
