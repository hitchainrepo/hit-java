/*******************************************************************************
 * Copyright (c) 2019-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.contract.ethereum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.PullRequestContractEthereumApi;
import org.iff.infra.util.FCS;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static PullRequestContractEthereumApi getApi() {
        ContractApi.setInstance(PullRequestContractEthereumApi.class, new PullRequestContractEthereumService());
        return (PullRequestContractEthereumApi) ContractApi.getInstance(PullRequestContractEthereumApi.class);
    }

    public static List<Map<String, Object>> listCommunityPRs(String fromAddress, String contractAddress) {
        List<Map<String, Object>> pullRequests = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create();
        {
            List<String> hashes = new ArrayList<>();
            int count = getApi().readCommunityPullRequestCount(fromAddress, contractAddress);
            for (int i = 0; i < 100 && count > 0; i++, count--) {
                String hash = getApi().readCommunityPullRequest(fromAddress, contractAddress, count);
                hashes.add(hash);
            }
            Method readFileFromIpfs = null;
            try {
                Class<?> cls = Class.forName("org.hitchain.hit.util.GitHelper");
                readFileFromIpfs = cls.getDeclaredMethod("readFileFromIpfs", String.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (String hash : hashes) {
                try {
                    byte[] bs = (byte[]) readFileFromIpfs.invoke(null, hash);
                    String json = new String(bs, "UTF-8").trim();
                    if (json.startsWith("[") && json.endsWith("]")) {
                        List<Map<String, Object>> list = gson.fromJson(json, ArrayList.class);
                        pullRequests.addAll(list);
                    }
                } catch (Exception e) {
                    System.err.println("Can read pull request content: " + hash);
                }
            }
        }
        return pullRequests;
    }

    public static List<Map<String, Object>> listAuthedPRs(String fromAddress, String contractAddress) {
        List<Map<String, Object>> pullRequests = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create();
        {
            List<String> hashes = new ArrayList<>();
            int count = getApi().readAuthedPullRequestCount(fromAddress, contractAddress);
            for (int i = 0; i < 100 && count > 0; i++, count--) {
                String hash = getApi().readAuthedPullRequest(fromAddress, contractAddress, count);
                hashes.add(hash);
            }
            Method readFileFromIpfs = null;
            try {
                Class<?> cls = Class.forName("org.hitchain.hit.util.GitHelper");
                readFileFromIpfs = cls.getDeclaredMethod("readFileFromIpfs", String.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (String hash : hashes) {
                try {
                    byte[] bs = (byte[]) readFileFromIpfs.invoke(null, hash);
                    String json = new String(bs, "UTF-8").trim();
                    if (json.startsWith("[") && json.endsWith("]")) {
                        List<Map<String, Object>> list = gson.fromJson(json, ArrayList.class);
                        pullRequests.addAll(list);
                    }
                } catch (Exception e) {
                    System.err.println("Can read pull request content: " + hash);
                }
            }
        }
        return pullRequests;
    }

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

    public String listCommunityPR(String fromAddress, String contractAddress) {
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> list = listAuthedPRs(fromAddress, contractAddress);
        for (Map<String, Object> map : list) {
            sb.append(StringUtils.leftPad((String) map.get("id"), 50, ' '));
            sb.append(StringUtils.leftPad(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(map.get("date")), 40, ' '));
            sb.append(map.get("message")).append('\n');
        }
        return sb.toString();
    }

    public String listAuthedPR(String fromAddress, String contractAddress) {
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> list = listAuthedPRs(fromAddress, contractAddress);
        for (Map<String, Object> map : list) {
            sb.append(StringUtils.rightPad((String) map.get("id"), 42, ' '));
            sb.append(StringUtils.rightPad(
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(
                            DateUtils.parseDate((String) map.get("date"), new String[]{"EEE, dd MMM yyyy HH:mm:ss Z"})), 27, ' '));
            String message = (String) map.get("message");
            message = StringUtils.substringBefore(message, "\n");
            sb.append(message).append('\n');
        }
        return sb.toString();
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
