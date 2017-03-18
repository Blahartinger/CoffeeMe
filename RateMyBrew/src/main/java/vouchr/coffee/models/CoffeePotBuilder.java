package vouchr.coffee.models;

public class CoffeePotBuilder {
    private String dateString;
    private String barista;
    private String beanName;
    private String roast;
    private Float tbspCount;
    private Double avgRating;

    private CoffeePotBuilder() {
    }

    public static CoffeePotBuilder init() {
        return new CoffeePotBuilder();
    }

    public static CoffeePotBuilder coffeePotBuilderFromCoffeePot(CoffeePot pot) {
        return CoffeePotBuilder.init().setDateString(pot.getDateString())
                .setBarista(pot.getBarista())
                .setBeanName(pot.getBeanName())
                .setRoast(pot.getRoast())
                .setTbspCount(pot.getTbspCount())
                .setAvgRating(pot.getAvgRating());
    }

    public CoffeePotBuilder setDateString(String dateString) {
        this.dateString = dateString;
        return this;
    }

    public CoffeePotBuilder setBarista(String barista) {
        this.barista = barista;
        return this;
    }

    public CoffeePotBuilder setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public CoffeePotBuilder setRoast(String roast) {
        this.roast = roast;
        return this;
    }

    public CoffeePotBuilder setTbspCount(Float tbspCount) {
        this.tbspCount = tbspCount;
        return this;
    }

    public CoffeePotBuilder setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
        return this;
    }

    public CoffeePot createCoffeePot() {
        if(isValidPot()) {
            return new CoffeePot(dateString, barista, beanName, roast, tbspCount, avgRating);
        } else {
            return null;
        }
    }

    public boolean isValidPot() {
        return dateString != null && barista != null && beanName != null && roast != null && tbspCount != null;
    }
}