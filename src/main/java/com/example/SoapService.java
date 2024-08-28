package com.example;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

@WebService
public class SoapService {

    @WebMethod
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }

    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8081/SoapService", new SoapService());
        System.out.println("Service is running at http://localhost:8081/SoapService?wsdl");
    }
}
