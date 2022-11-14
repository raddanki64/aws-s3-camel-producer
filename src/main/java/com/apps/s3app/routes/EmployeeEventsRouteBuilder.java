package com.apps.s3app.routes;

import  org.springframework.beans.factory.annotation.Autowired;
import 	org.springframework.beans.factory.annotation.Value;
import  org.springframework.stereotype.Component;

import  org.apache.camel.builder.RouteBuilder;
import	org.apache.camel.model.dataformat.JsonLibrary;

import	lombok.NoArgsConstructor;

import  org.slf4j.Logger;
import  org.slf4j.LoggerFactory;

import  com.apps.s3app.exceptions.ValidationException;
import	com.apps.s3app.processors.EmployeeEventProcessor;
import	com.apps.s3app.services.domain.Employee;

@Component
@NoArgsConstructor
public class EmployeeEventsRouteBuilder extends RouteBuilder {
    private static Logger logger = LoggerFactory.getLogger(EmployeeEventsRouteBuilder.class);

	@Value("${quartz2.initialDelay}")
	private int initialDelay;

	@Value("${quartz2.repetionCount}")
	private int repetionCount;
	
	@Value("${camel.component.aws2-s3.bucketName}")
	private String bucketName;

	@Autowired
	private EmployeeEventProcessor empEventProcessor;

    @Override
    public void configure() throws Exception {
		String timerExpression = String.format("timer://empeventstimer?delay=%d&repeatCount=%d",
					initialDelay,
					repetionCount);
		
		String awsUrl = String.format("aws2-s3://%s", bucketName);
		
        onException(ValidationException.class)
        .log("Observed validation exception")
        .markRollbackOnly()
        .useOriginalMessage()
        .logStackTrace(true)        
        .end();

        onException(Exception.class)
        .log("Observed exception")
        .markRollbackOnly()
        .useOriginalMessage()
        .logStackTrace(true)
        .end();
        
        // Writer route
		from(timerExpression)
		.routeId("Aws-s3.Producer.Route")
		.process(empEventProcessor)		
		.to(awsUrl)
		.log("Content saved as aws-s3 file - ${header." + EmployeeEventProcessor.AWS_S3_KEY + "}")
		.end();
    }
}