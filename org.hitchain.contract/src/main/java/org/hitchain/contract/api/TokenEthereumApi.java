/*******************************************************************************
 * Copyright (c) 2019-05-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.api;

/**
 * ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-09
 * auto generate by qdp.
 */
public interface TokenEthereumApi extends TokenApi {

    /**
     * request free test token.
     *
     * @param address the account address
     * @return
     */
    String requestTestToken(String address);

    /**
     * read contract token, only support functionName(Address) style function.
     *
     * @param address         the account address
     * @param contractAddress the contract address
     * @param functionName    the contract function name to call, Not support arguments.
     * @return
     */
    String readContactToken(String address, String contractAddress, String functionName);
}
