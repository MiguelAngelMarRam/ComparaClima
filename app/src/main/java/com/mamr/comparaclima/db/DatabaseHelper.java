package com.mamr.comparaclima.db;

/**
 * @author Miguel Ángel Martínez Ramírez
 * Proyecto: ComparaClima - TFG DAM
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mamr.comparaclima.models.Preset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ComparaClima.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Crear tablas de la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. TABLA USUARIOS
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "pref_temp INTEGER DEFAULT 25, " +
                "pref_viento INTEGER DEFAULT 50)");

        // 2. TABLA PRESETS
        db.execSQL("CREATE TABLE presets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "user_email TEXT, " + // Columna para vinculación
                "temp_max INTEGER, " +
                "temp_min INTEGER, " +
                "viento_max INTEGER, " +
                "lluvia_max INTEGER, " +
                "requiere_sol INTEGER)");

        // 3. TABLA HISTORIAL
        db.execSQL("CREATE TABLE historial (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT, " +
                "ciudad1 TEXT, " +
                "ciudad2 TEXT, " +
                "ganador TEXT, " +
                "modo TEXT, " +
                "periodo TEXT, " +
                "fecha TEXT)");

        // Insertar preset por defecto para el sistema/todos
        insertarPresetInicial(db, "sistema", "Solo Clima", 25, 15, 40, 20, 0);
    }

    private void insertarPresetInicial(SQLiteDatabase db, String email, String nom, int max, int min, int v, int ll, int s) {
        ContentValues vls = new ContentValues();
        vls.put("user_email", email);
        vls.put("nombre", nom);
        vls.put("temp_max", max);
        vls.put("temp_min", min);
        vls.put("viento_max", v);
        vls.put("lluvia_max", ll);
        vls.put("requiere_sol", s);
        db.insertWithOnConflict("presets", null, vls, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS presets");
        db.execSQL("DROP TABLE IF EXISTS historial");
        onCreate(db);
    }

    // --- MÉTODOS DE USUARIO ---

    public boolean registerUser(String nombre, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("email", email);
        values.put("password", password);
        return db.insert("usuarios", null, values) != -1;
    }

    public boolean verificarUsuario(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM usuarios WHERE email=? AND password=?", new String[]{email, password});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public String getNombreUsuario(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM usuarios WHERE email=?", new String[]{email});
        String nombre = "Usuario";
        if (cursor.moveToFirst()) nombre = cursor.getString(0);
        cursor.close();
        return nombre;
    }

    // --- MÉTODOS DE PRESETS ---

    public List<String> obtenerNombresPresets(String email) {
        List<String> nombres = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nombre FROM presets WHERE user_email=? OR user_email='sistema'", new String[]{email});
        if (c.moveToFirst()) {
            do { nombres.add(c.getString(0)); } while (c.moveToNext());
        }
        c.close();
        return nombres;
    }

    public Preset obtenerObjetoPreset(String nombre, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Buscamos el preset que coincida con nombre Y usuario (o sistema)
        Cursor c = db.rawQuery("SELECT * FROM presets WHERE nombre=? AND (user_email=? OR user_email='sistema')", new String[]{nombre, email});
        Preset p = null;
        if (c.moveToFirst()) {
            p = new Preset(
                    c.getString(1), // nombre
                    c.getInt(3),    // temp_max (Ahora es la columna 3 porque la 2 es user_email)
                    c.getInt(4),    // temp_min
                    c.getInt(5),    // viento_max
                    c.getInt(6),    // lluvia_max
                    c.getInt(7)     // requiere_sol
            );
        }
        c.close();
        return p;
    }

    public void insertarOActualizarPreset(String email, String nom, int max, int min, int v, int ll, int s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues vls = new ContentValues();
        vls.put("user_email", email);
        vls.put("nombre", nom);
        vls.put("temp_max", max);
        vls.put("temp_min", min);
        vls.put("viento_max", v);
        vls.put("lluvia_max", ll);
        vls.put("requiere_sol", s);

        db.replace("presets", null, vls);
    }

    // --- MÉTODOS DE HISTORIAL ---

    public void guardarHistorial(String email, String c1, String c2, String ganador, String modo, String periodo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", email);
        values.put("ciudad1", c1);
        values.put("ciudad2", c2);
        values.put("ganador", ganador);
        values.put("modo", modo);
        values.put("periodo", periodo);
        values.put("fecha", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));

        db.insert("historial", null, values);
        db.close();
    }

    public List<String[]> obtenerHistorial(String email) {
        List<String[]> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT ciudad1, ciudad2, modo, fecha FROM historial WHERE user_email=? ORDER BY id DESC", new String[]{email});

        if (c.moveToFirst()) {
            do {
                lista.add(new String[]{
                        c.getString(0), // ciudad1
                        c.getString(1), // ciudad2
                        c.getString(2), // modo (preset)
                        c.getString(3)  // fecha
                });
            } while (c.moveToNext());
        }
        c.close();
        return lista;
    }

    // --- METODOS DE BORRADO ---

    public void borrarRegistroHistorial(String email, String c1, String c2, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("historial", "user_email=? AND ciudad1=? AND ciudad2=? AND fecha=?",
                new String[]{email, c1, c2, fecha});
        db.close();
    }

    public void borrarTodoElHistorial(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("historial", "user_email=?", new String[]{email});
        db.close();
    }
}