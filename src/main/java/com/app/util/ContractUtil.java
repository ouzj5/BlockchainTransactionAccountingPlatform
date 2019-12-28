package com.app.util;

import com.app.solidity.Finance;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tx.txdecode.*;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContractUtil {
    static ContractUtil contractUtil;
    Credentials credentials;
    Web3j web3j;
    public Finance finance;
    TransactionDecoder txdecoder;
    public ContractUtil(Finance finance, Web3j web3j, Credentials credentials) {
        this.finance = finance;
        txdecoder = TransactionDecoderFactory.buildTransactionDecoder(Finance.ABI, "");
    }
    public String nameToAddress(String name) throws Exception {
        int c = finance.c_num().send().intValue();
        for (int i = 1; i < c; i ++ ) {
            BigInteger bi = new BigInteger(String.valueOf(i));
            Tuple5<String, String, String, BigInteger, String> cp = finance.companys(bi).send();
            if (cp.getValue1().equals(name)) {
                return cp.getValue5();
            }
        }
        return null;
    }
    public String addressToName(String address) throws Exception {
        BigInteger bi =  finance.companysMap(address).send();
        if (bi.intValue() == 0) return null;
        Tuple5<String, String, String, BigInteger, String> n = finance.companys(bi).send();
        System.out.println(n.getValue1() + " : " + bi + " add: " + address);
        return n.getValue1();
    }
    public BigInteger getIntReturn(TransactionReceipt res) throws BaseException, TransactionException {
        InputAndOutputResult ior = txdecoder.decodeOutputReturnObject(res.getInput(), res.getOutput());
        ResultEntity n = ior.getResult().get(0);
        return (BigInteger) n.getData();
    }
    public Boolean getBooleanReturn(TransactionReceipt res) throws BaseException, TransactionException {
        InputAndOutputResult ior = txdecoder.decodeOutputReturnObject(res.getInput(), res.getOutput());
        ResultEntity n = ior.getResult().get(0);
        return (Boolean) n.getData();
    }
    public int getLoan(String add_from){
        int num = 0;
        try {
            if (add_from == null) {
                System.out.println("get loan arg null");
                return num;
            }
            TransactionReceipt re = finance.getLoan(fileToAddress(add_from)).send();
            num = getIntReturn(re).intValue();
            System.out.println("get loan: " + num);
        } catch (Exception e) {
            e.printStackTrace();
            return num;
        }
        return num;
    }
    public String fileToAddress(String filename) {
        String pstr = "([^.]*).([^.]*)";
        String address = null;
        String format = null;
        Pattern pa = Pattern.compile(pstr);
        Matcher matcher = pa.matcher(filename);
        if (matcher.find()) {
            address = matcher.group(1);
            format  = matcher.group(2);
        }
        return address;
    }
}
