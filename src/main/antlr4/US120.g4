grammar US120;

flightPlan : flight EOF ;

flight : FLIGHT ID flightType '{' leg+ '};' ;

flightType : REGULAR | CHARTER ;

leg : LEG '{' departure arrival route segment+ fuel '};' ;

departure : DEPARTURE '{' airport ',' date ',' time '};' ;

arrival : ARRIVAL '{' airport ',' time '};' ;

route : ROUTE ID '{' routes+ '};' ;

segment : SEGMENT '{' '(' beginNode ')' ',' '(' endNode ')' ',' '{' altSlot+ '}' ',' width ',' windAngle ',' speed '};' ;

beginNode : coord ;

endNode : coord ;

altSlot : '(' number ',' number ')' ;

width : number ;

windAngle : number ;

speed : number ;

coord : '-'? number '|' '-'? number ;

routes : '(' coord ')';

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

COMMENT : '#' ~[\r\n]* -> skip ;
WS : [ \t\r\n]+ -> skip ;