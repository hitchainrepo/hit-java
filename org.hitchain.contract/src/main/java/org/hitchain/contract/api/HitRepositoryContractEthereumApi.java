/*******************************************************************************
 * Copyright (c) 2019-05-10 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.api;

import org.iff.infra.util.Tuple;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Hit Repository Contract Api for Ethereum
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-10
 */
public interface HitRepositoryContractEthereumApi extends ContractApi {

    /**
     * this type use for owner settings.
     */
    public static final int TYPE_OWNER = 0;
    /**
     * how to search delegator, var count = id_type_count(id, 1),
     * var address = id_type_count_address(id, 1, count), var enable = id_type_address(id, 1, address).
     */
    public static final int TYPE_DELEGATOR = 1;
    /**
     * how to search member, var count = id_type_count(id, 2),
     * var address = id_type_count_address(id, 2, count), var enable = id_type_address(id, 2, address).
     */
    public static final int TYPE_MEMBER = 2;
    /**
     * how to search pr member, var count = id_type_count(id, 3),
     * var address = id_type_count_address(id, 3, count), var enable = id_type_address(id, 3, address).
     */
    public static final int TYPE_PR_MEMBER = 3;
    /**
     * how to search authorized pr, var count = id_type_count(id, 4),
     * var string = id_type_count_string(id, 4, count).
     */
    public static final int TYPE_PR_AUTH = 4;
    /**
     * how to search community pr, var count = id_type_count(id, 5),
     * var string = id_type_count_string(id, 5, count).
     */
    public static final int TYPE_PR_COMM = 5;
    /**
     * how to search started repository, var count = id_type_count(0, 6),
     * var string = id_type_count_string(0, 6, count).
     */
    public static final int TYPE_STARTED = 6;
    /**
     * contract version.
     */
    public static final int VERSION = 2019071100;

    /**
     * Get user name.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return user name
     */
    String readName(String fromAddress, String contractAddress);

    /**
     * Get user email.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return user email
     */
    String readEmail(String fromAddress, String contractAddress);

    /**
     * Get repository max id.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return repository max id
     */
    int readId(String fromAddress, String contractAddress);

    /**
     * Get contract/repository owner address.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return owner address
     */
    String readOwner(String fromAddress, String contractAddress);

    /**
     * Get repository id by name.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param repository      repository name
     * @return repository id
     */
    int readIdByName(String fromAddress, String contractAddress, String repository);

    /**
     * Get repository id by hash.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param hash            repository name hash
     * @return repository id
     */
    int readIdByHash(String fromAddress, String contractAddress, BigInteger hash);

    /**
     * Get repository name by id.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return repository name
     */
    String readRepositoryById(String fromAddress, String contractAddress, int id);

    /**
     * Get repository url by id.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return repository url
     */
    String readUrlById(String fromAddress, String contractAddress, int id);

    /**
     * Test if contains address.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @param type            data type
     * @param address         address
     * @return
     */
    boolean readHasAddress(String fromAddress, String contractAddress, int id, int type, String address);

    /**
     * Get count by type.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @param type            data type
     * @return
     */
    int readTypeCount(String fromAddress, String contractAddress, int id, int type);

    /**
     * Get address by type and index.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @param type            data type
     * @param index           index
     * @return
     */
    String readAddressByTypeIndex(String fromAddress, String contractAddress, int id, int type, int index);

    /**
     * Get string by type and index.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @param type            data type
     * @param index           index
     * @return
     */
    String readStringByTypeIndex(String fromAddress, String contractAddress, int id, int type, int index);

    /**
     * Get type is disable.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @param type            data type
     * @return
     */
    boolean readDisableByType(String fromAddress, String contractAddress, int id, int type);

    //================================================================================

    /**
     * update user name.
     *
     * @param name            user name
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeUpdateName(String name, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * update user email.
     *
     * @param email           user email
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeUpdateEmail(String email, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add repository.
     *
     * @param repository      repository name
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddRepository(String repository, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * update repository name.
     *
     * @param repository      repository name
     * @param newRepository   new repository name
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeUpdateRepository(String repository, String newRepository, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * update repository url.
     *
     * @param id              repository id
     * @param url             repository url
     * @param newUrl          new repository url
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeUpdateUrl(int id, String url, String newUrl, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add repository delegator.
     *
     * @param id              repository id
     * @param address         delegator address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddDelegator(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * remove repository delegator.
     *
     * @param id              repository id
     * @param address         delegator address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeRemoveDelegator(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add repository member.
     *
     * @param id              repository id
     * @param address         member address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * remove repository member.
     *
     * @param id              repository id
     * @param address         member address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeRemoveMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add pull request member.
     *
     * @param id              repository id
     * @param address         member address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddPrMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * remove pull request member.
     *
     * @param id              repository id
     * @param address         member address
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeRemovePrMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add pull request.
     *
     * @param id              repository id
     * @param url             pull request url
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddPullRequest(int id, String url, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * add started repository.
     *
     * @param url             started repository url
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeAddStarted(String url, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * remove started repository by index.
     *
     * @param index           started repository url index
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeRemoveStarted(int index, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * disable function by type.
     *
     * @param id              repository id
     * @param type            type
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeDisableType(int id, int type, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * enable function by type.
     *
     * @param id              repository id
     * @param type            type
     * @param privateKey      operator private key
     * @param contractAddress contract address
     * @param gasLimit        gas limit prefer to 500000
     * @param gWei            gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String writeEnableType(int id, int type, String privateKey, String contractAddress, long gasLimit, long gWei);

    /**
     * deploy contract.
     *
     * @param privateKey operator private key
     * @param gasLimit   gas limit prefer to 5000000
     * @param gWei       gas wei prefer to 10
     * @return if has error return result starts with "ERROR:"
     */
    String deployContract(String privateKey, long gasLimit, long gWei);

    //================================================================================

    /**
     * Get all repositories.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @return List[Tuple.Three[Object, String: contract, Integer: id, String: name]]
     */
    List<Tuple.Three<Object, String/*contract*/, Integer/*id*/, String/*name*/>> listRepositories(String fromAddress, String contractAddress);

    /**
     * Get repository commit history.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: from, Date: date, String: url]]
     */
    List<Tuple.Three<Object, String/*from*/, Date/*date*/, String/*url*/>> listHistoryUrls(String fromAddress, String contractAddress, int id);

    /**
     * Get repository members.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listMembers(String fromAddress, String contractAddress, int id);

    /**
     * Get pull request members.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listPrMembers(String fromAddress, String contractAddress, int id);

    /**
     * Get pull requests.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    List<Map<String,Object>> listPullRequests(String fromAddress, String contractAddress, int id);

    /**
     * Get members's pull requests.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    List<Map<String,Object>> listAuthoredPullRequests(String fromAddress, String contractAddress, int id);

    /**
     * Get committer's pull requests.
     *
     * @param fromAddress     the address to read contract
     * @param contractAddress contract address
     * @param id              repository id
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    List<Map<String,Object>> listCommunityPullRequests(String fromAddress, String contractAddress, int id);
}
