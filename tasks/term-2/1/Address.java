class Address {
    public String city;
    public String street;
    public int number;

    @Override
    public String toString() {
        return "Address{city='" + city + "', street='" + street +
                "', number=" + number + "}";
    }
}
