package pt.isep.sem4pi.domain.model.flight.dep_and_arr;


import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class Time implements ValueObject, Serializable {

    private Integer hour;
    private Integer minute;
    private Integer second;

    protected Time(){
        //for ORM
    }

    public Time(Integer hour, Integer minute, Integer second){
        if (hour < 0){
            throw new IllegalArgumentException("Hours cannot be lower than 0");
        }
        if (minute < 0){
            throw new IllegalArgumentException("Minutes cannot be lower than 0");
        }
        if (second < 0){
            throw new IllegalArgumentException("Seconds cannot be lower than 0");
        }
        if (hour > 23){
            throw new IllegalArgumentException("Hour cannot be higher than 23");
        }
        if (minute > 59){
            throw new IllegalArgumentException("Minutes cannot be higher than 59");
        }
        if (second > 59){
            throw new IllegalArgumentException("Seconds cannot be higher than 59");
        }

        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public int hour() {
        return hour;
    }

    public int minute() {
        return minute;
    }

    public int second() {
        return second;
    }
}
