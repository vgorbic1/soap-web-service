package com.gorbich.soap.webservices;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.gorbich.soap.courses.CourseDetails;
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
		CourseDetails courseDetails = new CourseDetails();
		courseDetails.setId(request.getId());
		courseDetails.setName("Microservices Course");
		courseDetails.setDescription("This is a must have course");
		return response;
	}
}
