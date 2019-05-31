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
import org.bouncycastle.util.encoders.Hex;
import org.iff.infra.util.*;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.AbiTypes;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public static ParameterType type() {
        return new ParameterType();
    }

    public static ParameterType type(List<Type> types) {
        ParameterType parameterType = new ParameterType();
        for (Type type : types) {
            parameterType.add(type.getClass());
        }
        return parameterType;
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

    public static void main(String[] args) {
        RequestHelper.RequestResult result = RequestHelper.get("http://api-ropsten.etherscan.io/api",
                MapHelper.toMap(
                        "module", "account",
                        "action", "txlist",
                        "address", "0x8b20bbe008d7b3dc0ad8d0a74c9cdf7772b0eee1",
                        "startblock", "0",
                        "endblock", "99999999",
                        "page", "1",
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
        Map<String, Object> bodyAsJson = result.getBodyAsJson();
        System.out.println(decodeInput(bodyAsJson, new Function(
                "updateRepositoryAddress",
                args().string("-").string("-").get(),
                result().add(Utf8String.class).add(Utf8String.class).get())));
        System.out.println(new Address("0x0"));
    }

    public static int transactionResultSize(Map transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return 0;
        }
        List<Map> results = (List<Map>) transactions.get("result");
        return results != null ? results.size() : 0;
    }

    public static List<Tuple.Four<Object/*result*/, String/*methodName*/, List<Type>/*inputParameter*/, String/*from*/, Date/*timestamp*/>> decodeInput(Map transactions, Function function) {
        List<Tuple.Four<Object, String, List<Type>, String, Date>> list = new ArrayList<>();
        if (transactions == null || transactions.isEmpty()) {
            return list;
        }
        List<Map> results = (List<Map>) transactions.get("result");
        if (results == null || results.isEmpty()) {
            return list;
        }
        String functionId = buildMethodId(buildMethodSignature(function.getName(), function.getInputParameters()));
        for (Map result : results) {
            String rawInput = (String) result.get("input");
            String from = (String) result.get("from");
            String to = (String) result.get("to");
            String timeStamp = (String) result.get("timeStamp");
            boolean isSuccess = "0".equals(result.get("isError"));
            boolean txReceiptStatus = "1".equals(result.get("txreceipt_status"));
            if (!(isSuccess && txReceiptStatus && StringUtils.isNotBlank(to))) {
                continue;
            }
            String methodId = rawInput.substring(0, 10);
            if (!StringUtils.equals(functionId, methodId)) {
                continue;
            }
            Tuple.Two<Object, String, List<Type>> two = decodeInput(rawInput, function);
            list.add(new Tuple.Four<>(two.first(), two.second(), from, new Date(NumberHelper.getLong(timeStamp, 0) * 1000)));
        }
        return list;
    }

    /**
     * decode transaction raw data to method and inputParameter data.
     *
     * @param input    the transaction raw input
     * @param function the contract function, NOTE: the outputParameters is not use, can set to any value.
     * @return Tuple.Two{methodName, inputParameter}
     */
    public static Tuple.Two<Object/*result*/, String/*methodName*/, List<Type>/*inputParameter*/> decodeInput(String input, Function function) {
        String methodId = input.substring(0, 10);
        input = input.substring(10);
        //
        List<Type> types = FunctionReturnDecoder.decode(input, type(function.getInputParameters()).get());
        //        String methodSignature = buildMethodSignature("updateRepositoryAddress", args().string("-").string("-").get());
        //        String methodId = buildMethodId(methodSignature);
        //        System.out.println(methodId);
        //
        //        String method = inputData.substring(0,10);
        //        System.out.println(method);
        //        String to = inputData.substring(10,74);
        //        String value = inputData.substring(74);
        //        Method refMethod = TypeDecoder.class.getDeclaredMethod("decode",String.class,int.class,Class.class);
        //        refMethod.setAccessible(true);
        //        Address address = (Address)refMethod.invoke(null,to,0,Address.class);
        //        System.out.println(address.toString());
        //        Uint256 amount = (Uint256) refMethod.invoke(null,value,0,Uint256.class);
        //        System.out.println(amount.getValue());
        //
        //FunctionEncoder.encode(function);
        //String functionId = buildMethodId(buildMethodSignature(function.getName(), function.getInputParameters()));
        return new Tuple.Two<>(function.getName(), types);
    }

    public static String buildMethodSignature(String methodName, List<Type> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        String params = parameters.stream()
                .map(Type::getTypeAsString)
                .collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }

    public static String buildMethodId(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = Hash.sha3(input);
        return Numeric.toHexString(hash).substring(0, 10);
    }

    public static Class<? extends Type> getType(String type) {
        type = StringUtils.defaultString(type, "");
        type = StringUtils.equalsIgnoreCase(type, "Utf8String") ? "string" : type;
        type = StringUtils.equalsIgnoreCase(type, "DynamicBytes") ? "bytes" : type;
        return AbiTypes.getType(StringUtils.lowerCase(type));
    }

    /**
     * 构造函数参数。
     */
    public static class FunctionCreator {
        private String functionName;
        private List<Type> arguments = new ArrayList<Type>();

        public FunctionCreator address(String address) {
            arguments.add(new Address(address));
            return this;
        }

        public FunctionCreator bool(boolean bool) {
            arguments.add(new Bool(bool));
            return this;
        }

        public FunctionCreator bool(String bool) {
            arguments.add(new Bool(Boolean.valueOf(bool)));
            return this;
        }

        public FunctionCreator string(String content) {
            arguments.add(new Utf8String(content));
            return this;
        }

        public FunctionCreator dynamicBytes(byte[] value) {
            arguments.add(new DynamicBytes(value));
            return this;
        }

        public FunctionCreator dynamicBytes(String hex) {
            arguments.add(new DynamicBytes(Hex.decode(hex)));
            return this;
        }

        public FunctionCreator bytes(String bytesName, byte[] value) {
            try {
                Class<? extends Type> cls = getType(bytesName);
                arguments.add(cls.getDeclaredConstructor(byte[].class).newInstance(value));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public FunctionCreator bytes(String bytesName, String hex) {
            try {
                Class<? extends Type> cls = getType(bytesName);
                arguments.add(cls.getDeclaredConstructor(byte[].class).newInstance(Hex.decode(hex)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public FunctionCreator uint256(BigInteger amount) {
            arguments.add(new Uint256(amount));
            return this;
        }

        public FunctionCreator uint256(String amount) {
            arguments.add(new Uint256(new BigInteger(amount)));
            return this;
        }

        public FunctionCreator uint(String unintName, BigInteger amount) {
            try {
                Class<? extends Type> cls = getType(unintName);
                arguments.add(cls.getDeclaredConstructor(BigInteger.class).newInstance(amount));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public FunctionCreator uint(String unintName, String amount) {
            try {
                Class<? extends Type> cls = getType(unintName);
                arguments.add(cls.getDeclaredConstructor(BigInteger.class).newInstance(new BigInteger(amount)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public FunctionCreator addressArray(List<String> addresses) {
            arguments.add(new DynamicArray(
                    Address.class,
                    Utils.typeMap(addresses, Address.class)));
            return this;
        }

        public FunctionCreator addressArray(String... addresses) {
            arguments.add(new DynamicArray(
                    Address.class,
                    Utils.typeMap(Arrays.asList(addresses), Address.class)));
            return this;
        }

        public FunctionCreator boolArray(List<Boolean> bools) {
            arguments.add(new DynamicArray(
                    Bool.class,
                    Utils.typeMap(bools, Bool.class)));
            return this;
        }

        public FunctionCreator boolArray(boolean... bools) {
            List<Boolean> value = new ArrayList<>();
            if (bools != null) {
                for (boolean bool : bools) {
                    value.add(bool);
                }
            }
            arguments.add(new DynamicArray(
                    Bool.class,
                    Utils.typeMap(value, Bool.class)));
            return this;
        }

        public FunctionCreator boolArray(String... bools) {
            List<Boolean> value = new ArrayList<>();
            if (bools != null) {
                for (String bool : bools) {
                    value.add(Boolean.valueOf(bool));
                }
            }
            arguments.add(new DynamicArray(
                    Bool.class,
                    Utils.typeMap(value, Bool.class)));
            return this;
        }

        public FunctionCreator stringArray(List<String> addresses) {
            arguments.add(new DynamicArray(
                    Address.class,
                    Utils.typeMap(addresses, Address.class)));
            return this;
        }

        public FunctionCreator stringArray(String... addresses) {
            arguments.add(new DynamicArray(
                    Address.class,
                    Utils.typeMap(Arrays.asList(addresses), Address.class)));
            return this;
        }

        public FunctionCreator dynamicBytesArray(List<byte[]> bytesList) {
            arguments.add(new DynamicArray(
                    DynamicBytes.class,
                    Utils.typeMap(bytesList, DynamicBytes.class)));
            return this;
        }

        public FunctionCreator dynamicBytesArray(byte[]... bytesArray) {
            arguments.add(new DynamicArray(
                    DynamicBytes.class,
                    Utils.typeMap(Arrays.asList(bytesArray), DynamicBytes.class)));
            return this;
        }

        public FunctionCreator dynamicBytesArray(String... hexBytesArray) {
            List<byte[]> value = new ArrayList<>();
            if (hexBytesArray != null) {
                for (String hex : hexBytesArray) {
                    value.add(Hex.decode(hex));
                }
            }
            arguments.add(new DynamicArray(
                    DynamicBytes.class,
                    Utils.typeMap(value, DynamicBytes.class)));
            return this;
        }

        public FunctionCreator bytesArray(String bytesName, List<byte[]> bytesList) {
            arguments.add(new DynamicArray(
                    getType(bytesName),
                    Utils.typeMap(bytesList, getType(bytesName))));
            return this;
        }

        public FunctionCreator bytesArray(String bytesName, byte[]... bytesArray) {
            arguments.add(new DynamicArray(
                    getType(bytesName),
                    Utils.typeMap(Arrays.asList(bytesArray), getType(bytesName))));
            return this;
        }

        public FunctionCreator bytesArray(String bytesName, String... hexBytesArray) {
            List<byte[]> value = new ArrayList<>();
            if (hexBytesArray != null) {
                for (String hex : hexBytesArray) {
                    value.add(Hex.decode(hex));
                }
            }
            arguments.add(new DynamicArray(
                    getType(bytesName),
                    Utils.typeMap(value, getType(bytesName))));
            return this;
        }

        public FunctionCreator unint256Array(List<BigInteger> amounts) {
            arguments.add(new DynamicArray<Uint256>(
                    Uint256.class,
                    Utils.typeMap(amounts, Uint256.class)));
            return this;
        }

        public FunctionCreator unint256Array(String... amounts) {
            List<BigInteger> list = new ArrayList<>();
            for (String amount : amounts) {
                list.add(new BigInteger(amount));
            }
            arguments.add(new DynamicArray(
                    Uint256.class,
                    Utils.typeMap(list, Uint256.class)));
            return this;
        }

        public FunctionCreator unintArray(String unintName, List<BigInteger> amounts) {
            arguments.add(new DynamicArray(
                    getType(unintName),
                    Utils.typeMap(amounts, getType(unintName))));
            return this;
        }

        public FunctionCreator unintArray(String unintName, String... amounts) {
            List<BigInteger> list = new ArrayList<>();
            for (String amount : amounts) {
                list.add(new BigInteger(amount));
            }
            arguments.add(new DynamicArray(
                    getType(unintName),
                    Utils.typeMap(list, getType(unintName))));
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

    /**
     * 输出参数类型。
     */
    public static class ParameterType {
        private List<TypeReference<Type>> types = new ArrayList();

        public ParameterType add(Class<? extends Type> cls) {
            types.add(new TypeReference<Type>() {
                public java.lang.reflect.Type getType() {
                    return cls;
                }
            });
            return this;
        }

        public List<TypeReference<Type>> get() {
            return types;
        }
    }
}
