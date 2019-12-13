package com.app.util;

import com.app.autoconfigure.AccountConfig;
import com.app.autoconfigure.ContractConfig;
import com.app.constants.GasConstants;
import com.app.solidity.Finance;
import com.app.solidity.HelloWorld;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class ContractUtil {
    @Autowired
    Credentials credentials;
    @Autowired
    Web3j web3j;
    @Autowired
    Finance finance;
    public Finance deploy(){
        Finance contract = null;
        HelloWorld helloWorld = null;
        try {
            contract = Finance.deploy(web3j, credentials,  new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT)).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contract;
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
