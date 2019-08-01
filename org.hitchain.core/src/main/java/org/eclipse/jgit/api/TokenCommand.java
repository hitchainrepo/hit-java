/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.hitchain.contract.api.TokenEthereumApi;
import org.hitchain.contract.ethereum.TokenEthereumService;
import org.web3j.utils.Convert;

import java.util.concurrent.Callable;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class TokenCommand implements Callable<String> {

    protected String type;/*eth, hit, request-test*/

    protected String account;

    public String account() {
        return account;
    }

    public TokenCommand account(String account) {
        this.account = account;
        return this;
    }

    public String type() {
        return type;
    }

    public TokenCommand type(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String call() throws Exception {
        TokenEthereumApi api = TokenEthereumService.getApi();
        if (type().equals("eth")) {
            String token = api.readToken(account());
            return Convert.fromWei(token, Convert.Unit.ETHER).toPlainString();
        }
        if (type().equals("hit")) {
            String token = api.readHitToken(account());
            return token;
        }
        if (type().equals("request-test")) {
            String transationHash = api.requestTestToken(account());
            return transationHash;
        }
        return null;
    }
}
