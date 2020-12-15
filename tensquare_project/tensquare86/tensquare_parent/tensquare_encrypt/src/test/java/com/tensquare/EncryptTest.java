package com.tensquare;

import com.tensquare.encrypt.rsa.RsaKeys;
import com.tensquare.encrypt.service.RsaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EncryptApplication.class)
public class EncryptTest {

    @Autowired
    private RsaService rsaService;

    @Test
    //加密
    public void fun01() throws Exception {
        String data = "{\"columnid\": \"1\", \"title\":\"java\"}";
        String rsaEncryptDataPEM = rsaService.rsaEncryptDataPEM(data, RsaKeys.getServerPubKey());
        System.out.println("加密后的数据:" + rsaEncryptDataPEM);

    }
    @Test
    //解密
    public void fun02() throws Exception {
        //加密参数
        String data = "IManQGHLGMJ62MQibJH1pRJljF76RxkqOoT4BZL1YRguf9vmL69Drz3IX9BbH7PjfqLIObNM0L1ZxTDl5BT1XGo+bs/LJhKx4VakYU91HO7NNaXtnpvLev8MlZnY7MGzc2lyxxO+gbgNiffsRxm1D6fqbP65hd+V9qtjf+bZvyzMTcKdAsFD/ESXVj4WygNCm83/KMmuN0xwazyYJpTAzuzLBSncMqxnborvjjILzNspu17zmkzaOtz2KAc/0OBZ5/a8txomW+Hf/rc4F1/RQ5yThNkTxKN5IrCyiaXGhDcJKUrG+1LaLYLAt2kiCaIlj8qRO2Gp4LDEXRmw9HXC1Q==";
        String rsaEncryptDataPEM = rsaService.rsaDecryptDataPEM(data, RsaKeys.getServerPrvKeyPkcs8());
        System.out.println("解密后的数据:" + rsaEncryptDataPEM);

    }
}