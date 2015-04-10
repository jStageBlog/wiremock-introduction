package de.isys.wiremock.example;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.SslConfigurator;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.isys.wiremock.example.dto.Data;

public class ApiTest {

	private static final String GZIP_TRANSFORMER = "gzip";
	private static final String GZIP_HEADER_VALUE = GZIP_TRANSFORMER;
    private static final String REQUEST_URL = "/request-data/5";
	private static final Integer HTTP_PORT = 62013;
    private static final Integer HTTPS_PORT = 46881;
    private static final String LOCAL_ENDPOINT_HTTP = "http://localhost:" + HTTP_PORT + "/";
    private static final String LOCAL_ENDPOINT_HTTPS = "https://localhost:" + HTTPS_PORT + "/";
    private static final String API_USERNAME = "user";
    private static final String API_PASSWORD = "password";

    @Rule
    public final WireMockRule serviceMock = new WireMockRule(wireMockConfig()
	    .httpsPort(HTTPS_PORT)
	    .trustStorePath(getTrustAndKeystorePath())
	    .keystorePath(getTrustAndKeystorePath())
	    .port(HTTP_PORT)
	    .extensions(new GZipTransformer()));

    @Test
    public void testFetchDataHttp() throws IOException {
        // Given
        stubService();
        final RestApiConsumer consumer = createRestApiConsumer(LOCAL_ENDPOINT_HTTP);

        // When
        final Data receivedData = consumer.fetchData(5);

        // Then
        final Data expectedData = new Data(5, "test-content-here");
        assertThat(receivedData, equalTo(expectedData));
        verify(getRequestedFor(urlPathEqualTo(REQUEST_URL)));
    }

    @Test
    public void testFetchDataHttps() throws IOException {
    	// Given
    	stubService();
    	final RestApiConsumer consumer = createRestApiConsumer(LOCAL_ENDPOINT_HTTPS);
    	enableSslSupport(consumer);

    	// When
    	final Data receivedData = consumer.fetchData(5);

    	// Then
    	final Data expectedData = new Data(5, "test-content-here");
    	assertThat(receivedData, equalTo(expectedData));
    	verify(getRequestedFor(urlPathEqualTo(REQUEST_URL)));
    }

	@Test
	public void testFetchDataHttpsAndGzip() throws IOException {
		// Given
		serviceMock.stubFor(get(urlPathEqualTo(REQUEST_URL))
				.willReturn(
						aResponse()
							.withStatus(200)
							.withHeader("Content-Type", MediaType.APPLICATION_JSON)
							.withHeader("Content-Encoding", GZIP_HEADER_VALUE)
							.withBody(getServiceResponseContent("sample-response.json"))
							.withTransformers(GZIP_TRANSFORMER)));
		final RestApiConsumer consumer = createRestApiConsumer(LOCAL_ENDPOINT_HTTPS);
		enableSslSupport(consumer);
		consumer.setCompressionEnabled(true);

		// When
		final Data receivedData = consumer.fetchData(5);

		// Then
		final Data expectedData = new Data(5, "test-content-here");
		assertThat(receivedData, equalTo(expectedData));
		verify(getRequestedFor(urlPathEqualTo(REQUEST_URL)));
	}

	private void stubService() throws IOException {
		serviceMock.stubFor(get(urlPathEqualTo(REQUEST_URL))
				.willReturn(
						aResponse()
							.withStatus(200)
							.withHeader("Content-Type", MediaType.APPLICATION_JSON)
							.withBody(getServiceResponseContent("sample-response.json"))));
	}

    private RestApiConsumer createRestApiConsumer(String endpoint) {
    	return new RestApiConsumer(endpoint, API_USERNAME, API_PASSWORD);
    }

	private String getTrustAndKeystorePath() {
		return getClass().getResource("/keystore").getPath();
	}

	private void enableSslSupport(final RestApiConsumer consumer) {
		final SslConfigurator sslConfig = SslConfigurator.newInstance()
				.trustStoreFile(getTrustAndKeystorePath())
				.trustStorePassword("password")
				.keyStoreFile(getTrustAndKeystorePath())
				.keyStorePassword("password");
		final SSLContext sslContext = sslConfig.createSSLContext();
		consumer.setSSLContext(sslContext);
	}

	private String getServiceResponseContent(final String testCasePath) throws IOException {
		return readStringFromResourcePath("/" + testCasePath);
	}

	private String readStringFromResourcePath(final String path) throws IOException {
		final InputStream resourceAsStream = getClass().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream);
	}
}
