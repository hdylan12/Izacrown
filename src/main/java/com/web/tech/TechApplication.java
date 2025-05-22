package com.web.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ComponentScan(basePackages = "com.web.tech")
public class TechApplication {

	public static void main(String[] args) {
		// Explicitly set the port from the environment variable (AWS may provide a PORT environment variable for Beanstalk)
		String port = System.getenv("PORT");
		if (port != null && !port.isEmpty()) {
			System.setProperty("server.port", port); // Set the port for the Spring Boot app
			System.out.println("Binding to AWS port: " + port);  // Log the port to confirm
		} else {
			// Default to port 8080 if no port is set
			System.setProperty("server.port", "8080");
			System.out.println("Binding to default port: 8080");
		}
		SpringApplication.run(TechApplication.class, args);
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		String baseUrl = getBaseUrl();
		System.out.println("\n=========================================");
		System.out.println("  Glam Nexa Application Started!");
		System.out.println("  Landing Page: " + baseUrl + "/landing");
		System.out.println("=========================================\n");
	}

	private String getBaseUrl() {
		// Check if we're running on Elastic Beanstalk
		String elasticBeanstalkAppName = System.getenv("AWS_ELASTIC_BEANSTALK_APP_NAME");
		if (elasticBeanstalkAppName != null && !elasticBeanstalkAppName.isEmpty()) {
			// If running on Elastic Beanstalk, return the Beanstalk URL
			return "http://" + elasticBeanstalkAppName + ".elasticbeanstalk.com";
		}

		// Check if we have a custom domain set
		String customDomain = System.getenv("CUSTOM_DOMAIN");
		if (customDomain != null && !customDomain.isEmpty()) {
			return "https://" + customDomain;
		}

		// Default to localhost with the port from environment variables (for local or EC2 instances)
		String port = System.getenv("PORT");
		if (port != null && !port.isEmpty()) {
			return "http://localhost:" + port;
		}

		// Fallback to default localhost:8080 if no port is set
		return "http://localhost:8080";
	}

}
