grammar US83;

@header {
    import java.util.*;
}

@members {
    private void validateDate(String dateStr, int linha) {
        List<String> dateParts = Arrays.asList(dateStr.split("/"));
        if (dateParts.size() != 3) {
            System.out.println("Line " + linha + " :  Invalid date: " + dateStr);
            return;
        }
        int day = Integer.parseInt(dateParts.get(0));
        int month = Integer.parseInt(dateParts.get(1));
        int year = Integer.parseInt(dateParts.get(2));
        if (day < 1 || day > 31 || month < 1 || month > 12) {
            System.out.println("Line " + linha + " :  Invalid date: " + dateStr);
            return;
        }
        if (month == 2) {
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                if (day > 29) {
                    System.out.println("Line " + linha + " :  Invalid date: " + dateStr);
                    return;
                }
            } else {
                if (day > 28) {
                    System.out.println("Line " + linha + " :  Invalid date: " + dateStr);
                    return;
                }
            }
        } else if (Arrays.asList(4, 6, 9, 11).contains(month)) {
            if (day > 30) {
                System.out.println("Line " + linha + " :  Invalid date: " + dateStr);
                return;
            }
        }
    }

    private void validateTime(String timeStr, int linha) {
        List<String> timeParts = Arrays.asList(timeStr.split(":"));
        if (timeParts.size() < 2 || timeParts.size() > 3) {
            System.out.println("Line " + linha + " :  Invalid time: " + timeStr);
            return;
        }
        if (timeParts.size() == 2) {
            int hour = Integer.parseInt(timeParts.get(0));
            int minute = Integer.parseInt(timeParts.get(1));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                System.out.println("Line " + linha + " :  Invalid time: " + timeStr);
                return;
            }
        } else {
            int hour = Integer.parseInt(timeParts.get(0));
            int minute = Integer.parseInt(timeParts.get(1));
            int second = Integer.parseInt(timeParts.get(2));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                System.out.println("Line " + linha + " :  Invalid time: " + timeStr);
                return;
            }
        }
    }

    private void validateAngle(String angleStr, int linha) {
        double angle = Double.parseDouble(angleStr);
        if (angle < 0 || angle >= 360) {
            System.out.println("Line " + linha + " :  Invalid angle: " + angleStr);
            return;
        }
    }
}

flightPlan : flight EOF ;

flight : FLIGHT ID flightType '{' leg+ '};' ;

flightType : REGULAR | CHARTER ;

leg : LEG '{' departure arrival route segment+ fuel '};' ;

departure : DEPARTURE '{' airport ',' d1=date {
        validateDate($d1.text, $d1.start.getLine());
}',' t1=time {
        validateTime($t1.text, $t1.start.getLine());
} '};' ;

arrival : ARRIVAL '{' airport ',' t2=time {
        validateTime($t2.text, $t2.start.getLine());
} '};' ;

route : ROUTE ID ';' ;

segment : SEGMENT '{' '(' beginNode ')' ',' '(' endNode ')' ',' altSlot ',' width ',' windAngle ',' speed '};' ;

beginNode : coord ;

endNode : coord ;

altSlot : number ;

width : number ;

windAngle : ang=number {
    validateAngle($ang.text, $ang.start.getLine());
} ;

speed : number ;

coord : '-'? number '|' '-'? number ;

fuel : FUEL '{' number ',' unit '};' ;

airport : IATA | ICAO ;

date : DATE_LIT ;

time : TIME_LIT ;

number : INT | FLOAT ;

unit : UNIT_KG | UNIT_L | UNIT_M | UNIT_MS ;

FLIGHT : [fF][lL][iI][gG][hH][tT] ;
LEG : [lL][eE][gG] ;
DEPARTURE : [dD][eE][pP][aA][rR][tT][uU][rR][eE] ;
ARRIVAL : [aA][rR][rR][iI][vV][aA][lL] ;
ROUTE : [rR][oO][uU][tT][eE] ;
SEGMENT : [sS][eE][gG][mM][eE][nN][tT] ;
FUEL : [fF][uU][eE][lL] ;
REGULAR : [rR][eE][gG][uU][lL][aA][rR] ;
CHARTER : [cC][hH][aA][rR][tT][eE][rR] ;

DATE_LIT : [0-9]+ '/' [0-9]+ '/' [0-9]+ ;
TIME_LIT : [0-9]+ ':' [0-9]+ (':' [0-9]+)? ;
UNIT_KG : 'kg' ;
UNIT_L : 'l' ;
UNIT_M : 'm' ;
UNIT_MS : 'm/s' ;

IATA : [A-Za-z][A-Za-z][A-Za-z] ;
ICAO : [A-Za-z][A-Za-z][A-Za-z][A-Za-z] ;

ID : [a-zA-Z][a-zA-Z0-9]* ;
FLOAT : [0-9]+ '.' [0-9]+ ;
INT : [0-9]+ ;

WS : [ \t\r\n]+ -> skip ;