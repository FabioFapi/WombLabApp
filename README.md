# 🔬 WombLab

**La tua app per eventi formativi professionali nel settore sanitario**

## 📱 Panoramica

WombLab è un'applicazione Android moderna e intuitiva progettata per professionisti sanitari che desiderano rimanere aggiornati sui migliori eventi formativi del settore. Con un'interfaccia elegante e funzionalità avanzate, WombLab rende semplice scoprire, salvare e partecipare a congressi, corsi ECM e workshop medici.

### ✨ Caratteristiche Principali

- 🎯 **Eventi Personalizzati** - Scopri eventi formativi mirati alla tua specializzazione
- ⭐ **Sistema Preferiti** - Salva e organizza i tuoi eventi di interesse
- 📅 **Calendario Integrato** - Visualizza eventi per data con interfaccia intuitiva
- 🔍 **Ricerca Avanzata** - Trova rapidamente eventi per categoria, luogo o parole chiave
- 🔐 **Autenticazione Sicura** - Login con Google o email/password
- 📱 **UI Moderna** - Design Material 3 con animazioni fluide
- 🌐 **Modalità Offline** - Accesso ai contenuti anche senza connessione

---

## 🏗️ Architettura

Il progetto segue i principi della **Clean Architecture** con pattern **MVVM**, garantendo:

- 🔄 **Separazione delle responsabilità**
- 🧪 **Testabilità elevata**
- 📈 **Scalabilità e manutenibilità**
- 🎯 **Single Source of Truth**

```
app/
├── presentation/          # UI Layer (Jetpack Compose)
│   ├── components/       # Componenti riutilizzabili
│   ├── navigation/       # Navigazione e routing
│   ├── home/            # Schermata principale
│   ├── calendar/        # Calendario eventi
│   ├── detail/          # Dettaglio evento
│   ├── auth/            # Autenticazione
│   └── profile/         # Profilo utente
├── domain/               # Business Logic Layer
│   ├── model/           # Modelli di dominio
│   ├── repository/      # Interfacce repository
│   └── usecase/         # Casi d'uso
└── data/                # Data Layer
    ├── local/           # Database locale (Room)
    ├── remote/          # API (Retrofit)
    └── repository/      # Implementazioni repository
```

---

## 🛠️ Stack Tecnologico

### **Core**
- **Kotlin** - Linguaggio principale
- **Jetpack Compose** - UI toolkit moderna
- **Coroutines & Flow** - Programmazione asincrona
- **Hilt** - Dependency Injection

### **Data & Storage**
- **Room** - Database locale
- **Retrofit** - Networking
- **Gson** - Serializzazione JSON
- **SharedPreferences** - Configurazioni utente

### **Authentication & Firebase**
- **Firebase Auth** - Autenticazione utenti
- **Google Sign-In** - Login con Google

### **UI & UX**
- **Material Design 3** - Design system
- **Coil** - Image loading ottimizzato
- **Lottie** - Animazioni avanzate
- **Navigation Compose** - Navigazione

---

## 🚀 Quick Start

### Prerequisiti

- **Android Studio** Hedgehog | 2023.1.1 o successivo
- **JDK 17** o superiore
- **Android SDK** API 24+ (Android 7.0)
- **Git**

### Installazione

1. **Clona il repository**
```bash
git clone https://github.com/FabioFapi/WombLabApp
cd womblab
```

2. **Configura Firebase**
   - Crea un progetto su [Firebase Console](https://console.firebase.google.com)
   - Scarica `google-services.json` e inseriscilo in `app/`
   - Abilita Authentication con Google e Email/Password

3. **Configura le API Keys**
   
   Crea il file `local.properties` nella root del progetto:
   ```properties
   # API Configuration
   WOMBLAB_BASE_URL="https://www.womblab.com/"
   GOOGLE_CLIENT_ID="your_google_client_id"
   
   # Debug Configuration
   DEBUG_MODE=true
   ENABLE_LOGGING=true
   ```

4. **Build e Run**
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### 🧪 Testing

```bash
# Unit Tests
./gradlew test

# Instrumented Tests
./gradlew connectedAndroidTest

# Test Coverage
./gradlew jacocoTestReport
```

---

## 📋 Funzionalità Dettagliate

### 🏠 **Home Screen**
- Lista eventi in evidenza
- Eventi salvati dall'utente
- Prossimi eventi con infinite scroll
- Pull-to-refresh per aggiornamenti

### 📅 **Calendario**
- Vista mensile con eventi evidenziati
- Navigazione fluida tra mesi
- Dettaglio eventi per data selezionata
- Indicatori visivi per giorni con eventi

### 🔍 **Ricerca e Filtri**
- Ricerca real-time per titolo/descrizione
- Filtri per categoria medica
- Filtri per data e località
- Cronologia ricerche

### 👤 **Profilo Utente**
- Informazioni professionali
- Statistiche eventi salvati
- Preferenze notifiche
- Logout sicuro

### 📱 **Dettaglio Evento**
- Informazioni complete evento
- Parsing intelligente descrizioni
- Link iscrizione diretta
- Condivisione sociale
- Toggle preferiti

---

## 🎨 Design System

### **Colori Principali**
```kotlin
val WombLabPrimary = Color(0xFF006B5B)      // Verde medicale
val WombLabSecondary = Color(0xFF018786)    // Verde acqua
val WombLabAccent = Color(0xFF03DAC6)       // Accento brillante
```

### **Typography**
- **Roboto** per testi principali
- **Peso variabile** per gerarchia visiva
- **Line height ottimizzato** per leggibilità

### **Componenti Riutilizzabili**
- `EventCard` - Card evento con animazioni
- `WombLabTopBar` - Top bar con ricerca
- `LoadingIndicator` - Indicatori di caricamento
- `ErrorMessage` - Gestione errori elegante

---

## 🔧 Configurazione Avanzata

### **Build Variants**

```gradle
buildTypes {
    debug {
        applicationIdSuffix ".debug"
        debuggable true
        minifyEnabled false
    }
    
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        signingConfig signingConfigs.release
    }
    
    staging {
        initWith debug
        applicationIdSuffix ".staging"
        debuggable false
    }
}
```

### **Performance Optimization**

- **Image Loading**: Cache multi-livello con Coil
- **Database**: Indici ottimizzati su query frequenti
- **Memory**: Gestione lifecycle-aware dei componenti
- **Network**: Retry policy e timeout configurabili

---

## 📊 Metriche e Monitoring

### **Code Quality**
- **Ktlint** per code style
- **Detekt** per static analysis
- **Jacoco** per test coverage target 80%

### **Performance**
- **LeakCanary** per memory leak detection
- **Firebase Performance** per monitoring prod
- **Crashlytics** per crash reporting

---

## 🤝 Contributing

Contribuzioni sono benvenute! Per contribuire:

1. **Fork** il repository
2. **Crea** un branch feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** le modifiche (`git commit -m 'Add AmazingFeature'`)
4. **Push** al branch (`git push origin feature/AmazingFeature`)
5. **Apri** una Pull Request

### **Coding Standards**

- Segui le [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Scrivi test per nuove funzionalità
- Aggiorna la documentazione se necessario
- Usa commit messages descrittivi

---

## 📄 API Reference

### **WordPress Events API**

```http
GET /wp-json/tribe/events/v1/events
```

**Parameters:**
- `page`: Numero pagina (default: 1)
- `per_page`: Eventi per pagina (default: 15)
- `search`: Query di ricerca
- `categories`: Filtro categorie
- `featured`: Solo eventi in evidenza

**Response:**
```json
{
  "events": [...],
  "total": 150,
  "total_pages": 10,
  "rest_url": "..."
}
```

---

## 🐛 Troubleshooting

### **Problemi Comuni**

**Build Failed: google-services.json missing**
```bash
Soluzione: Assicurati di aver scaricato google-services.json da Firebase Console
```

**Network Error in Debug**
```bash
Soluzione: Verifica la configurazione NETWORK_SECURITY_CONFIG in AndroidManifest.xml
```

**Room Migration Failed**
```bash
Soluzione: Incrementa DATABASE_VERSION e aggiungi migration appropriata
```

---

## 📱 Screenshots

<table>
  <tr>
    <td><img src="screenshots/home.png" width="200" alt="Home"/></td>
    <td><img src="screenshots/calendar.png" width="200" alt="Calendario"/></td>
    <td><img src="screenshots/detail.png" width="200" alt="Dettaglio"/></td>
    <td><img src="screenshots/profile.png" width="200" alt="Profilo"/></td>
  </tr>
  <tr>
    <td align="center">Home</td>
    <td align="center">Calendario</td>
    <td align="center">Dettaglio Evento</td>
    <td align="center">Profilo</td>
  </tr>
</table>

---

## 🏆 Roadmap

### **v2.0 - Q2 2024**
- [ ] 🔔 Notifiche push per eventi
- [ ] 📊 Analytics avanzate
- [ ] 🌙 Dark mode completo
- [ ] 🗺️ Mappa integrata eventi

### **v2.1 - Q3 2024**
- [ ] 👥 Condivisione profili professionali
- [ ] 📈 Dashboard statistiche personali
- [ ] 🎓 Certificazioni ECM integrate
- [ ] 🔄 Sincronizzazione calendario dispositivo

---

## 📞 Supporto

- 📧 **Email**: recchiappmobile@gmail.com
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/FabioFapi/WombLabApp/issues)
- 💬 **Discussioni**: [GitHub Discussions](https://github.com/FabioFapi/WombLabApp/discussions)
- 📖 **Documentation**: [Wiki](https://github.com/FabioFapi/WombLabApp/wiki)

---

## 📄 Licenza

Questo progetto è licenziato sotto la **MIT License** - vedi il file [LICENSE](LICENSE) per dettagli.

---

## 👨‍💻 Autore

**Il Tuo Nome**
- GitHub: [FabioFapi](https://github.com/FabioFapi)
- LinkedIn: [Fabio Recchia](https://www.linkedin.com/in/fabio-recchia-435633339/)
- Email: recchiappmobile@gmail.com

---

## 🙏 Ringraziamenti

- **WombLab Team** per l'API e il supporto
- **Android Community** per le librerie open source
- **Material Design Team** per le guidelines UI/UX
- **JetBrains** per l'eccellente IDE

---

<div align="center">

**⭐ Se questo progetto ti è utile, lascia una stella! ⭐**

*Made with ❤️ for healthcare professionals*

</div>
