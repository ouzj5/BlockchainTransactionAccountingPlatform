package com.app.util;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateAccount {
    String address;
    String pr_key;
    String pu_key;
    public void getAccount(boolean isP12, String password) {
        Process p;
        String path = "./get_account.sh";
        if (isP12) {
            path += " -p";
        }
        try {
            Integer i = 0;
            String tem = null;
            p = Runtime.getRuntime().exec(path, null, new File("src/main/resources"));
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            PrintWriter pw = null;
            if (p != null)
                pw = new PrintWriter(p.getOutputStream());
            pw.println(password);
            pw.println(password);
            pw.flush();
            String pattern= ":\\s*([A-Za-z0-9_/.]*)";
            Pattern pat = Pattern.compile(pattern);
            while ((tem = br.readLine()) != null) {
                System.out.println(tem);
                Matcher m = pat.matcher(tem);
                if (m.find()) {
                    i ++;
                    switch (i) {
                        case 1:
                            address = m.group(1);
                            break;
                        case 2:
                            pr_key = m.group(1);
                            break;
                        case 3:
                            pu_key = m.group(1);
                            break;
                    }
                    System.out.println(m.group(1));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public String getPublicKey(){
        return pu_key;
    }
    public String getPrivateKey(){
        return pr_key;
    }
    public String getAddress(){
        return address;
    }
}
