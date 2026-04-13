package com.medicalreports;

import com.medicalreports.gui.MainGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe principale del plugin per LibreOffice
 * Questo plugin permette di creare e archiviare referti medici con feature avanzate
 * 
 * Feature implementate:
 * - Creazione e gestione referti medici
 * - Archivio database locale crittografato (H2)
 * - Ricerca avanzata per paziente, codice fiscale, stato
 * - Hash di integrità per verificare la non alterazione dei referti
 * - Offuscamento dati sensibili per la privacy
 * - Audit log per tracciare tutte le operazioni
 * - Template per referti standardizzati
 * - Esportazione PDF (in sviluppo)
 * - Integrazione con LibreOffice Writer (in sviluppo)
 */
public class MedicalReportsPlugin {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicalReportsPlugin.class);
    
    public static void main(String[] args) {
        logger.info("Avvio Medical Reports Plugin v1.0.0");
        
        try {
            // Avvia l'interfaccia grafica
            MainGUI.main(args);
            logger.info("Plugin avviato con successo");
        } catch (Exception e) {
            logger.error("Errore durante l'avvio del plugin", e);
            System.err.println("Errore durante l'avvio: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
