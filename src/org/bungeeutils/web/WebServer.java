package org.bungeeutils.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public abstract class WebServer {
	
	private final HttpServer httpServer;
	private final HttpsServer httpsServer;
	
	public WebServer() {
		httpServer = null;
    	httpsServer = null;
	}
	
    public WebServer(InetSocketAddress httpHost, String address) throws IOException {
    	System.setProperty("java.net.preferIPv4Stack", "true");
    	httpServer = HttpServer.create(httpHost, 0);
    	httpServer.createContext("/", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				WebRequest webrequest = new WebRequest(exchange);
				try {
					onRequest(webrequest);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
    	httpServer.setExecutor(Executors.newCachedThreadPool());
    	httpServer.start();
    	
    	httpsServer = null;
	}
	
    public WebServer(InetSocketAddress httpHost, String address, InetSocketAddress httpsHost, File keystore, String password) throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, KeyManagementException {
    	System.setProperty("java.net.preferIPv4Stack", "true");
    	
    	
    	httpServer = HttpServer.create(httpHost, 0);
		httpServer.createContext("/", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				exchange.getResponseHeaders().set("Location", "https://" + address + exchange.getRequestURI().toString());
				exchange.sendResponseHeaders(307, !exchange.getRequestMethod().equalsIgnoreCase("head") ? 0 : -1);
				exchange.close();
			}
		});
		httpServer.setExecutor(Executors.newCachedThreadPool());
		httpServer.start();
    	
    	
    	httpsServer = HttpsServer.create(httpsHost, 0);
		
		//SSL
		SSLContext sslContext = SSLContext.getInstance("TLS");
		char[] chars = password.toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream fis = new FileInputStream(keystore);
		ks.load(fis, chars);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, chars);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext c = SSLContext.getDefault();
                    SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // get the default parameters
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
		
		httpsServer.createContext("/", new HttpHandler() {
			public void handle(HttpExchange exchange) throws IOException {
				WebRequest webrequest = new WebRequest(exchange);
				try {
					onRequest(webrequest);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		httpsServer.setExecutor(Executors.newCachedThreadPool());
		httpsServer.start();
	}
    
    public final void stop() {
    	if (httpServer != null) httpServer.stop(1);
    	if (httpsServer != null) httpsServer.stop(1);
    }
    
    public abstract void onRequest(WebRequest request) throws Exception;
	
}