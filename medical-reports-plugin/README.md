# Medical Reports LibreOffice Plugin

Plugin avanzato per LibreOffice per la creazione e archiviazione di referti medici per studi medici.

## 🏥 Caratteristiche Principali

### Funzionalità Core
- **Creazione Referti**: Interfaccia intuitiva per creare referti medici completi
- **Archiviazione Sicura**: Database locale H2 crittografato per l'archiviazione dei dati
- **Ricerca Avanzata**: Ricerca per cognome, codice fiscale, stato, data e specializzazione
- **Gestione Stati**: Workflow con stati (BOZZA, COMPLETATO, FIRMATO, ARCHIVIATO)
- **Priorità**: Gestione priorità (NORMALE, URGENTE, PRIORITARIO)

### Sicurezza e Privacy
- **Hash di Integrità**: SHA-256 per verificare che i referti non siano stati alterati
- **Crittografia AES-GCM**: Per la protezione dei dati sensibili
- **Offuscamento Dati**: Visualizzazione parziale di codici fiscali e nomi per la privacy
- **Audit Log**: Tracciamento completo di tutte le operazioni effettuate
- **GDPR Compliant**: Progettato per rispettare la normativa sulla privacy

### Feature Avanzate
- **Template Referti**: Modelli predefiniti per diverse specializzazioni mediche
- **Esportazione PDF**: Generazione automatica di referti in formato PDF professionale
- **Multi-medico**: Supporto per più medici compilatori e richiedenti
- **Timestamping**: Data e ora precise di creazione e modifica
- **Firma Digitale**: Preparazione per integrazione con sistemi di firma digitale

## 📋 Struttura del Progetto

```
medical-reports-plugin/
├── pom.xml                          # Configurazione Maven
├── src/main/java/com/medicalreports/
│   ├── MedicalReportsPlugin.java    # Classe principale
│   ├── model/
│   │   └── Referto.java             # Modello dati referto
│   ├── gui/
│   │   ├── MainGUI.java             # Interfaccia principale
│   │   └── RefertoForm.java         # Form creazione/modifica
│   ├── database/
│   │   └── DatabaseManager.java     # Gestione database H2
│   ├── service/
│   │   └── PDFExportService.java    # Esportazione PDF
│   └── util/
│       └── SecurityUtil.java        # Utility sicurezza
└── src/main/resources/
    └── logback.xml                  # Configurazione logging
```

## 🛠️ Requisiti

- Java Development Kit (JDK) 11 o superiore
- Apache Maven 3.6+
- LibreOffice 7.x (per integrazione completa)

## 🚀 Installazione e Build

### Compilazione
```bash
cd medical-reports-plugin
mvn clean package
```

### Esecuzione Standalone
```bash
java -jar target/medical-reports-plugin-1.0.0.jar
```

### Integrazione con LibreOffice

1. Copiare il JAR generato nella cartella delle estensioni di LibreOffice:
   - Linux: `~/.config/libreoffice/4/user/extensions/`
   - Windows: `%APPDATA%\LibreOffice\4\user\extensions\`
   - macOS: `~/Library/Application Support/LibreOffice/4/user/extensions/`

2. Riavviare LibreOffice

3. Abilitare l'estensione dal menu: `Strumenti > Gestore Estensioni`

## 💾 Database

Il plugin utilizza H2 Database embedded con le seguenti tabelle:

### Tabella REFERTI
- id (UUID)
- Dati paziente (nome, cognome, CF, data nascita, sesso)
- Informazioni referto (tipo, specializzazione, medici)
- Contenuto (testo, diagnosi, terapia, note)
- Metadati (stato, priorità, timestamp)
- Sicurezza (hash integrità, firma digitale)

### Tabella TEMPLATE_REFERTI
- Modelli predefiniti per diverse specializzazioni

### Tabella AUDIT_LOG
- Tracciamento di tutte le operazioni CRUD

## 🔐 Sicurezza

### Crittografia
- **AES-GCM 256-bit** per la cifratura dei dati sensibili
- **SHA-256** per gli hash di integrità
- Password del database configurabile

### Privacy
- Offuscamento automatico dei dati sensibili nelle visualizzazioni
- Logging senza dati personali
- Conformità GDPR

## 📊 Utilizzo

### Creare un Nuovo Referto
1. Cliccare su "Nuovo Referto"
2. Compilare i dati del paziente
3. Inserire le informazioni cliniche
4. Generare l'hash di integrità
5. Salvare nel database

### Cercare un Referto
1. Utilizzare i filtri di ricerca (cognome, CF, stato)
2. Cliccare su "Cerca"
3. Selezionare il referto dalla tabella risultati

### Esportare in PDF
1. Selezionare un referto dalla lista
2. Cliccare su "Esporta PDF"
3. Scegliere il percorso di salvataggio

## 📝 Template Disponibili

Il plugin include template per:
- Referti di laboratorio analisi
- Referti radiologici
- Referti specialistici
- Certificati medici
- Visite generali

## 🔧 Configurazione

Modificare il file `src/main/resources/application.properties` per personalizzare:
- Percorso del database
- Credenziali di accesso
- Impostazioni di esportazione PDF
- Template predefiniti

## 📄 Licenza

Questo progetto è distribuito sotto licenza MIT. Vedere il file LICENSE per dettagli.

## ⚠️ Disclaimer

Questo software è destinato all'uso da parte di personale medico autorizzato. 
L'utilizzo deve essere conforme alle normative locali sulla privacy e la gestione 
dei dati sanitari (GDPR in Europa, HIPAA negli USA, ecc.).

## 🤝 Contributi

I contributi sono benvenuti! Per favore:
1. Forkare il repository
2. Creare un branch per la feature
3. Effettuare le modifiche
4. Inviare una pull request

## 📞 Supporto

Per supporto tecnico o segnalazione di bug, aprire una issue sul repository GitHub.

---

**Versione**: 1.0.0  
**Ultimo Aggiornamento**: 2024  
**Sviluppato per**: LibreOffice 7.x
