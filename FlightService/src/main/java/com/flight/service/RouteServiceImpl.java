package com.flight.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class RouteServiceImpl implements RouteService {

	private static final double AVERAGE_FLIGHT_SPEED_KMH = 900.0; // Average cruising speed
	private static final int TAXI_TAKEOFF_LANDING_TIME = 30; // Additional time in minutes
	private static final double EARTH_RADIUS_KM = 6371.0; // Radius of Earth in kilometers

	private static final Map<String, String> ROUTE_MAP = new HashMap<>();

	static {
		// Define routes: current -> next destination
		ROUTE_MAP.put("New York", "London");
		ROUTE_MAP.put("London", "Paris");
		ROUTE_MAP.put("Paris", "Tokyo");
		ROUTE_MAP.put("Tokyo", "Dubai");
		ROUTE_MAP.put("Dubai", "Sydney");
		ROUTE_MAP.put("Sydney", "Singapore");
		ROUTE_MAP.put("Singapore", "New York");
	}

	@Override
	public String getNextDestination(String currentDestination) {
		String nextDestination = ROUTE_MAP.get(currentDestination);

		if (nextDestination == null) {
			throw new IllegalArgumentException("No route defined for the current destination: " + currentDestination);
		}

		return nextDestination;
	}

	@Override
	public int calculateFlightDuration(String source, String destination) {
		// Previous method implementation
		double[] sourceCoords = getCoordinates(source);
		double[] destCoords = getCoordinates(destination);

		if (sourceCoords == null || destCoords == null) {
			throw new IllegalArgumentException("Invalid source or destination: " + source + " -> " + destination);
		}

		double distance = calculateDistance(sourceCoords[0], sourceCoords[1], destCoords[0], destCoords[1]);
		double flightHours = distance / AVERAGE_FLIGHT_SPEED_KMH; // Average flight speed in km/h
		int flightMinutes = (int) (flightHours * 60);

		return flightMinutes + TAXI_TAKEOFF_LANDING_TIME; // Add taxiing, takeoff, and landing time
	}

	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);

		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_KM * c; // Earth radius in kilometers
	}

	private double[] getCoordinates(String city) {
		switch (city.toLowerCase()) {
		case "new york":
			return new double[] { 40.7128, -74.0060 };
		case "london":
			return new double[] { 51.5074, -0.1278 };
		case "paris":
			return new double[] { 48.8566, 2.3522 };
		case "tokyo":
			return new double[] { 35.6895, 139.6917 };
		case "dubai":
			return new double[] { 25.276987, 55.296249 };
		case "sydney":
			return new double[] { -33.8688, 151.2093 };
		case "singapore":
			return new double[] { 1.3521, 103.8198 };
		default:
			return null;
		}
	}
}
