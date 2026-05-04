package com.mylearning.oauth_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * this is authorization server running on 8082 like keycloak or okta or active directory
 * this can issue token knows who is who in the system on behalf of those users, token is issued by this server
 *
 */
@SpringBootApplication
public class OauthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthServerApplication.class, args);
	}

}
