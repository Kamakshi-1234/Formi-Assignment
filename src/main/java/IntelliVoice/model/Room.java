package IntelliVoice.model;

public class Room {
    private String name;
    private String amenities;
    private double pricePerNight;

    public Room() {}

    public Room(String name, String amenities, double pricePerNight) {
        this.name = name;
        this.amenities = amenities;
        this.pricePerNight = pricePerNight;
    }

    public String getName() {
        return name;
    }

    public String getAmenities() {
        return amenities;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }
}
