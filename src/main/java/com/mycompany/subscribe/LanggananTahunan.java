/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.subscribe;

import java.time.LocalDate;

public class LanggananTahunan extends Langganan {

    public LanggananTahunan(String nama, Kategori kategori, double harga, LocalDate tanggalTagihan) {
        super(nama, kategori, harga, tanggalTagihan);
    }

    @Override
    public double hitungBiayaBulanan() { return getHarga() / 12; }

    @Override
    public double hitungBiayaTahunan() { return getHarga(); }

    @Override
    public String getSiklus() { return "Tahunan"; }
}