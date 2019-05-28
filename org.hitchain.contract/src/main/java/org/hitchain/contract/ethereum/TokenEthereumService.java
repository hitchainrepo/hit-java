/*******************************************************************************
 * Copyright (c) 2018-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;


import org.hitchain.contract.api.TokenApi;
import org.hitchain.contract.api.TokenEthereumApi;
import org.iff.infra.util.FCS;
import org.iff.infra.util.RequestHelper;

import java.util.Collections;

/**
 * Token service for Ethereum implements TokenEthereumApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class TokenEthereumService extends TokenService implements TokenEthereumApi {

    public static TokenEthereumApi getApi() {
        TokenApi.setInstance(new TokenEthereumService());
        return (TokenEthereumApi) TokenApi.getInstance();
    }

    @Override
    public String requestTestToken(String address) {
        System.out.println("Please wait a while if has no error occurred, such as 60 seconds, and check the account.");
        return RequestHelper.get("https://faucet.ropsten.be/donate/" + address, Collections.EMPTY_MAP, Collections.EMPTY_MAP).getBody();
    }

    @Override
    public String readContactToken(String address, String contractAddress, String functionName) {
        String data = FCS.get(CONTRACT_READ, address, contractAddress, functionName, "Uint256", address).toString();
        return readContactToken(data);
    }
}
