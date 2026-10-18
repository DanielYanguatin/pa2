import { useEffect, useState } from 'react'

const apiUrl = (import.meta.env.VITE_API_URL || 'http://127.0.0.1:8081').replace(/\/$/, '')
const formularioVacio = { name: '', price: '', description: '' }

async function solicitar(ruta, opciones) {
  const respuesta = await fetch(`${apiUrl}${ruta}`, opciones)
  if (!respuesta.ok) {
    const detalle = respuesta.status === 400
      ? await respuesta.text()
      : respuesta.status === 500
        ? 'Error del backend: comprueba que las bases de datos y Spring Boot estén activos.'
        : `Error HTTP ${respuesta.status}`
    throw new Error(detalle)
  }
  return respuesta.status === 204 ? null : respuesta.json()
}

function productosRuta(modo) {
  return modo === 'mongo' ? '/productos-mongo' : '/productos'
}

function App() {
  const [modo, setModo] = useState('sql') // 'sql' | 'mongo'
  const [productos, setProductos] = useState([])
  const [formulario, setFormulario] = useState(formularioVacio)
  const [editandoId, setEditandoId] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [mensaje, setMensaje] = useState('')
  // Pipeline de MongoDB ejecutado en el último filtro (para mostrar/imprimir)
  const [pipelineJson, setPipelineJson] = useState(null)
  const [filtroActivo, setFiltroActivo] = useState(null) // null | 'precio' | 'nombre' | 'descripcion' | 'descendente'

  async function cargarProductos() {
    setCargando(true)
    setPipelineJson(null)
    setFiltroActivo(null)
    try {
      const datos = await solicitar(productosRuta(modo))
      setProductos(datos)
      setError('')
    } catch (fallo) {
      setError(fallo.message === 'Failed to fetch'
        ? `No se pudo conectar con Spring Boot. Revisa que el backend esté activo en el puerto 8081 (modo ${modo === 'mongo' ? 'MongoDB' : 'MySQL'}).`
        : fallo.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => { cargarProductos() }, [modo])

  function cambiarCampo(evento) {
    setFormulario({ ...formulario, [evento.target.name]: evento.target.value })
  }

  function editar(producto) {
    setEditandoId(producto.id)
    setFormulario({
      name: producto.name,
      price: String(producto.price),
      description: producto.description
    })
    setMensaje('')
    setError('')
  }

  function cancelar() {
    setEditandoId(null)
    setFormulario(formularioVacio)
  }

  async function guardar(evento) {
    evento.preventDefault()
    setGuardando(true)
    setError('')
    setMensaje('')
    try {
      const producto = {
        name: formulario.name.trim(),
        price: Number(formulario.price),
        description: formulario.description.trim()
      }
      const ruta = editandoId === null ? productosRuta(modo) : `${productosRuta(modo)}/${editandoId}`
      await solicitar(ruta, {
        method: editandoId === null ? 'POST' : 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(producto)
      })
      setMensaje(editandoId === null ? 'Producto creado.' : 'Producto actualizado.')
      cancelar()
      await cargarProductos()
    } catch (fallo) {
      setError(fallo.message)
    } finally {
      setGuardando(false)
    }
  }

  async function eliminar(producto) {
    if (!window.confirm(`¿Eliminar ${producto.name}?`)) return
    setError('')
    setMensaje('')
    try {
      await solicitar(`${productosRuta(modo)}/${producto.id}`, { method: 'DELETE' })
      if (editandoId === producto.id) cancelar()
      setMensaje('Producto eliminado.')
      await cargarProductos()
    } catch (fallo) {
      setError(fallo.message)
    }
  }

  /* ---------------- Filtros específicos de MongoDB (aggregate) ---------------- */

  async function aplicarFiltro(tipo, valor) {
    setCargando(true)
    setError('')
    setMensaje('')
    try {
      let ruta = ''
      let etiqueta = ''
      switch (tipo) {
        case 'precio':
          ruta = `/productos-mongo/filtro/precio-minimo?minimo=${encodeURIComponent(valor)}`
          etiqueta = `Precio >= ${valor}`
          break
        case 'nombre':
          ruta = `/productos-mongo/filtro/nombre?texto=${encodeURIComponent(valor)}`
          etiqueta = `Nombre contiene "${valor}"`
          break
        case 'descripcion':
          ruta = `/productos-mongo/filtro/descripcion?texto=${encodeURIComponent(valor)}`
          etiqueta = `Descripción contiene "${valor}"`
          break
        case 'descendente':
          ruta = '/productos-mongo/filtro/precio-descendente'
          etiqueta = 'Ordenado por precio descendente'
          break
      }
      const respuesta = await solicitar(ruta)
      // La respuesta de Mongo trae { data: [...], pipeline: "..." }
      setProductos(respuesta.data || [])
      setPipelineJson(respuesta.pipeline || null)
      setFiltroActivo(etiqueta)
    } catch (fallo) {
      setError(fallo.message)
    } finally {
      setCargando(false)
    }
  }

  return (
    <main className="contenedor">
      <header>
        <p className="etiqueta">Spring Boot + React + MongoDB</p>
        <h1>CRUD de productos</h1>
        <p>Elige la persistencia: <strong>{modo === 'sql' ? 'MySQL (SQL)' : 'MongoDB (NoSQL)'}</strong>.</p>

        <div className="cabecera-lista" style={{ marginTop: '16px', marginBottom: '22px' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontWeight: '600' }}>
            Persistencia:
            <select
              value={modo}
              onChange={(e) => setModo(e.target.value)}
              style={{ padding: '8px 10px', borderRadius: '8px', border: '1px solid #b9c7d9', background: '#fff' }}
            >
              <option value="sql">MySQL (SQL)</option>
              <option value="mongo">MongoDB (NoSQL)</option>
            </select>
          </label>
          <button type="button" className="secundario" onClick={cargarProductos}>Cargar productos</button>
        </div>
      </header>

      {/* Filtros solo visibles en modo MongoDB */}
      {modo === 'mongo' && (
        <section className="tarjeta" aria-labelledby="titulo-filtros">
          <h2 id="titulo-filtros">Filtros con aggregate de MongoDB</h2>
          <p>Elige un filtro. Al ejecutarlo se muestra el código JSON del pipeline usado.</p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px', marginBottom: '14px' }}>
            <div>
              <label style={{ fontWeight: '600', display: 'block', marginBottom: '6px' }}>Precio mayor o igual a</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  name="precioMin"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0"
                  style={{ flex: 1, padding: '8px 10px', border: '1px solid #b9c7d9', borderRadius: '8px' }}
                />
                <button
                  type="button"
                  onClick={() => {
                    const input = document.querySelector('input[name="precioMin"]')
                    aplicarFiltro('precio', input.value)
                  }}
                  disabled={guardando}
                >
                  Aplicar
                </button>
              </div>
            </div>
            <div>
              <label style={{ fontWeight: '600', display: 'block', marginBottom: '6px' }}>Nombre contiene</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  name="nombreFiltro"
                  type="text"
                  placeholder="texto"
                  style={{ flex: 1, padding: '8px 10px', border: '1px solid #b9c7d9', borderRadius: '8px' }}
                />
                <button
                  type="button"
                  onClick={() => {
                    const input = document.querySelector('input[name="nombreFiltro"]')
                    aplicarFiltro('nombre', input.value)
                  }}
                  disabled={guardando}
                >
                  Aplicar
                </button>
              </div>
            </div>
            <div>
              <label style={{ fontWeight: '600', display: 'block', marginBottom: '6px' }}>Descripción contiene</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  name="descripcionFiltro"
                  type="text"
                  placeholder="texto"
                  style={{ flex: 1, padding: '8px 10px', border: '1px solid #b9c7d9', borderRadius: '8px' }}
                />
                <button
                  type="button"
                  onClick={() => {
                    const input = document.querySelector('input[name="descripcionFiltro"]')
                    aplicarFiltro('descripcion', input.value)
                  }}
                  disabled={guardando}
                >
                  Aplicar
                </button>
              </div>
            </div>
            <div>
              <button
                type="button"
                onClick={() => aplicarFiltro('descendente', '')}
                disabled={guardando}
                style={{ width: '100%', padding: '10px 14px' }}
              >
                Ordenar por precio descendente
              </button>
            </div>
          </div>

          {filtroActivo && (
            <p style={{ fontSize: '0.9rem', color: '#53647d', marginBottom: '8px' }}>
              Filtro aplicado: <strong>{filtroActivo}</strong>
            </p>
          )}

          {pipelineJson && (
            <div style={{ background: '#17243a', borderRadius: '8px', padding: '14px', overflowX: 'auto', marginBottom: '14px' }}>
              <p style={{ margin: '0 0 8px 0', color: '#75a5ed', fontSize: '.85rem', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '.05em' }}>
                Pipeline de MongoDB (JSON)
              </p>
              <pre style={{ margin: 0, color: '#d7e3f3', fontSize: '.9rem', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>{pipelineJson}</pre>
            </div>
          )}
        </section>
      )}

      <section className="tarjeta" aria-labelledby="titulo-formulario">
        <h2 id="titulo-formulario">{editandoId === null ? 'Nuevo producto' : `Editar producto #${editandoId}`}</h2>
        <form onSubmit={guardar}>
          <label>Nombre
            <input name="name" value={formulario.name} onChange={cambiarCampo} required maxLength="100" />
          </label>
          <label>Precio
            <input name="price" type="number" value={formulario.price} onChange={cambiarCampo} required min="0" step="0.01" />
          </label>
          <label className="ancho">Descripción
            <input name="description" value={formulario.description} onChange={cambiarCampo} maxLength="255" />
          </label>
          <div className="acciones ancho">
            <button type="submit" disabled={guardando}>{guardando ? 'Guardando...' : editandoId === null ? 'Crear producto' : 'Guardar cambios'}</button>
            {editandoId !== null && <button type="button" className="secundario" onClick={cancelar}>Cancelar</button>}
          </div>
        </form>
      </section>

      {error && <p role="alert" className="aviso error">{error}</p>}
      {mensaje && <p role="status" className="aviso exito">{mensaje}</p>}

      <section className="tarjeta" aria-labelledby="titulo-lista">
        <div className="cabecera-lista">
          <h2 id="titulo-lista">Productos</h2>
          <button type="button" className="secundario" onClick={cargarProductos}>Actualizar lista</button>
        </div>
        {cargando ? <p>Cargando productos...</p> : (
          <div className="tabla-contenedor">
            <table>
              <thead><tr><th>ID</th><th>Nombre</th><th>Precio</th><th>Descripción</th><th>Acciones</th></tr></thead>
              <tbody>
                {productos.map((producto) => (
                  <tr key={producto.id}>
                    <td>{producto.id}</td>
                    <td>{producto.name}</td>
                    <td>{producto.price}</td>
                    <td>{producto.description}</td>
                    <td className="acciones-fila">
                      <button type="button" className="secundario" onClick={() => editar(producto)}>Editar</button>
                      <button type="button" className="peligro" onClick={() => eliminar(producto)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
                {productos.length === 0 && <tr><td colSpan="5">No hay productos todavía.</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  )
}

export default App
