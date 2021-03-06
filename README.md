# Course Management Service (SOAP Web Service Provider)

This project is a SOAP Web Service Provider that utilizes Contract First approach, which means that the developer defines the format of the request before the beginning of developing the service. 

## Process
For this application, XML request and response was chosen. Then, the XSD (access to the resources) is defined.
- Decide how the structure of request should look like.
- Decide how the structure of response should look like.
- Generate WSDL (Spring generates and according to our configuration).
- Configure JAXB (XML Java Binding) which converts XML notation to object notation in the Java application (our Web Service Application).
- Endpoint is responsible for accepting requests, process it and send the response back.
- Wizdler Chrome Plugin is used to mimic the SOAP Web Service Client.

### XML Structure
Define the structure for your request (input) and response (output):
```
input                     output
GetCourseDetailRequest -> GetCourseDetailsResponse
id -> 123						 "Spring Course"
```
The structure of request in XML:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<GetCourseDetailsRequest xmlns="http://gorbich.com/soap/courses">
  <id>123</id>
</GetCourseDetailsRequest>
```
The structure of response in XML:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<GetCourseDetailsResponse xmlns="http://gorbich.com/soap/courses">
  <CourseDetails>
    <id>123</id>
    <name>Spring Course</name>
    <description>I hope you will learn something</description>
  </CourseDetails>  
</GetCourseDetailsResponse>
```

### Validation for XML in XSD (Schema Definition)
Validate that the input comes in a correct way. For example, the course id request should contain only numbers. Put it to `course-details.xsd` file:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  targetNamespace="http://gorbich.com/soap/courses" 
  xmlns:tns="http://gorbich.com/soap/courses" 
  elementFormDefault="qualified">
  <xs:element name="GetCourseDetailsRequest"> 	
    <xs:complexType>
  	  <xs:sequence>
  	    <xs:element name="id" type="xs:integer"></xs:element>
  	  </xs:sequence>
  	</xs:complexType>
  </xs:element>
  
  <xs:element name="GetCourseDetailsResponse">  	
    <xs:complexType>
  	  <xs:sequence>
  	    <xs:element name="CourseDetails" type="tns:CourseDetails"></xs:element>
  	  </xs:sequence>
  	</xs:complexType>
  </xs:element>
  
  <xs:complexType name="CourseDetails">  
    <xs:sequence>
      <xs:element name="id" type="xs:integer" />
      <xs:element name="name" type="xs:string" />
      <xs:element name="description" type="xs:string" />
    </xs:sequence>
  </xs:complexType>
</xs:schema>
```
Assign schema location to the request XML file:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<GetCourseDetailsRequest xmlns="http://gorbich.com/soap/courses"
 xmlns:xsi="http://www.w3.org/201/XMLSchema-instance"
 xsi:schemaLocation="http://gorbich.com/soap/courses course-details.xsd">
 ...
 ```
 ### XML Binding
 Convert object from XML into Java object in the application and back. The JAXB wil take the schema definition file and create the objects. Make sure that the schema definition file is in the `resources` folder. Configure JAXB2 plugin in Maven pom file.
```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<!-- JAXB2 -->
			<plugin>
			    <groupId>org.codehaus.mojo</groupId>
			    <artifactId>jaxb2-maven-plugin</artifactId>
			    <version>1.6</version>
			    <executions>
			        <execution>
			            <id>xjc</id>
			            <goals>
			                <goal>xjc</goal>
			            </goals>
			        </execution>
			    </executions>
			    <configuration>
			        <schemaDirectory>${project.basedir}/src/main/resources</schemaDirectory>
			        <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
			        <clearOutputDir>false</clearOutputDir>
			    </configuration>
			</plugin>
		</plugins>
	</build>
```
### Creating Endpoint
Endpoint receives the request, processes it and sends response back.
The class is created to handle this procedure. The class has a method that
takes the request as a parameter and returns the response. `@Endpoint` annotation is added to the class and `@PayloadRoot()` for the method to specify namespace and localpart. Another annotation is added to the parameter of the method - @RequestPayload. It converts XML object to Java object. And still another to method, to return the object converted to XML.
```java
package com.gorbich.soap.webservices;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.gorbich.soap.courses.GetCourseDetailsRequest;
import com.gorbich.soap.courses.GetCourseDetailsResponse;

@Endpoint
public class CourseDetailsEndpoint {

	@PayloadRoot(namespace="http://gorbich.com/soap/courses",
			localPart="GetCourseDetailsRequest")
	@ResponsePayload
	public GetCourseDetailsResponse 
		processCourseDetailsRequest(@RequestPayload GetCourseDetailsRequest request) {	
		GetCourseDetailsResponse response = new GetCourseDetailsResponse();
		return response;
		
	}
}
```
Business logic is added (dummy data for example)
```java
...
CourseDetails courseDetails = new CourseDetails();
		courseDetails.setId(request.getId());
		courseDetails.setName("Microservices Course");
		courseDetails.setDescription("This is a must learn course");
		return response;
...
```
### Web Service Configuration
A `WebServiceConfig` class created to put web service configuration in.
`@EnableWs` and `@Configuration` annotations were added. Message Dispatcher Servlet is added and mapped to the specific URL.
```java
package com.gorbich.soap.webservices;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

@EnableWs
@Configuration
public class WebServiceConfig {

	@Bean
	public ServletRegistrationBean messageDispatcherServlet(ApplicationContext context) {
		MessageDispatcherServlet messageDispatcherServlet = new MessageDispatcherServlet();
		messageDispatcherServlet.setApplicationContext(context);
		messageDispatcherServlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean(messageDispatcherServlet, "/ws/*");
	}
}
```
### WSDL Configuration
First, schema is defined by creating an `XsdSchema` and `DefaultWsdl11Definition` beans:
```java
...
	@Bean
	public XsdSchema coursesSchema(){
		return new SimpleXsdSchema(new ClassPathResource("course-details.xsd"));
	}
	
	@Bean(name="courses")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema coursesSchema){
		DefaultWsdl11Definition definition = new DefaultWsdl11Definition();	
		definition.setPortTypeName("CoursePort");
		definition.setTargetNamespace("http://gorbich.com/soap/courses");
		definition.setLocationUri("/ws");
		definition.setSchema(coursesSchema);
		return definition;
	}
...
```
Added `wsdl4j` dependency into `pom.xml` file.