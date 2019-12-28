package com.app.controller;

import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import com.sun.deploy.net.URLEncoder;
import org.fisco.bcos.channel.client.P12Manager;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class LoginController {
    @Autowired
    ContractUtil contractUtil;
    @RequestMapping(value ="login", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login.html");
        return mv;
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> home(HttpServletRequest req, @RequestParam(name = "file", defaultValue = "xxx") MultipartFile file,
                                                    @RequestParam(name = "password", defaultValue = "xxx")String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String filename = URLEncoder.encode(file.getOriginalFilename(), "utf-8");
            File file2 = new File("src/main/resources/accounts/"+filename);
            BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream(file2));
            out.write(file.getBytes());
            out.flush();
            out.close();
            String pstr = "([^.]*).([^.]*)";
            String address = null;
            String format = null;
            Pattern pa = Pattern.compile(pstr);
            Matcher matcher = pa.matcher(filename);
            System.out.println(password);
            if (matcher.find()) {
                address = matcher.group(1);
                format  = matcher.group(2);
            }
            Credentials credentials;
            try {
                if (format.equals("pem")) {
                    credentials = loadPemAccount(filename);
                } else if (format.equals("p12")){
                    credentials = loadP12Account(filename, password);
                } else {
                    map.put("status", "error");
                    map.put("msg", "文件格式错误");
                    return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
                }
                if (credentials == null) {
                    map.put("status", "error");
                    map.put("msg", "文件格式错误");
                    return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
                }
            } catch (Exception e) {
                System.out.println("login failed");
                e.printStackTrace();
                map.put("status", "error");
                map.put("msg", "文件格式错误");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            System.out.println("login adress: " + address);
            String name = contractUtil.addressToName(address);
            if (name == null) {
                map.put("status", "error");
                map.put("msg", "用户名未注册");
                return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
            }
            req.getSession().setAttribute("PrKey", "accounts/" + filename);
            req.getSession().setAttribute("address", filename);
            req.getSession().setAttribute("company", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("lgoin post");
        map.put("status", "success");
        map.put("msg", "登录成功");
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }
    private Credentials loadPemAccount(String filename)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        PEMManager pem = new PEMManager();
        pem.setPemFile("classpath:/accounts/" + filename);
        pem.load();
        ECKeyPair keyPair = pem.getECKeyPair();
        System.out.println(keyPair.getPrivateKey().toString(16));
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        System.out.println(credentials.getAddress());
        return credentials;
    }
    private Credentials loadP12Account(String filename, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            NoSuchProviderException, InvalidKeySpecException, UnrecoverableKeyException {
        P12Manager p12Manager = new P12Manager();
        p12Manager.setP12File("classpath:" + filename);
        p12Manager.setPassword(password);
        p12Manager.load();
        ECKeyPair keyPair = p12Manager.getECKeyPair();
        Credentials credentials = GenCredential.create(keyPair.getPrivateKey().toString(16));
        System.out.println(credentials.getAddress());
        return credentials;
    }

}
