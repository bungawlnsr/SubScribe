package com.mycompany.subscribe;

import java.time.LocalDate;

public class LanggananBulanan extends Langganan {

    public LanggananBulanan(String nama, Kategori kategori, double harga, LocalDate tanggalTagihan) {
        super(nama, kategori, harga, tanggalTagihan);
    }

    @Override
    public double hitungBiayaBulanan() { return getHarga(); }

    @Override
    public double hitungBiayaTahunan() { return getHarga() * 12; }

    @Override
    public String getSiklus() { return "Bulanan"; }
}