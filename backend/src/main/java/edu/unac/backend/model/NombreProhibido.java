package edu.unac.backend.model;

public enum NombreProhibido {
    POPO("popo"),
    PERRO("perro"),
    LOCA("loca"),
    ESTUPIDO("estupido"),
    JORGE_NITALES("jorge nitales"),
    BENITO_CAMELO("benito camelo");


    private final String texto;

    NombreProhibido(String texto) {
        this.texto = texto;
    }

    public static boolean contiene(String nombre) {
        if (nombre == null) {
            return false;
        }
        String buscado = nombre.trim().replaceAll("\\s+", " ");
        for (NombreProhibido prohibido : values()) {
            if (prohibido.texto.equalsIgnoreCase(buscado)) {
                return true;
            }
        }
        return false;
    }
}