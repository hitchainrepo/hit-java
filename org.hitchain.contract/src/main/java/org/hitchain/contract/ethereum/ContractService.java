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
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
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

    public static Web3j getWeb3j() {
        return Web3jHelper.getWeb3j();
    }
//// browse repository name contract.
//    public static void main(String[] args) throws Exception {
//        EthBlock.Block block = getWeb3j().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(5595195)), true).send().getBlock();
//        System.out.println(GsonHelper.toJsonString(block));
//        for (EthBlock.TransactionResult<EthBlock.TransactionObject> tr : block.getTransactions()) {
//            EthBlock.TransactionObject transactionObject = tr.get();
//            String input = transactionObject.getInput();
//            if (input.length() == RepositoryNameContract.repositoryNameByteCode.length() + 2 + "0000000000000000000000008ab9cff82197b9673ec6e26c41176798f88f2cb5".length()) {
//                if (RepositoryNameContract.repositoryNameByteCode.equals(input.substring(2, 2 + RepositoryNameContract.repositoryNameByteCode.length()))) {
//                    System.out.println(GsonHelper.toJsonString(tr));
//                    System.out.println(input);
//                    RequestHelper.RequestResult result = RequestHelper.get("http://api-ropsten.etherscan.io/api",
//                            MapHelper.toMap(
//                                    "module", "account",
//                                    "action", "txlist",
//                                    "address", transactionObject.getFrom(),
//                                    "startblock", transactionObject.getBlockNumber().toString(),
//                                    "endblock", transactionObject.getBlockNumber().toString(),
//                                    "sort", "desc",
//                                    "apikey", "YourApiKeyToken"
//                            ),
//                            Collections.EMPTY_MAP);
//                    System.out.println(((List<Map>) result.getBodyAsJson().get("result")).get(0).get("contractAddress"));
//                }
//            }
//        }
//    }


    /**
     * <pre>
     * data(for ethereum):
     * PrivateKey=\n
     * ContractByteCode=\n
     * GasLimit=5000000\n
     * Gwei=10\n
     * </pre>
     * ContractByteCode: without 0x
     *
     * @param data per key-value as one line: key=value.
     * @return if has error, then return result starts with "ERROR:".
     */
    public String deployContract(/*PrivateKey=\n
                                   ContractByteCode=\n
                                   GasLimit=5000000\n
                                   Gwei=10\n*/String data) {
        //https://docs.web3j.io/transactions.html#creation-of-smart-contract
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String privateKey = PreRequiredHelper.requireNotBlank((String) map.get("PrivateKey"), "Private key is required!");
            String contractByteCode = PreRequiredHelper.requireNotBlank((String) map.get("ContractByteCode"), "Contract byte code key is required!");
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
                    contractByteCode + encodedConstructor);
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

    /**
     * <pre>
     * data(for ethereum):
     * FromAddress=\n
     * ContractAddress=\n
     * FunctionName=functionName(Uint256|Address|Utf8String|Uint256Array|AddressArray|Utf8StringArray,...)\n
     * FunctionType=Uint256|Address|Bool|Utf8String\n
     * Arg*=\n
     * </pre>
     *
     * @param data per key-value as one line: key=value.
     * @return if has error, then return result starts with "ERROR:".
     * @see org.web3j.abi.datatypes.generated.AbiTypes
     */
    public String readContract(String data) {
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String fromAddress = PreRequiredHelper.requireNotBlank((String) map.get("FromAddress"), "FromAddress is required!");
            String contractAddress = PreRequiredHelper.requireNotBlank((String) map.get("ContractAddress"), "ContractAddress is required!");
            String functionName = PreRequiredHelper.requireNotBlank((String) map.get("FunctionName"), "FunctionName is required!");
            String functionType = PreRequiredHelper.requireNotBlank((String) map.get("FunctionType"), "FunctionType is required!");
            {
                functionType = "-".equals(functionType) ? "" : functionType;
            }
            TransactionHelper.FunctionCreator args = TransactionHelper.args();
            TransactionHelper.FunctionResult result = TransactionHelper.result();
            for (int i = 0; i < 1; i++) {// reduce code indent, function args and type.
                String argTypes = functionName.indexOf("(") < 1 ? "" : StringUtils.substringBefore(StringUtils.substringAfter(functionName, "("), ")").trim();
                functionName = StringUtils.substringBefore(functionName, "(").trim();
                if (StringUtils.isBlank(argTypes)) {
                    break;
                }
                String[] argTypeArr = StringUtils.split(argTypes, ",");
                for (int count = 0; count < argTypeArr.length; count++) {
                    String argType = argTypeArr[count].trim().toLowerCase();
                    if ("addressarray".equals(argType)) {
                        args.addressArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("boolarray".equals(argType)) {
                        args.addressArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("utf8stringarray".equals(argType) || "stringarray".equals(argType)) {
                        args.stringArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("dynamicbytesarray".equals(argType) || "bytesarray".equals(argType)) {
                        args.dynamicBytesArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if (argType.endsWith("array") && argType.startsWith("uint")) {
                        args.unintArray(StringUtils.remove(argType, "array"),
                                StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if (argType.endsWith("array") && argType.startsWith("bytes")) {
                        args.bytesArray(StringUtils.remove(argType, "array"),
                                StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }

                    if ("address".equals(argType)) {
                        args.address((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("bool".equals(argType)) {
                        args.bool((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("utf8string".equals(argType) || "string".equals(argType)) {
                        args.string((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("dynamicbytes".equals(argType) || "bytes".equals(argType)) {
                        args.dynamicBytes((String) map.get("Arg" + count));
                        continue;
                    }
                    if (argType.startsWith("uint")) {
                        args.uint(argType, (String) map.get("Arg" + count));
                        continue;
                    }
                    if (argType.startsWith("bytes")) {
                        args.bytes(argType, (String) map.get("Arg" + count));
                        continue;
                    }
                }
            }
            {// function return type.
                if (StringUtils.isBlank(functionType)) {
                    //
                } else {
                    result.add(TransactionHelper.getType(functionType));
                }
            }
            Function function = new Function(functionName, args.get(), result.get());
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

    /**
     * <pre>
     * data structure:
     * PrivateKey=\n
     * ContractAddress=\n
     * FunctionName=functionName(Uint256|Address|Utf8String|Uint256Array|AddressArray|Utf8StringArray,...)\n
     * FunctionType=Uint256|Address|Bool|Utf8String\n
     * Arg*=\n
     * GasLimit=500000\n
     * Gwei=10\n
     * </pre>
     *
     * @param data
     * @return if has error, then return result starts with "ERROR:".
     * @see org.web3j.abi.datatypes.generated.AbiTypes
     */
    public String writeContract(String data) {
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String privateKey = PreRequiredHelper.requireNotBlank((String) map.get("PrivateKey"), "PrivateKey is required!");
            String contractAddress = PreRequiredHelper.requireNotBlank((String) map.get("ContractAddress"), "ContractAddress is required!");
            String functionName = PreRequiredHelper.requireNotBlank((String) map.get("FunctionName"), "FunctionName is required!");
            String functionType = PreRequiredHelper.requireNotBlank((String) map.get("FunctionType"), "FunctionType is required!");
            String gasLimit = PreRequiredHelper.requireNotBlank((String) map.get("GasLimit"), "GasLimit is required!");
            String gwei = PreRequiredHelper.requireNotBlank((String) map.get("Gwei"), "Gwei is required!");
            BigInteger gasPrice = Convert.toWei(gwei, Convert.Unit.GWEI).toBigInteger();
            BigInteger gasLimits = new BigInteger(gasLimit);
            {
                functionType = "-".equals(functionType) ? "" : functionType;
            }
            Credentials credentials = TransactionHelper.getCredentials(privateKey);
            TransactionHelper.FunctionCreator args = TransactionHelper.args();
            TransactionHelper.FunctionResult result = TransactionHelper.result();
            for (int i = 0; i < 1; i++) {// reduce code indent, function args and type.
                String argTypes = functionName.indexOf("(") < 1 ? "" : StringUtils.substringBefore(StringUtils.substringAfter(functionName, "("), ")").trim();
                functionName = StringUtils.substringBefore(functionName, "(").trim();
                if (StringUtils.isBlank(argTypes)) {
                    break;
                }
                String[] argTypeArr = StringUtils.split(argTypes, ",");
                for (int count = 0; count < argTypeArr.length; count++) {
                    String argType = argTypeArr[count].trim().toLowerCase();
                    if ("addressarray".equals(argType)) {
                        args.addressArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("boolarray".equals(argType)) {
                        args.addressArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("utf8stringarray".equals(argType) || "stringarray".equals(argType)) {
                        args.stringArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if ("dynamicbytesarray".equals(argType) || "bytesarray".equals(argType)) {
                        args.dynamicBytesArray(StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if (argType.endsWith("array") && argType.startsWith("uint")) {
                        args.unintArray(StringUtils.remove(argType, "array"),
                                StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }
                    if (argType.endsWith("array") && argType.startsWith("bytes")) {
                        args.bytesArray(StringUtils.remove(argType, "array"),
                                StringUtils.split((String) map.get("Arg" + count), ","));
                        continue;
                    }

                    if ("address".equals(argType)) {
                        args.address((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("bool".equals(argType)) {
                        args.bool((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("utf8string".equals(argType) || "string".equals(argType)) {
                        args.string((String) map.get("Arg" + count));
                        continue;
                    }
                    if ("dynamicbytes".equals(argType) || "bytes".equals(argType)) {
                        args.dynamicBytes((String) map.get("Arg" + count));
                        continue;
                    }
                    if (argType.startsWith("uint")) {
                        args.uint(argType, (String) map.get("Arg" + count));
                        continue;
                    }
                    if (argType.startsWith("bytes")) {
                        args.bytes(argType, (String) map.get("Arg" + count));
                        continue;
                    }
                }
            }
            {// function return type.
                if (StringUtils.isBlank(functionType)) {
                    //
                } else {
                    result.add(TransactionHelper.getType(functionType));
                }
            }
            Function function = new Function(functionName, args.get(), result.get());
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
            TransactionReceipt receipt = null;
            try {
                receipt = JsonHelper.toObject(TransactionReceipt.class, json);
            } catch (Exception e) {
                System.out.println(e.getMessage() + ", TransactionReceipt:" + json);
                throw e;
            }
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
}
