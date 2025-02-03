package com.example.realestate.util;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

public class UTMConverter {

    public static double[] convertUTMToLatLon(double easting, double northing) {
        CRSFactory crsFactory = new CRSFactory();
        // Use EPSG:26912 for NAD83 / UTM zone 12N
        CoordinateReferenceSystem utmCRS = crsFactory.createFromName("EPSG:26912");
        CoordinateReferenceSystem wgs84CRS = crsFactory.createFromName("EPSG:4326");

        CoordinateTransform transform = new BasicCoordinateTransform(utmCRS, wgs84CRS);
        ProjCoordinate utmCoord = new ProjCoordinate(easting, northing);
        ProjCoordinate latLonCoord = new ProjCoordinate();

        transform.transform(utmCoord, latLonCoord);

        return new double[]{latLonCoord.y, latLonCoord.x}; // [latitude, longitude]
    }
}
