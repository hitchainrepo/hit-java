/*******************************************************************************
 * Copyright (c) 2018-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.iff.infra.util.FCS;
import org.iff.infra.util.MapHelper;
import org.iff.infra.util.RequestHelper;
import org.iff.infra.util.Tuple;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contract service implements ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class HitRepositoryContractEthereumService extends ContractService implements HitRepositoryContractEthereumApi {

    public static final String CONTRACT_CREATE = "PrivateKey={0}\nContractByteCode={1}\nGasLimit={2}\nGwei={3}\n";
    public static final String CONTRACT_READ = "FromAddress={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\nArg1={5}\nArg2={6}\n";
    public static final String CONTRACT_WRITE = "PrivateKey={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\nArg1={5}\nArg2={6}\nGasLimit={7}\nGwei={8}\n";

    public static HitRepositoryContractEthereumApi getApi() {
        ContractApi.setInstance(HitRepositoryContractEthereumApi.class, new HitRepositoryContractEthereumService());
        return (HitRepositoryContractEthereumApi) ContractApi.getInstance(HitRepositoryContractEthereumApi.class);
    }

    @Override
    public String readName(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "name", "string").toString();
        return readContract(data);
    }

    @Override
    public String readEmail(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "email", "string").toString();
        return readContract(data);
    }

    @Override
    public int readId(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id", "uint256").toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }

    @Override
    public String readOwner(String fromAddress, String contractAddress) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "owner", "address").toString();
        return readContract(data);
    }

    @Override
    public int readIdByName(String fromAddress, String contractAddress, String repository) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "repositoryId(string)", "uint256", repository).toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }

    @Override
    public int readIdByHash(String fromAddress, String contractAddress, BigInteger hash) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "hash_id(uint256)", "uint256", hash == null ? "0" : hash.toString()).toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }

    @Override
    public String readRepositoryById(String fromAddress, String contractAddress, int id) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_name(uint256)", "string", id).toString();
        return readContract(data);
    }

    @Override
    public String readUrlById(String fromAddress, String contractAddress, int id) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_url(uint256)", "string", id).toString();
        return readContract(data);
    }

    @Override
    public boolean readHasAddress(String fromAddress, String contractAddress, int id, int type, String address) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_type_address(uint256,uint256,address)", "bool", id, type, address).toString();
        String result = readContract(data);
        return "true".equals(result);
    }

    @Override
    public int readTypeCount(String fromAddress, String contractAddress, int id, int type) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_type_count(uint256,uint256)", "uint256", id, type).toString();
        String result = readContract(data);
        return new BigInteger(result).intValue();
    }

    @Override
    public String readAddressByTypeIndex(String fromAddress, String contractAddress, int id, int type, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_type_count_address(uint256,uint256,uint256)", "address", id, type, index).toString();
        return readContract(data);
    }

    @Override
    public String readStringByTypeIndex(String fromAddress, String contractAddress, int id, int type, int index) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_type_count_string(uint256,uint256,uint256)", "string", id, type, index).toString();
        return readContract(data);
    }

    @Override
    public boolean readDisableByType(String fromAddress, String contractAddress, int id, int type) {
        String data = FCS.get(CONTRACT_READ, fromAddress, contractAddress, "id_type_disable(uint256,uint256)", "bool", id, type).toString();
        String result = readContract(data);
        return "true".equals(result);
    }

    @Override
    public String writeUpdateName(String name, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateName(string)", "-", name, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateEmail(String email, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateEmail(string)", "-", email, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddRepository(String repository, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addRepository(string)", "-", repository, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateRepository(String repository, String newRepository, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateRepository(string,string)", "-", repository, newRepository, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeUpdateUrl(int id, String url, String newUrl, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "updateUrl(uint256,string,string)", "-", id, url, newUrl, gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddDelegator(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addDelegator(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemoveDelegator(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeDelegator(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addMember(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemoveMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeMember(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddPrMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addPrMember(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemovePrMember(int id, String address, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removePrMember(uint256,address)", "-", id, address, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddPullRequest(int id, String url, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addPullRequest(uint256,string)", "-", id, url, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeAddStarted(String url, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "addStarted(string)", "-", url, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeRemoveStarted(int index, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "removeStarted(uint256)", "-", index, "-", "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeDisableType(int id, int type, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "disableType(uint256,uint256)", "-", id, type, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String writeEnableType(int id, int type, String privateKey, String contractAddress, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_WRITE, privateKey, contractAddress, "enableType(uint256,uint256)", "-", id, type, "-", gasLimit, gWei).toString();
        return writeContract(data);
    }

    @Override
    public String deployContract(String privateKey, long gasLimit, long gWei) {
        String data = FCS.get(CONTRACT_CREATE, privateKey, HitRepositoryContract.bytes, gasLimit, gWei).toString();
        return deployContract(data);
    }

    //================================================================================

    @Override
    public List<Tuple.Three<Object, String/*contract*/, Integer/*id*/, String/*name*/>> listRepositories(String fromAddress, String contractAddress) {
        List<Tuple.Three<Object, String/*contract*/, Integer/*id*/, String/*name*/>> list = new ArrayList<>();
        int id = readId(fromAddress, contractAddress);
        for (int i = id; i >= 1; i--) {
            String repository = readRepositoryById(fromAddress, contractAddress, i);
            list.add(new Tuple.Three<>(contractAddress, i, repository));
        }
        return list;
    }

    @Override
    public List<Tuple.Three<Object, String/*from*/, Date/*date*/, String/*url*/>> listHistoryUrls(String fromAddress, String contractAddress, int id) {
        return listHistoryUrls0(contractAddress, new AtomicInteger(1), new AtomicInteger(100));
    }

    @Override
    public List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listMembers(String fromAddress, String contractAddress, int id) {
        List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> list = new ArrayList<>();
        int count = readTypeCount(fromAddress, contractAddress, id, TYPE_MEMBER);
        for (int i = count; i >= 1; i--) {
            String address = readAddressByTypeIndex(fromAddress, contractAddress, id, TYPE_MEMBER, i);
            boolean status = readHasAddress(fromAddress, contractAddress, id, TYPE_MEMBER, address);
            list.add(new Tuple.Two<>(address, status));
        }
        return list;
    }

    @Override
    public List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> listPrMembers(String fromAddress, String contractAddress, int id) {
        List<Tuple.Two<Object, String/*member*/, Boolean/*status*/>> list = new ArrayList<>();
        int count = readTypeCount(fromAddress, contractAddress, id, TYPE_PR_MEMBER);
        for (int i = count; i >= 1; i--) {
            String address = readAddressByTypeIndex(fromAddress, contractAddress, id, TYPE_PR_MEMBER, i);
            boolean status = readHasAddress(fromAddress, contractAddress, id, TYPE_PR_MEMBER, address);
            list.add(new Tuple.Two<>(address, status));
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listPullRequests(String fromAddress, String contractAddress, int id) {
        List<Map<String, Object>> list = new ArrayList<>();
        {
            List<Map<String, Object>> authPrs = listPullRequests0(fromAddress, contractAddress, id, TYPE_PR_AUTH);
            List<Map<String, Object>> commPrs = listPullRequests0(fromAddress, contractAddress, id, TYPE_PR_COMM);
            for (Map<String, Object> map : authPrs) {
                map.put("pr_type", "authored");
                list.add(map);
            }
            for (Map<String, Object> map : commPrs) {
                map.put("pr_type", "community");
                list.add(map);
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> listAuthoredPullRequests(String fromAddress, String contractAddress, int id) {
        return listPullRequests0(fromAddress, contractAddress, id, TYPE_PR_AUTH);
    }

    @Override
    public List<Map<String, Object>> listCommunityPullRequests(String fromAddress, String contractAddress, int id) {
        return listPullRequests0(fromAddress, contractAddress, id, TYPE_PR_COMM);
    }

    private List<Map<String, Object>> listPullRequests0(String fromAddress, String contractAddress, int id, int type) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        {
            int count = readTypeCount(fromAddress, contractAddress, id, type);
            for (int i = count; i >= 1 && i >= count - 100; i--) {// the latest 100 pull requests.
                String url = readStringByTypeIndex(fromAddress, contractAddress, id, type, i);
                urls.add(url);
            }
        }
        Method readFileFromIpfs = null;
        {
            try {
                Class<?> cls = Class.forName("org.hitchain.hit.util.GitHelper");
                readFileFromIpfs = cls.getDeclaredMethod("readFileFromIpfs", String.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create();
        for (String url : urls) {
            try {
                byte[] bs = (byte[]) readFileFromIpfs.invoke(null, url);
                String json = new String(bs, "UTF-8").trim();
                if (json.startsWith("[") && json.endsWith("]")) {
                    List<Map<String, Object>> prs = gson.fromJson(json, ArrayList.class);
                    list.addAll(prs);
                }
            } catch (Exception e) {
                System.err.println("Can read pull request content: " + url);
            }
        }
        return list;
    }

    /**
     * @param contractAddress
     * @param page
     * @param extraRequestTimes
     * @return
     */
    private List<Tuple.Three<Object, String/*from*/, Date/*date*/, String/*url*/>> listHistoryUrls0(String contractAddress, AtomicInteger page, AtomicInteger extraRequestTimes) {
        List<Tuple.Three<Object, String/*from*/, Date/*date*/, String/*url*/>> resultList = new ArrayList<>();
        RequestHelper.RequestResult result = RequestHelper.get(Web3jHelper.getChainApiUrl(),
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
                        "updateUrl",
                        TransactionHelper.args().uint256("1").string("-").string("-").get(),
                        TransactionHelper.result().get()));
        for (Tuple.Four<Object, String, List<Type>, String, Date> four : list) {
            List<Type> types = four.second();
            if (types == null || types.size() != 3) {
                continue;
            }
            resultList.add(new Tuple.Three<>(four.third(), four.fourth(), types.get(2).toString()));
        }
        if (resultSize == 100 && resultList.size() < 20 && extraRequestTimes.getAndDecrement() > 0) {
            resultList.addAll(listHistoryUrls0(contractAddress, page, extraRequestTimes));
        }
        return resultList;
    }
}
