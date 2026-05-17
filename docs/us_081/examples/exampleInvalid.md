flight FL123 INVALID {

    leg {

        departure {
            P1,          # Erro Sintático: Aeroporto com formato inválido (apenas 2 caracteres)
            32/13/2026,  # Erro Semântico: Mês 13 e Dia 32 não existem
            25:90:00     # Erro Semântico: Hora 25 e Minuto 90 não existem
        };

        arrival {
            LPPT,
            12:00:00
        };

        route 123route;

        segment {
            (10 | 20),
            (30 | 40),
            5,
            3,
            400,         # Erro Semântico: Ângulo do vento não existe (>= 360)
            250
        };

        fuel {
            100,
            l
        };

    };

};