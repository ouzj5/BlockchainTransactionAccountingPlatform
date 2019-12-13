package com.app.controller;

import com.app.constants.StateType;
import com.app.solidity.Finance;
import com.app.util.Company;
import com.app.util.Receipt;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.tuples.generated.Tuple6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TListController {
    @Autowired
    Finance finance;
    @RequestMapping("trlist")
    public ModelAndView tllist(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView();
        if (req.getSession().getAttribute("address") == null) {
            mv.setViewName("404.html");
            return mv;
        }
        mv.addObject("t_list", getRecList(finance));
        mv.addObject("page","transactionlist");
        mv.addObject("company", req.getSession().getAttribute("company"));
        mv.setViewName("homepage.html");
        return mv;
    }
    @RequestMapping("cplist")
    public ModelAndView cplist(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView();
        if (req.getSession().getAttribute("address") == null) {
            mv.setViewName("404.html");
            return mv;
        }
        mv.addObject("c_list", getCompanyList(finance));
        mv.addObject("page","companylist");
        mv.addObject("company", req.getSession().getAttribute("company"));
        mv.setViewName("homepage.html");
        return mv;
    }
    public List<Receipt> getRecList(Finance fin) {
        List<Receipt> li = new ArrayList<Receipt>();
        try {
            int r_num = fin.r_num().send().intValue();
            for (int i = 0; i < r_num; i ++ ) {
                BigInteger bi = new BigInteger(String.valueOf(i));
                Tuple6<String, String, String, String, BigInteger, BigInteger> re =  fin.receipts(bi).send();
                int mount = re.getValue5().intValue();
                int state = re.getValue6().intValue();
                li.add(new Receipt(re.getValue1(), re.getValue2(),re.getValue3(),re.getValue4(),mount, StateType.stringValue(state)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return li;
    }
    public List<Company> getCompanyList(Finance fin) {
        List<Company> li = new ArrayList<Company>();
        try {
            int c_num = fin.c_num().send().intValue();
            for (int i = 0; i < c_num; i ++ ) {
                BigInteger bi = new BigInteger(String.valueOf(i));
                Tuple5<String, String, String, BigInteger, String> re =  fin.companys(bi).send();
                int state = re.getValue4().intValue();
                li.add(new Company(re.getValue1(), re.getValue2(),re.getValue3(), StateType.stringValue(state), re.getValue5()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return li;
    }
}
