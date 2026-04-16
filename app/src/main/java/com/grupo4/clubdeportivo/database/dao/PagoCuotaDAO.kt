package com.grupo4.clubdeportivo.database.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.grupo4.clubdeportivo.database.BDatos
import com.grupo4.clubdeportivo.database.models.PagoCuota

// PagoCuotaDAO — maneja los pagos de cuota mensual de los SOCIOS.
// Trabaja con las tablas PagoCuota y PagoRealizado definidas en BDatos.kt.
// IMPORTANTE: me adecue en el trabajo de Marcos:
//   - Columnas en PascalCase: SocioId, MontoCuota, FechaVencimientoCuota, etc.
//   - PagoRealizado es el registro maestro del comprobante (viene del diseño original)
class PagoCuotaDAO(context: Context) {

    private val dbHelper = BDatos(context)

    // ==========================================================================
    // INSERTAR PAGO ÚNICO — caso de uso 5, escenario 1
    // ==========================================================================
    // Flujo en dos pasos dentro de una transacción:
    //   1. Insertar en PagoRealizado → genera el comprobante
    //   2. Insertar en PagoCuota vinculado al comprobante
    // Retorna el PagoRealizadoId (para mostrarlo como número de comprobante), -1 si falla.
    fun insertarPagoCuota(
        socioId: Int,
        fechaVencimiento: String,
        montoCuota: Double,
        medioPago: String,
        mesCuota: Int,
        anioCuota: Int
    ): Long {

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {

            // Paso 1: comprobante maestro en PagoRealizado
            val valoresPR = ContentValues().apply {
                put("MontoTotal",    montoCuota)
                put("TipoConcepto", "Cuota")
                put("MedioPago",     medioPago)
                put("ReferenciaId",  socioId)
            }
            val pagoRealizadoId = db.insert("PagoRealizado", null, valoresPR)
            if (pagoRealizadoId == -1L) throw Exception("Error al insertar PagoRealizado")

            // Paso 2: detalle de la cuota
            val valoresPC = ContentValues().apply {
                put("SocioId",               socioId)
                put("FechaVencimientoCuota", fechaVencimiento)
                put("EstadoCuota",           "Pagado")
                put("MontoCuota",            montoCuota)
                put("MedioPago",             medioPago)
                put("PagoRealizadoId",       pagoRealizadoId)
                put("MesCuota",              mesCuota)
                put("AnioCuota",             anioCuota)
            }
            val cuotaId = db.insert("PagoCuota", null, valoresPC)
            if (cuotaId == -1L) throw Exception("Error al insertar PagoCuota")

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
    // INSERTAR PLAN DE CUOTAS — caso de uso 5, escenario 2
    // ==========================================================================
    // Prototipo Figma: opciones de medio de pago incluyen "Tarjeta (3 pagos)" y "Tarjeta (6 pagos)".
    // Crea UN PagoRealizado por el total y N filas en PagoCuota.
    // La columna PagoRealizadoId en PagoCuota tiene restricción UNIQUE en BDatos,
    // por eso solo la primera cuota lleva el ID del comprobante; las demás quedan en NULL.
    // Retorna la lista de cuotas generadas para mostrar el comprobante, lista vacía si falla.
    fun insertarPlanCuotas(
        socioId: Int,
        montoTotal: Double,
        cantidadCuotas: Int,
        medioPago: String,
        mesInicio: Int,
        anioInicio: Int
    ): List<PagoCuota> {

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        val cuotasGeneradas = mutableListOf<PagoCuota>()

        return try {

            val montoPorCuota = montoTotal / cantidadCuotas

            // Un único comprobante para todo el plan
            val valoresPR = ContentValues().apply {
                put("MontoTotal",    montoTotal)
                put("TipoConcepto", "Cuota")
                put("MedioPago",     medioPago)
                put("ReferenciaId",  socioId)
            }
            val pagoRealizadoId = db.insert("PagoRealizado", null, valoresPR)
            if (pagoRealizadoId == -1L) throw Exception("Error al insertar PagoRealizado del plan")

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()

            for (i in 0 until cantidadCuotas) {
                // Mes y año de esta cuota (Calendar maneja el overflow de diciembre automáticamente)
                cal.set(anioInicio, mesInicio - 1 + i, 1)
                val mesCuota  = cal.get(java.util.Calendar.MONTH) + 1
                val anioCuota = cal.get(java.util.Calendar.YEAR)

                // Vencimiento = último día del mes
                cal.set(java.util.Calendar.DAY_OF_MONTH,
                    cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                val fechaVenc = sdf.format(cal.time)

                val estado = if (i == 0) "Pagado" else "Pendiente"

                val valoresPC = ContentValues().apply {
                    put("SocioId",               socioId)
                    put("FechaVencimientoCuota", fechaVenc)
                    put("EstadoCuota",           estado)
                    put("MontoCuota",            montoPorCuota)
                    put("MedioPago",             medioPago)
                    put("MesCuota",              mesCuota)
                    put("AnioCuota",             anioCuota)
                    // Solo la primera cuota vincula al comprobante (UNIQUE constraint en BDatos)
                    if (i == 0) put("PagoRealizadoId", pagoRealizadoId)
                }

                val cuotaId = db.insert("PagoCuota", null, valoresPC)
                if (cuotaId == -1L) throw Exception("Error al insertar cuota ${i + 1}")

                cuotasGeneradas.add(
                    PagoCuota(
                        cuotaId               = cuotaId.toInt(),
                        socioId               = socioId,
                        fechaVencimientoCuota = fechaVenc,
                        estadoCuota           = estado,
                        montoCuota            = montoPorCuota,
                        medioPago             = medioPago,
                        mesCuota              = mesCuota,
                        anioCuota             = anioCuota
                    )
                )
            }

            db.setTransactionSuccessful()
            cuotasGeneradas

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // ==========================================================================
    // LISTAR PAGOS CON DETALLE — para la pantalla principal de Pagos (lista del Figma)
    // ==========================================================================
    // Retorna los PagoRealizado de tipo Cuota con nombre y apellido del socio.
    // El Figma muestra: nombre apellido | $monto | ícono comprobante
    fun listarPagosConDetalle(): List<Map<String, String>> {

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
                pc.MesCuota,
                pc.AnioCuota,
                pc.EstadoCuota
            FROM PagoRealizado pr
            INNER JOIN PagoCuota pc ON pr.PagoRealizadoID = pc.PagoRealizadoId
            INNER JOIN Socio s      ON pc.SocioId = s.SocioId
            INNER JOIN Persona p    ON s.SocioId  = p.PersonaId
            ORDER BY pr.FechaPago DESC
            """,
            null
        )

        while (cursor.moveToNext()) {
            lista.add(mapOf(
                "pagoId"   to cursor.getInt(cursor.getColumnIndexOrThrow("PagoRealizadoID")).toString(),
                "fecha"    to (cursor.getString(cursor.getColumnIndexOrThrow("FechaPago")) ?: ""),
                "monto"    to cursor.getDouble(cursor.getColumnIndexOrThrow("MontoTotal")).toString(),
                "medio"    to cursor.getString(cursor.getColumnIndexOrThrow("MedioPago")),
                "nombre"   to cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
                "apellido" to cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
                "mes"      to cursor.getInt(cursor.getColumnIndexOrThrow("MesCuota")).toString(),
                "anio"     to cursor.getInt(cursor.getColumnIndexOrThrow("AnioCuota")).toString(),
                "estado"   to cursor.getString(cursor.getColumnIndexOrThrow("EstadoCuota")),
                "tipo"     to "Cuota"
            ))
        }

        cursor.close()
        db.close()
        return lista
    }

    // ==========================================================================
    // VERIFICAR SI YA PAGÓ EL MES
    // ==========================================================================
    fun yaPagoElMes(socioId: Int, mes: Int, anio: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM PagoCuota WHERE SocioId=? AND MesCuota=? AND AnioCuota=? AND EstadoCuota='Pagado'",
            arrayOf(socioId.toString(), mes.toString(), anio.toString())
        )
        var yaPago = false
        if (cursor.moveToFirst()) yaPago = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return yaPago
    }

    // ==========================================================================
    // LISTAR MOROSOS — cuotas sin pagar (caso de uso 7)
    // ==========================================================================
    fun listarCuotasPendientes(): List<Map<String, String>> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Map<String, String>>()

        val cursor = db.rawQuery(
            """
            SELECT p.Nombre, p.Apellido, pc.MesCuota, pc.AnioCuota, pc.MontoCuota, pc.FechaVencimientoCuota
            FROM PagoCuota pc
            INNER JOIN Socio s   ON pc.SocioId = s.SocioId
            INNER JOIN Persona p ON s.SocioId  = p.PersonaId
            WHERE pc.EstadoCuota != 'Pagado'
            ORDER BY pc.FechaVencimientoCuota ASC
            """, null
        )

        while (cursor.moveToNext()) {
            lista.add(mapOf(
                "nombre"      to cursor.getString(cursor.getColumnIndexOrThrow("Nombre")),
                "apellido"    to cursor.getString(cursor.getColumnIndexOrThrow("Apellido")),
                "mes"         to cursor.getInt(cursor.getColumnIndexOrThrow("MesCuota")).toString(),
                "anio"        to cursor.getInt(cursor.getColumnIndexOrThrow("AnioCuota")).toString(),
                "monto"       to cursor.getDouble(cursor.getColumnIndexOrThrow("MontoCuota")).toString(),
                "vencimiento" to cursor.getString(cursor.getColumnIndexOrThrow("FechaVencimientoCuota"))
            ))
        }

        cursor.close()
        db.close()
        return lista
    }
}
