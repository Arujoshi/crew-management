package com.flight.service;

public interface RouteService {

	String getNextDestination(String currentDestination);
	int calculateFlightDuration(String source, String destination);
}
