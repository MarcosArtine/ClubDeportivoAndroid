package com.grupo4.clubdeportivo.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//LA BASE DE DATOS SE TIENE QUE LLAMAR CLUB.DB
class BDatos(context: Context) : SQLiteOpenHelper(context, "Club.db", null, 1) {

    companion object {
        private const val SQL_CREATE_USUARIOS = """CREATE TABLE Usuario (
        UsuarioId INTEGER PRIMARY KEY AUTOINCREMENT,
        Email TEXT NOT NULL UNIQUE,
        Contrasena TEXT NOT NULL,   
        Nombre TEXT NOT NULL UNIQUE
        )
        """

        private const val SQL_CREATE_PERSONA = """CREATE TABLE Persona (
        PersonaId INTEGER PRIMARY KEY AUTOINCREMENT,
        Nombre TEXT NOT NULL,
        Apellido TEXT NOT NULL,
        TipoDni TEXT,
        NroDni TEXT NOT NULL UNIQUE,
        FechaNacimiento TEXT,
        Telefono TEXT,
        Email TEXT
        )
        """

        private const val SQL_CREATE_SOCIOS = """CREATE TABLE Socio (
        SocioId INTEGER PRIMARY KEY,
        FechaAltaSocio TEXT NOT NULL,
        NroCarnet TEXT NOT NULL UNIQUE,
        FOREIGN KEY (SocioId) REFERENCES Persona(PersonaId) ON DELETE CASCADE
        )
        """

        private const val SQL_CREATE_NOSOCIOS = """CREATE TABLE NoSocio (
        NoSocioId INTEGER PRIMARY KEY,
        FOREIGN KEY (NoSocioId) REFERENCES Persona(PersonaId) ON DELETE CASCADE
        )
        """

        private const val SQL_CREATE_ACTIVIDAD = """CREATE TABLE Actividad (
        ActividadId INTEGER PRIMARY KEY AUTOINCREMENT,
        NombreActividad TEXT NOT NULL UNIQUE,
        MontoActividad REAL NOT NULL
        )
        """

        private const val SQL_CREATE_PAGODIARIO = """CREATE TABLE PagoDiario (
        PagoDiarioId INTEGER PRIMARY KEY AUTOINCREMENT,
        NoSocioId INTEGER NOT NULL,
        ActividadId INTEGER NOT NULL,
        FechaPago TEXT NOT NULL,
        MontoPagado REAL NOT NULL,
        PagoRealizadoId INTEGER UNIQUE,
        FOREIGN KEY (NoSocioId) REFERENCES NoSocio(NoSocioId) ON DELETE CASCADE,
        FOREIGN KEY (ActividadId) REFERENCES Actividad(ActividadId) ON DELETE RESTRICT,
        FOREIGN KEY (PagoRealizadoId) REFERENCES PagoRealizado(PagoRealizadoID)
        )
        """

        private const val SQL_CREATE_PAGOCUOTA = """CREATE TABLE PagoCuota (
        CuotaId INTEGER PRIMARY KEY AUTOINCREMENT,
        SocioId INTEGER NOT NULL,
        FechaVencimientoCuota TEXT NOT NULL,
        EstadoCuota TEXT NOT NULL,
        MontoCuota REAL NOT NULL,
        MedioPago TEXT,
        PagoRealizadoId INTEGER UNIQUE,
        MesCuota INTEGER,
        AnioCuota INTEGER,
        FOREIGN KEY (SocioId) REFERENCES Socio(SocioId) ON DELETE CASCADE,
        FOREIGN KEY (PagoRealizadoId) REFERENCES PagoRealizado(PagoRealizadoID)
        )
        """

        private const val SQL_CREATE_PAGOREALIZADO = """CREATE TABLE PagoRealizado (
        PagoRealizadoID INTEGER PRIMARY KEY AUTOINCREMENT,
        FechaPago TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        MontoTotal REAL NOT NULL,
        TipoConcepto TEXT NOT NULL,
        MedioPago TEXT NOT NULL,
        ReferenciaId INTEGER NOT NULL
        )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_USUARIOS)
        db?.execSQL(SQL_CREATE_PERSONA)
        db?.execSQL(SQL_CREATE_SOCIOS)
        db?.execSQL(SQL_CREATE_NOSOCIOS)
        db?.execSQL(SQL_CREATE_ACTIVIDAD)
        db?.execSQL(SQL_CREATE_PAGODIARIO)
        db?.execSQL(SQL_CREATE_PAGOCUOTA)
        db?.execSQL(SQL_CREATE_PAGOREALIZADO)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Usuario")
        db?.execSQL("DROP TABLE IF EXISTS Persona")
        db?.execSQL("DROP TABLE IF EXISTS Socio")
        db?.execSQL("DROP TABLE IF EXISTS NoSocio")
        db?.execSQL("DROP TABLE IF EXISTS Actividad")
        db?.execSQL("DROP TABLE IF EXISTS PagoDiario")
        db?.execSQL("DROP TABLE IF EXISTS PagoCuota")
        db?.execSQL("DROP TABLE IF EXISTS PagoRealizado")
        onCreate(db)
    }
}