package com.mycompany.subscribe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.Function;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private final PenyimpananFile penyimpanan = new PenyimpananFile("data_langganan.txt");
    private final ObservableList<Langganan> daftar = FXCollections.observableArrayList();
    private final FilteredList<Langganan> tampil = new FilteredList<>(daftar, l -> true);
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Stage stage;
    private Scene sceneUtama;

    private final ComboBox<String> cbKategori = new ComboBox<>();
    private final TextField txtNama = new TextField();
    private final TextField txtHarga = new TextField();
    private final ComboBox<String> cbSiklus = new ComboBox<>();
    private final TextField txtTanggal = new TextField();

    private final TextField txtCari = new TextField();
    private final ComboBox<String> cbFilter = new ComboBox<>();
    private final Label lblAktif = new Label();
    private final Label lblTagihan = new Label();
    private final Label lblTahunan = new Label();
    private final TableView<Langganan> tabel = new TableView<>(tampil);

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        daftar.addAll(penyimpanan.muat());

        for (Kategori k : Kategori.values()) {
            cbKategori.getItems().add(k.getLabel());
            cbFilter.getItems().add(k.getLabel());
        }
        cbFilter.getItems().add(0, "Semua kategori");
        cbFilter.setValue("Semua kategori");
        cbSiklus.getItems().addAll("Bulanan", "Tahunan");
        cbKategori.setPromptText("Pilih kategori");
        cbSiklus.setPromptText("Pilih siklus");
        cbKategori.setMaxWidth(Double.MAX_VALUE);
        cbSiklus.setMaxWidth(Double.MAX_VALUE);
        txtNama.setPromptText("Contoh: Netflix");
        txtHarga.setPromptText("Contoh: 54000");
        txtTanggal.setPromptText("Contoh: 15-12-2026");
        txtCari.setPromptText("Cari nama layanan...");

        Button btnTambah = new Button("Tambah");
        Button btnUbah = new Button("Ubah");
        Button btnHapus = new Button("Hapus");
        Button btnToggle = new Button("Aktif / Nonaktif");
        Button btnReset = new Button("Reset Form");
        Button btnRingkasan = new Button("Ringkasan");

        tabel.getColumns().add(kolom("Layanan", l -> l.getNama()));
        tabel.getColumns().add(kolom("Kategori", l -> l.getKategori().getLabel()));
        tabel.getColumns().add(kolom("Siklus", l -> l.getSiklus()));
        tabel.getColumns().add(kolom("Harga", l -> rupiah(l.getHarga())));
        tabel.getColumns().add(kolom("Jatuh Tempo", l -> l.getTanggalTagihan().format(format)));
        tabel.getColumns().add(kolom("Sisa Hari", l -> l.isAktif() ? l.hitungSisaHari() + " hari" : "-"));
        tabel.getColumns().add(kolom("Status", l -> l.isAktif() ? "Aktif" : "Nonaktif"));
        double[] lebar = {100, 170, 75, 95, 100, 85, 70};  
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumns().get(i).setMinWidth(lebar[i]);
        }
        tabel.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabel.setRowFactory(tv -> new TableRow<Langganan>() {
            @Override
            protected void updateItem(Langganan l, boolean kosong) {
                super.updateItem(l, kosong);
                if (kosong || l == null) setStyle("");
                else if (!l.isAktif()) setStyle(warna("#E0E0E0"));
                else if (l.tentukanWarna().equals("MERAH")) setStyle(warna("#FFCDD2"));
                else if (l.tentukanWarna().equals("KUNING")) setStyle(warna("#FFF9C4"));
                else setStyle(warna("#C8E6C9"));
            }
        });

        // Klik baris tabel -> isi form
        tabel.getSelectionModel().selectedItemProperty().addListener((obs, lama, l) -> {
            if (l != null) {
                cbKategori.setValue(l.getKategori().getLabel());
                txtNama.setText(l.getNama());
                txtHarga.setText(String.valueOf((long) l.getHarga()));
                cbSiklus.setValue(l.getSiklus());
                txtTanggal.setText(l.getTanggalTagihan().format(format));
            }
        });

        btnTambah.setOnAction(e -> {
            Langganan baru = bacaForm();
            if (baru == null) return;
            daftar.add(baru);
            simpanDanPerbarui();
            resetForm();
            tampilkanAlert(AlertType.INFORMATION, "Berhasil", "Langganan \"" + baru.getNama() + "\" ditambahkan.");
        });

        btnUbah.setOnAction(e -> {
            Langganan lama = tabel.getSelectionModel().getSelectedItem();
            if (lama == null) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih dulu langganan di tabel!");
                return;
            }
            Langganan baru = bacaForm();
            if (baru == null) return;
            baru.setAktif(lama.isAktif());
            daftar.set(daftar.indexOf(lama), baru);
            simpanDanPerbarui();
            tampilkanAlert(AlertType.INFORMATION, "Berhasil", "Data langganan diperbarui.");
        });

        btnHapus.setOnAction(e -> {
            Langganan l = tabel.getSelectionModel().getSelectedItem();
            if (l == null) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih dulu langganan di tabel!");
                return;
            }
            Alert konfirmasi = new Alert(AlertType.CONFIRMATION, "Hapus \"" + l.getNama() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            if (konfirmasi.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                daftar.remove(l);
                simpanDanPerbarui();
                resetForm();
            }
        });

        btnToggle.setOnAction(e -> {
            Langganan l = tabel.getSelectionModel().getSelectedItem();
            if (l == null) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Pilih dulu langganan di tabel!");
                return;
            }
            l.setAktif(!l.isAktif());
            simpanDanPerbarui();
        });

        btnReset.setOnAction(e -> resetForm());
        btnRingkasan.setOnAction(e -> stage.setScene(buatSceneRingkasan()));   
        txtCari.textProperty().addListener((obs, a, b) -> terapkanFilter());
        cbFilter.valueProperty().addListener((obs, a, b) -> terapkanFilter());

        Label judul = new Label("SubScribe - Smart Economy");
        judul.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox dashboard = new HBox(30, lblAktif, lblTagihan, lblTahunan);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.add(new Label("Kategori:"), 0, 0);
        grid.add(cbKategori, 1, 0);
        grid.add(new Label("Nama Layanan:"), 0, 1);
        grid.add(txtNama, 1, 1);
        grid.add(new Label("Harga (Rp):"), 0, 2);
        grid.add(txtHarga, 1, 2);
        grid.add(new Label("Siklus Tagihan:"), 0, 3);
        grid.add(cbSiklus, 1, 3);
        grid.add(new Label("Jatuh Tempo:"), 0, 4);
        grid.add(txtTanggal, 1, 4);
        grid.add(new HBox(8, btnTambah, btnUbah, btnHapus), 0, 5, 2, 1);
        grid.add(new HBox(8, btnToggle, btnReset, btnRingkasan), 0, 6, 2, 1);

        HBox pencarian = new HBox(10, txtCari, cbFilter);
        HBox.setHgrow(txtCari, Priority.ALWAYS);
        VBox kanan = new VBox(10, pencarian, tabel);
        VBox.setVgrow(tabel, Priority.ALWAYS);
        HBox.setHgrow(kanan, Priority.ALWAYS);

        HBox isi = new HBox(20, grid, kanan);
        VBox.setVgrow(isi, Priority.ALWAYS);

        VBox rootLayout = new VBox(15, judul, dashboard, isi);
        rootLayout.setPadding(new Insets(20));

        simpanDanPerbarui();
        sceneUtama = new Scene(rootLayout, 1150, 560);
        primaryStage.setTitle("SubScribe - Pencatatan Langganan Digital");
        primaryStage.setScene(sceneUtama);
        primaryStage.show();
    }

    @Override
    public void stop() {
        penyimpanan.simpan(daftar);
    }

    private Scene buatSceneRingkasan() {
        Label judul = new Label("Ringkasan Pengeluaran");
        judul.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        GridPane tb = new GridPane();
        tb.setHgap(60);
        tb.setVgap(12);
        String[] judulKolom = {"Kategori", "Jumlah", "Bulanan", "Tahunan"};
        for (int i = 0; i < judulKolom.length; i++) {
            tb.add(tebal(judulKolom[i]), i, 0);
        }

        int totalJumlah = 0;
        double totalBulanan = 0, totalTahunan = 0;
        int baris = 1;
        for (Kategori k : Kategori.values()) {
            int jumlah = 0;
            double bulanan = 0, tahunan = 0;
            for (Langganan l : daftar) {
                if (l.isAktif() && l.getKategori() == k) {
                    jumlah++;
                    if (l.getSiklus().equals("Bulanan")) bulanan += l.hitungBiayaBulanan();     
                    else tahunan += l.hitungBiayaTahunan();                                     
                }
            }
            totalJumlah += jumlah;
            totalBulanan += bulanan;
            totalTahunan += tahunan;
            tb.add(new Label(k.getLabel()), 0, baris);
            tb.add(new Label(String.valueOf(jumlah)), 1, baris);
            tb.add(new Label(bulanan == 0 ? "-" : rupiah(bulanan)), 2, baris);
            tb.add(new Label(tahunan == 0 ? "-" : rupiah(tahunan)), 3, baris);
            baris++;
        }
        
        tb.add(tebal("Total Keseluruhan"), 0, baris);
        tb.add(tebal(String.valueOf(totalJumlah)), 1, baris);
        tb.add(tebal(rupiah(totalBulanan)), 2, baris);
        tb.add(tebal(rupiah(totalTahunan)), 3, baris);

        Label total = tebal("Total Seluruh Langganan: " + rupiah(totalBulanan + totalTahunan));
        total.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label ket = new Label("Gabungan nominal langganan bulanan dan tahunan sesuai harga yang diinput, bukan tagihan per bulan.");

        Button btnKembali = new Button("Kembali ke Dashboard");
        btnKembali.setOnAction(e -> stage.setScene(sceneUtama));   

        VBox layout = new VBox(18, judul, tb, total, ket, btnKembali);
        layout.setPadding(new Insets(20));
        return new Scene(layout, sceneUtama.getWidth(), sceneUtama.getHeight());
    }

    private Langganan bacaForm() {
        String nama = txtNama.getText().trim();
        String strHarga = txtHarga.getText().trim().replace(".", "");
        String strTanggal = txtTanggal.getText().trim();

        // A. Validasi kolom kosong
        if (cbKategori.getValue() == null || cbSiklus.getValue() == null
                || nama.isEmpty() || strHarga.isEmpty() || strTanggal.isEmpty()) {
            tampilkanAlert(AlertType.WARNING, "Peringatan", "Seluruh kolom form wajib diisi!");
            return null;
        }

        try {
            double harga = Double.parseDouble(strHarga);
            LocalDate tanggal = LocalDate.parse(strTanggal, format);
            Kategori kategori = cariKategori(cbKategori.getValue());
            if (cbSiklus.getValue().equals("Bulanan")) {
                return new LanggananBulanan(nama, kategori, harga, tanggal);
            }
            return new LanggananTahunan(nama, kategori, harga, tanggal);
        } catch (NumberFormatException ex) {
            tampilkanAlert(AlertType.ERROR, "Kesalahan Format", "Harga harus berupa angka, contoh: 54000");
        } catch (DateTimeParseException ex) {
            tampilkanAlert(AlertType.ERROR, "Kesalahan Format", "Tanggal harus berformat dd-MM-yyyy, contoh: 15-12-2026");
        } catch (IllegalArgumentException ex) {
            tampilkanAlert(AlertType.WARNING, "Peringatan", ex.getMessage());   
        }
        return null;
    }

    private void tampilkanAlert(AlertType tipe, String judul, String pesan) {
        Alert alert = new Alert(tipe);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    private void simpanDanPerbarui() {
        penyimpanan.simpan(daftar);
        LocalDate sekarang = LocalDate.now();
        int aktif = 0;
        double tagihanBulanIni = 0, totalTahunan = 0;
        for (Langganan l : daftar) {
            if (l.isAktif()) {
                aktif++;
                if (l.getSiklus().equals("Tahunan")) {
                    totalTahunan += l.hitungBiayaTahunan();    
                }
                LocalDate t = l.getTanggalTagihan();
                if (t.getMonth() == sekarang.getMonth() && t.getYear() == sekarang.getYear()) {
                    tagihanBulanIni += l.getHarga();
                }
            }
        }
        lblAktif.setText("Langganan aktif: " + aktif + " dari " + daftar.size());
        lblTagihan.setText("Tagihan bulan ini: " + rupiah(tagihanBulanIni));
        lblTahunan.setText("Total langganan tahunan: " + rupiah(totalTahunan));
        tabel.refresh();
    }

    private void terapkanFilter() {
        String kunci = txtCari.getText().trim().toLowerCase();
        String kat = cbFilter.getValue();
        tampil.setPredicate(l -> l.getNama().toLowerCase().contains(kunci)
                && (kat.equals("Semua kategori") || l.getKategori().getLabel().equals(kat)));
    }

    private void resetForm() {
        tabel.getSelectionModel().clearSelection();
        cbKategori.setValue(null);
        cbSiklus.setValue(null);
        txtNama.clear();
        txtHarga.clear();
        txtTanggal.clear();
    }

    private Kategori cariKategori(String label) {
        for (Kategori k : Kategori.values()) {
            if (k.getLabel().equals(label)) return k;
        }
        return null;
    }

    private TableColumn<Langganan, String> kolom(String judul, Function<Langganan, String> isi) {
        TableColumn<Langganan, String> k = new TableColumn<>(judul);
        k.setCellValueFactory(c -> new SimpleStringProperty(isi.apply(c.getValue())));
        return k;
    }

    private Label tebal(String teks) {
        Label l = new Label(teks);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private String warna(String hex) {
        return "-fx-control-inner-background: " + hex + "; -fx-control-inner-background-alt: " + hex + ";";
    }

    private String rupiah(double nilai) {
        return String.format(Locale.forLanguageTag("id-ID"), "Rp %,.0f", nilai);
    }

    public static void main(String[] args) {
        launch(args);
    }
}