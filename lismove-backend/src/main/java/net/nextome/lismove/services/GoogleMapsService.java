package net.nextome.lismove.services;

import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Address;
import net.nextome.lismove.models.Partial;
import net.nextome.lismove.repositories.CityRepository;
import net.nextome.lismove.services.utils.WaypointsList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleMapsService {
	@Autowired
	private GeoApiContext geoApiContext;
	@Autowired
	private CityRepository cityRepository;

	public WaypointsList.WaypointsListResult generateWaypointsListResult(List<Partial> partials) throws IOException, InterruptedException, ApiException {
		WaypointsList waypointsList = new WaypointsList();
		partials.forEach(
				partial -> waypointsList.add(new LatLng(partial.getLatitude(), partial.getLongitude()))
		);
		return generateWaypointsListResult(waypointsList);
	}

	/**
	 * It takes the WaypointsList and performs as many requests as the waypoints arrays are
	 *
	 * @return An instance of WaypointsListResult with the list of legs and the polyline
	 */
	public WaypointsList.WaypointsListResult generateWaypointsListResult(WaypointsList list) throws IOException, InterruptedException, ApiException {
		StringBuilder polyline = new StringBuilder();
		ArrayList<DirectionsLeg> directionsLegs = new ArrayList<>();

		boolean first = true;
		for(LatLng[] waypoints : list.getTrimmedList()) {
			DirectionsResult response = directionsApiResult(waypoints);
			if(response != null) {
//            Getting polyline
				if(first) {
					polyline.append(response.routes[0].overviewPolyline.getEncodedPath());
					first = false;
				} else {
					polyline.append(response.routes[0].overviewPolyline.getEncodedPath().substring(10));
				}
//            Getting distances
				Collections.addAll(directionsLegs, response.routes[0].legs);
			}
		}
		return new WaypointsList.WaypointsListResult(directionsLegs, polyline.toString());
	}

	public LatLng generateLatLng(Address address) throws ApiException, InterruptedException, IOException {
		return generateLatLng(address.formatAddress());
	}

	public LatLng generateLatLng(String address) throws ApiException, InterruptedException, IOException {
		return geocodingApiResult(address)[0].geometry.location;
	}

	/**
	 * popola l' oggetto Address in base all' indirizo mandato come stringa
	 */
	public Address generateAddress(String address) throws ApiException, InterruptedException, IOException {
		GeocodingResult[] result = geocodingApiResult(address);
		Address a = new Address();
		mapResult(a, result, true);
		return a;
	}

	public GeocodingResult[] geocodingApiResult(String address) throws ApiException, InterruptedException, IOException {
		GeocodingResult[] result = GeocodingApi.newRequest(geoApiContext)
				.address(address)
				.locationType(LocationType.APPROXIMATE)
				.resultType(AddressType.STREET_ADDRESS)
				.await();
		if(result != null && result.length > 0) {
			return result;
		} else {
			throw new LismoveException("Non-existent address");
		}
	}

	public Address reverseGeocoding(Double lat, Double lng) {
		Address address = new Address();
		try {
			GeocodingResult[] result = GeocodingApi.reverseGeocode(geoApiContext, new LatLng(lat, lng)).locationType(LocationType.ROOFTOP).resultType(AddressType.STREET_ADDRESS, AddressType.ROUTE, AddressType.STREET_NUMBER).await();
			mapResult(address, result);
			if(address.getAddress() == null) {//se non è presente un address facico un reverse geocoding senza considerare il numero civico
				result = GeocodingApi.reverseGeocode(geoApiContext, new LatLng(lat, lng)).await();
				mapResult(address, result);
			}

		} catch(ApiException | InterruptedException | IOException e) {
			e.printStackTrace();
		}
		return address;
	}

	private void mapResult(Address address, GeocodingResult[] result) {
		mapResult(address, result, false);
	}

	/**
	 * mappa i risultati del geocoding nell' oggetto Address
	 * @param address
	 * @param result
	 * @param mapLatLng indica se inserire nell'address anche informazioni su latitudine e longitudine
	 */
	private void mapResult(Address address, GeocodingResult[] result, boolean mapLatLng) {
		for(GeocodingResult r : result) {
			if(mapLatLng) {
				address.setLatitude(r.geometry.location.lat);
				address.setLongitude(r.geometry.location.lng);
			}
			for(AddressComponent ac : r.addressComponents) {
				if(Arrays.asList(ac.types).contains(AddressComponentType.STREET_NUMBER)) {
					address.setNumber(ac.longName);
				}
				if(Arrays.asList(ac.types).contains(AddressComponentType.STREET_ADDRESS) || Arrays.asList(ac.types).contains(AddressComponentType.ROUTE)) {
					address.setAddress(ac.longName);
				}
				if(Arrays.asList(ac.types).contains(AddressComponentType.POLITICAL)) {
					cityRepository.findByCity(ac.longName).ifPresent(address::setCity);
				}
			}
		}
	}

	public DirectionsResult directionsApiResult(LatLng[] waypoints) throws ApiException, InterruptedException, IOException {
		return DirectionsApi.newRequest(geoApiContext)
				.units(Unit.METRIC)
				.mode(TravelMode.WALKING)
				.origin(waypoints[0])
				.destination(waypoints[waypoints.length - 1])
				.alternatives(false)
				.waypoints(Arrays.copyOfRange(waypoints, waypoints.length > 1 ? 1 : 0, waypoints.length - 1))
				.await();
	}

	public String getEncodedPolyline(List<Partial> partials) {
		return new EncodedPolyline(partials.stream().filter(p -> p.getLatitude() != null && p.getLongitude() != null).map(p -> new LatLng(p.getLatitude(), p.getLongitude())).collect(Collectors.toList())).getEncodedPath();
	}

	/**
	 * genera una lista di possibili indirizzi in base alla stringa in input
	 * @param address indirizzo da autocompletare
	 * @param token un token di sessione, deve rimanere uguale da qaundo l'utente inizia a scrivere a quando seleziona un luogo
	 * @return list of possible addresses
	 */
	public List<String> addressAutocomplete(String address, PlaceAutocompleteRequest.SessionToken token) throws IOException, InterruptedException, ApiException {
		AutocompletePrediction[] response = PlacesApi.placeAutocomplete(geoApiContext, address, token).await();
		return Arrays.stream(response).map((AutocompletePrediction item) -> item.description).collect(Collectors.toList());
	}
}
