package lic;

import ln.DESCoder;
import ln.RSACoder;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import weaver.general.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 生成license
 */

public class CreateLicense {
    public static void main(String[] args) throws Exception {
        //授权用户
        String companyname = "TEST";
        //标识码
        String licensecode = "656DEB6ABE52C044D71540838B6D3AE1";
        String software = "ALL";
        //用户数
        String hrmnum = "99999";
        //结束日期
        String expiredate = "9999-99-99";
        String concurrentFlag = "0";
        //客户id
        String cid = "277514";
        String scType = "1";
        String scCount = "";
        //文件目录
        String licensefilepath = "E:\\" + licensecode + "_ecology8.license";
        createLicenseFile(companyname, licensecode, software, hrmnum, expiredate, concurrentFlag, cid, scType, scCount, licensefilepath);
        System.out.println("license生成完成,文件位置:" + licensefilepath);
    }

    public static void putFile(ZipOutputStream out, String name, byte[] bts) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        out.write(bts);
        out.flush();
        out.closeEntry();
    }

    public static void createLicenseFile(String companyname, String licensecode, String software, String hrmnum, String expiredate, String concurrentFlag, String cid, String scType, String scCount, String licensefilepath) throws Exception {
        String license = Util.getEncrypt(String.valueOf(companyname) + licensecode + software + hrmnum + expiredate + concurrentFlag);
        String realPublicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALUgEZ7eGZmJJM/3Ajj5Zdd2MG1ZONVybJV+v+jQT+csNWBBqxosLVlWvwaod1ix8Gg9GsyRJgoTs1Mg25raZcsCAwEAAQ==";
        String reallicenseEncryptKey = "N3GrCfcVb1h3HVsbglWh8SjjpMycWOCO9Dkijv0ZkdXLhna0gXdkJtykvbsSzr3XSEACF+HDIPToybYdNrGHRQ==";
        byte[] publicKey = Base64.decodeBase64(realPublicKey.getBytes());
        byte[] licenseEncryptKey = Base64.decodeBase64(reallicenseEncryptKey.getBytes());
        JSONObject jsonLicense = new JSONObject();
        jsonLicense.put("companyname", companyname);
        jsonLicense.put("license", license);
        jsonLicense.put("licensecode", licensecode);
        jsonLicense.put("software", software);
        jsonLicense.put("hrmnum", hrmnum);
        jsonLicense.put("expiredate", expiredate);
        jsonLicense.put("concurrentFlag", concurrentFlag);
        jsonLicense.put("cid", cid);
        jsonLicense.put("scType", scType);
        jsonLicense.put("scCount", scCount);
        byte[] licenseKey = RSACoder.decryptByPublicKey(licenseEncryptKey, publicKey);
        byte[] licenseFile = DESCoder.encrypt(jsonLicense.toString().getBytes(), licenseKey);
        byte[] licenseFile2 = DESCoder.encrypt(jsonLicense.toString().getBytes(), "ecology7".getBytes());
        File localfile = new File(licensefilepath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(localfile));
        putFile(out, "publicKey", publicKey);
        putFile(out, "licenseEncryptKey", licenseEncryptKey);
        putFile(out, "license", licenseFile);
        putFile(out, "license2", licenseFile2);
        out.close();
    }
}
