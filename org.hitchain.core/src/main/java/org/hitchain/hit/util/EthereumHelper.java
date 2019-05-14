/*******************************************************************************
 * Copyright (c) 2018-12-17 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.RepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.RepositoryContractEthereumService;

/**
 * Utils
 * <pre>
 * /api/web3j/readRepositoryNameContract
 * 读取RepositoryName合约
 * 数据格式(数据间请勿输入空格，Arg是输入的参数，为空时设值为-，出错时返回’ERROR:’开头的错误信息)：
 * FromAddress=
 * ContractAddress=
 * FunctionName=repositoryName|repositoryAddress|owner|delegator|authedAccounts(addr)|authedAccountList(int)|authedAccountSize|hasTeamMember(addr)|teamMemberAtIndex(int)
 * Arg=-
 * </pre>
 * <pre>
 * /api/web3j/writeRepositoryNameContract
 * 写RepositoryName合约
 * 数据格式(数据间请勿输入空格，PrivateKey需要先加密采用默认值时为-，Arg是输入的参数，为空时设值为-，出错时返回’ERROR:’开头的错误信息)：
 * PrivateKey=
 * ContractAddress=
 * FunctionName=init(addr,repoName)|initWithDelegator(addr,repoName,delegator)|updateRepositoryName(repoName)|updateRepositoryAddress(oldAddr,newAddr)|addTeamMember(addr)|removeTeamMember(addr)|changeOwner(addr)|delegateTo(addr)
 * Arg1=-
 * Arg2=-
 * Arg3=-
 * GasLimit=5000000
 * Gwei=0
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-12-17
 * auto generate by qdp.
 */
public class EthereumHelper {

    public static RepositoryContractEthereumApi getApi() {
        return RepositoryContractEthereumService.getApi();
    }

    public static String createContractForProject(String urlBase, String ownerAddressEcc, String projectName) {
        ///====create
        System.out.println("Contract is deploying...please wait for seconds...");
        String address = getApi().deployContract(HitHelper.getAccountPriKeyWithPasswordInput(), 5000000, 10);
        if (isError(address)) {
            return null;
        }
        //====init
        System.out.println("Contract " + address + " is initializing...please wait for seconds...");
        String init = getApi().writeInit(ownerAddressEcc, projectName, HitHelper.getAccountPriKeyWithPasswordInput(), address, 500000, 10);
        if (!isError(init)) {
            return address;
        }
        return null;
    }

    public static void updateProjectAddress(String urlBase, String contractAddress, String ownerPriKeyEcc, String newProjectHash) {
        String oldRepositoryAddress = getApi().readRepositoryAddress(HitHelper.getAccountAddress(), contractAddress);
        String result = getApi().writeUpdateRepositoryAddress(oldRepositoryAddress, newProjectHash, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, 500000, 10);
        if (!isError(result)) {
            return;
        }
        throw new RuntimeException(result);

    }

    public static String getProjectAddress(String urlBase, String contractAddress) {
        return getApi().readRepositoryAddress(HitHelper.getAccountAddress(), contractAddress);
    }

    public static boolean hasTeamMember(String urlBase, String contractAddress, String memberAddress) {
        return getApi().readHasTeamMember(HitHelper.getAccountAddress(), contractAddress, memberAddress);
    }

    public static String addTeamMember(String urlBase, String contractAddress, String memberAddress) {
        return getApi().writeAddTeamMember(memberAddress, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, 500000, 10);
    }

    public static String removeTeamMember(String urlBase, String contractAddress, String memberAddress) {
        return getApi().writeRemoveTeamMember(memberAddress, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, 500000, 10);
    }

    public static boolean isError(String result) {
        return ContractApi.isError(result);
    }

//    /**
//     * 绕过验证
//     *
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws KeyManagementException
//     */
//    public static SSLContext createIgnoreVerifySSL() throws Exception {
//        SSLContext sc = SSLContext.getInstance("SSLv3");
//        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
//        X509TrustManager trustManager = new X509TrustManager() {
//            public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
//                                           String paramString) throws CertificateException {
//            }
//
//            public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
//                                           String paramString) throws CertificateException {
//            }
//
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return null;
//            }
//        };
//        sc.init(null, new TrustManager[]{trustManager}, null);
//        return sc;
//    }
//
//    public static String post(String url, String content) {
//        try {
//            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE;
//            RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
//            //采用绕过验证的方式处理https请求
//            SSLContext sslcontext = createIgnoreVerifySSL();
//            // 设置协议http和https对应的处理socket链接工厂的对象
//            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
//            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                    .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
//            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
//                    socketFactoryRegistry);
//            HttpClients.custom().setConnectionManager(connManager);
//            //创建自定义的httpclient对象
//            CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config)
//                    .setConnectionManager(connManager).build();
//            //
//            HttpPost post = new HttpPost(url);
//            StringEntity entity = new StringEntity(content, "UTF-8");
//            entity.setContentType("text/plain");
//            post.setEntity(entity);
//            // 构造消息头
//            post.setHeader("Content-type", "text/plain");
//            post.setHeader("Connection", "Close");
//            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post);
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode != 200) {
//                post.abort();
//                return "ERROR:HttpClient,error status code :" + statusCode;
//            }
//            HttpEntity responseEntity = response.getEntity();
//            String result = null;
//            if (entity != null) {
//                result = EntityUtils.toString(responseEntity);
//                EntityUtils.consume(entity);
//                response.close();
//            }
//            return StringUtils.defaultString(result, "ERROR:null");
//        } catch (Exception e) {
//            return ERROR_PREFIX + e.getMessage();
//        }
//    }
//
//    public static String createContractForProject(String urlBase, String ownerAddressEcc, String projectName) {
//        ///====create
//        String url = urlBase + "/api/web3j/deployRepositoryNameContract";
//        String content = "PrivateKey=-\nGasLimit=5000000\nGwei=1\n";
//        String address = post(url, content);
//        if (isError(address)) {
//            for (int i = 0; i < 10; i++) {
//                address = post(url, content);
//                if (!isError(address)) {
//                    break;
//                }
//            }
//        }
//        if (isError(address)) {
//            return null;
//        }
//        //====init
//        url = urlBase + "/api/web3j/writeRepositoryNameContract";
//        content = "PrivateKey=-\n" + "ContractAddress=" + address + "\n" + "FunctionName=initWithDelegator(addr,repoName,delegator)\n"
//                + "Arg1=" + ownerAddressEcc + "\n" + "Arg2=" + projectName + "\n" + "Arg3=-\n" + "GasLimit=5000000\n" + "Gwei=10\n";
//        String init = post(url, content);
//        if (isError(init)) {
//            for (int i = 0; i < 10; i++) {
//                init = post(url, content);
//                if (!isError(init)) {
//                    return address;
//                }
//            }
//        } else {
//            return address;
//        }
//        return null;
//    }
//
//    public static void updateProjectAddress(String urlBase, String contractAddress, String ownerPriKeyEcc, String newProjectHash) {
//        String result = "";
//        for (int i = 0; i < 10; i++) {
//            String projectAddress = getProjectAddress(urlBase, contractAddress);
//            String oldProjectAddress = StringUtils.isBlank(projectAddress) ? "-" : projectAddress;
//            //
//            String url = urlBase + "/api/web3j/writeRepositoryNameContract";
//            String content = "PrivateKey=-\n" + "ContractAddress=" + contractAddress + "\n"
//                    + "FunctionName=updateRepositoryAddress(oldAddr,newAddr)\n" + "Arg1=" + oldProjectAddress + "\n"
//                    + "Arg2=" + newProjectHash + "\nArg3=-\n" + "GasLimit=5000000\n" + "Gwei=10\n";
//            result = post(url, content);
//            if (!isError(result)) {
//                return;
//            }
//        }
//        throw new RuntimeException(result);
//    }
//
//    public static String getProjectAddress(String urlBase, String contractAddress) {
//        String url = urlBase + "/api/web3j/readRepositoryNameContract";
//        String content = "FromAddress=-\nContractAddress=" + contractAddress + "\nFunctionName=repositoryAddress\nArg=-\n";
//        return StringUtils.defaultString(post(url, content), "");
//    }
//
//    public static String encryptPriKeyEcc(String urlBase, String priKeyEcc) {
//        String url = urlBase + "/api/web3j/encryptPrivateKey";
//        String content = "PrivateKey=" + priKeyEcc + "\n";
//        String encrypt = post(url, content);
//        return isError(encrypt) ? null : encrypt;
//    }
//
//    public static boolean hasTeamMember(String urlBase, String contractAddress, String memberAddress) {
//        String url = urlBase + "/api/web3j/readRepositoryNameContract";
//        String content = "FromAddress=-\nContractAddress=" + contractAddress + "\nFunctionName=hasTeamMember\nArg=" + memberAddress + "\n";
//        String post = post(url, content);
//        return StringUtils.equals(post, "true");
//    }
//
//    public static String addTeamMember(String urlBase, String contractAddress, String memberAddress) {
//        String url = urlBase + "/api/web3j/writeRepositoryNameContract";
//        String content = "PrivateKey=-\n" + "ContractAddress=" + contractAddress + "\n"
//                + "FunctionName=addTeamMember(addr)\n" + "Arg1=" + memberAddress + "\n"
//                + "Arg2=-\nArg3=-\n" + "GasLimit=500000\n" + "Gwei=10\n";
//        String post = post(url, content);
//        return post;
//    }
//
//    public static String removeTeamMember(String urlBase, String contractAddress, String memberAddress) {
//        String url = urlBase + "/api/web3j/writeRepositoryNameContract";
//        String content = "PrivateKey=-\n" + "ContractAddress=" + contractAddress + "\n"
//                + "FunctionName=removeTeamMember(addr)\n" + "Arg1=" + memberAddress + "\n"
//                + "Arg2=-\nArg3=-\n" + "GasLimit=500000\n" + "Gwei=10\n";
//        String post = post(url, content);
//        return post;
//    }
//
//    public static boolean isError(String result) {
//        return StringUtils.startsWith(result, ERROR_PREFIX);
//    }
}
