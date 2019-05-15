/*******************************************************************************
 * Copyright (c) 2018-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hitchain.contract.api.TokenApi;
import org.hitchain.contract.api.TokenEthereumApi;
import org.iff.infra.util.FCS;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Token service for Ethereum implements TokenEthereumApi
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @version 1.0.0
 * auto generate by qdp v5.0.
 * @since 2018-07-03
 */
public class TokenEthereumService extends TokenService implements TokenEthereumApi {

    public static TokenEthereumApi getApi() {
        TokenApi.setInstance(new TokenEthereumService());
        return (TokenEthereumApi) TokenApi.getInstance();
    }

    public static String requestEthereum(String address) {
        String result = "ERROE: unknown";
        try {
            String url = "https://faucet.ropsten.be/donate/" + address;
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();
            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);

            //创建自定义的httpclient对象
            CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build();

            HttpGet httpGet = new HttpGet(url);
            //装填参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            {
                //nvps.add(new BasicNameValuePair(name, value));
            }
            //设置参数到请求对象中

            //System.out.println("请求地址：" + url);
            //System.out.println("请求参数：" + nvps.toString());

            //设置header信息
            //指定报文头【Content-type】、【User-Agent】

            //
            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpGet);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null && response.getStatusLine().getStatusCode() == 200) {
                //按指定编码转换结果实体为String类型
                result = EntityUtils.toString(entity, "UTF-8");
            } else {
                result = "ERROR:" + response.getStatusLine().getStatusCode() + ", msg:" + response.getStatusLine().toString();
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
        } catch (Exception e) {
            return "ERROR:" + e.toString();
        }
        return result;
    }

    /**
     * 绕过验证
     *
     * @return
     */
    public static SSLContext createIgnoreVerifySSL() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSLv3");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    @Override
    public String requestTestToken(String address) {
        System.out.println("Please wait a while if has no error occurred, such as 60 seconds, and check the account.");
        return requestEthereum(address);
    }

    @Override
    public String readContactToken(String address, String contractAddress, String functionName) {
        String data = FCS.get(CONTRACT_READ, address, contractAddress, functionName, "Uint256", address).toString();
        return readContactToken(data);
    }
}
