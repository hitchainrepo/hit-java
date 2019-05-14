/*******************************************************************************
 * Copyright (c) 2018-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;


import org.apache.commons.lang3.StringUtils;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.RepositoryContractEthereumApi;
import org.iff.infra.util.FCS;
import org.iff.infra.util.NumberHelper;

/**
 * Contract service implements ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class RepositoryContractEthereumService extends ContractService implements RepositoryContractEthereumApi {

    public static final String CONTRACT_READ = "FromAddress={0}\nContractAddress={1}\nFunctionName={2}\nArg={3}\n";
    public static final String CONTRACT_WRITE = "PrivateKey={0}\nContractAddress={1}\nFunctionName={2}\nArg1={3}\nArg2={4}\nArg3={5}\nGasLimit={6}\nGwei={7}\n";
    public static final String CONTRACT_DEPLOY = "PrivateKey={0}\nGasLimit={6}\nGwei={7}\n";

    public static RepositoryContractEthereumApi getApi() {
        ContractApi.setInstance(new RepositoryContractEthereumService());
        return (RepositoryContractEthereumApi) ContractApi.getInstance();
    }

    @Override
    public String readRepositoryName(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "repositoryName", "-").toString();
        return readContract(data);
    }

    @Override
    public String readRepositoryAddress(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "repositoryAddress", "-").toString();
        return readContract(data);
    }

    @Override
    public String readOwner(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "owner", "-").toString();
        return readContract(data);
    }

    @Override
    public String readDelegator(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "delegator", "-").toString();
        return readContract(data);
    }

    @Override
    public boolean readAuthedAccounts(String fromAddress, String contractAddress, String memberAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccounts(addr)", memberAddress).toString();
        return Boolean.TRUE.equals(readContract(data));
    }

    @Override
    public String readAuthedAccountList(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccountList(int)", index).toString();
        return readContract(data);
    }

    @Override
    public int readAuthedAccountSize(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "authedAccountSize", "-").toString();
        return NumberHelper.getInt(readContract(data), 0);
    }

    @Override
    public boolean readHasTeamMember(String fromAddress, String contractAddress, String memberAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "hasTeamMember(addr)", memberAddress).toString();
        return Boolean.TRUE.equals(readContract(data));
    }

    @Override
    public String readTeamMemberAtIndex(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "teamMemberAtIndex(int)", index).toString();
        return readContract(data);
    }

    @Override
    public String writeInit(String ownerAddress, String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "init(addr,repoName)", ownerAddress, repositoryName, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeInitWithDelegator(String ownerAddress, String repositoryName, String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "initWithDelegator(addr,repoName,delegator)", ownerAddress, repositoryName, delegatorAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateRepositoryName(String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateRepositoryName(repoName)", repositoryName, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateRepositoryAddress(String oldRepositoryAddress, String newRepositoryAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateRepositoryAddress(oldAddr,newAddr)", StringUtils.defaultString(oldRepositoryAddress, "-"), newRepositoryAddress, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addTeamMember(addr)", memberAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemoveTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeTeamMember(addr)", memberAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeChangeOwner(String ownerAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "changeOwner(addr)", ownerAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeDelegateTo(String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "delegateTo(addr)", delegatorAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String deployContract(String privateKey, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_DEPLOY, privateKey, gasLimit, gWei).toString();
        return deployContract(data);
    }
}
