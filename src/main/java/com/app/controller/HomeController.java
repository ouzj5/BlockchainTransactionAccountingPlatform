package com.app.controller;

import org.bouncycastle.math.raw.Mod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import sun.tools.jconsole.JConsole;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;

@RestController
public class HomeController {
    @RequestMapping(value ="home", method = RequestMethod.GET)
    public ModelAndView home(HttpServletRequest req){
        ModelAndView mv = getView(req);
        mv.addObject("page", "info");
        return mv;
    }
    @RequestMapping(value = "addcompany", method = RequestMethod.GET)
    public ModelAndView addCompany(HttpServletRequest req) {
        ModelAndView mv = getView(req);
        mv.addObject("page", "addcompany");
        return mv;
    }
    @RequestMapping(value = "transaction", method = RequestMethod.GET)
    public ModelAndView transaction(HttpServletRequest req) {
        ModelAndView mv = getView(req);
        mv.addObject("page", "transaction");
        mv.addObject("loanAmount", new ContractController().getLoan((String) req.getSession().getAttribute("address")));
        return mv;
    }
    @RequestMapping(value = "download", method = RequestMethod.GET)
    public ModelAndView download(HttpServletRequest req) {
        ModelAndView mv = getView(req);
        mv.addObject("page", "download");
        mv.addObject("address", req.getSession().getAttribute("Address"));
        mv.addObject("PrKey", req.getSession().getAttribute("PrKey"));
        mv.addObject("PlKey", req.getSession().getAttribute("PlKey"));
        return mv;
    }

    public ModelAndView getView(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView();
        if (req.getSession().getAttribute("address") != null) {
            System.out.println(req.getSession().getAttribute("address"));
            mv.addObject("company", req.getSession().getAttribute("company"));
            mv.addObject("page", "info");
            mv.setViewName("homepage.html");
        } else {
            mv.setViewName("404.html");
        }
        return mv;
    }
}
