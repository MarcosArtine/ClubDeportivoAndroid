package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.PagoDiario

// PagoDiarioDAO — maneja los pagos por actividad de los NO SOCIOS.
// Trabaja con las tablas PagoDiario y PagoRealizado definidas en BDatos.kt.
// Convención: columnas en PascalCase (NoSocioId, ActividadId, FechaPago, etc.)
class PagoDiarioDAO(context: Context) {

    private val dbHelper = BDatos(context)

    // ==========================================================================
    // INSERTAR PAGO DIARIO — caso de uso 5, escenario 3 (NoSocio paga actividad)
    // ==========================================================================
    // Flujo igual que PagoCuota:
    //   1. Insertar en PagoRealizado (comprobante)
    //   2. Insertar en PagoDiario vinculado al comprobante
    // Retorna el PagoRealizadoId si salió bien, -1 si falla.
    fun insertarPagoDiario(
        noSocioId: Int,
        actividadId: Int,
        montoPagado: Double,
        medioPago: String,
        fechaPago: String       // formato "yyyy-MM-dd"
    ): Long {

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {

            // Paso 1: comprobante en PagoRealizado
            val valoresPR = ContentValues().apply {
                put("MontoTotal",    montoPagado)
                put("TipoConcepto", "Actividad")
                put("MedioPago",     medioPago)
                put("ReferenciaId",  noSocioId)
            }
            val pagoRealizadoId = db.insert("PagoRealizado", null, valoresPR)
            if (pagoRealizadoId == -1L) throw Exception("Error al insertar PagoRealizado")

            // Paso 2: detalle del pago diario
            val valoresPD = ContentValues().apply {
                put("NoSocioId",       noSocioId)
                put("ActividadId",     actividadId)
                put("FechaPago",       fechaPago)
                put("MontoPagado",     montoPagado)
                put("PagoRealizadoId", pagoRealizadoId)
            }
            val pagoDiarioId = db.insert("PagoDiario", null, valoresPD)
            if (pagoDiarioId == -1L) throw Exception("Error al insertar PagoDiario")

            db.setTransactionSuccessful()
            pagoRealizadoId

        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // ==========================================================================
    // LISTAR PAGOS DIARIOS CON DETALLE — para la lista principal de Pagos (Figma)
    // ==========================================================================
    // Retorna nombre + apellido del NoSocio, nombre de actividad, monto y fecha.
    fun listarPagosDiariosConDetalle(): List<Map<String, String>> {

        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Map<String, String>>()

        val cursor: Cursor = db.rawQuery(
            """
            SELECT
                pr.PagoRealizadoID,
                pr.FechaPago,
                pr.MontoTotal,
                pr.MedioPago,
                p.Nombre,
                p.Apellido,
                a.NombreActividad
            FROM PagoRealizado pr
            INNER JOIN PagoDiario pd ON pr.PagoRealizadoID = pd.PagoRealizadoId
            INNER JOIN NoSocio ns    ON pd.NoSocioId       = ns.NoSocioId
            INNER JOIN Persona p     ON ns.NoSocioId       = p.PersonaId
            INNER JOIN Actividad a   ON pd.ActividadId     = a.ActividadId
            ORDER BY pr.FechaPago DESC
            """,
            null
        )

        while (cursor.moveToNext()) {
            lista.add(mapOf(
                "pagoId"     to cursor.getInt(cursor.getColumnIndexOrThrow("PagoRealizadoID")).toString(),
                "fecha"      to (cursor.getString(cursor.getColumnIndexOrThrow("FechaPago")) ?: ""),
                "monto"      to cursor.getDouble(cursor.getColumnIndexOrThrow("MontoTotal")).toString(),
                "medio"      to cursor.getString(cursor.getColumnIndexOrThrow("MedioPago")),
                "nombre"     to cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
                "apellido"   to cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
                "actividad"  to cursor.getString(cursor.getColumnIndexOrThrow("NombreActividad")),
                "tipo"       to "Actividad"
            ))
        }

        cursor.close()
        db.close()
        return lista
    }

    // ==========================================================================
    // BUSCAR NOSOCIO POR ID — para prellenar el formulario de Nuevo Pago
    // ==========================================================================
    // El Figma muestra: al ingresar el ID, se autocompletan Nombre, Apellido y Tipo de cliente.
    fun buscarNoSocioPorId(noSocioId: Int): Map<String, String>? {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT p.Nombre, p.Apellido, p.NroDni
            FROM Persona p
            INNER JOIN NoSocio ns ON p.PersonaId = ns.NoSocioId
            WHERE ns.NoSocioId = ?
            """,
            arrayOf(noSocioId.toString())
        )

        val resultado = if (cursor.moveToFirst()) {
            mapOf(
                "nombre"   to cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
                "apellido" to cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
                "nroDni"   to cursor.getString(cursor.getColumnIndexOrThrow("NroDni")),
                "tipo"     to "No Socio"
            )
        } else null

        cursor.close()
        db.close()
        return resultado
    }

    // ==========================================================================
    // TOTAL RECAUDADO EN UNA FECHA
    // ==========================================================================
    fun totalRecaudadoEnFecha(fecha: String): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(MontoPagado) FROM PagoDiario WHERE FechaPago = ?",
            arrayOf(fecha)
        )
        var total = 0.0
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0)
        cursor.close()
        db.close()
        return total
    }
}
