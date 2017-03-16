package vouchr.coffee.models;

/**
 * Created by Bryan on 2017-03-16.
 */

public class CoffeePot {

    private String dateString;
    private String barista;
    private String beanName;
    private String roast;
    private Float tbspCount;
    private Double avgRating;

    CoffeePot(String dateString, String barista, String beanName, String roast, Float tbspCount, Double avgRating) {
        this.dateString = dateString;
        this.barista = barista;
        this.beanName = beanName;
        this.roast = roast;
        this.tbspCount = tbspCount;
        this.avgRating = avgRating;
    }

    public String getDateString() {
        return dateString;
    }

    public String getBarista() {
        return barista;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getRoast() {
        return roast;
    }

    public Float getTbspCount() {
        return tbspCount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    @Override
    public String toString() {
        return dateString + "," + barista + "," + beanName + "," + roast + "," + tbspCount + "," + avgRating;
    }
}