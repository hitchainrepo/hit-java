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
 * Repository Contract Api
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-10
 */
public interface RepositoryContractApi extends ContractApi {

//    /**
//     * Get repository name.
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return repository name
//     */
//    String readRepositoryName(String data);
//
//    /**
//     * Get repository address.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return repository address
//     */
//    String readRepositoryAddress(String data);
//
//    /**
//     * Get contract/repository owner address.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return owner address
//     */
//    String readOwner(String data);
//
//    /**
//     * Get delegator address.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return delegator address
//     */
//    String readDelegator(String data);
//
//    /**
//     * If the contract authed account (team member) contains the given address.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     *  MemberAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return true if contains the address
//     */
//    boolean readAuthedAccounts(String data);
//
//    /**
//     * Get authed account (team member)  by index.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     *  Index=\n
//     * </pre>
//     *
//     * @param data
//     * @return team member address or null
//     */
//    String readAuthedAccountList(String data);
//
//    /**
//     * Get authed account (team member) size.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return the team member address counts
//     */
//    int readAuthedAccountSize(String data);
//
//    /**
//     * If the contract team member contains the given address.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     *  MemberAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return true if contains the address
//     */
//    boolean readHasTeamMember(String data);
//
//    /**
//     * Get team member address by index.
//     *
//     * <pre>
//     *  data structure:
//     *  FromAddress=\n
//     *  ContractAddress=\n
//     *  Index=\n
//     * </pre>
//     *
//     * @param data
//     * @return team member address or null
//     */
//    boolean readTeamMemberAtIndex(String data);
//
//
//    /**
//     * Initialize the contract.
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * OwnerAddress=\n
//     * RepositoryName=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeInit(String data);
//
//    /**
//     * Initialize the contract with delegator.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * OwnerAddress=\n
//     * RepositoryName=\n
//     * DelegatorAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeInitWithDelegator(String data);
//
//    /**
//     * Update repository name
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * RepositoryName=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeUpdateRepositoryName(String data);
//
//    /**
//     * Update repository address.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * OldRepositoryAddress=\n
//     * NewRepositoryAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeUpdateRepositoryAddress(String data);
//
//    /**
//     * Add repository's team member.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * MemberAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeAddTeamMember(String data);
//
//    /**
//     * Remove repository's team member.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * MemberAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeRemoveTeamMember(String data);
//
//    /**
//     * Change the contract/Repository's Owner.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * OwnerAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeChangeOwner(String data);
//
//    /**
//     * Change delegator to new address.
//     *
//     * <pre>
//     * data structure:
//     * PrivateKey=\n
//     * ContractAddress=\n
//     * GasLimit=300000\n
//     * Gwei=10\n
//     * DelegatorAddress=\n
//     * </pre>
//     *
//     * @param data
//     * @return if has error return result starts with "ERROR:"
//     */
//    String writeDelegateTo(String data);
}
