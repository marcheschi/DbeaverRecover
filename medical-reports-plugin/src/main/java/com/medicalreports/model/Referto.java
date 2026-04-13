package com.medicalreports.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modello dati per un referto medico
 */
public class Referto {
    
    private String id;
    private String codiceFiscalePaziente;
    private String nomePaziente;
    private String cognomePaziente;
    private String dataNascita;
    private String sesso;
    private String tipoReferto;
    private String specializzazione;
    private String medicoRichiedente;
    private String medicoCompilatore;
    private String strutturaSanitaria;
    private String testoReferto;
    private String diagnosi;
    private String terapia;
    private String note;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataModifica;
    private String stato; // BOZZA, COMPLETATO, FIRMATO, ARCHIVIATO
    private String priorita; // NORMALE, URGENTE, PRIORITARIO
    private boolean firmato;
    private String firmaDigitale;
    private String hashIntegrita;
    
    public Referto() {
        this.id = UUID.randomUUID().toString();
        this.dataCreazione = LocalDateTime.now();
        this.stato = "BOZZA";
        this.priorita = "NORMALE";
        this.firmato = false;
    }
    
    // Getters e Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCodiceFiscalePaziente() {
        return codiceFiscalePaziente;
    }
    
    public void setCodiceFiscalePaziente(String codiceFiscalePaziente) {
        this.codiceFiscalePaziente = codiceFiscalePaziente;
    }
    
    public String getNomePaziente() {
        return nomePaziente;
    }
    
    public void setNomePaziente(String nomePaziente) {
        this.nomePaziente = nomePaziente;
    }
    
    public String getCognomePaziente() {
        return cognomePaziente;
    }
    
    public void setCognomePaziente(String cognomePaziente) {
        this.cognomePaziente = cognomePaziente;
    }
    
    public String getDataNascita() {
        return dataNascita;
    }
    
    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }
    
    public String getSesso() {
        return sesso;
    }
    
    public void setSesso(String sesso) {
        this.sesso = sesso;
    }
    
    public String getTipoReferto() {
        return tipoReferto;
    }
    
    public void setTipoReferto(String tipoReferto) {
        this.tipoReferto = tipoReferto;
    }
    
    public String getSpecializzazione() {
        return specializzazione;
    }
    
    public void setSpecializzazione(String specializzazione) {
        this.specializzazione = specializzazione;
    }
    
    public String getMedicoRichiedente() {
        return medicoRichiedente;
    }
    
    public void setMedicoRichiedente(String medicoRichiedente) {
        this.medicoRichiedente = medicoRichiedente;
    }
    
    public String getMedicoCompilatore() {
        return medicoCompilatore;
    }
    
    public void setMedicoCompilatore(String medicoCompilatore) {
        this.medicoCompilatore = medicoCompilatore;
    }
    
    public String getStrutturaSanitaria() {
        return strutturaSanitaria;
    }
    
    public void setStrutturaSanitaria(String strutturaSanitaria) {
        this.strutturaSanitaria = strutturaSanitaria;
    }
    
    public String getTestoReferto() {
        return testoReferto;
    }
    
    public void setTestoReferto(String testoReferto) {
        this.testoReferto = testoReferto;
    }
    
    public String getDiagnosi() {
        return diagnosi;
    }
    
    public void setDiagnosi(String diagnosi) {
        this.diagnosi = diagnosi;
    }
    
    public String getTerapia() {
        return terapia;
    }
    
    public void setTerapia(String terapia) {
        this.terapia = terapia;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }
    
    public void setDataCreazione(LocalDateTime dataCreazione) {
        this.dataCreazione = dataCreazione;
    }
    
    public LocalDateTime getDataModifica() {
        return dataModifica;
    }
    
    public void setDataModifica(LocalDateTime dataModifica) {
        this.dataModifica = dataModifica;
    }
    
    public String getStato() {
        return stato;
    }
    
    public void setStato(String stato) {
        this.stato = stato;
    }
    
    public String getPriorita() {
        return priorita;
    }
    
    public void setPriorita(String priorita) {
        this.priorita = priorita;
    }
    
    public boolean isFirmato() {
        return firmato;
    }
    
    public void setFirmato(boolean firmato) {
        this.firmato = firmato;
    }
    
    public String getFirmaDigitale() {
        return firmaDigitale;
    }
    
    public void setFirmaDigitale(String firmaDigitale) {
        this.firmaDigitale = firmaDigitale;
    }
    
    public String getHashIntegrita() {
        return hashIntegrita;
    }
    
    public void setHashIntegrita(String hashIntegrita) {
        this.hashIntegrita = hashIntegrita;
    }
    
    @Override
    public String toString() {
        return "Referto{" +
                "id='" + id + '\'' +
                ", paziente='" + cognomePaziente + " " + nomePaziente + '\'' +
                ", tipo='" + tipoReferto + '\'' +
                ", stato='" + stato + '\'' +
                ", data=" + dataCreazione +
                '}';
    }
}
