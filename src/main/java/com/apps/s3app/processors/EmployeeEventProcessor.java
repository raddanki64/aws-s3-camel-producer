package com.apps.s3app.processors;

import  org.springframework.stereotype.Component;
import 	org.springframework.beans.factory.annotation.Value;

import  java.util.ArrayList;
import  java.util.List;

import  org.apache.camel.Exchange;
import  org.apache.camel.Processor;

import 	lombok.NoArgsConstructor;

import  org.slf4j.Logger;
import  org.slf4j.LoggerFactory;

import	com.apps.s3app.services.domain.Employee;

@Component
@NoArgsConstructor
public class EmployeeEventProcessor implements Processor {
    private static Logger logger = LoggerFactory.getLogger(EmployeeEventProcessor.class);
    
    public static String AWS_S3_KEY = "CamelAwsS3Key";
    public static String AWS_S3_CONTENT_LENGTH = "CamelAwsS3ContentLength";

    public void process(Exchange exchange) throws Exception {
        try {
            Employee anEmployee = Employee.createEmployee();
            String asJson = anEmployee.toJson();            	
                
            logger.info("Employee {},{} is being dispatched: ", anEmployee.getLastName(), anEmployee.getFirstName());
            
            exchange.getIn().setBody(asJson.getBytes());
            exchange.getIn().setHeader(AWS_S3_CONTENT_LENGTH, asJson.getBytes().length);
            
            String s3Filename = String.format("tst.%s_%s.json", anEmployee.getLastName(), anEmployee.getFirstName());
            exchange.getIn().setHeader(AWS_S3_KEY, s3Filename);
        }
        catch(Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
