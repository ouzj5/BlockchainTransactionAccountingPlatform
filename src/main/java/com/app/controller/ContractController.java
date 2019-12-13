package com.app.controller;

import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
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

@RestController
public class ContractController {
    @Autowired
    Finance finance;
    TransactionDecoder txdecoder;
    public ContractController() {
        txdecoder = TransactionDecoderFactory.buildTransactionDecoder(Finance.ABI, "");
    }
    @RequestMapping(value="addCrediableCompany", method = RequestMethod.POST)
    public ResponseEntity<String> addCrediableCompany(HttpServletRequest req, @RequestParam(name="name",defaultValue = "")String name, @RequestParam(name="id",defaultValue = "")String address,
                                                      @RequestParam(name="property",defaultValue = "")String property, @RequestParam(name="ad", defaultValue = "")String ad){
        System.out.println("addcrediableCompany");
        try {
            if (req.getSession().getAttribute("address") == null) {
                return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
            }
            if (address.length() != 42) {
                return new ResponseEntity<String>("地址错误，地址长度应为40位", HttpStatus.FORBIDDEN);
            }
            if (nameToAddress(name) != null) {
                return new ResponseEntity<String>("已经添加过", HttpStatus.FORBIDDEN);
            }

            finance.addCrediableCompany(address, name, ad, property).send();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>("没有添加权限", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("信用公司添加成功", HttpStatus.OK);
    }

    @RequestMapping(value="addUnkownCompany", method = RequestMethod.POST)
    public ResponseEntity<String> addUnkownCompany(HttpServletRequest req,@RequestParam(name="name",defaultValue = "")String name, @RequestParam(name="id",defaultValue = "")String address,
                                                      @RequestParam(name="property",defaultValue = "")String property, @RequestParam(name="ad", defaultValue = "")String ad) {
        try {
            if (req.getSession().getAttribute("address") == null) {
                return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
            }
            if (address.length() != 42) {
                return new ResponseEntity<String>("地址错误，地址长度应为40位", HttpStatus.FORBIDDEN);
            }
            if (nameToAddress(name) != null) {
                return new ResponseEntity<String>("已经添加过", HttpStatus.FORBIDDEN);
            }

            finance.addUnknowCompany(address, name, ad, property).send();
        } catch (Exception e) {
            return new ResponseEntity<String>("添加错误", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("公司添加成功", HttpStatus.OK);
    }

    @RequestMapping(value="borrow", method = RequestMethod.POST)
    public ResponseEntity<String> brow(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        if (req.getSession().getAttribute("address") == null) {
            return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
        }
        try {
            String add_from = nameToAddress(from);
            String add_to = nameToAddress(to);
            if (add_from == null || add_to == null) {
                return new ResponseEntity<String>("借条登记失败，其中一方没有添加", HttpStatus.FORBIDDEN);
            }
            TransactionReceipt re = finance.brow(add_from, add_to, new BigInteger(mount)).send();
        } catch (Exception e) {
            return new ResponseEntity<String>("借条登记失败", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("借条登记成功", HttpStatus.OK);
    }

    @RequestMapping(value="transfer", method = RequestMethod.POST)
    public ResponseEntity<String> transfer(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        if (req.getSession().getAttribute("address") == null) {
            return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
        }
        int num = 0;
        try {
            String add_from = nameToAddress(from);
            String add_to = nameToAddress(to);
            if (add_from == null || add_to == null) {
                return new ResponseEntity<String>("转移失败，其中一方没有添加", HttpStatus.FORBIDDEN);
            }
            TransactionReceipt re = finance.transferMount(add_from, add_to, new BigInteger(mount)).send();
            num = getReturn(re).intValue();
        } catch (Exception e) {
            return new ResponseEntity<String>("转移失败", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("转移成功，共转移了" + num + "金额", HttpStatus.OK);
    }

    @RequestMapping(value="payback", method = RequestMethod.POST)
    public ResponseEntity<String> payback(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from, @RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        if (req.getSession().getAttribute("address") == null) {
            return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
        }
        int num = 0;
        try {
            String add_from = nameToAddress(from);
            String add_to = nameToAddress(to);
            if (add_from == null || add_to == null) {
                return new ResponseEntity<String>("还款失败，其中一方没有添加", HttpStatus.FORBIDDEN);
            }
            TransactionReceipt re = finance.payBack(add_from, add_to, new BigInteger(mount)).send();
            num = getReturn(re).intValue();
        } catch (Exception e) {
            return new ResponseEntity<String>("还款失败", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("还款成功，共还了" + num + "金额", HttpStatus.OK);
    }

    @RequestMapping(value="loan", method = RequestMethod.POST)
    public ResponseEntity<String> loan(HttpServletRequest req,@RequestParam(name="to",defaultValue = "")String to,
                             @RequestParam(name="mount",defaultValue = "")String mount){
        if (req.getSession().getAttribute("address") == null) {
            return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
        }
        int num = 0;
        try {
            String add_to = nameToAddress(to);
            if (add_to == null) {
                return new ResponseEntity<String>("贷款失败, 贷款这没有加入", HttpStatus.FORBIDDEN);
            }
            TransactionReceipt re = finance.loan(add_to, new BigInteger(mount)).send();
            num = getReturn(re).intValue();
        } catch (Exception e) {
            return new ResponseEntity<String>("贷款失败", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>("贷款成功，共还了" + num + "金额", HttpStatus.OK);
    }

    @RequestMapping(value="getloan", method = RequestMethod.GET)
    public ResponseEntity<String> getLoan(HttpServletRequest req,@RequestParam(name="from",defaultValue = "")String from){
        int num = 0;
        if (req.getSession().getAttribute("address") == null) {
            return new ResponseEntity<String>("请先登录", HttpStatus.FORBIDDEN);
        }
        try {
            String add_from = nameToAddress(from);
            if (add_from == null) {
                return new ResponseEntity<String>("查询失败，其中一方没有添加", HttpStatus.FORBIDDEN);
            }
            TransactionReceipt re = finance.getLoan(add_from).send();
            num = getReturn(re).intValue();
        } catch (Exception e) {
            return new ResponseEntity<String>("查询失败", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<String>(String.valueOf(num), HttpStatus.OK);
    }

    public BigInteger getReturn(TransactionReceipt res) throws BaseException, TransactionException {
        InputAndOutputResult ior = txdecoder.decodeOutputReturnObject(res.getInput(), res.getOutput());
        ResultEntity n = ior.getResult().get(0);
        return (BigInteger) n.getData();
    }
    public String nameToAddress(String name) throws Exception {
        int c = finance.c_num().send().intValue();
        for (int i = 0; i < c; i ++ ) {
            BigInteger bi = new BigInteger(String.valueOf(i));
            Tuple5<String, String, String, BigInteger, String> cp = finance.companys(bi).send();
            if (cp.getValue1().equals(name)) {
                return cp.getValue5();
            }
        }
        return null;
    }
    public String addressToName(String address) {
        try {
            BigInteger bi = finance.companysMap(address).send();
            Tuple5<String, String, String, BigInteger, String> n = finance.companys(bi).send();
            return n.getValue1();
        } catch (Exception e) {
            return null;
        }
    }
    public int getLoan(String add_from){
        int num = 0;
        try {
            if (add_from == null) {
                return num;
            }
            TransactionReceipt re = finance.getLoan(add_from).send();
            num = getReturn(re).intValue();
        } catch (Exception e) {
            return num;
        }
        return num;
    }
}
