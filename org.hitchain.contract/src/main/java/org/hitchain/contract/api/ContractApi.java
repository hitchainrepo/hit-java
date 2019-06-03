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

import java.util.HashMap;
import java.util.Map;

/**
 * ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-09
 * auto generate by qdp.
 */
public interface ContractApi {

    static final Map<Class<?>, ContractApi> instance = new HashMap<>();

    public static boolean isError(String result) {
        return StringUtils.startsWith(result, "ERROR:");
    }

    public static ContractApi getInstance(Class<?> cls) {
        return ContractApi.instance.get(cls);
    }

    public static ContractApi setInstance(Class<?> cls, ContractApi contractApi) {
        return ContractApi.instance.put(cls, contractApi);
    }

    /**
     * <pre>
     * data(for ethereum):
     * PrivateKey=\n
     * ContractByteCode=\n
     * GasLimit=5000000\n
     * Gwei=10\n
     * </pre>
     * ContractByteCode: without 0x
     *
     * @param data per key-value as one line: key=value.
     * @return if has error, then return result starts with "ERROR:".
     */
    String deployContract(String data);

    /**
     * <pre>
     * data(for ethereum):
     * FromAddress=\n
     * ContractAddress=\n
     * FunctionName=repositoryName|repositoryAddress|owner|delegator|authedAccounts(addr)|authedAccountList(int)|authedAccountSize|hasTeamMember(addr)|teamMemberAtIndex(int)\n
     * Arg=-\n
     * </pre>
     *
     * @param data per key-value as one line: key=value.
     * @return if has error, then return result starts with "ERROR:".
     */
    String readContract(String data);

    /**
     * <pre>
     * data(for ethereum):
     * PrivateKey=\n
     * ContractAddress=\n
     * FunctionName=init(addr,repoName)|initWithDelegator(addr,repoName,delegator)|updateRepositoryName(repoName)|updateRepositoryAddress(oldAddr,newAddr)|addTeamMember(addr)|removeTeamMember(addr)|changeOwner(addr)|delegateTo(addr)\n
     * Arg1=-\n
     * Arg2=-\n
     * Arg3=-\n
     * GasLimit=300000\n
     * Gwei=10\n
     * </pre>
     *
     * @param data per key-value as one line: key=value.
     * @return if has error, then return result starts with "ERROR:".
     */
    String writeContract(String data);
}
