grammar US81;

@header {
    import java.util.*;
}

@members {

    private void validateDate(String dateStr, int line) {

        List<String> parts = Arrays.asList(dateStr.split("/"));

        if (parts.size() != 3) {
            System.out.println("Line " + line + ": Invalid date -> " + dateStr);
            return;
        }

        int day = Integer.parseInt(parts.get(0));
        int month = Integer.parseInt(parts.get(1));
        int year = Integer.parseInt(parts.get(2));

        if (day < 1 || day > 31 || month < 1 || month > 12) {
            System.out.println("Line " + line + ": Invalid date -> " + dateStr);
            return;
        }

        if (month == 2) {

            boolean leapYear =
                (year % 4 == 0 && year % 100 != 0)
                || (year % 400 == 0);

            if ((leapYear && day > 29) ||
                (!leapYear && day > 28)) {

                System.out.println("Line " + line + ": Invalid date -> " + dateStr);
            }

        } else if (
            month == 4 ||
            month == 6 ||
            month == 9 ||
            month == 11) {

            if (day > 30) {
                System.out.println("Line " + line + ": Invalid date -> " + dateStr);
            }
        }
    }

    private void validateTime(String timeStr, int line) {

        List<String> parts = Arrays.asList(timeStr.split(":"));

        if (parts.size() != 3) {
            System.out.println("Line " + line + ": Invalid time -> " + timeStr);
            return;
        }

        int hour = Integer.parseInt(parts.get(0));
        int minute = Integer.parseInt(parts.get(1));
        int second = Integer.parseInt(parts.get(2));

        if (hour < 0 || hour > 23 ||
            minute < 0 || minute > 59 ||
            second < 0 || second > 59) {

            System.out.println("Line " + line + ": Invalid time -> " + timeStr);
        }
    }

    private void validateAngle(String angleStr, int line) {

        double angle = Double.parseDouble(angleStr);

        if (angle < 0 || angle >= 360) {
            System.out.println("Line " + line + ": Invalid angle -> " + angleStr);
        }
    }
}

flightPlan : flight EOF ;

flight : FLIGHT ID flightType '{' leg+ '};' ;

flightType : REGULAR | CHARTER ;

leg : LEG '{'
        departure
        arrival
        route
        segment+
        fuel
      '};'
    ;

departure
    : DEPARTURE '{'
        airport ','
        d1=date {
            validateDate($d1.text, $d1.start.getLine());
        } ','
        t1=time {
            validateTime($t1.text, $t1.start.getLine());
        }
      '};'
    ;

arrival
    : ARRIVAL '{'
        airport ','
        t2=time {
            validateTime($t2.text, $t2.start.getLine());
        }
      '};'
    ;

route : ROUTE ID ';' ;

segment
    : SEGMENT '{'
        '(' coord ')' ','
        '(' coord ')' ','
        altitude ','
        width ','
        windAngle ','
        speed
      '};'
    ;

altitude : number ;

width : number ;

windAngle
    : ang=number {
        validateAngle($ang.text, $ang.start.getLine());
    }
    ;

speed : number ;

coord : number '|' number ;

fuel
    : FUEL '{'
        number ','
        unit
      '};'
    ;

airport : IATA | ICAO ;

date : DATE ;

time : TIME ;

number : '-'? (INT | FLOAT) ;

unit : KG | L | M | MS ;

FLIGHT : [fF][lL][iI][gG][hH][tT] ;
LEG : [lL][eE][gG] ;
DEPARTURE : [dD][eE][pP][aA][rR][tT][uU][rR][eE] ;
ARRIVAL : [aA][rR][rR][iI][vV][aA][lL] ;
ROUTE : [rR][oO][uU][tT][eE] ;
SEGMENT : [sS][eE][gG][mM][eE][nN][tT] ;
FUEL : [fF][uU][eE][lL] ;

REGULAR : [rR][eE][gG][uU][lL][aA][rR] ;
CHARTER : [cC][hH][aA][rR][tT][eE][rR] ;

KG : 'kg' ;
L : 'l' ;
M : 'm' ;
MS : 'm/s' ;

DATE : [0-9]+ '/' [0-9]+ '/' [0-9]+ ;
TIME : [0-9]+ ':' [0-9]+ ':' [0-9]+ ;

IATA : [A-Za-z][A-Za-z][A-Za-z] ;
ICAO : [A-Za-z][A-Za-z][A-Za-z][A-Za-z] ;

ID : [a-zA-Z][a-zA-Z0-9]* ;

FLOAT : [0-9]+ '.' [0-9]+ ;
INT : [0-9]+ ;

COMMENT : '#' ~[\r\n]* -> skip ;

WS : [ \t\r\n]+ -> skip ;