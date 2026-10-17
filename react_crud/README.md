# React para el CRUD de productos

Proyecto React independiente en la raíz de `Downloads`. Consume el backend Spring Boot de `../productos_crud` mediante la API `http://127.0.0.1:8081/productos`.

## Ejecutar

Primero inicia MySQL y el backend `productos_crud` en el puerto 8081. Luego, desde esta carpeta:

```powershell
npm ci
npm run dev
```

Abre [http://127.0.0.1:5175/](http://127.0.0.1:5175/). La pantalla permite listar, crear, editar y eliminar productos.

Si cambias la dirección del backend, copia `.env.example` como `.env.local`, modifica `VITE_API_URL` y reinicia Vite.

## Compilar

```powershell
npm run build
```

La salida queda en `dist/`.
