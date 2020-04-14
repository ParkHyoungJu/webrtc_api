package com.example.webrtc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

	@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
        		.apiInfo(apiInfo()) //기본정보
                .select()
                .apis(RequestHandlerSelectors.any())  //주소접근 옵션
                .paths(PathSelectors.ant("/api/v1/**"))        
                // .paths(PathSelectors.any())        
                .build();
    }

	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("HJPARK WEB_RTC REST API")
                .description("HJPARK WEB_RTC REST API")
                .termsOfServiceUrl("")
                .license("HJPARK")
                .licenseUrl("")
                .version("1.0")
                .build();
    }
	
}
