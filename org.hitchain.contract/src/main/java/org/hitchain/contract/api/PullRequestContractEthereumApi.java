/*******************************************************************************
 * Copyright (c) 2019-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.api;

/**
 * PullRequestContractEthereumApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-29
 * auto generate by qdp.
 */
public interface PullRequestContractEthereumApi extends ContractApi {
    /**
     * Get contract owner address.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return owner address
     */
    String readOwner(String fromAddress, String contractAddress);

    /**
     * Get contract delegator address.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return delegator address
     */
    String readDelegator(String fromAddress, String contractAddress);

    /**
     * Get community pull request at index.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param index           the pull request index
     * @return true if contains the address
     */
    String readCommunityPullRequest(String fromAddress, String contractAddress, int index);

    /**
     * Get community pull request count.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return
     */
    int readCommunityPullRequestCount(String fromAddress, String contractAddress);

    /**
     * Get authored pull request at index.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param index           the pull request index
     * @return
     */
    String readAuthedPullRequest(String fromAddress, String contractAddress, int index);

    /**
     * Get authored pull request count.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return
     */
    int readAuthedPullRequestCount(String fromAddress, String contractAddress);

    /**
     * Get authored account by address.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param address         the address to test
     * @return true if contains the address
     */
    boolean readAuthedAccount(String fromAddress, String contractAddress, String address);

    /**
     * Get authored account at index.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param index           the account index
     * @return
     */
    String readAuthedAccountList(String fromAddress, String contractAddress, int index);

    /**
     * Get authored account count.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return
     */
    int readAuthedAccountCount(String fromAddress, String contractAddress);

    /**
     * list community pull requests split by \n.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return
     */
    String listCommunityPR(String fromAddress, String contractAddress);

    /**
     * list authored pull requests split by \n.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return
     */
    String listAuthedPR(String fromAddress, String contractAddress);

    /**
     * Change the contract's Owner.
     *
     * @param ownerAddress    new owner address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeChangeOwner(String ownerAddress, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * Change delegator to new address.
     *
     * @param delegatorAddress new delegator address
     * @param privateKey       operator private key
     * @param contractAddress  contract address
     * @param gasLimit         gas limit prefer to 500000
     * @param gWei             gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeDelegateTo(String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * Add pull request, if msg.sender is authored then pull request will add to authedPullRequest otherwise add to communityPullRequest.
     *
     * @param pullRequest     the pull request address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddPullRequest(String pullRequest, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * Add authored account address.
     *
     * @param accountAddress  the account address to be authored
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddAuthedAccount(String accountAddress, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * Remove authored account address.
     *
     * @param accountAddress  the account address to remove
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeRemoveAuthedAccount(String accountAddress, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * deploy contract.
     *
     * @param privateKey operator private key
     * @param gasLimit   gas limit prefer to 5000000
     * @param gWei       gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String deployContract(String privateKey, long gasLimit, long gWei);
}
