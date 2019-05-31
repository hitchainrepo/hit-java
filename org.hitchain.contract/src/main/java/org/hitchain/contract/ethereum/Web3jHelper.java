/*******************************************************************************
 * Copyright (c) 2019-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.contract.ethereum;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.lang.reflect.Method;

/**
 * Web3jHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-29
 * auto generate by qdp.
 */
public class Web3jHelper {

    private static Web3j web3j = null;

    /**
     * get web3j instance.
     *
     * @return
     */
    public static Web3j getWeb3j() {
        if (web3j == null) {
            String url = "https://ropsten.infura.io";
            try {
                Method getChain = Class.forName("org.hitchain.hit.util.HitHelper").getDeclaredMethod("getChain");
                url = (String) getChain.invoke(null);
            } catch (Exception e) {
            }
            web3j = Web3j.build(new HttpService(url));
            System.out.println("Connect chain on " + url);
        }
        return web3j;
    }

}
