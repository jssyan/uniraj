package test;

import cn.com.syan.jcee.common.impl.SparkCipher;
import cn.com.syan.jcee.common.sdk.utils.PrivateKeyBuilder;
import cn.com.syan.jcee.utils.PublicKeyGenerator;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Security;

/**
 * Created by Iceberg on 16/4/8.
 */
public class TestClass {

    public static int[] convert(byte buf[]) {
        int intArr[] = new int[buf.length / 4];
        int offset = 0;
        for (int i = 0; i < intArr.length; i++) {
            intArr[i] = (buf[3 + offset] & 0xFF) | ((buf[2 + offset] & 0xFF) << 8) |
                    ((buf[1 + offset] & 0xFF) << 16) | ((buf[0 + offset] & 0xFF) << 24);
            offset += 4;
        }
        return intArr;
    }

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        String ekeyblob = "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";

        String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC112e2y6ZSsaGUhADMJBCnNIW+mQRtFb2LeYn4EGgVFhvTYqJhmZSeDeR+ELLyShdqI4Su1t3lolJPtgzgO9AIjTib9GDEdBPgB81R96PaMToDJh4j9M3ZJfdafDfY1YLPEA/DeOlW3dBs7Axpkq/+sAU8KpOpVNIvmtTcI3p5LQIDAQAB";

        String prvKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANMjdTtMD7G2x7N3VdowXTdTi7jOGJSlz38uCdc97pWnOnIYTnz27HNsk/YWLhQAp/WDGv3M0WDB6Z4H8gDIWHOU7nohpZuYW/UyJ1nq4cyWUgQByBavWM6nWHobLq84AWLs0F5zwLqn0ZgvJPZSu9IUqxoxrATnntVAVAGIj+nRAgMBAAECgYAjHVUMcRh1Gmx5FQJCwONn5a2ppSEia7coNCxvzz2ERyUpv5AiKWDmy2qKNWMhcYB9jPnqtusll+p2HWUqqydGeGixP5jQsU61efQ8REwr/QgPeiu0t0pRe5YdYDEAP8LzZ0o5XIg/3u/3ne9no2v6Qz0M+3CoApQB0CrKw9qKdwJBAOy6SCo09vch4yVP2h62x4Z6B/kUGc6ffAuvMQlIkfw46pBH6tM3bgNbfLqtFIGYcbvlyG9ZQ02DDe37MGHJtNsCQQDkU903sCVfZOqcczQD0ptXOHJbj+DzlVtxB417NQhw6x8vq1Ono31x1peEamkPnJoPz9MjoMJBDlKq9k0RLqXDAkEAqPiyaAYUPgRfORPNUl0PAEr8g+q4HQczqLdtHf5BzbHmpAPhdtA644Sa/DhsybBBhmgW8HvbkWk5OSQxvasXPwJAL+TTi2HchQUTHVwg9ghQY1xIyKIGirGyq6Ps7oIyUhVPxl0GwCxQHPfpcnIrtYWg3141qMyI0sKhXpEpm0gQpwJAe0oHKkAEBskiX5I+x+ivn8/p9hlVh0i4bWz9HFxfAqfOePBg08HN62MIf3tVrkPKgHwjqfCs7eGeJ2yVJWQSAQ==";


        byte[] data = Base64.decode(ekeyblob);

        System.out.println(data.length);


        byte[] ulSymmAlgID = new byte[4];
        byte[] ulWrappedKeyLen = new byte[4];
        byte[] bWrappedKey = new byte[1024];
        byte[] ulEncryptedDataLen = new byte[4];
        byte[] bEncryptedData = new byte[data.length - 4 - 4 - 4 - 1024];

        System.arraycopy(data, 0, ulSymmAlgID, 0, 4);
        System.arraycopy(data, 4, ulWrappedKeyLen, 0, 4);
        System.arraycopy(data, 8, bWrappedKey, 0, 1024);
        System.arraycopy(data, 1032, ulEncryptedDataLen, 0, 4);
        System.arraycopy(data, data.length - bEncryptedData.length, bEncryptedData, 0, bEncryptedData.length);

        int p1 = java.nio.ByteBuffer.wrap(ulSymmAlgID).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        int p2 = java.nio.ByteBuffer.wrap(ulWrappedKeyLen).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        int p3 = java.nio.ByteBuffer.wrap(ulEncryptedDataLen).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();


        byte[] realWrappedKey = new byte[128];

        System.arraycopy(bWrappedKey, 0, realWrappedKey, 0, 128);
//
//
//        byte[] rr = Arrays.reverse(realWrappedKey);

//
//        Cipher c = Cipher.getInstance("RSA");
//        c.init(Cipher.ENCRYPT_MODE, PublicKeyGenerator.generate(pubKey));
//
//        byte[] cdddd = c.doFinal("1234567812345678".getBytes());


        Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding","SC");
        cipher.init(SparkCipher.DECRYPT_MODE, PrivateKeyBuilder.buildPrivateKey(Base64.decode(prvKey)));

        byte[] symmKey = cipher.doFinal(realWrappedKey);


        byte xyxyx = -128;


        System.out.println(1);

    }
}