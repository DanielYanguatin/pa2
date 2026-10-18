# React para el CRUD de productos (SQL y NoSQL)

Proyecto React independiente en la raíz de `Downloads`. Consume el backend Spring Boot en `http://127.0.0.1:8081`.

Permite elegir entre dos persistencias desde un menú:

- **MySQL (SQL)**: consume `GET/POST/PUT/DELETE /productos`.
- **MongoDB (NoSQL)**: consume `GET/POST/PUT/DELETE /productos-mongo` y 4 filtros con pipelines de `aggregate`.

## Ejecutar

Primero inicia MySQL, MongoDB y el backend `productos_crud` en el puerto 8081. Luego, desde esta carpeta:

```powershell
npm ci
npm run dev
```

Abre [http://127.0.0.1:5175/](http://127.0.0.1:5175/).

### Menú de persistencia

Al cargar la aplicación verás un selector "Persistencia" con dos opciones:

- **MySQL (SQL)**: el CRUD clásico sobre la base relacional.
- **MongoDB (NoSQL)**: el mismo CRUD (listar, crear, editar, eliminar) pero sobre MongoDB, además de 4 botones de filtro que ejecutan pipelines de `aggregate`. Cada filtro imprime en pantalla el código JSON del pipeline utilizado.

### Filtros de MongoDB (solo en modo NoSQL)

1. Precio mayor o igual a: filtra por `price >= X` ordenado asc.
2. Nombre contiene: filtra por nombre con expresión regular (case insensitive).
3. Descripción contiene: filtra por descripción con expresión regular (case insensitive).
4. Ordenar por precio descendente: lista completa ordenada de mayor a menor precio.

Si cambias la dirección del backend, copia `.env.example` como `.env.local`, modifica `VITE_API_URL` y reinicia Vite.

## Compilar

```powershell
npm run build
```

La salida queda en `dist/`.
