grammar US120;

flightPlan : flight EOF ;

flight : FLIGHT ID flightType '{' aircraft pilot route leg+ '};' ;

flightType : REGULAR | CHARTER ;

aircraft : AIRCRAFT registration ';' ;

pilot : PILOT pilotId ';' ;

route : ROUTE ID '{' routes+ '};' ;

leg : LEG '{' departure arrival segment+ fuel '};' ;

departure : DEPARTURE '{' airport ',' date ',' time '};' ;

arrival : ARRIVAL '{' airport ',' time '};' ;

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

registration : REGISTRATION | ID ;

pilotId : ID ;

airport : IATA | ICAO ;

date : DATE_LIT ;

time : TIME_LIT ;

number : INT | FLOAT ;

unit : UNIT_KG | UNIT_L | UNIT_M | UNIT_MS ;

FLIGHT : [fF][lL][iI][gG][hH][tT] ;
LEG : [lL][eE][gG] ;
AIRCRAFT : [aA][iI][rR][cC][rR][aA][fF][tT] ;
PILOT : [pP][iI][lL][oO][tT] ;
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

REGISTRATION : [A-Za-z] [A-Za-z0-9]* ('-' [A-Za-z0-9]+)+ ;
ID : [a-zA-Z][a-zA-Z0-9]* ;
FLOAT : [0-9]+ '.' [0-9]+ ;
INT : [0-9]+ ;

COMMENT : '#' ~[\r\n]* -> skip ;
WS : [ \t\r\n]+ -> skip ;
