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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class LoginController {
    @Autowired
    Finance finance;
    @RequestMapping(value ="login", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login.html");
        return mv;
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<Object> home(HttpServletRequest req,@RequestParam(name = "file", defaultValue = "xxx") MultipartFile file,
                                       @RequestParam(name = "password", defaultValue = "xxx")String password) {
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
                    return new ResponseEntity<Object>("文件格式错误", HttpStatus.FORBIDDEN);
                }
                if (credentials == null)
                    return new ResponseEntity<Object>("文件格式错误", HttpStatus.FORBIDDEN);
            } catch (Exception e) {
                System.out.println("login failed");
                System.out.println(e);
                return new ResponseEntity<Object>("文件格式错误", HttpStatus.FORBIDDEN);
            }
            System.out.println("login adress: " + address);

            req.getSession().setAttribute("PrKey", "accounts/" + filename);
            req.getSession().setAttribute("address", filename);
            req.getSession().setAttribute("company", addressToName(address));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("lgoin post");
        return new ResponseEntity<Object>("登录成功", HttpStatus.OK);
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
    public String addressToName(String address) throws Exception {
        BigInteger bi =  finance.companysMap(address).send();
        Tuple5<String, String, String, BigInteger, String> n = finance.companys(bi).send();
        return n.getValue1();
    }
}
