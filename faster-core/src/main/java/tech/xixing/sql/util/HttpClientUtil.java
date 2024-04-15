package tech.xixing.sql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public final static String SERVER_ERROR = "-10000";

    private static String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Win32)";

    /**
     * Determines the timeout in milliseconds until a connection is established.
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private static int CONNECT_TIME = 20000;

    /**
     * Defines the socket timeout in milliseconds,
     * which is the timeout for waiting for data  or, put differently
     */
    private static int WAIT_DATA_TIME = 20000;

    /**
     * 设置 HttpClient 属性 retryHandler,超时时间
     *
     * @return DefaultHttpClient
     */
    private static DefaultHttpClient getHttpClient() {
        //设置请求超时参数
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIME);
        HttpConnectionParams.setSoTimeout(params, WAIT_DATA_TIME);

        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        //设置重连3次条件
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, false));

        // 模拟浏览器，解决一些服务器程序只允许浏览器访问的问题
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        httpClient.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, Consts.UTF_8);

        return httpClient;
    }


    /**
     * 访问https的网站
     *
     * @param httpClient
     */
    @SuppressWarnings("deprecation")
    private static void enableSSL(DefaultHttpClient httpClient) {
        //调用ssl
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{truseAllManager}, null);
            SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme https = new Scheme("https", sf, 443);
            httpClient.getConnectionManager().getSchemeRegistry()
                    .register(https);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 重写验证方法，取消检测ssl
     */
    private static TrustManager truseAllManager = new X509TrustManager() {
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };


    /**
     * 获取get 请求返回数据
     *
     * @param getUrl 请求url
     * @return 字符串
     */
    public static String get(String getUrl, Map<String, String> headerParams) {
        return httpGetMethod(getUrl, null, headerParams);
    }

    /**
     * 获取get 请求返回数据
     *
     * @param getUrl 请求url
     * @return 字符串
     */
    public static String get(String getUrl, Charset charset, Map<String, String> headerParams) {
        return httpGetMethod(getUrl, charset, headerParams);
    }

    public static String get(String getUrl) {
        return httpGetMethod(getUrl, Charset.forName("UTF-8"), null);
    }

    /**
     * http post 提交json 数据
     *
     * @param postUrl 提交url
     * @param jsonStr json 数据格式
     * @return
     */
    public static String postJsonStr(String postUrl, String jsonStr) {
        return httpPostMethod(postUrl, null, jsonStr, true, null);
    }


    /**
     * get 方式请求数据
     *
     * @param getUrl 请求url
     * @return 字符串数据
     */
    private static String httpGetMethod(String getUrl, Charset charset, Map<String, String> headerParams) {
        DefaultHttpClient defaultHttpClient = getHttpClient();

        //判断是否访问https
        if (getUrl.startsWith("https")) {
            enableSSL(defaultHttpClient);
        }
        HttpGet httpget = new HttpGet(getUrl);
        //向header 中设置值
        if (null != headerParams && !headerParams.isEmpty()) {
            for (String key : headerParams.keySet()) {
                httpget.setHeader(key, headerParams.get(key));
            }
        }
        return handlerResponse(defaultHttpClient, httpget, charset);

    }


    /**
     * post 方式请求数据
     *
     * @param postUrl           请求url
     * @param nameValuePairList 参数
     * @param isJsonType        返回数据是否是json格式
     * @return 字符串数据
     */
    private static String httpPostMethod(String postUrl, List<NameValuePair> nameValuePairList, String jsonStr, boolean isJsonType, Map<String, String> headerParams) {
        DefaultHttpClient defaultHttpClient = getHttpClient();

        //判断是否访问https
        if (postUrl.startsWith("https")) {
            enableSSL(defaultHttpClient);
        }

        HttpPost httppost = new HttpPost(postUrl);

        //向header 中设置值
        if (null != headerParams && !headerParams.isEmpty()) {
            for (String key : headerParams.keySet()) {
                httppost.setHeader(key, headerParams.get(key));
            }
        }
        //post json 格式数据
        if (isJsonType) {
            StringEntity entity = new StringEntity(jsonStr, Consts.UTF_8);
            entity.setContentType("application/json");
            httppost.setEntity(entity);
        } else {//post form 数据
            if (null != nameValuePairList) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, Consts.UTF_8);
                httppost.setEntity(entity);
            }
        }
        return handlerResponse(defaultHttpClient, httppost, null);
    }


    /**
     * 处理response 返回数据
     *
     * @param defaultHttpClient httpClient 对象
     * @param httpRequestBase   请求方式
     * @return 请求响应数据
     */
    private static String handlerResponse(DefaultHttpClient defaultHttpClient, HttpRequestBase httpRequestBase, Charset charset) {
        String output = null;
        try {
            HttpResponse response = defaultHttpClient.execute(httpRequestBase);
            HttpEntity httpEntity = response.getEntity();
            if (null != httpEntity) {
                output = EntityUtils.toString(httpEntity, charset);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return SERVER_ERROR;
        } finally {//释放请求链接
            defaultHttpClient.getConnectionManager().shutdown();
        }
        return output == null ? "" : output;
    }



}
