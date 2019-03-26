/*******************************************************************************
 * Copyright (c) 2019-02-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.MnemonicUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * WalletHelper
 * <pre>
 * generate:
 * 1. use password to encrypt the private key.
 * 2. generate the password mnemonic words.
 * use:
 * 1. use the password to decrypt the private key.
 * 2. use the mnemonic to recover the password.
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-28
 * auto generate by qdp.
 */
public class WalletHelper {
    public static void main(String[] args) throws Exception {
        //
        {
            String sign = sign("helloworld");
            System.out.println("verify=" + verifySign(sign, "helloworld"));
        }
        {
            Tuple.Two<Object, String, String> two = createAccount("helloworld");
            System.out.println("pub=" + two.first());
            System.out.println("pri=" + two.second());
            System.out.println("pub=" + toHex(ECKey.fromPrivate(fromHex(decryptWithPasswordHex(two.second(), "helloworld"))).getPubKey()));
            byte[] bytes = ECCHelper.publicEncrypt(ByteHelper.utf8("helloworld"), ECCHelper.getPublicKeyFromEthereumPublicKeyHex(two.first()));
            byte[] helloworlds = ECCHelper.privateDecrypt(bytes, ECCHelper.getPrivateKeyFromEthereumHex(decryptWithPasswordHex(two.second(), "helloworld")));
            System.out.println("decrypt=" + ByteHelper.utf8(helloworlds));
        }
        {
            Tuple.Two<Object, String, String> two = createRsa("helloworld");
            System.out.println("pub=" + two.first());
            System.out.println("pri=" + two.second());
            String sign = RSAHelper.encrypt("helloworld", RSAHelper.getPublicKeyFromHex(two.first()));
            System.out.println("decrypt=" + RSAHelper.decrypt(sign, RSAHelper.getPrivateKeyFromHex(decryptWithPasswordHex(two.second(), "helloworld"))));
        }
        //
        {
            for (int i = 0; i < 10; i++) {
                String helloworld = stringToMnemonic("helloworld");
                System.out.println(helloworld);
                System.out.println(mnemonicToString(helloworld));
            }
        }
    }

    /**
     * 01byte: str length.
     * 02-password_length:str.
     * 1+password_length+1-[16|20|24|28|32]:random bytes.
     *
     * @param str
     * @return
     */
    public static String stringToMnemonic(String str) {
        byte[] bs = str.getBytes();
        int len = 16;//new int[]{16, 20, 24, 28, 32}[(int) (System.currentTimeMillis() % 5)];
        byte[] data = new byte[len];
        data[0] = (byte) bs.length;
        System.arraycopy(bs, 0, data, 1, bs.length);
        if (len - bs.length - 1 > 0) {
            byte[] tmp = RandomUtils.nextBytes(len - bs.length - 1);
            System.arraycopy(tmp, 0, data, bs.length + 1, tmp.length);
        }
        return MnemonicUtils.generateMnemonic(data);
    }

    /**
     * 01byte: mnemonic length.
     * 02-password_length:mnemonic.
     * 1+password_length+1-[16|20|24|28|32]:random bytes.
     *
     * @param mnemonic
     * @return
     */
    public static String mnemonicToString(String mnemonic) {
        byte[] bs = MnemonicUtils.generateEntropy(mnemonic);
        int len = bs[0];
        return new String(bs, 1, len);
    }

    /**
     * encrypt content and output hex string by using password.
     *
     * @param content
     * @param password
     * @return
     */
    public static String encryptWithPasswordHex(String content, String password) {
        return ECCHelper.encryptWithPasswordHex(content, password);
    }

    /**
     * decrypt hex content and output string by using password.
     *
     * @param encryptedHex
     * @param password
     * @return
     */
    public static String decryptWithPasswordHex(String encryptedHex, String password) {
        return ECCHelper.decryptWithPassword(encryptedHex, password);
    }

    /**
     * create rsa key pair.
     *
     * @param password
     * @return address and privateKey encrypted with password.
     */
    public static Tuple.Two<Object, String, String> createRsa(String password) {
        KeyPair key = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
            keyGen.initialize(1024, new SecureRandom());
            key = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String publicKey = toHex(key.getPublic().getEncoded());
        String privateKey = encryptWithPasswordHex(toHex(key.getPrivate().getEncoded()), password);
        Tuple.Two<Object, String, String> two = new Tuple.Two<>(publicKey, privateKey);
        return two;
    }

    /**
     * create rsa by key pair.
     *
     * @param password
     * @return address and privateKey encrypted with password.
     */
    public static Tuple.Two<Object, String, String> createExistsRsa(String password, String priKey, String pubKey) {
        try {
            String encrypt = RSAHelper.encrypt("hello", RSAHelper.getPublicKeyFromHex(pubKey));
            String decrypt = RSAHelper.decrypt(encrypt, RSAHelper.getPrivateKeyFromHex(priKey));
            if ("hello".equals(decrypt)) {
                String privateKey = encryptWithPasswordHex(priKey, password);
                Tuple.Two<Object, String, String> two = new Tuple.Two<>(pubKey, privateKey);
                return two;
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * create ethereum account.
     *
     * @param password
     * @return address(0x) and privateKey encrypted with password.
     */
    public static Tuple.Two<Object, String, String> createAccount(String password) {
        ECKey key = new ECKey();
        String publicKey = toHex(key.getPubKey());
        String privateKey = encryptWithPasswordHex(toHex(key.getPrivKeyBytes()), password);
        Tuple.Two<Object, String, String> two = new Tuple.Two<>(publicKey, privateKey);
        return two;
    }

    /**
     * create ethereum account by private key.
     *
     * @param password
     * @return address(0x) and privateKey encrypted with password.
     */
    public static Tuple.Two<Object, String, String> createExistsAccount(String password, String priHex) {
        try {
            ECKey key = ECKey.fromPrivate(fromHex(priHex));
            String publicKey = toHex(key.getPubKey());
            String privateKey = encryptWithPasswordHex(toHex(key.getPrivKeyBytes()), password);
            Tuple.Two<Object, String, String> two = new Tuple.Two<>(publicKey, privateKey);
            return two;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * sign a random content by password, step:
     * 1.(sign+content) -> 2.(encrypt) -> 3.(to hex)
     *
     * @param password
     * @return
     */
    public static String sign(String password) {
        byte[] bs = RandomUtils.nextBytes(RandomUtils.nextInt(128, 256));
        //#1.(sign+content)
        byte[] sign = ECCHelper.sha256(bs);
        byte[] data = ArrayUtils.addAll(sign, bs);
        //#2.(encrypt)
        byte[] encrypt = ECCHelper.encryptWithPassword(data, password);
        //#3.(to hex)
        return toHex(encrypt);
    }

    /**
     * verify the signed content, step:
     * 1.(from hex) -> 2.(decrypt) -> 3.(verify sign)
     *
     * @param hexContent
     * @param password
     * @return
     */
    public static boolean verifySign(String hexContent, String password) {
        try {
            //#1.(from hex)
            byte[] hex = fromHex(hexContent);
            //#2.(decrypt)
            byte[] data = ECCHelper.decryptWithPassword(hex, password);
            byte[] sign = ArrayUtils.subarray(data, 0, 32);
            byte[] bs = ArrayUtils.subarray(data, 32, data.length);
            //#3.(verify sign)
            return Arrays.equals(sign, ECCHelper.sha256(bs));
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * encode bytes to hex string.
     *
     * @param bs
     * @return
     */
    public static String toHex(byte[] bs) {
        return Hex.toHexString(bs);
    }

    /**
     * decode hex string to bytes.
     *
     * @param hex
     * @return
     */
    public static byte[] fromHex(String hex) {
        return Hex.decode(hex);
    }
}
