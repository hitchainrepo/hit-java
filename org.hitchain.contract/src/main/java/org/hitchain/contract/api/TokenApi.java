/*******************************************************************************
 * Copyright (c) 2019-05-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.api;

import org.apache.commons.lang3.StringUtils;

/**
 * ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-09
 * auto generate by qdp.
 */
public interface TokenApi {

    public static final String HIT_CONTACT = "0x7995ab36bb307afa6a683c24a25d90dc1ea83566";

    static final TokenApi[] instance = new TokenApi[1];

    public static boolean isError(String result) {
        return StringUtils.startsWith(result, "ERROR:");
    }

    public static TokenApi getInstance() {
        return TokenApi.instance[0];
    }

    public static TokenApi setInstance(TokenApi contractApi) {
        return TokenApi.instance[0] = contractApi;
    }

    /**
     * return the block chain token, for ethereum will return ETH.
     *
     * @param address the account address
     * @return token
     */
    String readToken(String address);

    /**
     * return the hit token.
     *
     * @param address the account address
     * @return
     */
    String readHitToken(String address);

    /**
     * return the contract token.
     * <pre>
     * data structure(for ethereum):
     * FromAddress=\n
     * ContractAddress=\n
     * FunctionName=functionName(Uint256|Address|Utf8String|Uint256Array|AddressArray,...)\n
     * FunctionType=Uint256|Address|Bool|Utf8String\n
     * Arg*=\n
     * </pre>
     *
     * @return
     */
    String readContactToken(String data);
}
