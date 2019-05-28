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
import org.iff.infra.util.JsonHelper;
import org.iff.infra.util.PreRequiredHelper;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contract service implements ContractApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class ContractService implements ContractApi {

    private static Web3j web3j = null;

    public static Web3j getWeb3j() {
        if (web3j == null) {
            String url = "https://ropsten.infura.io";
            try {
                Method getChain = Class.forName("org.hitchain.hit.util.HitHelper").getDeclaredMethod("getChain");
                url = (String) getChain.invoke(null);
            } catch (Exception e) {
            }
            web3j = Web3j.build(new HttpService(url));
            System.out.println("Connect chain on " + url);
        }
        return web3j;
    }

    //@ApiOperation(value = "部署代码库合约", notes = "部署代码库合约")
    //@PostMapping(value = "/deployRepositoryNameContract", produces = {"text/plain;charset=UTF-8"}, consumes = {"text/plain"})
    public static String deployRepositoryNameContract(
            /*@ApiParam(value = "数据格式(数据间请勿输入空格，PrivateKey需要先加密采用默认值时为-，出错时返回'ERROR:'开头的错误信息)：\n" +
                    "PrivateKey=\n" +
                    "GasLimit=5000000\n" +
                    "Gwei=0\n")
            @RequestBody*/ String data) {
        //https://docs.web3j.io/transactions.html#creation-of-smart-contract
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String privateKey = PreRequiredHelper.requireNotBlank((String) map.get("PrivateKey"), "Private key is required!");
//            {
//                if (StringUtils.equals(privateKey, "-") && contractQueue.size() > 0) {
//                    try {
//                        return contractQueue.remove();
//                    } catch (Exception e) {
//                    }
//                }
//                privateKey = StringUtils.equals(privateKey, "-") ? PRI_KEY : privateKey;
//                privateKey = RSAHelper.decryptByDefaultKey(privateKey);
//            }
            String gasLimit = PreRequiredHelper.requireNotBlank((String) map.get("GasLimit"), "GasLimit is required!");
            String gwei = PreRequiredHelper.requireNotBlank((String) map.get("Gwei"), "Gwei is required!");
            Credentials credentials = TransactionHelper.getCredentials(privateKey);
            BigInteger gasPrice = Convert.toWei(gwei, Convert.Unit.GWEI).toBigInteger();
            BigInteger gasLimits = new BigInteger(gasLimit);
            String encodedConstructor = FunctionEncoder.encodeConstructor(TransactionHelper.args().address(credentials.getAddress()).get());
            BigInteger nonce = getWeb3j().ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING)
                    .send()
                    .getTransactionCount();
            RawTransaction rawTransaction = RawTransaction.createContractTransaction(
                    nonce,
                    gasPrice,
                    gasLimits,
                    BigInteger.ZERO,
                    RepositoryNameContract.repositoryNameByteCode + encodedConstructor);
            String signedTransactionData = Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials));
            EthSendTransaction send = getWeb3j().ethSendRawTransaction(signedTransactionData)
                    .sendAsync()
                    .get();
            if (send.hasError()) {
                return "ERROR:" + send.getError().getMessage();
            }
            String json = TransactionHelper.waitTransactionSeconds(getWeb3j(), send.getTransactionHash(), 180);
            TransactionReceipt receipt = JsonHelper.toObject(TransactionReceipt.class, json);
            if (receipt.getLogs() == null || receipt.getLogs().isEmpty()) {
                return "ERROR:May be out of gas, try 5000000gas";
            }
            return receipt.getContractAddress();
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter writer = new StringWriter();
            writer.append("ERROR: ");
            e.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
    }

    //@ApiOperation(value = "读取RepositoryName合约", notes = "读取RepositoryName合约")
    //@PostMapping(value = "/readRepositoryNameContract", produces = {"text/plain;charset=UTF-8"}, consumes = {"text/plain"})
    public static String readRepositoryNameContract(
            /*@ApiParam(value = "数据格式(数据间请勿输入空格，Arg是输入的参数，为空时设值为-，出错时返回'ERROR:'开头的错误信息)：\n" +
                    "FromAddress=\n" +
                    "ContractAddress=\n" +
                    "FunctionName=repositoryName|repositoryAddress|owner|delegator|authedAccounts(addr)|authedAccountList(int)|authedAccountSize|hasTeamMember(addr)|teamMemberAtIndex(int)\n" +
                    "Arg=-\n")
            @RequestBody*/ String data) {
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String fromAddress = PreRequiredHelper.requireNotBlank((String) map.get("FromAddress"), "FromAddress key is required!");
//            {
//                fromAddress = StringUtils.equals(fromAddress, "-") ? PUB_KEY : fromAddress;
//            }
            String contractAddress = PreRequiredHelper.requireNotBlank((String) map.get("ContractAddress"), "ContractAddress key is required!");
            String functionName = PreRequiredHelper.requireNotBlank((String) map.get("FunctionName"), "FunctionName key is required!");
            String arg = StringUtils.defaultString((String) map.get("Arg"), "");
            Function function = null;
            // readable field
            if (StringUtils.startsWith(functionName, "repositoryName")) {
                function = new Function("repositoryName", Collections.EMPTY_LIST, TransactionHelper.result().add(Utf8String.class).get());
            } else if (StringUtils.startsWith(functionName, "repositoryAddress")) {
                function = new Function("repositoryAddress", Collections.EMPTY_LIST, TransactionHelper.result().add(Utf8String.class).get());
            } else if (StringUtils.startsWith(functionName, "owner")) {
                function = new Function("owner", Collections.EMPTY_LIST, TransactionHelper.result().add(Address.class).get());
            } else if (StringUtils.startsWith(functionName, "delegator")) {
                function = new Function("delegator", Collections.EMPTY_LIST, TransactionHelper.result().add(Address.class).get());
            } else if (StringUtils.startsWith(functionName, "authedAccounts")) {
                function = new Function("authedAccounts", TransactionHelper.args().address(arg).get(), TransactionHelper.result().add(Bool.class).get());
            } else if (StringUtils.startsWith(functionName, "authedAccountList")) {
                function = new Function("authedAccounts", TransactionHelper.args().uint256(arg).get(), TransactionHelper.result().add(Address.class).get());
            } else if (StringUtils.startsWith(functionName, "authedAccountSize")) {
                function = new Function("authedAccountSize", Collections.EMPTY_LIST, TransactionHelper.result().add(Uint256.class).get());
            }
            // readable function
            else if (StringUtils.startsWith(functionName, "hasTeamMember")) {
                function = new Function("hasTeamMember", TransactionHelper.args().address(arg).get(), TransactionHelper.result().add(Bool.class).get());
            } else if (StringUtils.startsWith(functionName, "teamMemberAtIndex")) {
                function = new Function("teamMemberAtIndex", TransactionHelper.args().uint256(arg).get(), TransactionHelper.result().add(Address.class).get());
            } else {
                return "ERROR: function not found: " + functionName;
            }
            EthCall send = getWeb3j().ethCall(Transaction.createEthCallTransaction(
                    fromAddress, contractAddress, FunctionEncoder.encode(function)), DefaultBlockParameterName.LATEST).send();
            if (send.hasError()) {
                return "ERROR:" + send.getError().getMessage();
            }
            List<Type> values = FunctionReturnDecoder.decode(send.getValue(), function.getOutputParameters());
            if (values.isEmpty()) {
                return "";
            }
            return values.get(0).getValue().toString();
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter writer = new StringWriter();
            writer.append("ERROR: ");
            e.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
    }

    //@ApiOperation(value = "写RepositoryName合约", notes = "写RepositoryName合约")
    //@PostMapping(value = "/writeRepositoryNameContract", produces = {"text/plain;charset=UTF-8"}, consumes = {"text/plain"})
    public static String writeRepositoryNameContract(
            /*@ApiParam(value = "数据格式(数据间请勿输入空格，PrivateKey需要先加密采用默认值时为-，Arg是输入的参数，为空时设值为-，出错时返回'ERROR:'开头的错误信息)：\n" +
                    "PrivateKey=\n" +
                    "ContractAddress=\n" +
                    "FunctionName=init(addr,repoName)|initWithDelegator(addr,repoName,delegator)|updateRepositoryName(repoName)|updateRepositoryAddress(oldAddr,newAddr)|addTeamMember(addr)|removeTeamMember(addr)|changeOwner(addr)|delegateTo(addr)\n" +
                    "Arg1=-\n" +
                    "Arg2=-\n" +
                    "Arg3=-\n" +
                    "GasLimit=5000000\n" +
                    "Gwei=0\n")
            @RequestBody*/ String data) {
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String privateKey = PreRequiredHelper.requireNotBlank((String) map.get("PrivateKey"), "PrivateKey key is required!");
//            {
//                privateKey = StringUtils.equals(privateKey, "-") ? PRI_KEY : privateKey;
//                privateKey = RSAHelper.decryptByDefaultKey(privateKey);
//            }
            String contractAddress = PreRequiredHelper.requireNotBlank((String) map.get("ContractAddress"), "ContractAddress key is required!");
            String functionName = PreRequiredHelper.requireNotBlank((String) map.get("FunctionName"), "FunctionName key is required!");
            String arg1 = StringUtils.defaultString((String) map.get("Arg1"), "");
            String arg2 = StringUtils.defaultString((String) map.get("Arg2"), "");
            String arg3 = StringUtils.defaultString((String) map.get("Arg3"), "");
            String gasLimit = PreRequiredHelper.requireNotBlank((String) map.get("GasLimit"), "GasLimit is required!");
            String gwei = PreRequiredHelper.requireNotBlank((String) map.get("Gwei"), "Gwei is required!");
            BigInteger gasPrice = Convert.toWei(gwei, Convert.Unit.GWEI).toBigInteger();
            BigInteger gasLimits = new BigInteger(gasLimit);
            Credentials credentials = TransactionHelper.getCredentials(privateKey);
            Function function = null;
            if (StringUtils.startsWith(functionName, "initWithDelegator")) {
//                arg3 = StringUtils.equals(arg3, "-") ? PUB_KEY : arg3;
                function = new Function("initWithDelegator", TransactionHelper.args().address(arg1).string(arg2).address(arg3).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "init")) {
                function = new Function("init", TransactionHelper.args().address(arg1).string(arg2).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "updateRepositoryName")) {
                function = new Function("updateRepositoryName", TransactionHelper.args().address(arg1).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "updateRepositoryAddress")) {
                function = new Function("updateRepositoryAddress", TransactionHelper.args().string(arg1).string(arg2).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "addTeamMember")) {
                function = new Function("addTeamMember", TransactionHelper.args().address(arg1).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "removeTeamMember")) {
                function = new Function("removeTeamMember", TransactionHelper.args().address(arg1).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "changeOwner")) {
                function = new Function("changeOwner", TransactionHelper.args().address(arg1).get(), Collections.EMPTY_LIST);
            } else if (StringUtils.startsWith(functionName, "delegateTo")) {
                function = new Function("delegateTo", TransactionHelper.args().address(arg1).get(), Collections.EMPTY_LIST);
            } else {
                return "ERROR: function not found: " + functionName;
            }
            BigInteger nonce = getWeb3j().ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING)
                    .send()
                    .getTransactionCount();
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimits,
                    contractAddress,
                    BigInteger.ZERO,
                    FunctionEncoder.encode(function));
            EthSendTransaction send = getWeb3j().ethSendRawTransaction(Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, credentials)))
                    .sendAsync().get();
            if (send.hasError()) {
                return "ERROR:" + send.getError().getMessage();
            }
            String json = TransactionHelper.waitTransactionSeconds(getWeb3j(), send.getTransactionHash(), 60);
            TransactionReceipt receipt = JsonHelper.toObject(TransactionReceipt.class, json);
            if (receipt.getLogs() == null || receipt.getLogs().isEmpty()) {
                return "ERROR:May be out of gas please try 5000000gas or increase gas price, or maybe no authorization.";
            }
            return "true";
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter writer = new StringWriter();
            writer.append("ERROR: ");
            e.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
    }

    @Override
    public String deployContract(String data) {
        return deployRepositoryNameContract(data);
    }

    @Override
    public String readContract(String data) {
        return readRepositoryNameContract(data);
    }

    @Override
    public String writeContract(String data) {
        return writeRepositoryNameContract(data);
    }
}
