package com.app.autoconfigure;

import com.app.solidity.Finance;
import com.app.util.ContractUtil;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {
    @Bean
    public ContractUtil getContractUtil(Finance finance, Web3j web3j, Credentials credentials) {
        return new ContractUtil(finance, web3j, credentials);
    }
}
