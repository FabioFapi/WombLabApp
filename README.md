# 🩺 Womblab App

App Android ufficiale di **Womblab**, realizzata con **Jetpack Compose**, **Firebase** e **REST API** per la visualizzazione di eventi formativi rivolti a professionisti sanitari.

## 📱 Funzionalità

- 🔐 **Login con Google** tramite Firebase Authentication
- 📆 **Visualizzazione eventi** da WordPress (plugin The Events Calendar via REST API)
- 📄 **Dettaglio evento** completo, come da sito ufficiale
- ⭐ **Salvataggio eventi preferiti** tramite DataStore
- 🔔 **Notifiche push** con Firebase Cloud Messaging (in sviluppo)
- 🧭 **Navigazione bottom bar** tra:
  - Home (lista eventi)
  - Calendario
  - Profilo

---

## 🧱 Tech Stack

- **Jetpack Compose**
- **Firebase (Auth + FCM)**
- **Kotlin + MVVM**
- **Ktor Client** per chiamate HTTP
- **DataStore** per preferenze locali
- **Navigation Compose**
- **Material 3**

---

## 🚀 Setup del progetto

1. Clona la repo:

```bash
git clone https://github.com/tuo-username/womblab.git
