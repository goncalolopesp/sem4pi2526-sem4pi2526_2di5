flight TP100 regular {
    aircraft CS-TST;
    pilot PILOT01;
    route TP100 { (41.0|-8.0) (40.0|-3.0) };
    leg {
        departure {
            P1,          # Syntactic error: invalid airport format (only 2 characters)
            32/13/2026,  # Semantic error: month 13 and day 32 do not exist
            25:90:00     # Semantic error: hour 25 and minute 90 do not exist
        };
        arrival { MAD, 11:00 };
        segment { (41.0|-8.0), (40.0|-3.0), {(8000,10000)}, 450, 400, 5 };
        fuel { 4200, kg };
    };
};
