package net.nextome.lismove.services.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Stream;

public class UtilitiesService {
	/**
	 * modifica i campi non nulli della source nel target
	 *
	 * @param src    oggetto da cui prendere i campi
	 * @param target oggetto i cui campi verranno modificati
	 * @param ignore lista di campi da ignorare
	 */
	public static void notNullBeanCopy(Object src, Object target, String... ignore) {
		BeanWrapper wrappedSource = new BeanWrapperImpl(src);
		String[] nullPropertyNames = Stream.of(wrappedSource.getPropertyDescriptors())
				.map(FeatureDescriptor::getName)
				.filter(propertyName -> Arrays.asList(ignore).contains(propertyName) || wrappedSource.getPropertyValue(propertyName) == null)
				.toArray(String[]::new);
		BeanUtils.copyProperties(src, target, nullPropertyNames);
	}

	/**
	 * Calculates distance between two points in latitude and longitude. Uses Haversine method as its base.
	 *
	 * @return Distance in Meters
	 */
	public static double distance(double originLat, double originLng, double destLat, double destLng) {
		double earthRadius = 6371;
		double dLat = Math.toRadians(destLat - originLat);
		double dLng = Math.toRadians(destLng - originLng);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2)
				+ Math.pow(sindLng, 2) * Math.cos(Math.toRadians(originLat)) * Math.cos(Math.toRadians(destLat));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c * 1000;
		return Math.round(dist * 100.0) / 100.0;
	}

	public static double distanceKm(double originLat, double originLng, double destLat, double destLng) {
		return distance(originLat, originLng, destLat, destLng) / 1000;
	}

	public static double acceleration(double oldDistance, double newDistance, double duration) {
		return (newDistance - oldDistance) / (duration * duration);
	}

	/**
	 * Calculates speed in km/s
	 *
	 * @param distance in m
	 * @param time     in seconds
	 * @return the result
	 */
	public static BigDecimal speed(BigDecimal distance, long time) {
		return distance.divide(BigDecimal.valueOf(time), 5, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(3.6), new MathContext(5, RoundingMode.HALF_UP));
	}

	public static boolean notNullAndNotEqual(Object a, Object b) {
		return a != null && b != null && !a.equals(b);
	}

	public static boolean notNullAndEqual(Object a, Object b) {
		return a != null && b != null && a.equals(b);
	}

	public Integer[] getInterval(String value) {
		Integer[] interval = {null, null};
		try {
			interval[0] = Integer.parseInt(value.split(" - ")[0]);
		} catch(ArrayIndexOutOfBoundsException | NumberFormatException ignored) {

		}
		try {
			interval[1] = Integer.parseInt(value.split(" - ")[1]);
		} catch(ArrayIndexOutOfBoundsException | NumberFormatException ignored) {

		}
		return interval;
	}

	/**
	 * Calculates random number between min and max included
	 *
	 * @param min minimum value
	 * @param max maximum value
	 * @return the result
	 */
	public static int randomNumber(int min, int max) {
		return (int) ((Math.random() * ((max + 1) - min)) + min);
	}

	/**
	 * @param percent tolerance accepted. Its value is between 0 and 1
	 * @return true if num is bigger/smaller than den within the specified percentage; false otherwise
	 */
	public boolean isInPercent(BigDecimal a, BigDecimal b, BigDecimal percent) {
		BigDecimal num = a.min(b);
		BigDecimal den = a.max(b);
		return num.divide(den, RoundingMode.HALF_UP).compareTo(percent) >= 0;
	}

	public boolean isInPercent(BigDecimal num, BigDecimal den, Double percent) {
		if(num.equals(BigDecimal.ZERO) || den.equals(BigDecimal.ZERO))
			return num.subtract(den).abs().compareTo(new BigDecimal("0.05")) < 0;
		return isInPercent(num, den, BigDecimal.valueOf(percent));
	}

	public boolean nullOrEmptyOrBlank(String s){ //return true if the string passed is null, blank or empty
		return s == null || s.isEmpty() || s.trim().isEmpty();
	}
}
