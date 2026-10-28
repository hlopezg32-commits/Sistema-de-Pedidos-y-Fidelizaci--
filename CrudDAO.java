
package com.subnotway.dao;

import java.util.List;

public interface CrudDAO<T> {

    boolean insertar(T objeto);

    boolean actualizar(T objeto);

    boolean eliminar(T objeto);

    T buscarPorId(int id);

    List<T> listarTodos();
}
