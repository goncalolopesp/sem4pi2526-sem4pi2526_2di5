flight TP100 regular {
    aircraft CS-TST;
    pilot PILOT01;
    route TP100 { (41.0|-8.0) (40.0|-3.0) };
    leg {
        departure { OPO, 15/06/2028, 09:30:15 };
        arrival { MAD, 11:45 };
        segment { (41.0|-8.0), (40.0|-3.0), {(8000,10000)}, 450, 90, 5 };
        fuel { 4200, l };
    };
};
