/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.StringUtils;
import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.HitRepositoryContractEthereumService;
import org.hitchain.hit.util.HitHelper;
import org.iff.infra.util.Tuple;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Util command for not in hit repository.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class UtilCommand implements Callable<Object> {
    protected HitRepositoryContractEthereumApi api = HitRepositoryContractEthereumService.getApi();

    @Override
    public Object call() throws Exception {
        return null;
    }

    public String contractAddress(String hitUri) {
        if (hitUri.endsWith(".git")) {
            hitUri = StringUtils.remove(hitUri, ".git");
        }
        if (hitUri.indexOf("/") > -1) {
            hitUri = StringUtils.substringAfterLast(hitUri, "/");
        }
        String[] split = StringUtils.split(hitUri, "-");
        return split.length == 2 ? split[0] : null;
    }

    public Integer repositoryId(String hitUri) {
        if (hitUri.endsWith(".git")) {
            hitUri = StringUtils.remove(hitUri, ".git");
        }
        if (hitUri.indexOf("/") > -1) {
            hitUri = StringUtils.substringAfterLast(hitUri, "/");
        }
        String[] split = StringUtils.split(hitUri, "-");
        return split.length == 2 ? new Integer(split[1]) : null;
    }

    public boolean isValidHitUri(String hitUri) {
        if (StringUtils.isBlank(hitUri)) {
            return false;
        }
        if (hitUri.endsWith(".git")) {
            hitUri = StringUtils.remove(hitUri, ".git");
        }
        if (hitUri.indexOf("/") > -1) {
            hitUri = StringUtils.substringAfterLast(hitUri, "/");
        }
        return StringUtils.split(hitUri, "-").length == 2;
    }

    public boolean checkValidHitUri(String hitUri) {
        if (!isValidHitUri(hitUri)) {
            throw new RuntimeException("Invalid hit uri: " + hitUri);
        }
        return true;
    }

    public String fromAddress() {
        return HitHelper.getAccountAddress();
    }

    public String contractAddress() {
        return HitHelper.getContract();
    }

    public long gasLimit() {
        return HitHelper.getGasWrite();
    }

    public long gWei() {
        return HitHelper.getGasWriteGwei();
    }

    /**
     * Get user name.
     */
    public String userName() {
        return api.readName(fromAddress(), contractAddress());
    }

    /**
     * Get user email.
     */
    public String email() {
        return api.readEmail(fromAddress(), contractAddress());
    }

    /**
     * Get repository max id.
     */
    public int maxId() {
        return api.readId(fromAddress(), contractAddress());
    }

    /**
     * Get contract/repository owner address.
     */
    public String owner() {
        return api.readOwner(fromAddress(), contractAddress());
    }

    /**
     * Get repository id by name.
     *
     * @param repository repository name
     */
    public int readId(String repository) {
        return api.readIdByName(fromAddress(), contractAddress(), repository);
    }

    /**
     * Get repository id by hash.
     *
     * @param hash repository name hash
     */
    public int readId(BigInteger hash) {
        return api.readIdByHash(fromAddress(), contractAddress(), hash);
    }

    /**
     * Get repository name by hitUri.
     *
     * @return repository name
     */
    public String repositoryName(String hitUri) {
        return api.readRepositoryById(fromAddress(), contractAddress(hitUri), repositoryId(hitUri));
    }

    /**
     * Get repository url by hitUri.
     */
    public String readUrl(String hitUri) {
        return api.readUrlById(fromAddress(), contractAddress(hitUri), repositoryId(hitUri));
    }

    //================================================================================

    /**
     * update user name.
     *
     * @param name user name
     * @return if has error return result starts with "ERROR:"
     */
    public String updateUserName(String name) {
        return api.writeUpdateName(name, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * update user email.
     *
     * @param email user email
     * @return if has error return result starts with "ERROR:"
     */
    public String updateEmail(String email) {
        return api.writeUpdateName(email, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add repository.
     *
     * @param repository repository name
     * @return if has error return result starts with "ERROR:"
     */
    public String addRepository(String repository) {
        return api.writeAddRepository(repository, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add started repository.
     *
     * @param url started repository url
     * @return if has error return result starts with "ERROR:"
     */
    public String addStarted(String url) {
        return api.writeAddStarted(url, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * remove started repository by index.
     *
     * @param index started repository url index
     * @return if has error return result starts with "ERROR:"
     */
    public String removeStarted(int index) {
        return api.writeRemoveStarted(index, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    //================================================================================

    /**
     * Get all repositories.
     *
     * @return List[Tuple.Three[Object, String: contract, Integer: id, String: name]]
     */
    public List<Tuple.Three<Object, String/*contract*/, Integer/*id*/, String/*name*/>> listRepositories() {
        return api.listRepositories(fromAddress(), contractAddress());
    }
}
