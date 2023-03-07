package org.wildfly.security.http.client;

import org.wildfly.security.http.client.hpi.ClientConfigProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Iterator;
import java.util.ServiceLoader;

public class ElytronHttpClient {

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
    public static void connect(String uri) throws Exception{
        Iterator<ClientConfigProvider> serviceLoaderIterator = ServiceLoader.load(ClientConfigProvider.class).iterator();
        ClientConfigProvider clientConfigProvider = serviceLoaderIterator.next();
        String username = clientConfigProvider.getUsername(new URI(uri));
        String password = clientConfigProvider.getPassword(new URI(uri));
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI(uri))
                .header("Authorization",basicAuth(username,password))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
