package com.app.controller;

import com.app.autoconfigure.AccountConfig;
import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import com.app.util.GenerateAccount;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

@RestController
public class SignupController {
    @Autowired
    Web3j web3j;
    @Autowired
    AccountConfig accountConifg;
    @Autowired
    Finance finance;
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<String>  signup(HttpServletRequest req,
                               @RequestParam(name="name",defaultValue = "")String name,
                               @RequestParam(name="property",defaultValue = "")String property,
                               @RequestParam(name="ad", defaultValue = "")String ad,
                               @RequestParam(name="usep12", defaultValue = "")String usep12,
                               @RequestParam(name="password", defaultValue = "")String password){
        try {
            if (name.length() == 0) {
                return new ResponseEntity<String>("请输入公司名字", HttpStatus.FORBIDDEN);
            }
            if (nameToAddress(name) != null) {
                return new ResponseEntity<String>("已经添加过", HttpStatus.FORBIDDEN);
            }
            GenerateAccount ac = new GenerateAccount();
            String format = "";
            if (usep12.equals("1")) {
                ac.getAccount(true, password);
                format = ".p12";
            } else {
                ac.getAccount(false, "");
                format = ".pem";
            }
            req.getSession().setAttribute("PrKey", ac.getPrivateKey());
            req.getSession().setAttribute("PlKey", ac.getPublicKey());
            finance.addUnknowCompany(ac.getAddress(), name, ad, property).send();
            req.getSession().setAttribute("address", ac.getAddress());
            req.getSession().setAttribute("company", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>( "注册成功", HttpStatus.OK);
    }

    @RequestMapping("blocknum")
    public String getBlockNumber() throws IOException {
        BigInteger blockNumber = web3j.getBlockNumber().send().getBlockNumber();
        return blockNumber.toString();
    }
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signup() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("signup.html");
        return mv;
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
}