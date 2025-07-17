package org.example.harmonicode.functions;

import org.example.harmonicode.models.Token;
import org.example.harmonicode.models.Tokens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Semantico {

    // Tabla de símbolos para almacenar variables declaradas
    private final Map<String, Variable> tablaSimbolos = new HashMap<>();

    // Resultado del análisis semántico
    private final StringBuilder resultado = new StringBuilder();

    // Contador de errores semánticos
    private int erroresSemanticos = 0;

    // Clase interna para representar variables
    private static class Variable {
        String nombre;
        String tipo;
        Object valor;
        boolean inicializada;
        int lineaDeclaracion;

        public Variable(String nombre, String tipo, Object valor, int lineaDeclaracion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.valor = valor;
            this.inicializada = valor != null;
            this.lineaDeclaracion = lineaDeclaracion;
        }
    }

    /**
     * Método principal para realizar el análisis semántico
     * @param tokens Lista de tokens del código fuente
     * @return String con el resultado del análisis semántico
     */
    public String analizar(List<Token> tokens) {
        resultado.setLength(0);
        tablaSimbolos.clear();
        erroresSemanticos = 0;

        resultado.append("=== ANÁLISIS SEMÁNTICO ===\n\n");

        // Análisis en dos pasadas
        // Primera pasada: recolectar declaraciones
        primeraPasada(tokens);

        // Segunda pasada: verificar uso de variables y operaciones
        segundaPasada(tokens);

        // Mostrar tabla de símbolos
        mostrarTablaSimbolos();

        // Resumen final
        if (erroresSemanticos == 0) {
            resultado.append("\nAnálisis semántico completado sin errores.\n");
        } else {
            resultado.append(String.format("\nAnálisis semántico completado con %d error(es).\n", erroresSemanticos));
        }

        return resultado.toString();
    }

    /**
     * Primera pasada: recolectar todas las declaraciones de variables
     */
    private void primeraPasada(List<Token> tokens) {
        resultado.append("--- PRIMERA PASADA: RECOLECCIÓN DE DECLARACIONES ---\n");

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            // Buscar declaraciones (registro variable = valor;)
            if (token.getTipo() == Tokens.Declaracion) {
                procesarDeclaracion(tokens, i);
            }
        }
        resultado.append("\n");
    }

    /**
     * Segunda pasada: verificar uso de variables y operaciones
     */
    private void segundaPasada(List<Token> tokens) {
        resultado.append("--- SEGUNDA PASADA: VERIFICACIÓN DE USO ---\n");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            // Verificar operaciones
            if (token.getTipo() == Tokens.Operacion) {
                procesarOperacion(tokens, i);
            }
        }
        resultado.append("\n");
    }

    /**
     * Procesar una declaración de variable
     */
    private void procesarDeclaracion(List<Token> tokens, int index) {
        try {
            // Formato esperado: registro identificador = constante ;
            Token tipoToken = tokens.get(index); // "registro"
            Token nombreToken = obtenerSiguienteTokenNoEspacio(tokens, index);
            Token asignacionToken = obtenerSiguienteTokenNoEspacio(tokens, buscarIndiceToken(tokens, nombreToken));
            Token valorToken = obtenerSiguienteTokenNoEspacio(tokens, buscarIndiceToken(tokens, asignacionToken));

            if (nombreToken.getTipo() != Tokens.Identificador) {
                agregarError(nombreToken, "Se esperaba un identificador después de 'registro'");
                return;
            }

            if (asignacionToken.getTipo() != Tokens.Asignacion) {
                agregarError(asignacionToken, "Se esperaba '=' después del identificador");
                return;
            }

            if (valorToken.getTipo() != Tokens.Constante) {
                agregarError(valorToken, "Se esperaba una constante después de '='");
                return;
            }

            String nombreVariable = nombreToken.getLexema();

            // Verificar redeclaración
            if (tablaSimbolos.containsKey(nombreVariable)) {
                Variable varExistente = tablaSimbolos.get(nombreVariable);
                agregarError(nombreToken, String.format("Variable '%s' ya fue declarada en la línea %d",
                        nombreVariable, varExistente.lineaDeclaracion));
                return;
            }

            // Validar el valor de la constante
            Object valor = validarConstante(valorToken);
            if (valor == null) {
                agregarError(valorToken, "Valor de constante inválido: " + valorToken.getLexema());
                return;
            }

            // Agregar variable a la tabla de símbolos
            Variable nuevaVariable = new Variable(nombreVariable, "registro", valor, nombreToken.getLine());
            tablaSimbolos.put(nombreVariable, nuevaVariable);

            resultado.append(String.format("Variable '%s' declarada correctamente con valor %s (línea %d)\n",
                    nombreVariable, valor, nombreToken.getLine()));

        } catch (Exception e) {
            agregarError(tokens.get(index), "Error al procesar declaración: " + e.getMessage());
        }
    }

    /**
     * Procesar una operación (transponer, invertir, modular, rotar)
     */
    private void procesarOperacion(List<Token> tokens, int index) {
        try {
            Token operacionToken = tokens.get(index);
            String tipoOperacion = operacionToken.getLexema().toLowerCase();

            // Buscar los parámetros de la operación
            Token parentesisIzq = obtenerSiguienteTokenNoEspacio(tokens, index);
            Token param1 = obtenerSiguienteTokenNoEspacio(tokens, buscarIndiceToken(tokens, parentesisIzq));
            Token coma = obtenerSiguienteTokenNoEspacio(tokens, buscarIndiceToken(tokens, param1));
            Token param2 = obtenerSiguienteTokenNoEspacio(tokens, buscarIndiceToken(tokens, coma));

            if (parentesisIzq.getTipo() != Tokens.ParentesisIzq) {
                agregarError(parentesisIzq, "Se esperaba '(' después de la operación");
                return;
            }

            if (param1.getTipo() != Tokens.Identificador) {
                agregarError(param1, "Se esperaba un identificador como primer parámetro");
                return;
            }

            if (coma.getTipo() != Tokens.Coma) {
                agregarError(coma, "Se esperaba ',' entre parámetros");
                return;
            }

            if (param2.getTipo() != Tokens.Identificador) {
                agregarError(param2, "Se esperaba un identificador como segundo parámetro");
                return;
            }

            // Verificar que las variables existan
            String var1 = param1.getLexema();
            String var2 = param2.getLexema();

            if (!tablaSimbolos.containsKey(var1)) {
                agregarError(param1, String.format("Variable '%s' no ha sido declarada", var1));
                return;
            }

            if (!tablaSimbolos.containsKey(var2)) {
                agregarError(param2, String.format("Variable '%s' no ha sido declarada", var2));
                return;
            }

            // Verificar que las variables estén inicializadas
            Variable variable1 = tablaSimbolos.get(var1);
            Variable variable2 = tablaSimbolos.get(var2);

            if (!variable1.inicializada) {
                agregarError(param1, String.format("Variable '%s' no ha sido inicializada", var1));
                return;
            }

            if (!variable2.inicializada) {
                agregarError(param2, String.format("Variable '%s' no ha sido inicializada", var2));
                return;
            }

            // Verificar compatibilidad de tipos para la operación
            if (validarOperacion(tipoOperacion, variable1, variable2, operacionToken)) {
                resultado.append(String.format("Operación '%s(%s, %s)' es válida (línea %d)\n",
                        tipoOperacion, var1, var2, operacionToken.getLine()));
            }

        } catch (Exception e) {
            agregarError(tokens.get(index), "Error al procesar operación: " + e.getMessage());
        }
    }

    /**
     * Validar una operación específica con sus parámetros
     */
    private boolean validarOperacion(String operacion, Variable var1, Variable var2, Token token) {
        switch (operacion) {
            case "transponer":
                return validarTransponer(var1, var2, token);
            case "invertir":
                return validarInvertir(var1, var2, token);
            case "modular":
                return validarModular(var1, var2, token);
            case "rotar":
                return validarRotar(var1, var2, token);
            default:
                agregarError(token, "Operación desconocida: " + operacion);
                return false;
        }
    }

    /**
     * Validar operación transponer
     */
    private boolean validarTransponer(Variable var1, Variable var2, Token token) {
        // Transponer requiere una nota/acorde y un intervalo
        if (!(var1.valor instanceof Double) || !(var2.valor instanceof Double)) {
            agregarError(token, "Transponer requiere valores numéricos para nota e intervalo");
            return false;
        }

        double nota = (Double) var1.valor;
        double intervalo = (Double) var2.valor;

        // Validar rango de notas (0-11 para notas cromáticas)
        if (nota < 0 || nota > 127) { // Rango MIDI
            agregarError(token, String.format("Nota fuera de rango válido (0-127): %.1f", nota));
            return false;
        }

        // Validar intervalo razonable
        if (Math.abs(intervalo) > 48) { // Máximo 4 octavas
            agregarError(token, String.format("Intervalo demasiado grande: %.1f", intervalo));
            return false;
        }

        return true;
    }

    /**
     * Validar operación invertir
     */
    private boolean validarInvertir(Variable var1, Variable var2, Token token) {
        // Invertir requiere un acorde y un punto de inversión
        if (!(var1.valor instanceof Double) || !(var2.valor instanceof Double)) {
            agregarError(token, "Invertir requiere valores numéricos para acorde y punto de inversión");
            return false;
        }

        double acorde = (Double) var1.valor;
        double puntoInversion = (Double) var2.valor;

        if (acorde < 0 || acorde > 127) {
            agregarError(token, String.format("Acorde fuera de rango válido (0-127): %.1f", acorde));
            return false;
        }

        if (puntoInversion < 0 || puntoInversion > 127) {
            agregarError(token, String.format("Punto de inversión fuera de rango válido (0-127): %.1f", puntoInversion));
            return false;
        }

        return true;
    }

    /**
     * Validar operación modular
     */
    private boolean validarModular(Variable var1, Variable var2, Token token) {
        // Modular requiere una secuencia y un módulo
        if (!(var1.valor instanceof Double) || !(var2.valor instanceof Double)) {
            agregarError(token, "Modular requiere valores numéricos para secuencia y módulo");
            return false;
        }

        double secuencia = (Double) var1.valor;
        double modulo = (Double) var2.valor;

        if (modulo <= 0) {
            agregarError(token, "El módulo debe ser mayor que 0");
            return false;
        }

        if (modulo > 12) {
            agregarError(token, "Módulo demasiado grande para operación musical");
            return false;
        }

        return true;
    }

    /**
     * Validar operación rotar
     */
    private boolean validarRotar(Variable var1, Variable var2, Token token) {
        // Rotar requiere una secuencia y posiciones a rotar
        if (!(var1.valor instanceof Double) || !(var2.valor instanceof Double)) {
            agregarError(token, "Rotar requiere valores numéricos para secuencia y posiciones");
            return false;
        }

        double secuencia = (Double) var1.valor;
        double posiciones = (Double) var2.valor;

        // Las posiciones deben ser enteras
        if (posiciones != Math.floor(posiciones)) {
            agregarError(token, "Las posiciones de rotación deben ser un número entero");
            return false;
        }

        return true;
    }

    /**
     * Validar el valor de una constante
     */
    private Object validarConstante(Token token) {
        String valor = token.getLexema();
        System.out.println("VAlor: "+valor);
        try {
            boolean v=Character.isDigit(valor.charAt(0));
            System.out.println("V: "+v);
            System.out.println("valor.chatAt(0): "+valor.charAt(0));
            if(!v)valor=decodificar(valor);
            System.out.println("Valor2: "+valor);
            // Intentar parsear como número decimal
            if (valor.contains(".")) {
                return Double.parseDouble(valor);
            } else {
                // Parsear como entero y convertir a double
                return (double) Integer.parseInt(valor);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obtener el siguiente token que no sea espacio
     */
    private Token obtenerSiguienteTokenNoEspacio(List<Token> tokens, int index) {
        for (int i = index + 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getTipo() != Tokens.Espacio) {
                return token;
            }
        }
        return new Token("EOF", Tokens.EOF, -1, -1);
    }

    /**
     * Buscar el índice de un token específico en la lista
     */
    private int buscarIndiceToken(List<Token> tokens, Token tokenBuscado) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) == tokenBuscado) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Agregar un error semántico
     */
    private void agregarError(Token token, String mensaje) {
        erroresSemanticos++;
        resultado.append(String.format("ERROR línea %d: %s\n", token.getLine(), mensaje));
    }

    /**
     * Mostrar la tabla de símbolos
     */
    private void mostrarTablaSimbolos() {
        resultado.append("--- TABLA DE SÍMBOLOS ---\n");

        if (tablaSimbolos.isEmpty()) {
            resultado.append("(Tabla vacía)\n");
            return;
        }

        resultado.append(String.format("%-15s %-10s %-10s %-15s %-10s\n",
                "NOMBRE", "TIPO", "VALOR", "INICIALIZADA", "LÍNEA"));
        resultado.append("─".repeat(70)).append("\n");

        for (Variable var : tablaSimbolos.values()) {
            resultado.append(String.format("%-15s %-10s %-10s %-15s %-10d\n",
                    var.nombre,
                    var.tipo,
                    var.valor != null ? var.valor.toString() : "null",
                    var.inicializada ? "Sí" : "No",
                    var.lineaDeclaracion));
        }
    }

    private String decodificar(String encrypted) {
        // Mapa de notas musicales a sus valores
        java.util.Map<Character, Integer> noteMap = new java.util.HashMap<>();
        noteMap.put('c', 1);
        noteMap.put('d', 2);
        noteMap.put('e', 3);
        noteMap.put('f', 4);
        noteMap.put('g', 5);
        noteMap.put('a', 6);
        noteMap.put('b', 7);

        StringBuilder result = new StringBuilder();
        char note;
        int octava,noteValue,product;
        //encrypted+="ZZ";
        for (int i = 0; i < encrypted.length(); i++) {
            note = encrypted.charAt(i);
            octava = Character.getNumericValue(encrypted.charAt(i + 1));
            noteValue = noteMap.getOrDefault(note, 0);
            System.out.println("note,octava,value: "+note+","+octava+","+noteValue);
            if(octava >= 10 || octava <= 0){
                if(noteValue == 1)product=0;
                else return "ERROR Cte numerica no valida";
            }else{
                product = noteValue * octava;
                i++;
            }
            result.append(product);
        }

        return result.toString();
    }
}