package com.app.controller;

import com.app.constants.StateType;
import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.txdecode.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ContractController {
    @Autowired
    Finance finance;
    @Autowired
    ContractUtil contractUtil;
    TransactionDecoder txdecoder;
    public ContractController() {
        txdecoder = TransactionDecoderFactory.buildTransactionDecoder(Finance.ABI, "");
    }
    @RequestMapping(value="addCrediableCompany", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addCrediableCompany(HttpServletRequest req, @RequestParam(name="name",defaultValue = "")String name,
                                                                   @RequestParam(name="id",defaultValue = "")String address, @RequestParam(name="property",defaultValue = "")String property,
                                                                   @RequestParam(name="ad", defaultValue = "")String ad, @RequestParam(name="state", defaultValue = "")String state){
        Map<String, Object> map = new HashMap<String, Object>();
        System.out.println("addcrediableCompany");
        try {

            if (req.getSession().getAttribute("address") == null) {
                map.put("status", "error");
                map.put("msg", "请先登录");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (address.length() != 42) {
                map.put("status", "error");
                map.put("msg", "地址错误，地址长度应为40位");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (contractUtil.nameToAddress(name) != null) {
                map.put("status", "error");
                map.put("msg", "已经添加过");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (contractUtil.addressToName(address) != null) {
                map.put("status", "error");
                map.put("msg", "地址已经使用过");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt tr = finance.addCrediableCompany(contractUtil.fileToAddress((String) req.getSession().getAttribute("address")), address, name, ad, property, new BigInteger(state)).send();
            if  (!contractUtil.getBooleanReturn(tr)) {
                map.put("status", "error");
                map.put("msg", "添加失败，没有添加信用公司或银行的权限");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", "error");
            map.put("msg", "没有添加权限");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "信用公司添加成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value="addUnkownCompany", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addUnkownCompany(HttpServletRequest req,@RequestParam(name="name",defaultValue = "")String name, @RequestParam(name="id",defaultValue = "")String address,
                                                      @RequestParam(name="property",defaultValue = "")String property, @RequestParam(name="ad", defaultValue = "")String ad) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (req.getSession().getAttribute("address") == null) {
                map.put("status", "error");
                map.put("msg", "请先登录");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (address.length() != 42) {
                map.put("status", "error");
                map.put("msg", "地址错误，地址长度应为40位");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (contractUtil.nameToAddress(name) != null) {
                map.put("status", "error");
                map.put("msg", "公司名字已经使用过");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (contractUtil.addressToName(address) != null) {
                map.put("status", "error");
                map.put("msg", "公司地址已经使用过");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            finance.addUnknowCompany(address, name, ad, property).send();
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "添加错误");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "公司添加成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
    //modifyCompany
    @RequestMapping(value="modifyCompany", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> modifyCompany(HttpServletRequest req,@RequestParam(name="address",defaultValue = "")String address) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (req.getSession().getAttribute("address") == null) {
                map.put("status", "error");
                map.put("msg", "请先登录");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (address.length() != 42) {
                map.put("status", "error");
                map.put("msg", "地址错误，地址长度应为40位");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt tr = finance.modifyCompany(contractUtil.fileToAddress((String) req.getSession().getAttribute("address")), address).send();
            if  (!contractUtil.getBooleanReturn(tr)) {
                map.put("status", "error");
                map.put("msg", "添加失败，没有修改公司状态权限");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "修改错误");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "公司修改成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
    //delCompany
    @RequestMapping(value="delCompany", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> delCompany(HttpServletRequest req,@RequestParam(name="address",defaultValue = "")String address) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if (req.getSession().getAttribute("address") == null) {
                map.put("status", "error");
                map.put("msg", "请先登录");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            if (address.length() != 42) {
                map.put("status", "error");
                map.put("msg", "地址错误，地址长度应为40位");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt tr = finance.delCompany(contractUtil.fileToAddress((String) req.getSession().getAttribute("address")), address).send();
            if  (!contractUtil.getBooleanReturn(tr)) {
                map.put("status", "error");
                map.put("msg", "删除失败，没有修改公司状态权限");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "删除错误");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "公司删除成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
    @RequestMapping(value="borrow", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> brow(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        Map<String, Object> map = new HashMap<String, Object>();
        if (req.getSession().getAttribute("address") == null) {
            map.put("status", "error");
            map.put("msg", "请先登录");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        try {
            String add_from = contractUtil.nameToAddress(from);
            String add_to = contractUtil.nameToAddress(to);
            if (add_from == null || add_to == null) {
                map.put("status", "error");
                map.put("msg", "借条登记失败，其中一方没有添加");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt re = finance.brow(add_from, add_to, new BigInteger(mount)).send();
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "借条登记失败");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "借条登记成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value="transfer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> transfer(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        Map<String, Object> map = new HashMap<String, Object>();
        if (req.getSession().getAttribute("address") == null) {
            map.put("status", "error");
            map.put("msg", "请先登录");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        int num = 0;
        try {
            String add_from = contractUtil.nameToAddress(from);
            String add_to = contractUtil.nameToAddress(to);
            if (add_from == null || add_to == null) {
                map.put("status", "error");
                map.put("msg", "转移失败，其中一方没有添加");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt re = finance.transferMount(add_from, add_to, new BigInteger(mount)).send();
            num = contractUtil.getIntReturn(re).intValue();
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "转移失败");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "转移成功，共转移了" + num + "金额");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value="payback", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> payback(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        Map<String, Object> map = new HashMap<String, Object>();
        if (req.getSession().getAttribute("address") == null) {
            map.put("status", "error");
            map.put("msg", "请先登录");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        int num = 0;
        try {
            String add_from = contractUtil.nameToAddress(from);
            String add_to = contractUtil.nameToAddress(to);
            if (add_from == null || add_to == null) {
                map.put("status", "error");
                map.put("msg", "还款失败，其中一方没有添加");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt re = finance.payBack(add_from, add_to, new BigInteger(mount)).send();
            num = contractUtil.getIntReturn(re).intValue();
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "还款失败");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "还款成功，共还了" + num + "金额");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping(value="loan", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> loan(HttpServletRequest req,@RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        Map<String, Object> map = new HashMap<String, Object>();
        if (req.getSession().getAttribute("address") == null) {
            map.put("status", "error");
            map.put("msg", "请先登录");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        int num = 0;
        try {
            String add_to = contractUtil.nameToAddress(to);
            if (add_to == null) {
                map.put("status", "error");
                map.put("msg", "贷款失败, 贷款这没有加入");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            TransactionReceipt re = finance.loan(add_to, new BigInteger(mount)).send();
            num = contractUtil.getIntReturn(re).intValue();
        } catch (Exception e) {
            map.put("status", "error");
            map.put("msg", "贷款失败");
            return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
        }
        map.put("status", "success");
        map.put("msg", "贷款成功，金额为" + num);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

}
