package servicio;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CatalogoRecetas {


    private static final Map<String, String[][]> RECETAS =
            new LinkedHashMap<>();

    static {

        RECETAS.put("SANDWICH_ATUN", new String[][]{
            {"PAN_CHABATA", "1"},
            {"ENSALADA_ATUN", "1"},
            {"LECHUGA_ROMANA", "1"},
            {"TOMATE", "1"},
            {"MAYONESA", "1"}
        });

        RECETAS.put("CHEESE_PARTY", new String[][]{
            {"PAN_CHABATA", "1"},
            {"QUESO_BLANCO", "1"},
            {"QUESO_AMARILLO", "1"},
            {"BURRATA", "1"},
            {"PARMESANO", "1"}
        });

        RECETAS.put("EL_COCHE", new String[][]{
            {"PAN_CHABATA", "1"},
            {"LOMO_CERDO", "1"},
            {"LECHUGA_ROMANA", "1"},
            {"TOMATE", "1"},
            {"RUGULA", "1"},
            {"MOSTAZA", "1"}
        });

        RECETAS.put("SANDWICH_CHOMPIPE", new String[][]{
            {"PAN_CHABATA", "1"},
            {"PAVO", "1"},
            {"MOSTAZA", "1"},
            {"QUESO_BLANCO", "1"},
            {"RUGULA", "1"}
        });

        RECETAS.put("POLLO_GRANJERO", new String[][]{
            {"PAN_CHABATA", "1"},
            {"POLLO", "1"},
            {"PARMESANO", "1"},
            {"TOMATE", "1"},
            {"LECHUGA_ROMANA", "1"},
            {"KETCHUP", "1"},
            {"MAYONESA", "1"},
            {"MOSTAZA", "1"}
        });

        RECETAS.put("ENSALADA_PANIZADA", new String[][]{
            {"PAN_CHABATA", "1"},
            {"RUGULA", "1"},
            {"LECHUGA_ROMANA", "1"},
            {"ACEITE_OLIVA", "1"},
            {"TOMATE", "1"},
            {"BURRATA", "1"}
        });

        RECETAS.put("SANDWICH_CLASICO", new String[][]{
            {"PAN_CHABATA", "1"},
            {"JAMON", "1"},
            {"TOMATE", "1"},
            {"LECHUGA_ROMANA", "1"},
            {"QUESO_AMARILLO", "1"},
            {"KETCHUP", "1"},
            {"MAYONESA", "1"}
        });
    }

    private CatalogoRecetas() {
    }

  
    public static String[][] obtenerReceta(String codigo) {

        if (codigo == null) {
            return null;
        }

        String[][] receta = RECETAS.get(codigo);

        if (receta == null) {
            return null;
        }

        return copiarReceta(receta);
    }


    public static boolean existeReceta(String codigo) {
        return codigo != null && RECETAS.containsKey(codigo);
    }


    public static Map<String, String[][]> obtenerTodasLasRecetas() {

        Map<String, String[][]> copia = new LinkedHashMap<>();

        for (Map.Entry<String, String[][]> entrada
                : RECETAS.entrySet()) {

            copia.put(
                    entrada.getKey(),
                    copiarReceta(entrada.getValue())
            );
        }

        return Collections.unmodifiableMap(copia);
    }


    private static String[][] copiarReceta(String[][] receta) {

        String[][] copia = new String[receta.length][];

        for (int i = 0; i < receta.length; i++) {
            copia[i] = receta[i].clone();
        }

        return copia;
    }
}
