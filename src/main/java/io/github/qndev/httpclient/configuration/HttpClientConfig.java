package io.github.qndev.httpclient.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource("classpath:httpclient.properties")
public class HttpClientConfig {

    @Value("${httpclient.connectTimeout}")
    private Integer connectTimeout;

    @Value("${httpclient.socketTimeout}")
    private Integer socketTimeout;

    @Value("${httpclient.connectionRequestTimeout}")
    private Integer connectionRequestTimeout;

    @Value("${httpclient.maxTotal}")
    private Integer maxTotal;

    @Value("${httpclient.defaultMaxPerRoute}")
    private Integer defaultMaxPerRoute;

    @Value("${httpclient.maxPerRoute}")
    private Integer maxPerRoute;

    @Value("${httpclient.targetHost}")
    private String targetHost;

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                // Set connection timeout (milliseconds)
                .setConnectTimeout(connectTimeout)
                // Set socket timeout (milliseconds)
                .setSocketTimeout(socketTimeout)
                // Set connection request timeout (milliseconds)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }

    @Bean
    public HttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // Set max total connection
        connectionManager.setMaxTotal(maxTotal);
        // Set max connection per route
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // Define target hosts
        List<HttpHost> targetHosts = getTargetHosts();
        targetHosts.forEach(targetHost -> connectionManager.setMaxPerRoute(new HttpRoute(targetHost), maxPerRoute));

        return connectionManager;
    }

    @Bean
    public List<HttpHost> getTargetHosts() {
        List<String> hosts = Arrays.asList(StringUtils.splitPreserveAllTokens(targetHost, ","));
        List<HttpHost> httpHosts = new ArrayList<>();
        hosts.forEach(hostname -> httpHosts.add(HttpHost.create(hostname)));
        return httpHosts;
    }

    @Bean
    public CloseableHttpClient createHttpClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager())
                .setDefaultRequestConfig(requestConfig())
                .build();

        return httpClient;
    }

}
