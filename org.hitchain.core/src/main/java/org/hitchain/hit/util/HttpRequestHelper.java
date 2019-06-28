/*******************************************************************************
 * Copyright (c) 2014-7-3 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.iff.infra.util.JsonHelper;
import org.iff.infra.util.*;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A request helper provides a set of utility methods to process the http request.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2014-7-3
 */
public class HttpRequestHelper {

    /**
     * setting http client properties
     **/
    static final HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(
            RequestConfig.custom().setConnectionRequestTimeout(2000).setSocketTimeout(2000).setCookieSpec(CookieSpecs.STANDARD).build()).build();

    /**
     * set request parameters
     *
     * @param builder
     * @param params
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    protected static URIBuilder setParams(URIBuilder builder, Map<?, ?> params) {
        if (params != null) {
            for (Entry<?, ?> entry : params.entrySet()) {
                builder.addParameter((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return builder;
    }

    /**
     * set request header
     *
     * @param method
     * @param header
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    protected static void setHeader(HttpRequestBase method, Map<?, ?> header) {
        if (header != null) {
            for (Entry<?, ?> entry : header.entrySet()) {
                method.addHeader(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    /**
     * execute http method
     *
     * @param method
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    protected static RequestResult executeMethod(HttpRequestBase method) {
        try {
            HttpResponse response = httpClient.execute(method);
            return RequestResult.get(response.getStatusLine().getStatusCode(), response.getStatusLine().toString(),
                    getResponseBodyAsString(response));
        } catch (Exception e) {
            return RequestResult.get(e.getMessage());
        } finally {
            try {
                method.releaseConnection();
            } catch (Exception e) {
            }
        }
    }

    /**
     * process request GET method and return result
     *
     * @param url
     * @param params
     * @param header
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    public static RequestResult get(String url, Map<?, ?> params, Map<?, ?> header) {
        try {
            if (Logger.getLogger().isDebugEnabled()) {
                Logger.debug(FCS.get("[method=GET,url={0},params={1},header={2}]", url,
                        GsonHelper.toJsonString(params), GsonHelper.toJsonString(header)));
            }
            HttpGet method = new HttpGet(setParams(new URIBuilder(url), params).build());
            setHeader(method, header);
            return executeMethod(method);
        } catch (Exception e) {
            return RequestResult.get(e.getMessage());
        }
    }

    /**
     * process request PUT method and return result
     *
     * @param url
     * @param params
     * @param header
     * @param data
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    public static RequestResult put(String url, Map<?, ?> params, Map<?, ?> header, String data) {
        try {
            if (Logger.getLogger().isDebugEnabled()) {
                Logger.debug(FCS.get("[method=PUT,url={0},params={1},header={2},data={3}]", url, GsonHelper
                        .toJsonString(params), GsonHelper.toJsonString(header), data));
            }
            HttpPut method = new HttpPut(setParams(new URIBuilder(url), params).build());
            setHeader(method, header);
            if (data != null) {
                method.setEntity(new StringEntity(data, ContentType.create("text/plain", "UTF-8")));
            }
            return executeMethod(method);
        } catch (Exception e) {
            return RequestResult.get(e.getMessage());
        }
    }

    /**
     * process request POST method and return result
     *
     * @param url
     * @param params
     * @param header
     * @param data
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    public static RequestResult post(String url, Map<?, ?> params, Map<?, ?> header, String data) {
        try {
            if (Logger.getLogger().isDebugEnabled()) {
                Logger.debug(FCS.get("[method=POST,url={0},params={1},header={2},data={3}]", url, GsonHelper
                        .toJsonString(params), GsonHelper.toJsonString(header), data));
            }
            HttpPost method = new HttpPost(setParams(new URIBuilder(url), params).build());
            setHeader(method, header);
            if (data != null) {
                method.setEntity(new StringEntity(data, ContentType.create("text/plain", "UTF-8")));
            }
            return executeMethod(method);
        } catch (Exception e) {
            return RequestResult.get(e.getMessage());
        }
    }

    /**
     * process request DELETE method and return result
     *
     * @param url
     * @param params
     * @param header
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    public static RequestResult delete(String url, Map<?, ?> params, Map<?, ?> header) {
        try {
            if (Logger.getLogger().isDebugEnabled()) {
                Logger.debug(FCS.get("[method=DELETE,url={0},params={1},header={2}]", url, GsonHelper
                        .toJsonString(params), GsonHelper.toJsonString(header)));
            }
            HttpDelete method = new HttpDelete(setParams(new URIBuilder(url), params).build());
            setHeader(method, header);
            return executeMethod(method);
        } catch (Exception e) {
            return RequestResult.get(e.getMessage());
        }
    }

    /**
     * convert respose content to string, default charset UTF-8
     *
     * @param response
     * @return
     * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
     * @since 2015-2-6
     */
    public static String getResponseBodyAsString(HttpResponse response) {
        try {
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                httpEntity = new BufferedHttpEntity(httpEntity);
                InputStream is = httpEntity.getContent();
                String content = IOUtils.toString(is, "UTF-8");
                is.close();
                return content;
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static void main(String[] args) {
        RequestResult result = get(
                "http://localhost:9900/service/search/addIndex?index=A:tb_project.main:a082c43db87643438e00043526bf4cf3",
                null, null);
        System.out.println(result);
    }

    public static void main1(String[] args) {
        try {
            //http://localhost:9200/twitter/_search?size=0
            RequestResult result = put("http://localhost:9200/twitter/tweet1/1", null, null, JsonHelper
                    .toJson(MapHelper.toMap("user", "kimchy",//
                            "postDate", "2009-11-15T13:12:00",//
                            "message", "Trying out Elasticsearch, so far so good?")));
            System.out.println(result);
            System.out.println(result.getBodyAsJson());
            result = get("http://localhost:9200/twitter/tweet1/1", null, null);
            System.out.println(result);
            System.out.println(result.getBodyAsJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Request result structure
     *
     * @author Tyler
     */
    public static class RequestResult {
        int code;
        String status;
        String body;
        String exception;
        Map<String, Object> json;

        public static RequestResult get(int code, String status, String body) {
            RequestResult r = new RequestResult();
            r.code = code;
            r.status = status;
            r.body = body;
            return r;
        }

        public static RequestResult get(String exception) {
            RequestResult r = new RequestResult();
            r.exception = exception;
            return r;
        }

        public boolean isOK() {
            return code == 200;
        }

        public boolean isRefused() {
            return code == 0;
        }

        public boolean isNotFound() {
            return code == 404;
        }

        public boolean isError() {
            return exception != null;
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }

        public Map<String, Object> getBodyAsJson() {
            if (json == null) {
                json = JsonHelper.toObject(Map.class, body == null ? "" : body);
            }
            return json;
        }

        public String getBodyJsonStringValue(String key) {
            Object value = getBodyAsJson().get(key);
            return value == null ? null : String.valueOf(value);
        }

        public String getException() {
            return exception;
        }

        public void throwException() {
            if (isRefused() || isError()) {
                Exceptions.runtime(FCS.get("[ERRORCODE:{0}][EXCEPTION:{1}]", code, exception));
            }
        }

        public String toString() {
            return "RequestResult [body=" + body + ", code=" + code + ", exception=" + exception + ", status=" + status
                    + "]";
        }
    }
}
