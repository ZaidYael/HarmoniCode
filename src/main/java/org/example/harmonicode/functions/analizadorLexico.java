package org.example.harmonicode.functions;

import org.example.harmonicode.models.Token;
import org.example.harmonicode.models.Tokens;

import java.util.ArrayList;
import java.util.List;

public class analizadorLexico extends Lexico {

    public List<Token> analizar(String codigoFuente) {
        List<Token> tokensReconocidos = new ArrayList<>();
        int estadoActual = q0;
        StringBuilder lexema = new StringBuilder();
        int fila = 1;
        int columnaActual = 1;

        for (int i = 0; i < codigoFuente.length(); i++) {
            char c = codigoFuente.charAt(i);

            if (c == '\n') {
                fila++;
                columnaActual = 1;
                continue;
            }

            int columna = obtenerIndiceCaracter(c); // columna del alfabeto
            if (columna == -1) {
                if (estadoActual != q0 && lexema.length() > 33) {
                    System.out.println("entra en estado de error");
                    tokensReconocidos.add(new Token(lexema.toString(), Tokens.Error, fila, columnaActual));
                    lexema.setLength(0);
                    estadoActual = q0;
                }
                columnaActual++;
                continue;
            }

            int siguienteEstado = matrizTransicion[estadoActual][columna];

            if ((siguienteEstado >= 200 && siguienteEstado <300) ||
                    (siguienteEstado >= 400)) {
                tokensReconocidos.add(new Token(lexema.toString(), nombreToken(siguienteEstado), fila, columnaActual));
                lexema.setLength(0);
                estadoActual = q0;
                i--;
            } else if (siguienteEstado >= 300) {
                lexema.append(c);
                tokensReconocidos.add(new Token(lexema.toString(), nombreToken(siguienteEstado), fila, columnaActual));
                lexema.setLength(0);
            }else if (siguienteEstado == 0) {
                System.out.println("entra a esto");
                if (estadoActual != q0 && lexema.length() > 0) {
                    tokensReconocidos.add(new Token(lexema.toString(), Tokens.Error, fila, columnaActual));
                    lexema.setLength(0);
                }
                estadoActual = q0;
            } else {
                estadoActual = siguienteEstado;
                lexema.append(c);
            }

            columnaActual++;
        }
        System.out.println(tokensReconocidos);
        return tokensReconocidos;
    }


    private int obtenerIndiceCaracter(char c) {
        for (int i = 0; i < alfabeto.length; i++) {
            if (alfabeto[i] == c) return i;
        }
        return -1;
    }

    private Tokens nombreToken(int token) {
        return switch (token) {
            case 200, 210, 220, 230-> Tokens.Operacion;
            case 240 -> Tokens.Declaracion;
            case 300-> Tokens.ParentesisIzq;
            case 310 -> Tokens.ParentesisDer;
            case 340 -> Tokens.PuntoYComa;
            case 320 -> Tokens.Coma;
            case 345 -> Tokens.Punto;
            case 330 -> Tokens.Espacio;
            case 350 -> Tokens.Asignacion;
            case 400 -> Tokens.Identificador;
            case 500 -> Tokens.Constante;
            default -> Tokens.Desconocido;
        };
    }
}
