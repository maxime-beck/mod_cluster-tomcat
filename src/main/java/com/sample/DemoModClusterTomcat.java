package com.sample;

import java.util.UUID;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.web.servlet.ServletComponentScan;

import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;

import org.jboss.modcluster.container.tomcat.ModClusterListener;

@SpringBootApplication
@ServletComponentScan
@RestController
public class DemoModClusterTomcat extends SpringBootServletInitializer {

	public static UUID uuid;
	public static void main(String[] args) throws Exception {
		uuid = UUID.randomUUID();
		SpringApplication.run(DemoModClusterTomcat.class, args);
	}

	private Connector ajpConnector() {
		Connector connector = new Connector("AJP/1.3");
		connector.setPort(8009);
		connector.setRedirectPort(8443);
		return connector;
	}

	private ModClusterListener modCluster() {
		ModClusterListener modClusterListener = new ModClusterListener();
		modClusterListener.setAdvertise(false);
		modClusterListener.setProxyList("httpd-modcluster:6666");
		//modClusterListener.setProxyList("localhost:6666");
		modClusterListener.setConnectorPort(8009);
	
		return modClusterListener;
	}

	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		return new TomcatServletWebServerFactory() {

			@Override
			protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
				tomcat.setConnector(ajpConnector());
				tomcat.getServer().addLifecycleListener(modCluster());
				return new TomcatWebServer(tomcat);
			}

		};
	}
}
