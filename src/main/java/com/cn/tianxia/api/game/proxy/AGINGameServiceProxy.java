package com.cn.tianxia.api.game.proxy;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cn.tianxia.api.common.v2.PatternUtils;
import com.cn.tianxia.api.game.GameInterfaceService;
import com.cn.tianxia.api.game.impl.AGINGameServiceImpl;
import com.cn.tianxia.api.game.impl.JDBGameServiceImpl;
import com.cn.tianxia.api.po.v2.TransferResponse;
import com.cn.tianxia.api.vo.v2.GameBalanceVO;
import com.cn.tianxia.api.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.api.vo.v2.GameForwardVO;
import com.cn.tianxia.api.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.api.vo.v2.GameTransferVO;

import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/3/8 14:10
 * @Description: AGIN游戏接口代理类
 */
@Service("AGIN")
public class AGINGameServiceProxy implements GameInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(AGINGameServiceProxy.class);

    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) throws Exception {
        Map<String,String> data = gameTransferVO.getConfig();
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String ag_password = gameTransferVO.getPassword();
        String type = gameTransferVO.getType();
        AGINGameServiceImpl gameService = new AGINGameServiceImpl(data);
        // 调用预转账
        String msg = gameService.PrepareTransferCredit(ag_username, billno, "IN", credit + "", ag_password, "CNY");
        if (!"0".equals(msg)) {
            return TransferResponse.transferFaild();
        }
        //调用确认转账
        msg = gameService.TransferCreditConfirm(ag_username, billno, "IN", credit + "", "1", ag_password,
                "CNY");
        if(msg == "error")    return TransferResponse.transferFaild();
        if (!"0".equals(msg)) {
            int counts = 0;
            while (true) {
                Thread.sleep(3000);
                logger.info("用户:【"+username+"】,订单号:【"+billno+"】,操作天下平台向游戏平台:【"+type+"】转入金额业务(游戏上分),第{}次轮询转账订单次数", counts);
                counts++;
                // 查询转账订单
                msg = gameService.QueryOrderStatus(billno, "CNY");
                if ("0".equals(msg)) {
                    // 转账订单处理成功
                    return TransferResponse.transferSuccess();
                } else if ("1".equals(msg)) {
                    // 查询成功订单返回失败状态
                    if (counts > 2) {
                        return TransferResponse.transferFaild();
                    }
                } else {
                    if (counts > 2) {
                        // 异常订单,未收到任何回馈的
                        return TransferResponse.transferProcess();
                    }
                }
            }
        } else {
            // 转账成功
            return TransferResponse.transferSuccess();
        }
    }

    @Override
    public JSONObject transferOut(GameTransferVO gameTransferVO) throws Exception {
        Map<String,String> data = gameTransferVO.getConfig();
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String ag_password = gameTransferVO.getPassword();
        String type = gameTransferVO.getType();
        AGINGameServiceImpl gameService = new AGINGameServiceImpl(data);
        // 调用预转账
        String msg = gameService.PrepareTransferCredit(ag_username, billno, "OUT", credit + "", ag_password,
                "CNY");
        if (!"0".equals(msg)) {
            return TransferResponse.transferFaild();
        }
        //调用确认转账
        msg = gameService.TransferCreditConfirm(ag_username, billno, "OUT", credit + "", "1", ag_password,
                "CNY");
        if(msg == "error")    return TransferResponse.transferFaild();
        if (!"0".equals(msg)) {
            int counts = 0;
            while (true) {
                Thread.sleep(3000);
                logger.info("用户【"+username+"】,订单号:【"+billno+"】,游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", counts);
                counts++;
                // 查询转账订单
                msg = gameService.QueryOrderStatus(billno, "CNY");
                if ("0".equals(msg)) {
                    // 转账订单处理成功
                    return TransferResponse.transferSuccess();
                } else if ("1".equals(msg)) {
                    // 查询成功订单返回失败状态
                    if (counts > 2) {
                        return TransferResponse.transferFaild();
                    }
                } else {
                    if (counts > 2) {
                        // 异常订单,未收到任何回馈的
                        return TransferResponse.transferProcess();
                    }
                }
            }
        } else {
            // 转账成功
            return TransferResponse.transferSuccess();
        }
    }

    @Override
    public JSONObject forwardGame(GameForwardVO gameForwardVO) throws Exception {
        return null;
    }

    @Override
    public JSONObject getBalance(GameBalanceVO gameBalanceVO) throws Exception {
        JSONObject data = new JSONObject();
        data.put("type", "AGIN");
        try {
            Map<String,String> pmap = gameBalanceVO.getConfig();
            String ag_username = gameBalanceVO.getGamename();
            String ag_password = gameBalanceVO.getPassword();
            //获取游戏余额
            AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
            //检查用户游戏账号是否存在
//            String msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
//            if(msg.equals("0")){
//                //查询用户游戏余额
//                String balance = agService.GetBalance(ag_username, ag_password, "CNY");
//                if(StringUtils.isNoneEmpty(balance) && PatternUtils.isMatch(balance, PatternUtils.MONEYREGEX)){
//                    data.put("balance", new DecimalFormat("0.00").format(Double.parseDouble(balance)));
//                    return data;
//                }
//            }
            
            String msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
            if ("0".equals(msg)) {
                String balance = agService.GetBalance(ag_username, ag_password, "CNY");
                if (balance == null || balance == "") {
                    data.put("balance", "维护中");
                    return data;
                } else {
                    data.put("balance", balance);
                    return data;
                }
            } else {
                data.put("balance", "维护中");
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.put("balance", "0.00");
        return data;
    }

    @Override
    public JSONObject checkOrCreateAccount(GameCheckOrCreateVO gameCheckOrCreateVO) throws Exception {
        return null;
    }

    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) throws Exception {
        return null;
    }
}
