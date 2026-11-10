/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.subscribe;

public enum Kategori {
    STREAMING("Streaming & Hiburan"),
    INTERNET("Internet & Komunikasi"),
    CLOUD("Penyimpanan & Cloud"),
    PRODUKTIVITAS("Produktivitas & Edukasi"),
    GAME("Game & Aplikasi");

    private final String label;

    Kategori(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

}