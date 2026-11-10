/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.subscribe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PenyimpananFile {
    private final Path path;

    public PenyimpananFile(String namaFile) {
        this.path = Paths.get(namaFile);
    }

    public void simpan(List<Langganan> daftar) {
        List<String> baris = new ArrayList<>();
        for (Langganan l : daftar) {
            baris.add(l.getSiklus() + "|" + l.getNama().replace("|", " ") + "|"
                    + l.getKategori().name() + "|" + l.getHarga() + "|"
                    + l.getTanggalTagihan() + "|" + l.isAktif());
        }
        try {
            Files.write(path, baris, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Gagal menyimpan data: " + e.getMessage());
        }
    }

    public List<Langganan> muat() {
        List<Langganan> hasil = new ArrayList<>();
        if (!Files.exists(path)) return hasil;
        try {
            for (String baris : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String[] p = baris.split("\\|");
                if (p.length < 6) continue;
                Kategori kat = Kategori.valueOf(p[2]);
                double harga = Double.parseDouble(p[3]);
                LocalDate tgl = LocalDate.parse(p[4]);
                Langganan l = p[0].equals("Tahunan")
                        ? new LanggananTahunan(p[1], kat, harga, tgl)
                        : new LanggananBulanan(p[1], kat, harga, tgl);
                l.setAktif(Boolean.parseBoolean(p[5]));
                hasil.add(l);
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat data: " + e.getMessage());
        }
        return hasil;
    }
}