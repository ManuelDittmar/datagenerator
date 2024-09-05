package org.camunda.consulting.datagenerator;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Deployment(resources = "classpath:processes/*.bpmn")
@EnableScheduling
public class DatageneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatageneratorApplication.class, args);
	}

}
