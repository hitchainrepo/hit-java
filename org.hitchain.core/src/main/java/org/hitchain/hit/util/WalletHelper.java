/*******************************************************************************
 * Copyright (c) 2019-02-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;

import java.io.File;

/**
 * WalletHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-28
 * auto generate by qdp.
 */
public class WalletHelper {
    public static void main(String[] args) {
        createWallet("123456");
    }
    public static void createWallet(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException("WalletHelper password is required!");
        }
        String walletFilePath = System.getProperty("user.home");
        if (StringUtils.isBlank(walletFilePath)) {
            throw new RuntimeException("WalletHelper can not get user.home!");
        }
        walletFilePath = walletFilePath + File.separator + ".hit";
        File walletDir = new File(walletFilePath);
        if (!walletDir.exists()) {
            walletDir.mkdir();
        }
        if (!walletDir.exists() || !walletDir.isDirectory()) {
            throw new RuntimeException("WalletHelper can not create .hit directory!");
        }
        //钱包文件保持路径，请替换位自己的某文件夹路径
        try {
            Bip39Wallet wallet = WalletUtils.generateBip39Wallet(password, new File(walletFilePath));
            System.out.println(wallet.getMnemonic());
            System.out.println(wallet.getFilename());
        } catch (Exception e) {
            throw new RuntimeException("WalletHelper can not create wallet file!", e);
        }
    }
}
