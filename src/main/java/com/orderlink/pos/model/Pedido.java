package com.orderlink.pos.model;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private int id;
    private LocalDateTime fecha;
    private List<Producto> productos;
    private String estado; // en_preparacion, listo, entregado

    public Pedido(int id, LocalDateTime fecha, List<Producto> productos, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.productos = productos;
        this.estado = estado;
    }

    public int getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public List<Producto> getProductos() { return productos; }
    public String getEstado() { return estado; }

    public void setId(int id) { this.id = id; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }
    public void setEstado(String estado) { this.estado = estado; }
}
