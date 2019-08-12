/*******************************************************************************
 * Copyright (c) 2019-05-10 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.api;

/**
 * Repository Contract Api for Ethereum
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-10
 */
public interface RepositoryContractEthereumApi extends ContractApi {
//
//    /**
//     * Get repository name.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return repository name
//     */
//    String readRepositoryName(String fromAddress, String contractAddress);
//
//    /**
//     * Get repository address.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return repository address
//     */
//    String readRepositoryAddress(String fromAddress, String contractAddress);
//
//    /**
//     * Get pull request contract address.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return pull request contract address
//     */
//    String readPullRequestAddress(String fromAddress, String contractAddress);
//
//    /**
//     * Get contract/repository owner address.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return owner address
//     */
//    String readOwner(String fromAddress, String contractAddress);
//
//    /**
//     * Get delegator address.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return delegator address
//     */
//    String readDelegator(String fromAddress, String contractAddress);
//
//    /**
//     * If the contract authed account (team member) contains the given address.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @param memberAddress   the address to test
//     * @return true if contains the address
//     */
//    boolean readTeamMember(String fromAddress, String contractAddress, String memberAddress);
//
//    /**
//     * Get team member address by index.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @param index           the team member address
//     * @return team member address or null
//     */
//    String readTeamMemberList(String fromAddress, String contractAddress, int index);
//
//    /**
//     * Get team member list count.
//     *
//     * @param fromAddress     the address to read contract
//     * @param contractAddress contract address
//     * @return
//     */
//    int readTeamMemberCount(String fromAddress, String contractAddress);
//
//    /**
//     * Get the repository history address.
//     *
//     * @param contractAddress contract address
//     * @return repository address list, data format=[yyyy-MM-dd'T'HH:mm:ss    fromAddress    repositoryAddress\n]*
//     */
//    String readHistoryRepositoryAddress(String contractAddress);
//
//    /**
//     * Initialize the contract.
//     *
//     * @param ownerAddress    contract owner address
//     * @param repositoryName  repository name
//     * @param privateKey      operator private key
//     * @param contractAddress contract address
//     * @param gasLimit        gas limit prefer to 500000
//     * @param gWei            gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeInit(String ownerAddress, String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Initialize the contract with delegator.
//     *
//     * @param ownerAddress     contract owner address
//     * @param repositoryName   repository name
//     * @param delegatorAddress delegator address
//     * @param privateKey       operator private key
//     * @param contractAddress  contract address
//     * @param gasLimit         gas limit prefer to 500000
//     * @param gWei             gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeInitWithDelegator(String ownerAddress, String repositoryName, String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Update repository name
//     *
//     * @param repositoryName  repository name
//     * @param privateKey      operator private key
//     * @param contractAddress contract address
//     * @param gasLimit        gas limit prefer to 500000
//     * @param gWei            gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeUpdateRepositoryName(String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Update repository address.
//     *
//     * @param oldRepositoryAddress old repository address
//     * @param newRepositoryAddress new repository address
//     * @param privateKey           operator private key
//     * @param contractAddress      contract address
//     * @param gasLimit             gas limit prefer to 500000
//     * @param gWei                 gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeUpdateRepositoryAddress(String oldRepositoryAddress, String newRepositoryAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Update repository address.
//     *
//     * @param pullRequestAddress the pull request contract address
//     * @param privateKey         operator private key
//     * @param contractAddress    contract address
//     * @param gasLimit           gas limit prefer to 500000
//     * @param gWei               gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeUpdatePullRequestAddress(String pullRequestAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Add repository's team member.
//     *
//     * @param memberAddress   the team member address to add.
//     * @param privateKey      operator private key
//     * @param contractAddress contract address
//     * @param gasLimit        gas limit prefer to 500000
//     * @param gWei            gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeAddTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Remove repository's team member.
//     *
//     * @param memberAddress   the team member address to remove.
//     * @param privateKey      operator private key
//     * @param contractAddress contract address
//     * @param gasLimit        gas limit prefer to 500000
//     * @param gWei            gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeRemoveTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Change the contract/Repository's Owner.
//     *
//     * @param ownerAddress    new owner address
//     * @param privateKey      operator private key
//     * @param contractAddress contract address
//     * @param gasLimit        gas limit prefer to 500000
//     * @param gWei            gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeChangeOwner(String ownerAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * Change delegator to new address.
//     *
//     * @param delegatorAddress new delegator address
//     * @param privateKey       operator private key
//     * @param contractAddress  contract address
//     * @param gasLimit         gas limit prefer to 500000
//     * @param gWei             gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeDelegateTo(String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei);
//
//    /**
//     * deploy contract.
//     *
//     * @param privateKey operator private key
//     * @param gasLimit   gas limit prefer to 5000000
//     * @param gWei       gas wei prefer to 10
//     * @return if has error return result starts with "ERROR:"
//     */
//    String deployContract(String privateKey, long gasLimit, long gWei);
}
