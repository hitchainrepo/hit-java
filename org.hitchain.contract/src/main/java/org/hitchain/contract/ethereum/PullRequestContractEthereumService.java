/*******************************************************************************
 * Copyright (c) 2019-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.contract.ethereum;

import org.hitchain.contract.api.PullRequestContractEthereumApi;
import org.iff.infra.util.FCS;

import java.math.BigInteger;

/**
 * PullRequestContractEthereumService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-29
 * auto generate by qdp.
 */
public class PullRequestContractEthereumService extends ContractService implements PullRequestContractEthereumApi {

    public static final String CONTRACT_CREATE = "PrivateKey={0}\nContractByteCode={1}\nGasLimit={2}\nGwei={3}\n";
    public static final String CONTRACT_READ = "FromAddress={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\n";
    public static final String CONTRACT_WRITE = "PrivateKey={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\nGasLimit={5}\nGwei={6}\n";

    public String readOwner(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "owner", "address", "").toString();
        return readContract(data);
    }


    public String readDelegator(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "delegator", "address", "").toString();
        return readContract(data);
    }


    public String readCommunityPullRequest(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "communityPullRequest(uint256)", "string", index).toString();
        return readContract(data);
    }


    public int readCommunityPullRequestCount(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "communityPullRequestCount", "uint256", "").toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }


    public String readAuthedPullRequest(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedPullRequest(uint256)", "string", index).toString();
        return readContract(data);
    }


    public int readAuthedPullRequestCount(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedPullRequestCount", "uint256", "").toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }


    public boolean readAuthedAccount(String fromAddress, String contractAddress, String address) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccount(address)", "bool", address).toString();
        String result = readContract(data);
        return "true".equals(result);
    }


    public String readAuthedAccountList(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccountList(uint256)", "address", index).toString();
        return readContract(data);
    }


    public int readAuthedAccountCount(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccountCount", "uint256", "").toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }


    public String writeChangeOwner(String ownerAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "changeOwner(address)", "-", ownerAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }


    public String writeDelegateTo(String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "delegateTo(address)", "-", delegatorAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }


    public String writeAddPullRequest(String pullRequest, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addPullRequest(string)", "-", pullRequest, gasLimit, gWei).toString();
        return writeContract(data);
    }


    public String writeAddAuthedAccount(String accountAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addAuthedAccount(address)", "-", accountAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }


    public String writeRemoveAuthedAccount(String accountAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeAuthedAccount(address)", "-", accountAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }


    public String deployContract(String privateKey, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_CREATE, privateKey, PullRequestContract.pullRequestByteCode, gasLimit, gWei).toString();
        return deployContract(data);
    }
}
