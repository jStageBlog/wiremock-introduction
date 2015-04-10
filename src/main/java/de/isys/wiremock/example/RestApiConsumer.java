package de.isys.wiremock.example;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import de.isys.wiremock.example.dto.Data;

public class RestApiConsumer {

	private static final String REQUEST_DATA_PATH = "request-data/{id}";
	private static final String GZIP = "gzip";
	private static final String ID = "id";
	private final String apiEndpoint;
	private final String apiPassword;
	private final String apiUsername;
	private final ClientConfig clientConfiguration;
	private boolean isCompressionEnabled;
	private SSLContext sslContext;

	public RestApiConsumer(String apiEndpoint, String apiUsername, String apiPassword) {
		this.apiEndpoint = apiEndpoint;
		this.apiPassword = apiPassword;
		this.apiUsername = apiUsername;
		clientConfiguration = createClientConfiguration();
	}

	public void setSSLContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public Data fetchData(int id) {
		return buildGetRequest(id).get(Data.class);
	}

	public void setCompressionEnabled(boolean isCompressionEnabled) {
		this.isCompressionEnabled = isCompressionEnabled;
	}

	private Builder buildGetRequest(int value) {
		return buildWebTarget()
				.path(REQUEST_DATA_PATH)
				.resolveTemplate(ID, value)
				.request(MediaType.APPLICATION_JSON_TYPE);
	}

	private WebTarget buildWebTarget() {
		return createClientBuilder().target(apiEndpoint);
	}

	private Client createClientBuilder() {
		final ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(clientConfiguration);

		if (sslContext != null) {
			clientBuilder.sslContext(sslContext).build();
		}

		if (isCompressionEnabled) {
			clientBuilder.property(ClientProperties.USE_ENCODING, GZIP);
		}

		return clientBuilder.build();
	}

	private ClientConfig createClientConfiguration() {
		return new ClientConfig()
			.register(JacksonJsonProvider.class)  // JSON (de)serialization support
			.register(createHttpAuthentication()) // HTTP Basic Authentication -> requires SSL / TLS!
			.register(EncodingFilter.class)       // Compression
			.register(GZipEncoder.class)          // Compression support
			.register(DeflateEncoder.class);      // Compression support
	}

	private HttpAuthenticationFeature createHttpAuthentication() {
		return HttpAuthenticationFeature.basic(apiUsername, apiPassword);
	}

}
