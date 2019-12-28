package com.app.autoconfigure;

import com.app.constants.GasConstants;
import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;

@Configuration
@ConfigurationProperties(prefix = "contract")
public class ContractConfig {
    String name;
    @Bean
    public Finance getFinance(Web3j web3j, Credentials credentials) throws Exception {
        File file = new File("src/main/resources/contract/address.txt");
        String address = null;

        char[] buff = new char[200];
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            address = br.readLine();
            System.out.println("adreess: " + address);
            return Finance.load(address, web3j, credentials, new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT));
        } else {
            file.createNewFile();
            Finance fin = Finance.deploy(web3j, credentials, new StaticGasProvider(GasConstants.GAS_PRICE, GasConstants.GAS_LIMIT)).send();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(fin.getContractAddress().getBytes());
            bos.flush();
            return fin;
        }
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
