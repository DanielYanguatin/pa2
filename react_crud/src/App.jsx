import { useEffect, useState } from 'react'

const apiUrl = (import.meta.env.VITE_API_URL || 'http://127.0.0.1:8081').replace(/\/$/, '')
const formularioVacio = { name: '', price: '', description: '' }

async function solicitar(ruta, opciones) {
  const respuesta = await fetch(`${apiUrl}${ruta}`, opciones)
  if (!respuesta.ok) {
    const detalle = respuesta.status === 400
      ? await respuesta.text()
      : respuesta.status === 500
        ? 'Error del backend: comprueba que MySQL y productoscruddb estén activos.'
        : `Error HTTP ${respuesta.status}`
    throw new Error(detalle)
  }
  return respuesta.status === 204 ? null : respuesta.json()
}

function App() {
  const [productos, setProductos] = useState([])
  const [formulario, setFormulario] = useState(formularioVacio)
  const [editandoId, setEditandoId] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [mensaje, setMensaje] = useState('')

  async function cargarProductos() {
    setCargando(true)
    try {
      setProductos(await solicitar('/productos'))
      setError('')
    } catch (fallo) {
      setError(fallo.message === 'Failed to fetch'
        ? 'No se pudo conectar con Spring Boot. Revisa que el CRUD esté activo en el puerto 8081.'
        : fallo.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => { cargarProductos() }, [])

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
      const ruta = editandoId === null ? '/productos' : `/productos/${editandoId}`
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
      await solicitar(`/productos/${producto.id}`, { method: 'DELETE' })
      if (editandoId === producto.id) cancelar()
      setMensaje('Producto eliminado.')
      await cargarProductos()
    } catch (fallo) {
      setError(fallo.message)
    }
  }

  return (
    <main className="contenedor">
      <header>
        <p className="etiqueta">Spring Boot + React</p>
        <h1>CRUD de productos</h1>
        <p>Consulta, crea, edita y elimina productos de la base de datos.</p>
      </header>

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
