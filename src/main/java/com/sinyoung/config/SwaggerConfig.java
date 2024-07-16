package com.sinyoung.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Value("${swagger.path.mapping}")
	String path;

	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
	}

	@Bean
	public Docket swaggerSpringfoxDocket() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiEndPointsInfo())
				.securityContexts(Arrays.asList(securityContext()))
				.securitySchemes(Arrays.asList(apiKey()))
				.pathMapping(path)
				.forCodeGeneration(true)
				.useDefaultResponseMessages(false)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.sinyoung.com.sinyoung.controller"))
				.paths(PathSelectors.any()).build();

		List<ResponseMessage> responseMessages = Arrays.asList(
		        new ResponseMessageBuilder().code(200).message("OK").build(),
		        new ResponseMessageBuilder().code(401).message("Unauthorized").build(),
		        new ResponseMessageBuilder().code(403).message("Forbidden").build(),
		        new ResponseMessageBuilder().code(404).message("NotFound").build(),
		        new ResponseMessageBuilder().code(408).message("Request Timeout").build(),
				new ResponseMessageBuilder().code(500).message("Internal Server Error").build(),
				new ResponseMessageBuilder().code(504).message("Gateway Timeout").build()
		);
		docket.globalResponseMessage(RequestMethod.PUT, responseMessages);
		docket.globalResponseMessage(RequestMethod.POST, responseMessages);
		docket.globalResponseMessage(RequestMethod.GET, responseMessages);
		docket.globalResponseMessage(RequestMethod.DELETE, responseMessages);
		docket.globalResponseMessage(RequestMethod.PATCH, responseMessages);

		return docket;
	}

	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder().title("AGENT REST API")
				.description("This documents describes about Agent REST API\nAuthorize Value: Bearer JWT_Token")
				.license("sinyoung")
				.version("v2")
				.build();
	}
}
