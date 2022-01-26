package tn.rabini.petadoption.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.maps.android.SphericalUtil;

import java.util.Comparator;

public class Pet {
    private String id, name, race, age, gender, description, lat, lng, image, owner, type;
    private boolean ready;
    private double distance;
    private long publishedDate;

    public Pet() {
    }

    public Pet(String id, String name, String race, String age, String gender,
               String description, String lat, String lng, String image,
               String owner, String type, boolean ready, long publishedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.race = race;
        this.age = age;
        this.gender = gender;
        this.image = image;
        this.owner = owner;
        this.type = type;
        this.ready = ready;
        this.publishedDate = publishedDate;
        this.distance = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getDescription() {
        return description;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getImage() {
        return image;
    }

    public boolean isReady() {
        return ready;
    }

    public String getOwner() {
        return owner;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(long publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public static Comparator<DataSnapshot> distanceComparator(double lat, double lng) {
        return (dt1, dt2) -> {
            Pet d1 = dt1.getValue(Pet.class);
            Pet d2 = dt2.getValue(Pet.class);
            if (d1 != null && d2 != null) {
                LatLng myLoc = new LatLng(lat, lng);
                LatLng loc1 = new LatLng(Double.parseDouble(d1.getLat()), Double.parseDouble(d1.getLng()));
                LatLng loc2 = new LatLng(Double.parseDouble(d2.getLat()), Double.parseDouble(d2.getLng()));
                double distance1 = SphericalUtil.computeDistanceBetween(myLoc, loc1);
                double distance2 = SphericalUtil.computeDistanceBetween(myLoc, loc2);
                return Double.compare(distance1, distance2);
            }
            return 0;
        };
    }

    public static Comparator<DataSnapshot> raceComparator() {
        return (dt1, dt2) -> {
            Pet d1 = dt1.getValue(Pet.class);
            Pet d2 = dt2.getValue(Pet.class);
            if (d1 != null && d2 != null)
                return d1.getRace().compareTo(d2.getRace());
            return 0;
        };
    }

    public static Comparator<DataSnapshot> nameComparator() {
        return (dt1, dt2) -> {
            Pet d1 = dt1.getValue(Pet.class);
            Pet d2 = dt2.getValue(Pet.class);
            if (d1 != null && d2 != null)
                return d1.getName().compareTo(d2.getName());
            return 0;
        };
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", race='" + race + '\'' +
                ", age='" + age + '\'' +
                ", gender='" + gender + '\'' +
                ", description='" + description + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", image='" + image + '\'' +
                ", owner='" + owner + '\'' +
                ", type='" + type + '\'' +
                ", ready=" + ready +
                ", distance=" + distance +
                ", publishedDate=" + publishedDate +
                '}';
    }
}
