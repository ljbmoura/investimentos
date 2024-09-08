package br.com.ljbm.configuracao;

import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

@Component
public class SecureRestTemplateConfig {
//    private final SSLContext sslContext;

//    @Autowired
//    public SecureRestTemplateConfig(SslBundles sslBundles) throws NoSuchSslBundleException {
//        SslBundle sslBundle = sslBundles.getBundle("secure-service");
//        this.sslContext = sslBundle.createSslContext();
//    }

//    @Bean
//    public RestTemplate secureRestTemplate() {
////        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(this.sslContext).build();
////        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
////        		.setSSLSocketFactory(sslSocketFactory)
////        		.build();
//        HttpClient httpClient = HttpClients.custom().setConnectionManager(cm).evictExpiredConnections().build();
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        return new RestTemplate(factory);
//    }
}