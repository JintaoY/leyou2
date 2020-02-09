package com.leyou.auth.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "D:\\javaeeeclipse\\heima\\jwt\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\javaeeeclipse\\heima\\jwt\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        //eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU3OTY5ODI3Nn0.W_1-m_l0ObAPAERCPktk5CTwuGhv8t0MpKBq83AkLsON63tdvAQkgoxCuuoN_rgOPU8JZ58E51POchxO41SW7Pm1pMmw91rygkWICJrlsJ2jsB1cWOuEb_lenpGbyVpKIS2ADHgFQ6PXjHe0B_A0E5AYsppIUIet44Eb6RZ9s0Q
        //eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU3OTY5ODMwNH0.Ir5qpTxHg8c_3xmBUoQqXCOehgYJD_YBSVOXpmOqeDWJ-VOZFB8_ezINdBYCqHvfpTduUmL1GZDg21rICjeVTwUbZnGSKPvxmLGp_Jx4gXSu7VHQvNUsoMpoKtzLBR2WU7dJf-auf0sctNpje7Q_xlfhSt1Dei0H08QLhLDtHms
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU3OTY5ODI3Nn0.W_1-m_l0ObAPAERCPktk5CTwuGhv8t0MpKBq83AkLsON63tdvAQkgoxCuuoN_rgOPU8JZ58E51POchxO41SW7Pm1pMmw91rygkWICJrlsJ2jsB1cWOuEb_lenpGbyVpKIS2ADHgFQ6PXjHe0B_A0E5AYsppIUIet44Eb6RZ9s0Q";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}