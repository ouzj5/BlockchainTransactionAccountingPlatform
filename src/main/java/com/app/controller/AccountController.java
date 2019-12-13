package com.app.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
@RestController
public class AccountController {
    @RequestMapping(value = "getaccount", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> exportXls(HttpServletRequest req) {
        String ac = req.getParameter("account");
        System.out.println(req.getSession().getAttribute("PrKey"));
        if (req.getSession().getAttribute("PrKey").equals(ac) || req.getSession().getAttribute("PlKey").equals(ac)) {
            return export(new File("src/main/resources/" + ac));
        }
        else
            return new ResponseEntity<FileSystemResource> ((FileSystemResource) null, HttpStatus.FORBIDDEN);
    }
    public ResponseEntity<FileSystemResource> export(File file) {
        if (file == null) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity .ok() .headers(headers) .contentLength(file.length()) .contentType(MediaType.parseMediaType("application/octet-stream")) .body(new FileSystemResource(file));
    }
}
