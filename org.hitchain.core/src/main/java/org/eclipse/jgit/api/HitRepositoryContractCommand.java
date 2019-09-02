/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.StringUtils;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.HitRepositoryContractEthereumService;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;
import org.iff.infra.util.Tuple;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * HitRepositoryContractCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class HitRepositoryContractCommand implements Callable<Hit> {
    protected Hit hit;
    protected HitRepositoryContractEthereumApi api = HitRepositoryContractEthereumService.getApi();

    @Override
    public Hit call() throws Exception {
        return null;
    }

    public Hit hit() {
        return hit;
    }

    public HitRepositoryContractCommand hit(Hit hit) {
        this.hit = hit;
        return this;
    }

    public String fromAddress() {
        return hit().projectInfoFile().getOwnerAddressEcc();
    }

    public String contractAddress() {
        String repoAddress = hit().projectInfoFile().getRepoAddress();
        return StringUtils.substringBefore(repoAddress, "-");
    }

    public int repositoryId() {
        String repoAddress = hit().projectInfoFile().getRepoAddress();
        return repoAddress.indexOf('-') < 0 ? -1 : Integer.valueOf(StringUtils.substringAfter(repoAddress, "-"));
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
     * Get repository name by id.
     *
     * @return repository name
     */
    public String repositoryName() {
        return api.readRepositoryById(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get repository url by id.
     */
    public String readUrl() {
        return api.readUrlById(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Test if contains address.
     *
     * @param type    data type
     * @param address address
     */
    public boolean hasAddress(int type, String address) {
        return api.readHasAddress(fromAddress(), contractAddress(), repositoryId(), type, address);
    }

    /**
     * Get count by type.
     *
     * @param type data type
     */
    public int readTypeCount(int type) {
        return api.readTypeCount(fromAddress(), contractAddress(), repositoryId(), type);
    }

    /**
     * Get address by type and index.
     *
     * @param type  data type
     * @param index index
     */
    public String readAddress(int type, int index) {
        return api.readAddressByTypeIndex(fromAddress(), contractAddress(), repositoryId(), type, index);
    }

    /**
     * Get string by type and index.
     *
     * @param type  data type
     * @param index index
     */
    public String readString(int type, int index) {
        return api.readStringByTypeIndex(fromAddress(), contractAddress(), repositoryId(), type, index);
    }

    /**
     * Get type is disable.
     *
     * @param type data type
     */
    public boolean readDisable(int type) {
        return api.readDisableByType(fromAddress(), contractAddress(), repositoryId(), type);
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
     * update repository name.
     *
     * @param newRepository new repository name
     * @return if has error return result starts with "ERROR:"
     */
    public String updateRepository(String newRepository) {
        return api.writeUpdateRepository(hit().projectInfoFile().getRepoName(), newRepository, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * update repository url.
     *
     * @param newUrl new repository use
     * @return if has error return result starts with "ERROR:"
     */
    public String updateUrl(String newUrl) {
        return api.writeUpdateUrl(repositoryId(), StringUtils.defaultIfBlank(readUrl(), "-"), newUrl, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add repository delegator.
     *
     * @param address delegator address
     * @return if has error return result starts with "ERROR:"
     */
    public String addDelegator(String address) {
        return api.writeAddDelegator(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * remove repository delegator.
     *
     * @param address delegator address
     * @return if has error return result starts with "ERROR:"
     */
    public String removeDelegator(String address) {
        return api.writeRemoveDelegator(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add repository member.
     *
     * @param address   member address
     * @param rsaPubKey member rsa pub key
     * @return if has error return result starts with "ERROR:"
     */
    public String addMember(String address, String rsaPubKey) {
        ProjectInfoFile pif = hit().projectInfoFile();
        if (StringUtils.isBlank(address)) {
            return "ERROR: member address is required.";
        }
        if (StringUtils.isBlank(rsaPubKey)) {
            return "ERROR: member rsa public key is required.";
        }
        if (pif.isPrivate()) {
            pif.addMemberPrivate(address, rsaPubKey, address, HitHelper.getRsaPriKeyWithPasswordInput());
        } else {
            pif.addMemberPublic(address, rsaPubKey, address);
        }
        String result = null;
        if (!hasAddress(HitRepositoryContractEthereumApi.TYPE_MEMBER, address)) {
            result = api.writeAddMember(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
        } else {
            result = "OK the member already exists.";
        }
        if (!ContractApi.isError(result)) {
            GitHelper.updateHitRepositoryProjectInfoFile(hit().getRepository().getDirectory(), pif);
        }
        return result;
    }

    /**
     * remove repository member.
     *
     * @param address member address
     * @return if has error return result starts with "ERROR:"
     */
    public String removeMember(String address) {
        return api.writeRemoveMember(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add pull request member.
     *
     * @param address member address
     * @return if has error return result starts with "ERROR:"
     */
    public String addPrMember(String address) {
        return api.writeAddPrMember(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * remove pull request member.
     *
     * @param address member address
     * @return if has error return result starts with "ERROR:"
     */
    public String removePrMember(String address) {
        return api.writeRemovePrMember(repositoryId(), address, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * add pull request.
     *
     * @param url pull request url
     * @return if has error return result starts with "ERROR:"
     */
    public String addPullRequest(String url) {
        return api.writeAddPullRequest(repositoryId(), url, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
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

    /**
     * disable function by type.
     *
     * @param type type
     * @return if has error return result starts with "ERROR:"
     */
    public String disableType(int type) {
        return api.writeDisableType(repositoryId(), type, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
    }

    /**
     * enable function by type.
     *
     * @param type type
     * @return if has error return result starts with "ERROR:"
     */
    public String enableType(int type) {
        return api.writeEnableType(repositoryId(), type, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress(), gasLimit(), gWei());
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

    /**
     * Get repository commit history.
     *
     * @return List[Tuple.Three[Object, String: from, Date: date, String: url]]
     */
    public List<Tuple.Three<Object, String/*from*/, Date/*date*/, String/*url*/>> listHistoryUrls() {
        return api.listHistoryUrls(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get repository members.
     *
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    public List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listMembers() {
        return api.listMembers(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get pull request members.
     *
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    public List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listPrMembers() {
        return api.listPrMembers(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get pull requests.
     *
     * @return List[Map]
     */
    public List<Map<String, Object>> listPullRequests() {
        return api.listPullRequests(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get members's pull requests.
     *
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    public List<Map<String, Object>> listAuthoredPullRequests() {
        return api.listAuthoredPullRequests(fromAddress(), contractAddress(), repositoryId());
    }

    /**
     * Get committer's pull requests.
     *
     * @return List[Tuple.Three[Object, String: member, Boolean: status]]
     */
    public List<Map<String, Object>> listCommunityPullRequests() {
        return api.listCommunityPullRequests(fromAddress(), contractAddress(), repositoryId());
    }
}
