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
import org.hitchain.contract.api.TokenApi;
import org.iff.infra.util.FCS;
import org.iff.infra.util.PreRequiredHelper;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
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
public class TokenService implements TokenApi {

    public static final String CONTRACT_READ = "FromAddress={0}\nContractAddress={1}\nFunctionName={2}\nFunctionType={3}\nArg0={4}\nArg1={5}\nArg2={6}\n";
    private static Web3j web3j = null;

    public static Web3j getWeb3j() {
        if (web3j == null) {
            String url = "https://mainnet.infura.io/0x7995ab36bB307Afa6A683C24a25d90Dc1Ea83566";
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

    public static String readContract(
            /*@ApiParam(value = "数据格式(数据间请勿输入空格，Arg是输入的参数，为空时设值为-，出错时返回'ERROR:'开头的错误信息)：\n" +
                    "FromAddress=\n" +
                    "ContractAddress=\n" +
                    "FunctionName=functionName(Uint256|Address|Utf8String|Uint256Array|AddressArray|Utf8StringArray,...)\n"+
                    "FunctionType=Uint256|Address|Bool|Utf8String\n"+
                    "Arg*=\n"
                    )
            @RequestBody*/ String data) {
        try {
            Map<String, Object> map = TransactionHelper.parseData(data);
            String fromAddress = PreRequiredHelper.requireNotBlank((String) map.get("FromAddress"), "FromAddress key is required!");
            String contractAddress = PreRequiredHelper.requireNotBlank((String) map.get("ContractAddress"), "ContractAddress key is required!");
            String functionName = PreRequiredHelper.requireNotBlank((String) map.get("FunctionName"), "FunctionName key is required!");
            String functionType = PreRequiredHelper.requireNotBlank((String) map.get("FunctionType"), "FunctionType key is required!");
            TransactionHelper.FunctionCreator args = TransactionHelper.args();
            TransactionHelper.FunctionResult result = TransactionHelper.result();
            for (int i = 0; i < 1; i++) {
                String argTypes = functionName.indexOf("(") < 1 ? "" : StringUtils.substringBefore(StringUtils.substringAfter(functionName, "("), ")").trim();
                functionName = StringUtils.substringBefore(functionName, "(").trim();
                if (StringUtils.isBlank(argTypes)) {
                    break;
                }
                String[] argTypeArr = StringUtils.split(argTypes, ",");
                for (int count = 0; count < argTypeArr.length; count++) {
                    String argType = argTypeArr[count];
                    if ("Uint256".equalsIgnoreCase(argType)) {
                        args.uint256((String) map.get("Arg" + count));
                    } else if ("Address".equalsIgnoreCase(argType)) {
                        args.address((String) map.get("Arg" + count));
                    } else if ("Utf8String".equalsIgnoreCase(argType)) {
                        args.string((String) map.get("Arg" + count));
                    } else if ("Uint256Array".equalsIgnoreCase(argType)) {
                        args.unint256Array(StringUtils.split((String) map.get("Arg" + count), ","));
                    } else if ("AddressArray".equalsIgnoreCase(argType)) {
                        args.addressArray(StringUtils.split((String) map.get("Arg" + count), ","));
                    }
                }
            }
            {
                if (StringUtils.isBlank(functionType)) {
                    //
                } else if ("Uint256".equalsIgnoreCase(functionType)) {
                    result.add(Uint256.class);
                } else if ("Address".equalsIgnoreCase(functionType)) {
                    result.add(Address.class);
                } else if ("Utf8String".equalsIgnoreCase(functionType)) {
                    result.add(Utf8String.class);
                } else if ("Bool".equalsIgnoreCase(functionType)) {
                    result.add(Bool.class);
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

    @Override
    public String readToken(String address) {
        try {
            EthGetBalance ethGetBalance = getWeb3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
            return ethGetBalance.getBalance().toString();
        } catch (Exception e) {
            return "ERROR:" + e.toString();
        }
    }

    @Override
    public String readHitToken(String address) {
        String data = FCS.get(CONTRACT_READ, address, TokenApi.HIT_CONTACT, "balanceOf(Address)", "Uint256", address).toString();
        return readContract(data);
    }

    @Override
    public String readContactToken(String data) {
        return readContract(data);
    }
}
