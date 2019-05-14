/*******************************************************************************
 * Copyright (c) 2018-08-07 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;

import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.Assert;
import org.iff.infra.util.GsonHelper;
import org.iff.infra.util.JsonHelper;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * FunctionHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-08-07
 * auto generate by qdp.
 */
public class TransactionHelper {

    public static FunctionCreator args() {
        return new FunctionCreator();
    }

    public static FunctionResult result() {
        return new FunctionResult();
    }

    public static Map<String, Object> parseData(String data) {
        Map<String, Object> map = new HashMap<String, Object>();
        String[] lines = StringUtils.split(StringUtils.trim(data), "\n");
        List<List<String>> arrs = new ArrayList<List<String>>();
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            int eqIndex = line.indexOf('=');
            if (eqIndex > 0) {
                //Assert.isTrue(eqIndex != line.length() - 1, "data is not key-value pair: " + line);
                String[] kv = new String[]{line.substring(0, eqIndex), eqIndex == line.length() - 1 ? "" : line.substring(eqIndex + 1)};
                map.put(kv[0], StringUtils.trim(kv[1]));
            } else {
                String[] values = StringUtils.split(line, ",");
                Assert.isTrue(arrs.isEmpty() || arrs.size() == values.length, "list data is not same size: " + line);
                if (arrs.isEmpty()) {
                    for (int i = 0; i < values.length; i++) {
                        arrs.add(new ArrayList<String>());
                    }
                }
                for (int i = 0; i < values.length; i++) {
                    arrs.get(i).add(values[i]);
                }
            }
        }
        map.put("ListArray", arrs);
        return map;
    }

    public static Credentials getCredentials(String privateKey) {
        BigInteger key = new BigInteger(privateKey, 16);
        ECKeyPair ecKeyPair = ECKeyPair.create(key.toByteArray());
        Credentials credentials = Credentials.create(ecKeyPair);
        return credentials;
    }

    /**
     * 等待
     *
     * @param web3j
     * @param transactionHash
     * @param seconds
     * @return
     */
    public static String waitTransactionSeconds(Web3j web3j, String transactionHash, long seconds) {
        long waitTime = (seconds < 1 ? 180 : seconds) * 1000, start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < waitTime) {
            try {
                EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
                if (receipt.getTransactionReceipt().isPresent()) {
                    String jsonString = GsonHelper.toJsonString(receipt.getTransactionReceipt().get());
                    return jsonString;
                }
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception e) {
            }
        }
        return "{\"ERROR\": \"Get Transaction status timeout(180s):\"} " + transactionHash;
    }

    /**
     * 输出会话信息。
     *
     * @param receipt
     * @return
     */
    public static String transactionReceiptToString(TransactionReceipt receipt) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(receipt.getStatus() == null || "0x1".equals(receipt.getStatus()) ? "[SUCCESS]" : "[FAILED]").append(receipt.getTransactionHash()).append("\n")
                .append("         TX:   ").append("https://ropsten.etherscan.io/tx/").append(receipt.getTransactionHash()).append("\n")
                .append("         From: ").append(receipt.getFrom()).append("\n")
                .append("         To:   ").append(receipt.getTo()).append("\n")
                .append("         Gas:  ").append(new BigDecimal(receipt.getGasUsed()).divide(Convert.Unit.ETHER.getWeiFactor()).toPlainString()).append("\n");
        return sb.toString();
    }

    /**
     * 输出会话信息。
     *
     * @param transactionReceiptJson
     * @return
     */
    public static String transactionReceiptToString(String transactionReceiptJson) {
        try {
            TransactionReceipt receipt = JsonHelper.toObject(TransactionReceipt.class, transactionReceiptJson);
            StringBuilder sb = new StringBuilder(1024);
            sb.append("0x1".equals(receipt.getStatus()) ? "[SUCCESS]" : "[FAILED]").append(receipt.getTransactionHash()).append("\n")
                    .append("         TX:   ").append("https://ropsten.etherscan.io/tx/").append(receipt.getTransactionHash()).append("\n")
                    .append("         From: ").append(receipt.getFrom()).append("\n")
                    .append("         To:   ").append(receipt.getTo()).append("\n")
                    .append("         Gas:  ").append(new BigDecimal(receipt.getGasUsed()).divide(Convert.Unit.ETHER.getWeiFactor()).toPlainString()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return transactionReceiptJson;
        }
    }

    /**
     * 构造函数参数。
     */
    public static class FunctionCreator {
        private String functionName;
        private List<Type> arguments = new ArrayList<Type>();

        public FunctionCreator address(String address) {
            arguments.add(new org.web3j.abi.datatypes.Address(address));
            return this;
        }

        public FunctionCreator string(String content) {
            arguments.add(new org.web3j.abi.datatypes.Utf8String(content));
            return this;
        }

        public FunctionCreator uint256(BigInteger amount) {
            arguments.add(new org.web3j.abi.datatypes.generated.Uint256(amount));
            return this;
        }

        public FunctionCreator uint256(String amount) {
            arguments.add(new org.web3j.abi.datatypes.generated.Uint256(new BigInteger(amount)));
            return this;
        }

        public FunctionCreator object(Type type) {
            arguments.add(type);
            return this;
        }

        public FunctionCreator addressArray(List<String> addresses) {
            arguments.add(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                    org.web3j.abi.Utils.typeMap(addresses, org.web3j.abi.datatypes.Address.class)));
            return this;
        }

        public FunctionCreator addressArray(String... addresses) {
            arguments.add(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                    org.web3j.abi.Utils.typeMap(Arrays.asList(addresses), org.web3j.abi.datatypes.Address.class)));
            return this;
        }

        public FunctionCreator unint256Array(List<BigInteger> amounts) {
            arguments.add(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                    org.web3j.abi.Utils.typeMap(amounts, org.web3j.abi.datatypes.generated.Uint256.class)));
            return this;
        }

        public FunctionCreator unint256Array(String... amounts) {
            List<BigInteger> list = new ArrayList<>();
            for (String amount : amounts) {
                list.add(new BigInteger(amount));
            }
            arguments.add(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                    org.web3j.abi.Utils.typeMap(list, org.web3j.abi.datatypes.generated.Uint256.class)));
            return this;
        }

        public <T> FunctionCreator arrayByType(List<BigInteger> amounts, Class<Type> type) {
            arguments.add(new org.web3j.abi.datatypes.DynamicArray<Type>(org.web3j.abi.Utils.typeMap(amounts, type)));
            return this;
        }

        public List<Type> get() {
            return arguments;
        }
    }

    /**
     * 返回类型参数。
     */
    public static class FunctionResult {
        private List<TypeReference<?>> resultType = new ArrayList();

        public FunctionResult add(Class<? extends Type> cls) {
            resultType.add(new TypeReference<Type>() {
                public java.lang.reflect.Type getType() {
                    return cls;
                }
            });
            return this;
        }

        public List<TypeReference<?>> get() {
            return resultType;
        }
    }
}
