flight FL100 REGULAR {

    leg {

        departure {
            PORT,
            15/06/2028,
            10:30:00
        };

        arrival {
            LPPT,
            12:00:00
        };

        route rt01;

        segment {
            (10 | 20),
            (30 | 40),
            5,
            3,
            45,
            250
        };

        fuel {
            100,
            l
        };

    };

};