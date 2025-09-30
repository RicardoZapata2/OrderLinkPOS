package com.orderlink.pos.model;

import java.time.LocalDateTime;

public class Venta {
    private int id;
    private LocalDateTime fecha;
    private double total;

    public Venta(int id, LocalDateTime fecha, double total) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
    }

    public int getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public double getTotal() { return total; }

    public void setId(int id) { this.id = id; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setTotal(double total) { this.total = total; }
}
