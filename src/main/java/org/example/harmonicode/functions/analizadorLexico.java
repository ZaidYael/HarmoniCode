package org.example.harmonicode.functions;

import org.example.harmonicode.functions.Token;
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
            System.out.println("c: "+c);
            System.out.println("lexema: "+lexema);
            System.out.println("columna actual: "+columnaActual);
            if (c == '\n') {
                fila++;
                columnaActual = 1;
                continue;
            }

            int columna = obtenerIndiceCaracter(c); // columna del alfabeto
            if (columna == -1) {
                if (estadoActual != q0 && lexema.length() > 0) {
                    System.out.println("entra en estado de error");
                    tokensReconocidos.add(new Token(lexema.toString(), "ERROR", fila, columnaActual));
                    lexema.setLength(0);
                    estadoActual = q0;
                }
                columnaActual++;
                continue;
            }

            int siguienteEstado = matrizTransicion[estadoActual][columna];
            System.out.println("Sig. estado: "+siguienteEstado);


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
                estadoActual = q0;
            } else if (siguienteEstado == 0) {
                System.out.println("ebtra a esto");
                if (estadoActual != q0 && lexema.length() > 0) {
                    tokensReconocidos.add(new Token(lexema.toString(), "ERROR", fila, columnaActual));
                    lexema.setLength(0);
                }
                estadoActual = q0;
            } else {
                estadoActual = siguienteEstado;
                lexema.append(c);
            }

            columnaActual++;
        }
        return tokensReconocidos;
    }


    private int obtenerIndiceCaracter(char c) {
        for (int i = 0; i < alfabeto.length; i++) {
            if (alfabeto[i] == c) return i;
        }
        return -1;
    }

    private String nombreToken(int token) {
        return switch (token) {
            case 200 -> "TRANSPONER";
            case 210 -> "INVERTIR";
            case 220 -> "MODULAR";
            case 230 -> "ROTAR";
            case 240 -> "REGISTRO";
            case 300 -> "PAR_IZQ";
            case 310 -> "PAR_DER";
            case 320 -> "COMA";
            case 330 -> "ESPACIO";
            case 340 -> "PUNTO_COMA";
            case 345 -> "PUNTO";
            case 350 -> "ASIGNACION";
            case 400 -> "IDENTIFICADOR";
            case 500 -> "CONSTANTE";
            default -> "DESCONOCIDO";
        };
    }
}
