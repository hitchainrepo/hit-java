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
import org.iff.infra.util.*;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contract service implements ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class RepositoryContractEthereumService extends ContractService implements RepositoryContractEthereumApi {

    public static final String CONTRACT_CREATE = "PrivateKey={0}\nContractByteCode={1}\nGasLimit={2}\nGwei={3}\n";
    public static final String CONTRACT_READ = "FromAddress={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\n";
    public static final String CONTRACT_WRITE = "PrivateKey={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\nArg1={5}\nArg2={6}\nGasLimit={7}\nGwei={8}\n";

    public static void main(String[] args) {
        RepositoryContractEthereumService service = new RepositoryContractEthereumService();
        String key = InputHelper.getContent("Private Key:");
        String from = InputHelper.getContent("Public address:");
        String from2 = InputHelper.getContent("Team Member address:");
        String contract = service.deployContract(key, 5000000, 10);
        System.out.println("Contract:" + contract);
        if (ContractApi.isError(contract)) {
            return;
        }
        {
            String writeInitWithDelegator = service.writeInitWithDelegator(from, "helloworld", from2, key, contract, 5000000, 10);
            System.out.println("InitWithDelegator:" + writeInitWithDelegator);
            System.out.println("Owner:" + service.readOwner(from, contract));
            System.out.println("Delegateor:" + service.readDelegator(from, contract));
            System.out.println("RepositoryName:" + service.readRepositoryName(from, contract));
        }
        {
            String old = service.readRepositoryAddress(from, contract);
            System.out.println("OldRepositoryAddress:" + old);
            String writeUpdateRepositoryAddress = service.writeUpdateRepositoryAddress(StringUtils.defaultIfBlank(old, "-"), "helloworld-" + IdHelper.uuid(), key, contract, 5000000, 10);
            System.out.println("UpdateRepositoryAddress:" + writeUpdateRepositoryAddress);
            System.out.println("RepositoryAddress:" + service.readRepositoryAddress(from, contract));
        }
        {
            String writeAddTeamMember = service.writeAddTeamMember(from2, key, contract, 5000000, 10);
            System.out.println("AddTeamMember:" + writeAddTeamMember);
            int count = service.readTeamMemberCount(from, contract);
            System.out.println("readTeamMemberCount:" + count);
            for (int i = count; i > 0; i--) {
                String address = service.readTeamMemberListIndex(from, contract, i);
                System.out.println("readTeamMemberListIndex-" + i + ":" + address);
                System.out.println("readTeamMember-" + address + ":" + service.readTeamMember(from, contract, address));
            }
        }
        {
            String writeRemoveTeamMember = service.writeRemoveTeamMember(from2, key, contract, 5000000, 10);
            System.out.println("RemoveTeamMember:" + writeRemoveTeamMember);
            int count = service.readTeamMemberCount(from, contract);
            System.out.println("readTeamMemberCount:" + count);
            for (int i = count; i > 0; i--) {
                String address = service.readTeamMemberListIndex(from, contract, i);
                System.out.println("readTeamMemberListIndex-" + i + ":" + address);
                System.out.println("readTeamMember-" + address + ":" + service.readTeamMember(from, contract, address));
            }
        }
        {
            String writeUpdatePullRequestAddress = service.writeUpdatePullRequestAddress(contract, key, contract, 5000000, 10);
            System.out.println("UpdatePullRequestAddress:" + writeUpdatePullRequestAddress);
            System.out.println("PullRequestAddress:" + service.readPullRequestAddress(from, contract));
        }
    }

    public static RepositoryContractEthereumApi getApi() {
        ContractApi.setInstance(new RepositoryContractEthereumService());
        return (RepositoryContractEthereumApi) ContractApi.getInstance();
    }

    @Override
    public String readRepositoryName(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "repositoryName", "string", "").toString();
        return readContract(data);
    }

    @Override
    public String readRepositoryAddress(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "repositoryAddress", "string", "").toString();
        return readContract(data);
    }

    @Override
    public String readPullRequestAddress(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "pullRequestAddress", "address", "").toString();
        return readContract(data);
    }

    @Override
    public String readOwner(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "owner", "address", "").toString();
        return readContract(data);
    }

    @Override
    public String readDelegator(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "delegator", "address", "").toString();
        return readContract(data);
    }

    @Override
    public boolean readTeamMember(String fromAddress, String contractAddress, String memberAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "teamMember(address)", "bool", memberAddress).toString();
        String result = readContract(data);
        return "true".equals(result);
    }

    @Override
    public String readTeamMemberListIndex(String fromAddress, String contractAddress, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "teamMemberList(uint256)", "address", index).toString();
        return readContract(data);
    }

    @Override
    public int readTeamMemberCount(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "teamMemberCount", "uint256", "").toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }

    @Override
    public String readHistoryRepositoryAddress(String contractAddress) {
        return readHistoryRepositoryAddress0(contractAddress, new AtomicInteger(1), new AtomicInteger(1));
    }

    @Override
    public String writeInit(String ownerAddress, String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "init(address,string)", "-", ownerAddress, repositoryName, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeInitWithDelegator(String ownerAddress, String repositoryName, String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "initWithDelegator(address,string,address)", "-", ownerAddress, repositoryName, delegatorAddress, gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateRepositoryName(String repositoryName, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateRepositoryName(string)", "-", repositoryName, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateRepositoryAddress(String oldRepositoryAddress, String newRepositoryAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateRepositoryAddress(string,string)", "-", oldRepositoryAddress, newRepositoryAddress, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdatePullRequestAddress(String pullRequestAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updatePullRequestAddress(address)", "-", pullRequestAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addTeamMember(address)", "-", memberAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemoveTeamMember(String memberAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeTeamMember(address)", "-", memberAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeChangeOwner(String ownerAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "changeOwner(address)", "-", ownerAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeDelegateTo(String delegatorAddress, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "delegateTo(address)", "-", delegatorAddress, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String deployContract(String privateKey, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_CREATE, privateKey, RepositoryNameContract.repositoryNameByteCode, gasLimit, gWei).toString();
        return deployContract(data);
    }

    /**
     * @param contractAddress
     * @param page
     * @param extraRequestTimes
     * @return
     */
    public String readHistoryRepositoryAddress0(String contractAddress, AtomicInteger page, AtomicInteger extraRequestTimes) {
        RequestHelper.RequestResult result = RequestHelper.get("http://api-ropsten.etherscan.io/api",
                MapHelper.toMap(
                        "module", "account",
                        "action", "txlist",
                        "address", contractAddress,
                        "startblock", "0",
                        "endblock", "99999999",
                        "page", String.valueOf(page.getAndIncrement()),
                        "offset", "100",
                        "sort", "desc",
                        "apikey", "YourApiKeyToken"
                ),
                Collections.EMPTY_MAP);
        // Result:
        // {"status":"1","message":"OK","result":[
        //  {"blockNumber":"5606491","timeStamp":"1557974025",
        //   "hash":"0x4dcd95b99c1afc14921584b63f7f1e24d83641c5f96b92b3c3cd371aba5f61eb","nonce":"17",
        //   "blockHash":"0x6cfaf0d3e7210898c580e59d99ce2671b98dec1207498b2be2317e9b11297598","transactionIndex":"0",
        //   "from":"0x8ab9cff82197b9673ec6e26c41176798f88f2cb5","to":"0x8b20bbe008d7b3dc0ad8d0a74c9cdf7772b0eee1",
        //   "value":"0","gas":"500000","gasPrice":"10000000000","isError":"0","txreceipt_status":"1",
        //   "input":"0x2d3156f6000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000002e516d53444653434d443153745a783947796e596f717974596e34556b31506a315031746b7936366b6770346d736f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002e516d64506e7a7738324357714c33585775677139434e66323759625242624d31473667343257674c43516a484b4d000000000000000000000000000000000000",
        //   "contractAddress":"","cumulativeGasUsed":"47709","gasUsed":"47709","confirmations":"241"}]
        Map<String, Object> transactions = result.getBodyAsJson();
        int resultSize = 0;
        {
            if (transactions != null && !transactions.isEmpty()) {
                List<Map> results = (List<Map>) transactions.get("result");
                resultSize = results != null ? results.size() : 0;
            }
        }
        List<Tuple.Four<Object/*result*/, String/*methodName*/, List<Type>/*inputParameter*/, String/*from*/, Date/*timestamp*/>> list =
                TransactionHelper.decodeInput(transactions, new Function(
                        "updateRepositoryAddress",
                        TransactionHelper.args().string("-").string("-").get(),
                        TransactionHelper.result().add(Utf8String.class).add(Utf8String.class).get()));
        StringBuilder sb = new StringBuilder();
        for (Tuple.Four<Object, String, List<Type>, String, Date> four : list) {
            List<Type> types = four.second();
            if (types == null || types.size() != 2) {
                continue;
            }
            sb.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(four.fourth()))
                    .append("    ").append(four.third())
                    .append("    ").append(types.get(1)).append("\n");
        }
        if (resultSize == 100 && list.size() < 20 && extraRequestTimes.getAndDecrement() > 0) {
            sb.append(readHistoryRepositoryAddress0(contractAddress, page, extraRequestTimes));
        }
        return sb.toString();
    }
}
