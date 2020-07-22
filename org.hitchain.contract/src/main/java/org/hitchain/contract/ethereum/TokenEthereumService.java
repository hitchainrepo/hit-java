/*******************************************************************************
 * Copyright (c) 2018-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.contract.ethereum;


import org.apache.commons.io.IOUtils;
import org.hitchain.contract.api.TokenApi;
import org.hitchain.contract.api.TokenEthereumApi;
import org.iff.infra.util.FCS;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;

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

    /**
     * 绕过验证
     *
     * @return
     */
    public static SSLContext createIgnoreVerifySSL() throws Exception {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");

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
        String requestUrl = "https://faucet.metamask.io/";
        String content = address;
        {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://faucet.ropsten.be/donate/" + address);
                {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("charset", "UTF-8");
                    connection.setRequestProperty("accept", "*/*");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36");
                    connection.setConnectTimeout(30 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.connect();
                }
                IOUtils.toString(connection.getInputStream(), "UTF-8");
            } catch (Exception e) {
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            HttpsURLConnection connection = null;
            DataOutputStream wr = null;
            try {
                URL url = new URL(requestUrl);
                {
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.setSSLSocketFactory(createIgnoreVerifySSL().getSocketFactory());
                    connection.setDoOutput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("origin", "https://faucet.metamask.io");
                    connection.setRequestProperty("content-type", "application/rawdata");
                    connection.setRequestProperty("referer", "https://faucet.metamask.io/");
                    connection.setRequestProperty("authority", "faucet.metamask.io");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36");
                    connection.setRequestProperty("Content-Length", String.valueOf(content.length()));
                    connection.setConnectTimeout(30 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    {
                        wr = new DataOutputStream(connection.getOutputStream());
                        wr.writeBytes(content);
                        wr.flush();
                        IOUtils.closeQuietly(wr);
                    }
                    connection.connect();
                }
                return IOUtils.toString(connection.getInputStream(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                }
            }
        }
        return content;
    }

    @Override
    public String readContactToken(String address, String contractAddress, String functionName) {
        String data = FCS.get(CONTRACT_READ, address, contractAddress, functionName, "Uint256", address).toString();
        return readContactToken(data);
    }
}
