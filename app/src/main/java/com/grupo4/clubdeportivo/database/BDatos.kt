package com.grupo4.clubdeportivo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//LA BASE DE DATOS SE TIENE QUE LLAMAR CLUB.DB
class BDatos(context: Context) : SQLiteOpenHelper(context, "Club.db", null, 1) {

    companion object {
        private const val SQL_CREATE_USUARIOS = "CREATE TABLE Usuario (id INTEGER PRIMARY KEY, nombre TEXT, clave INTEGER)"
        private const val SQL_CREATE_SOCIOS = "CREATE TABLE Socio (id INTEGER PRIMARY KEY, nombre TEXT, dni TEXT)"
        private const val SQL_CREATE_PAGOS = "CREATE TABLE Pago (id INTEGER PRIMARY KEY, monto REAL, fecha TEXT)"
    //ACTIVIDADES
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_USUARIOS)
        db?.execSQL(SQL_CREATE_SOCIOS)
        db?.execSQL(SQL_CREATE_PAGOS)
 //actividades
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Usuario")
        db?.execSQL("DROP TABLE IF EXISTS Socio")
        db?.execSQL("DROP TABLE IF EXISTS Pago")
       // db?.execSQL("DROP TABLE IF EXISTS Actividad")
        onCreate(db)
    }
}