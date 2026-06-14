package pt.isep.sem4pi.domain.model.flight.dep_and_arr;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Date implements ValueObject, Serializable {

    @Column(name = "calendar_day")
    private Integer day;

    @Column(name = "calendar_month")
    private Integer month;

    @Column(name = "calendar_year")
    private Integer year;

    protected Date(){
        //for ORM
    }

    public Date(Integer day, Integer month, Integer year){
        List<Integer> validMonths = getMonthsWith31Days();

        //Impossible
        if (year < 0){
            throw new IllegalArgumentException("Year cannot be lower than 0");
        }
        if (month <= 0){
            throw new IllegalArgumentException("Month cannot be lower than 0");
        }
        if (day <= 0){
            throw new IllegalArgumentException("Day cannot be lower than 0");
        }
        if (month > 12){
            throw new IllegalArgumentException("Month cannot be higher than 12");
        }
        if (day > 31){
            throw new IllegalArgumentException("Day cannot be higher than 31");
        }

        //Specifics

        if (year < LocalDate.now().getYear()){
            throw new IllegalArgumentException("Year cannot be lower than the current year");
        }
        if (year == LocalDate.now().getYear()){
            if (month < LocalDate.now().getMonth().getValue()){
                throw new IllegalArgumentException("You cannot schedule a flight to a date prior than today");
            }
        }
        if (!validMonths.contains(month) && day > 30){
            throw new IllegalArgumentException("For that month, the day cannot be 31");
        }
        if (month == 2){
            if (day > 29){
                throw new IllegalArgumentException("For that month, the day cannot be higher than 29");
            }
            if (!hasExtraDay(year) && day == 29){
                throw new IllegalArgumentException("For that year, that month cannot have 29 days");
            }
        }

        this.day = day;
        this.month = month;
        this.year = year;
    }

    public List<Integer> getMonthsWith31Days(){
        List<Integer> valids = new ArrayList<>();
        Integer[] months = {1, 3, 5, 7, 8, 10, 12};
        for (int i = 0; i < months.length; i++){
            valids.add(months[i]);
        }
        return valids;
    }

    public Boolean hasExtraDay(Integer year){
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0){
            return true;
        }
        return false;
    }

    public static Date today(){
        return new Date(LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue(), LocalDate.now().getYear());
    }

    public int day() {
        return day;
    }

    public int month() {
        return month;
    }

    public int year() {
        return year;
    }
}
