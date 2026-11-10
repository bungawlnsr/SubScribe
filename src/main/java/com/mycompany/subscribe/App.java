package com.mycompany.subscribe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {
    private static final Scanner input = new Scanner(System.in);
    private static final DateTimeFormatter FORMAT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        PenyimpananFile penyimpanan = new PenyimpananFile("data_langganan.txt");
        List<Langganan> daftar = penyimpanan.muat();  
        
        tampilkanDashboard(daftar);

        boolean jalan = true;
        while (jalan) {
            System.out.println("\n=== MENU SUBSCRIBE ===");
            System.out.println("1. Tambah langganan");
            System.out.println("2. Lihat daftar langganan");
            System.out.println("3. Ubah langganan");
            System.out.println("4. Hapus langganan");
            System.out.println("5. Aktifkan / nonaktifkan langganan");
            System.out.println("6. Cari / filter kategori");
            System.out.println("7. Ringkasan pengeluaran");
            System.out.println("0. Simpan & keluar");
            int pilihan = bacaInt("Pilih menu: ", 0, 7);

            switch (pilihan) {
                case 1: tambah(daftar); break;
                case 2: tampilkanTabel(daftar); break;
                case 3: ubah(daftar); break;
                case 4: hapus(daftar); break;
                case 5: toggleAktif(daftar); break;
                case 6: cari(daftar); break;
                case 7: ringkasan(daftar); break;
                case 0:
                    penyimpanan.simpan(daftar);
                    System.out.println("Data tersimpan. Sampai jumpa!");
                    jalan = false;
                    break;
            }
        }
    }

    private static void tambah(List<Langganan> daftar) {
        System.out.println("\n--- Tambah Langganan ---");
        Kategori kategori = pilihKategori();
        String nama = bacaTeks("Nama layanan: ");
        double harga = bacaHarga("Harga: ");
        System.out.println("Siklus tagihan: 1. Bulanan  2. Tahunan");
        int siklus = bacaInt("Pilih siklus: ", 1, 2);
        LocalDate tgl = bacaTanggal("Tanggal tagihan berikutnya: ");

        Langganan baru;
        if (siklus == 1) {
            baru = new LanggananBulanan(nama, kategori, harga, tgl);
        } else {
            baru = new LanggananTahunan(nama, kategori, harga, tgl);
        }
        daftar.add(baru);
        System.out.println("Langganan \"" + baru.getNama() + "\" berhasil ditambahkan.");
    }

    private static void ubah(List<Langganan> daftar) {
        Langganan l = pilihLangganan(daftar, "diubah");
        if (l == null) return;
        System.out.println("(Siklus tidak dapat diubah; hapus lalu tambah ulang bila perlu.)");
        System.out.println("Kosongkan input dan tekan Enter untuk mempertahankan nilai lama.");

        System.out.print("Nama [" + l.getNama() + "]: ");
        String nama = input.nextLine().trim();
        if (!nama.isEmpty()) l.setNama(nama);

        System.out.print("Ubah kategori? (y/n): ");
        if (input.nextLine().trim().equalsIgnoreCase("y")) l.setKategori(pilihKategori());

        System.out.print("Harga [" + rupiah(l.getHarga()) + "]: ");
        String h = input.nextLine().trim();
        if (!h.isEmpty()) {
            try {
                l.setHarga(Double.parseDouble(h));
            } catch (Exception e) {
                System.out.println("Harga tidak valid");
            }
        }

        System.out.print("Tanggal tagihan [" + l.getTanggalTagihan().format(FORMAT_TGL) + "]: ");
        String t = input.nextLine().trim();
        if (!t.isEmpty()) {
            try {
                l.setTanggalTagihan(LocalDate.parse(t, FORMAT_TGL));
            } catch (DateTimeParseException e) {
                System.out.println("Format tanggal salah");
            }
        }
        System.out.println("Data berhasil diperbarui.");
    }

    private static void hapus(List<Langganan> daftar) {
        Langganan l = pilihLangganan(daftar, "dihapus");
        if (l == null) return;
        System.out.print("Yakin hapus \"" + l.getNama() + "\" secara permanen? (y/n): ");
        if (input.nextLine().trim().equalsIgnoreCase("y")) {
            daftar.remove(l);
            System.out.println("Langganan dihapus.");
        } else {
            System.out.println("Dibatalkan.");
        }
    }

    private static void toggleAktif(List<Langganan> daftar) {
        Langganan l = pilihLangganan(daftar, "diubah statusnya");
        if (l == null) return;
        l.setAktif(!l.isAktif());
        System.out.println("\"" + l.getNama() + "\" sekarang " + (l.isAktif() ? "AKTIF" : "NONAKTIF")
                + " (hanya status pencatatan di aplikasi ini).");
    }

    private static void cari(List<Langganan> daftar) {
        System.out.print("Kata kunci nama (kosongkan untuk semua): ");
        String kunci = input.nextLine().trim().toLowerCase();
        System.out.println("Filter kategori: 0. Semua kategori");
        Kategori[] semua = Kategori.values();
        for (int i = 0; i < semua.length; i++) {
            System.out.println((i + 1) + ". " + semua[i].getLabel());
        }
        int p = bacaInt("Pilih: ", 0, semua.length);

        List<Langganan> hasil = new ArrayList<>();
        for (Langganan l : daftar) {
            boolean cocokNama = l.getNama().toLowerCase().contains(kunci);
            boolean cocokKategori = (p == 0) || l.getKategori() == semua[p - 1];
            if (cocokNama && cocokKategori) hasil.add(l);
        }
        tampilkanTabel(hasil);
    }

    private static void ringkasan(List<Langganan> daftar) {
        System.out.println("\n=== RINGKASAN PENGELUARAN (langganan aktif) ===");
        double totalBulan = 0, totalTahun = 0;
        System.out.printf("%-26s %6s %18s %18s%n", "Kategori", "Jumlah", "Per Bulan", "Per Tahun");
        for (Kategori k : Kategori.values()) {
            int jumlah = 0;
            double bulan = 0, tahun = 0;
            for (Langganan l : daftar) {
                if (l.isAktif() && l.getKategori() == k) {
                    jumlah++;
                    bulan += l.hitungBiayaBulanan();   
                    tahun += l.hitungBiayaTahunan();   
                }
            }
            totalBulan += bulan;
            totalTahun += tahun;
            System.out.printf("%-26s %6d %18s %18s%n", k.getLabel(), jumlah, rupiah(bulan), rupiah(tahun));
        }
        System.out.println("-".repeat(72));
        System.out.printf("%-26s %6s %18s %18s%n", "TOTAL", "", rupiah(totalBulan), rupiah(totalTahun));
        System.out.println("Estimasi pengeluaran setahun: " + rupiah(totalTahun));
    }

        private static void tampilkanDashboard(List<Langganan> daftar) {
        LocalDate sekarang = LocalDate.now();
        double tagihanBulanIni = 0;
        double rataBulanan = 0;
        int aktif = 0;

        for (Langganan l : daftar) {
            if (l.isAktif()) {
                aktif++;
                rataBulanan += l.hitungBiayaBulanan();

                LocalDate tgl = l.getTanggalTagihan();
                if (tgl.getMonth() == sekarang.getMonth() && tgl.getYear() == sekarang.getYear()) {
                    tagihanBulanIni += l.getHarga();
                }
            }
        }
        System.out.println("\n==================================================");
        System.out.println("  SUBSCRIBE");
        System.out.println("  Langganan aktif            : " + aktif + " dari " + daftar.size());
        System.out.println("  Total tagihan bulan ini    : " + rupiah(tagihanBulanIni));
        System.out.println("  Rata-rata pengeluaran/bulan: " + rupiah(rataBulanan));
        System.out.println("==================================================");
        tampilkanTabel(daftar);
    }

    private static void tampilkanTabel(List<Langganan> daftar) {
        if (daftar.isEmpty()) {
            System.out.println("(Belum ada data langganan.)");
            return;
        }
        System.out.printf("%-3s %-20s %-24s %-8s %14s %-12s %-10s %-8s%n",
                "No", "Layanan", "Kategori", "Siklus", "Harga", "Jatuh Tempo", "Sisa Hari", "Status");
        System.out.println("-".repeat(108));
        for (int i = 0; i < daftar.size(); i++) {
            Langganan l = daftar.get(i);
            String sisa, warna = "", reset = "";
            if (l.isAktif()) {
                sisa = l.hitungSisaHari() + " hari";
                warna = kodeAnsi(l.tentukanWarna());
                reset = RESET;
            } else {
                sisa = "-";
            }
            System.out.printf("%s%-3d %-20s %-24s %-8s %14s %-12s %-10s %-8s%s%n",
                    warna, i + 1, potong(l.getNama(), 20), l.getKategori().getLabel(), l.getSiklus(),
                    rupiah(l.getHarga()), l.getTanggalTagihan().format(FORMAT_TGL), sisa,
                    l.isAktif() ? l.tentukanWarna() : "NONAKTIF", reset);
        }
        System.out.println("Keterangan: MERAH <= 3 hari | KUNING 4-7 hari | HIJAU aman");
    }

    private static Kategori pilihKategori() {
        Kategori[] semua = Kategori.values();
        System.out.println("Pilih kategori:");
        for (int i = 0; i < semua.length; i++) {
            System.out.println((i + 1) + ". " + semua[i].getLabel());
        }
        return semua[bacaInt("Pilih kategori: ", 1, semua.length) - 1];
    }

    private static Langganan pilihLangganan(List<Langganan> daftar, String aksi) {
        if (daftar.isEmpty()) {
            System.out.println("Belum ada data langganan.");
            return null;
        }
        tampilkanTabel(daftar);
        int no = bacaInt("Nomor langganan yang akan " + aksi + " (0 = batal): ", 0, daftar.size());
        return no == 0 ? null : daftar.get(no - 1);
    }

    private static int bacaInt(String pesan, int min, int max) {
        while (true) {
            System.out.print(pesan);
            try {
                int n = Integer.parseInt(input.nextLine().trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException e) {
                // jatuh ke pesan error di bawah
            }
            System.out.println("Input tidak valid. Masukkan angka " + min + "-" + max + ".");
        }
    }

    private static double bacaHarga(String pesan) {
        while (true) {
            System.out.print(pesan);
            try {
                double h = Double.parseDouble(input.nextLine().trim());
                if (h > 0) return h;
            } catch (NumberFormatException e) {
            }
            System.out.println("Harga harus berupa angka positif.");
        }
    }

    private static String bacaTeks(String pesan) {
        while (true) {
            System.out.print(pesan);
            String s = input.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Input tidak boleh kosong.");
        }
    }

    private static LocalDate bacaTanggal(String pesan) {
        while (true) {
            System.out.print(pesan);
            try {
                return LocalDate.parse(input.nextLine().trim(), FORMAT_TGL);
            } catch (DateTimeParseException e) {
                System.out.println("Format tanggal salah. Contoh: 15-12-2026");
            }
        }
    }

    private static String rupiah(double nilai) {
        return String.format(ID, "Rp %,.0f", nilai);
    }

    private static String potong(String s, int maks) {
        return s.length() <= maks ? s : s.substring(0, maks - 1) + "~";
    }

    private static String kodeAnsi(String warna) {
        switch (warna) {
            case "MERAH":  return "\u001B[31m";
            case "KUNING": return "\u001B[33m";
            default:       return "\u001B[32m";
        }
    }
}