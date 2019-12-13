package com.app.contract;

import com.app.BaseTest;
import com.app.autoconfigure.ContractConfig;
import com.app.constants.GasConstants;
import com.app.constants.StateType;
import com.app.controller.ContractController;
import com.app.controller.TListController;
import com.app.solidity.Finance;
import com.app.solidity.HelloWorld;
import com.app.util.*;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.RemoteCall;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoderFactory;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class contractTest {
    @Autowired
    Credentials credentials;
    @Autowired
    Web3j web3j;
    GenerateAccount a1;
    GenerateAccount a2;
    @Test
    public void deployTest(){
        Finance fin = deploy();
        GenerateAccount a1 = new GenerateAccount();
        a1.getAccount(false, "");
        GenerateAccount a2 = new GenerateAccount();
        a2.getAccount(false, "");
        TransactionDecoder txdecoder = TransactionDecoderFactory.buildTransactionDecoder(Finance.ABI, "");
        try {
            if (fin != null) {
                TListController tlc = new TListController();
                ContractController cc = new ContractController();
                System.out.println("fin address is: " + fin.getContractAddress());
                // call set function
                TransactionReceipt result1 = fin.addCrediableCompany(a1.getAddress(), "company1", "adress 1", "property").send();
                // call get function
                TransactionReceipt result2 = fin.addCrediableCompany(a2.getAddress(), "company2", "adress 2", "property").send();
                TransactionReceipt result3 = fin.brow(a1.getAddress(), a2.getAddress(), BigInteger.valueOf(1000)).send();
                fin.brow(a1.getAddress(), a2.getAddress(), BigInteger.valueOf(1000)).send();
                fin.brow(a1.getAddress(), a2.getAddress(), BigInteger.valueOf(1000)).send();
                TransactionReceipt result5 = fin.getLoan(a1.getAddress()).send();
                String jsonRes2 = txdecoder.decodeEventReturnJson(result5.getLogs());
                BigInteger index = fin.companysMap(a1.getAddress()).send();
                Tuple5<String, String, String, BigInteger, String> result4 = fin.companys(index).send();
                String jsonRes = txdecoder.decodeEventReturnJson(result3.getLogs());
                List<Receipt> rl = tlc.getRecList(fin);
                for (Receipt re : rl) {
                    System.out.println(re.from + re.to + re.mount);
                }
                System.out.println(cc.getReturn(result5));
                System.out.println(result4.getValue1());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void nameToAddressTest() throws Exception {
        Finance fin = deploy();
        GenerateAccount a1 = new GenerateAccount();
        a1.getAccount(false, "");
        GenerateAccount a2 = new GenerateAccount();
        a2.getAccount(false, "");
        TransactionReceipt result1 = fin.addCrediableCompany(a1.getAddress(), "company1", "adress 1", "property").send();
        // call get function
        TransactionReceipt result2 = fin.addCrediableCompany(a2.getAddress(), "company2", "adress 2", "property").send();
        System.out.println(a1.getAddress().length());
        String name = "masterbank";
        try {
            int c = fin.c_num().send().intValue();
            for (int i = 0; i < c; i ++ ) {
                BigInteger bi = new BigInteger(String.valueOf(i));
                Tuple5<String, String, String, BigInteger, String> cp = fin.companys(bi).send();
                System.out.println(cp.getValue1());
                if (cp.getValue1().equals(name)) {

                    System.out.println("find: " + cp.getValue5());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Finance deploy(){
        Finance contract = null;
        HelloWorld helloWorld = null;
        try {
            System.out.println(web3j.getBlockNumber());
            contract = Finance.deploy(web3j, credentials,  new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT)).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contract;
    }
    @Test
    public void testReturn() throws Exception {
        Finance fin = deploy();
        GenerateAccount a1 = new GenerateAccount();
        a1.getAccount(false, "");
        GenerateAccount a2 = new GenerateAccount();
        a2.getAccount(false, "");
        // call set function
        TransactionReceipt result1 = fin.addCrediableCompany(a1.getAddress(), "company1", "adress 1", "property").send();
        // call get function
        TransactionReceipt result2 = fin.addCrediableCompany(a2.getAddress(), "company2", "adress 2", "property").send();
        fin.brow(a1.getAddress(), a2.getAddress(), BigInteger.valueOf(1000)).send();
    }
    @Test
    public void testDeploy() throws Exception{
        Finance fin = null;
        File file = new File("classpath:/contract/address");
        String address = null;
        char[] buff = new char[200];
        if (file.exists()) {
            FileReader fr = new FileReader(file);
            fr.read(buff);
            address = buff.toString();
            fin = Finance.load(address, web3j, credentials, new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT));
        } else {
            fin = Finance.deploy(web3j, credentials, new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT)).send();
            FileWriter fw = new FileWriter(file);
            address = fin.getContractAddress();
            fw.write(fin.getContractAddress());
        }
        System.out.println(address);
    }
}
