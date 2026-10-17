# CRUD de productos con Spring Boot

Proyecto independiente de `demo`. Permite **crear, listar, buscar, actualizar y eliminar** productos en MySQL. Usa Java 21, Spring Boot 4.1.1 y Maven 3.9.9. Corre en el puerto **8081** y usa la base **`productoscruddb`** para no modificar los datos de `demo`.

## Preparación

1. Ejecuta [dbs/productoscruddb.sql](dbs/productoscruddb.sql) en MySQL Workbench. Crea la tabla `products` y algunos datos de ejemplo.
2. Si tus credenciales no son `root` y `123`, define las variables en PowerShell:

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "TU_CLAVE"
```

También puedes definir `DB_HOST`, `DB_PORT` y `DB_NAME`. Los valores iniciales están en `src/main/resources/application.properties`.

3. Desde esta carpeta, ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

## Frontend React

También hay un proyecto React independiente en `../frontend_crud`. Desde esa carpeta ejecuta `npm ci` y `npm run dev`, y abre [http://127.0.0.1:5175/](http://127.0.0.1:5175/). Consume este mismo CRUD en el puerto 8081.

Este proyecto incluye su propio frontend en `frontend/`; no cambia el frontend de solo lectura de `demo`. Abre otra terminal y ejecuta:

```powershell
cd frontend
npm ci
npm run dev
```

Abre [http://127.0.0.1:5174/](http://127.0.0.1:5174/). React llama por defecto a `http://127.0.0.1:8081`, donde escucha este CRUD. Si cambias el puerto o el host del backend, copia `frontend/.env.example` a `frontend/.env.local`, ajusta `VITE_API_URL` y reinicia Vite. Primero deben estar activos MySQL y el backend.

## Operaciones HTTP

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/productos` | Lista todos los productos |
| GET | `/productos/{id}` | Busca uno; responde 404 si no existe |
| POST | `/productos` | Crea uno y responde 201 con el ID generado |
| PUT | `/productos/{id}` | Reemplaza sus datos; responde 404 si no existe |
| DELETE | `/productos/{id}` | Lo elimina; responde 204 o 404 |

Ejemplo completo en PowerShell:

```powershell
$base = "http://localhost:8081/productos"
Invoke-RestMethod $base

$nuevo = @{ name = "Monitor"; price = 700000; description = "24 pulgadas" } | ConvertTo-Json
$creado = Invoke-RestMethod -Method Post -Uri $base -ContentType "application/json" -Body $nuevo
Invoke-RestMethod "$base/$($creado.id)"

$editado = @{ name = "Monitor HD"; price = 720000; description = "24 pulgadas" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$base/$($creado.id)" -ContentType "application/json" -Body $editado
Invoke-RestMethod -Method Delete -Uri "$base/$($creado.id)"
```

Para crear o actualizar, `name` debe tener entre 1 y 100 caracteres, `price` debe ser mayor o igual a cero y `description` no puede superar 255 caracteres. Los datos inválidos reciben 400.

## Arquitectura

El flujo de cada petición es **ProductController → ProductService → ProductRepository → ProductRepositoryMySQL → MySQL**. `App` conecta el servicio con la implementación del repositorio al arrancar; el controlador no conoce MySQL.

- `domain/model/Product.java`: representa un producto en memoria, tanto al recibir JSON como al leer una fila. Comprueba las reglas básicas de sus datos. No guarda nada por sí mismo.
- `application/ports/ProductRepository.java`: clase abstracta con las cinco operaciones. `ProductRepositoryMySQL` la extiende.
- `application/service/ProductService.java`: casos de uso; solo conoce el contrato y el modelo.
- `infrastructure/web/controllers/ProductController.java`: traduce peticiones HTTP y devuelve JSON y códigos de respuesta.
- `infrastructure/persistence/mysql/ProductRepositoryMySQL.java`: ejecuta SQL parametrizado mediante Spring JDBC.
- `app/App.java`: inicia Spring y conecta los componentes.
- `frontend/src/App.jsx`: consume los cinco endpoints desde React.
