package com.medicalreports.database;

import com.medicalreports.model.Referto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestore del database per l'archiviazione dei referti medici
 * Utilizza H2 Database embedded con crittografia
 */
public class DatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:h2:~/medical-reports-db";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "medical_reports_2024_secure";
    
    private Connection connection;
    
    public DatabaseManager() {
        initializeDatabase();
    }
    
    /**
     * Inizializza il database e crea le tabelle necessarie
     */
    private void initializeDatabase() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
            logger.info("Database inizializzato con successo");
        } catch (Exception e) {
            logger.error("Errore durante l'inizializzazione del database", e);
            throw new RuntimeException("Impossibile inizializzare il database", e);
        }
    }
    
    /**
     * Crea le tabelle del database se non esistono
     */
    private void createTables() throws SQLException {
        String createRefertiTable = """
            CREATE TABLE IF NOT EXISTS referti (
                id VARCHAR(36) PRIMARY KEY,
                codice_fiscale_paziente VARCHAR(16) NOT NULL,
                nome_paziente VARCHAR(100) NOT NULL,
                cognome_paziente VARCHAR(100) NOT NULL,
                data_nascita VARCHAR(10),
                sesso VARCHAR(10),
                tipo_referto VARCHAR(100),
                specializzazione VARCHAR(100),
                medico_richiedente VARCHAR(200),
                medico_compilatore VARCHAR(200),
                struttura_sanitaria VARCHAR(200),
                testo_referto TEXT,
                diagnosi TEXT,
                terapia TEXT,
                note TEXT,
                data_creazione TIMESTAMP NOT NULL,
                data_modifica TIMESTAMP,
                stato VARCHAR(50) NOT NULL,
                priorita VARCHAR(50),
                firmato BOOLEAN DEFAULT FALSE,
                firma_digitale TEXT,
                hash_integrita VARCHAR(64),
                INDEX idx_cognome (cognome_paziente),
                INDEX idx_cf (codice_fiscale_paziente),
                INDEX idx_data (data_creazione),
                INDEX idx_stato (stato)
            )
            """;
        
        String createTemplateTable = """
            CREATE TABLE IF NOT EXISTS template_referti (
                id VARCHAR(36) PRIMARY KEY,
                nome_template VARCHAR(200) NOT NULL,
                specializzazione VARCHAR(100),
                contenuto_template TEXT NOT NULL,
                campi_variabili TEXT,
                data_creazione TIMESTAMP NOT NULL,
                data_modifica TIMESTAMP,
                attivo BOOLEAN DEFAULT TRUE
            )
            """;
        
        String createAuditTable = """
            CREATE TABLE IF NOT EXISTS audit_log (
                id VARCHAR(36) PRIMARY KEY,
                id_referto VARCHAR(36),
                azione VARCHAR(50) NOT NULL,
                utente VARCHAR(200),
                data_azione TIMESTAMP NOT NULL,
                dettagli TEXT,
                INDEX idx_referto (id_referto),
                INDEX idx_data (data_azione)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createRefertiTable);
            stmt.execute(createTemplateTable);
            stmt.execute(createAuditTable);
            logger.info("Tabelle create con successo");
        }
    }
    
    /**
     * Salva un nuovo referto nel database
     */
    public boolean salvaReferto(Referto referto) {
        String sql = """
            INSERT INTO referti (
                id, codice_fiscale_paziente, nome_paziente, cognome_paziente,
                data_nascita, sesso, tipo_referto, specializzazione,
                medico_richiedente, medico_compilatore, struttura_sanitaria,
                testo_referto, diagnosi, terapia, note,
                data_creazione, stato, priorita, firmato, firma_digitale, hash_integrita
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, referto.getId());
            pstmt.setString(2, referto.getCodiceFiscalePaziente());
            pstmt.setString(3, referto.getNomePaziente());
            pstmt.setString(4, referto.getCognomePaziente());
            pstmt.setString(5, referto.getDataNascita());
            pstmt.setString(6, referto.getSesso());
            pstmt.setString(7, referto.getTipoReferto());
            pstmt.setString(8, referto.getSpecializzazione());
            pstmt.setString(9, referto.getMedicoRichiedente());
            pstmt.setString(10, referto.getMedicoCompilatore());
            pstmt.setString(11, referto.getStrutturaSanitaria());
            pstmt.setString(12, referto.getTestoReferto());
            pstmt.setString(13, referto.getDiagnosi());
            pstmt.setString(14, referto.getTerapia());
            pstmt.setString(15, referto.getNote());
            pstmt.setTimestamp(16, Timestamp.valueOf(referto.getDataCreazione()));
            pstmt.setString(17, referto.getStato());
            pstmt.setString(18, referto.getPriorita());
            pstmt.setBoolean(19, referto.isFirmato());
            pstmt.setString(20, referto.getFirmaDigitale());
            pstmt.setString(21, referto.getHashIntegrita());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logAudit(referto.getId(), "CREATE", "Sistema", "Referto creato");
                logger.info("Referto salvato con ID: {}", referto.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio del referto", e);
        }
        return false;
    }
    
    /**
     * Aggiorna un referto esistente
     */
    public boolean aggiornaReferto(Referto referto) {
        String sql = """
            UPDATE referti SET
                codice_fiscale_paziente = ?,
                nome_paziente = ?,
                cognome_paziente = ?,
                data_nascita = ?,
                sesso = ?,
                tipo_referto = ?,
                specializzazione = ?,
                medico_richiedente = ?,
                medico_compilatore = ?,
                struttura_sanitaria = ?,
                testo_referto = ?,
                diagnosi = ?,
                terapia = ?,
                note = ?,
                data_modifica = ?,
                stato = ?,
                priorita = ?,
                firmato = ?,
                firma_digitale = ?,
                hash_integrita = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, referto.getCodiceFiscalePaziente());
            pstmt.setString(2, referto.getNomePaziente());
            pstmt.setString(3, referto.getCognomePaziente());
            pstmt.setString(4, referto.getDataNascita());
            pstmt.setString(5, referto.getSesso());
            pstmt.setString(6, referto.getTipoReferto());
            pstmt.setString(7, referto.getSpecializzazione());
            pstmt.setString(8, referto.getMedicoRichiedente());
            pstmt.setString(9, referto.getMedicoCompilatore());
            pstmt.setString(10, referto.getStrutturaSanitaria());
            pstmt.setString(11, referto.getTestoReferto());
            pstmt.setString(12, referto.getDiagnosi());
            pstmt.setString(13, referto.getTerapia());
            pstmt.setString(14, referto.getNote());
            pstmt.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(16, referto.getStato());
            pstmt.setString(17, referto.getPriorita());
            pstmt.setBoolean(18, referto.isFirmato());
            pstmt.setString(19, referto.getFirmaDigitale());
            pstmt.setString(20, referto.getHashIntegrita());
            pstmt.setString(21, referto.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logAudit(referto.getId(), "UPDATE", "Sistema", "Referto aggiornato");
                logger.info("Referto aggiornato con ID: {}", referto.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del referto", e);
        }
        return false;
    }
    
    /**
     * Cerca un referto per ID
     */
    public Referto cercaRefertoPerId(String id) {
        String sql = "SELECT * FROM referti WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReferto(rs);
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca del referto", e);
        }
        return null;
    }
    
    /**
     * Cerca referti per codice fiscale del paziente
     */
    public List<Referto> cercaRefertiPerCodiceFiscale(String codiceFiscale) {
        List<Referto> referti = new ArrayList<>();
        String sql = "SELECT * FROM referti WHERE codice_fiscale_paziente = ? ORDER BY data_creazione DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, codiceFiscale);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                referti.add(mapResultSetToReferto(rs));
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca dei referti", e);
        }
        return referti;
    }
    
    /**
     * Cerca referti per cognome del paziente
     */
    public List<Referto> cercaRefertiPerCognome(String cognome) {
        List<Referto> referti = new ArrayList<>();
        String sql = "SELECT * FROM referti WHERE cognome_paziente LIKE ? ORDER BY data_creazione DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + cognome + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                referti.add(mapResultSetToReferto(rs));
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca dei referti", e);
        }
        return referti;
    }
    
    /**
     * Ottiene tutti i referti con filtraggio opzionale
     */
    public List<Referto> ottieniTuttiReferti(String stato, String dataInizio, String dataFine) {
        List<Referto> referti = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM referti WHERE 1=1");
        
        if (stato != null && !stato.isEmpty()) {
            sql.append(" AND stato = ?");
        }
        if (dataInizio != null && !dataInizio.isEmpty()) {
            sql.append(" AND data_creazione >= ?");
        }
        if (dataFine != null && !dataFine.isEmpty()) {
            sql.append(" AND data_creazione <= ?");
        }
        sql.append(" ORDER BY data_creazione DESC");
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (stato != null && !stato.isEmpty()) {
                pstmt.setString(paramIndex++, stato);
            }
            if (dataInizio != null && !dataInizio.isEmpty()) {
                pstmt.setString(paramIndex++, dataInizio);
            }
            if (dataFine != null && !dataFine.isEmpty()) {
                pstmt.setString(paramIndex++, dataFine);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                referti.add(mapResultSetToReferto(rs));
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei referti", e);
        }
        return referti;
    }
    
    /**
     * Elimina un referto (soft delete - cambia lo stato)
     */
    public boolean eliminaReferto(String id) {
        String sql = "UPDATE referti SET stato = 'ELIMINATO', data_modifica = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logAudit(id, "DELETE", "Sistema", "Referto eliminato");
                logger.info("Referto eliminato con ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del referto", e);
        }
        return false;
    }
    
    /**
     * Registra un'azione di audit
     */
    private void logAudit(String idReferto, String azione, String utente, String dettagli) {
        String sql = "INSERT INTO audit_log (id, id_referto, azione, utente, data_azione, dettagli) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, java.util.UUID.randomUUID().toString());
            pstmt.setString(2, idReferto);
            pstmt.setString(3, azione);
            pstmt.setString(4, utente);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(6, dettagli);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Errore durante la registrazione dell'audit log", e);
        }
    }
    
    /**
     * Mappa un ResultSet su un oggetto Referto
     */
    private Referto mapResultSetToReferto(ResultSet rs) throws SQLException {
        Referto referto = new Referto();
        referto.setId(rs.getString("id"));
        referto.setCodiceFiscalePaziente(rs.getString("codice_fiscale_paziente"));
        referto.setNomePaziente(rs.getString("nome_paziente"));
        referto.setCognomePaziente(rs.getString("cognome_paziente"));
        referto.setDataNascita(rs.getString("data_nascita"));
        referto.setSesso(rs.getString("sesso"));
        referto.setTipoReferto(rs.getString("tipo_referto"));
        referto.setSpecializzazione(rs.getString("specializzazione"));
        referto.setMedicoRichiedente(rs.getString("medico_richiedente"));
        referto.setMedicoCompilatore(rs.getString("medico_compilatore"));
        referto.setStrutturaSanitaria(rs.getString("struttura_sanitaria"));
        referto.setTestoReferto(rs.getString("testo_referto"));
        referto.setDiagnosi(rs.getString("diagnosi"));
        referto.setTerapia(rs.getString("terapia"));
        referto.setNote(rs.getString("note"));
        referto.setDataCreazione(rs.getTimestamp("data_creazione").toLocalDateTime());
        
        Timestamp tsModifica = rs.getTimestamp("data_modifica");
        if (tsModifica != null) {
            referto.setDataModifica(tsModifica.toLocalDateTime());
        }
        
        referto.setStato(rs.getString("stato"));
        referto.setPriorita(rs.getString("priorita"));
        referto.setFirmato(rs.getBoolean("firmato"));
        referto.setFirmaDigitale(rs.getString("firma_digitale"));
        referto.setHashIntegrita(rs.getString("hash_integrita"));
        
        return referto;
    }
    
    /**
     * Chiude la connessione al database
     */
    public void chiudi() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Connessione al database chiusa");
            }
        } catch (SQLException e) {
            logger.error("Errore durante la chiusura della connessione", e);
        }
    }
}
