# Framework Playwright - Automation de Tests

Un framework de test complet basé sur **Playwright**, **Cucumber** et **JUnit5** pour automatiser les tests UI, API et Web Services avec des rapports détaillés via **Allure**.

---

## 📋 Table des matières

- [Technologies](#technologies)
- [Prérequis](#prérequis)
- [Installation & Configuration](#installation--configuration)
- [Exécution des tests en local](#exécution-des-tests-en-local)
- [Configuration Jenkins](#configuration-jenkins)
- [Structure du projet](#structure-du-projet)
- [Rapports Allure](#rapports-allure)

---

## 🛠️ Technologies

| Technologie | Version | Description |
|------------|---------|-------------|
| Java | 17 | Langage de programmation |
| Maven | 3.8+ | Gestionnaire de build |
| Playwright | 1.41.2 | Framework d'automatisation browser |
| Cucumber | 7.14.0 | BDD - Scénarios en Gherkin |
| JUnit5 | 5.10.2 | Framework de test |
| Allure | 2.25.0 | Rapports de test |
| REST Assured | 5.4.0 | Tests d'API |
| Logback | 1.4.14 | Logging |

---

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### 1. **Java Development Kit (JDK) 17+**
```bash
java -version
# Vous devez voir : openjdk version "17" ou supérieur
```
**Télécharger :** https://www.oracle.com/java/technologies/downloads/#java17

### 2. **Apache Maven 3.8+**
```bash
mvn -version
# Vous devez voir : Apache Maven 3.8.0 ou supérieur
```
**Télécharger :** https://maven.apache.org/download.cgi

### 3. **Git**
```bash
git --version
```
**Télécharger :** https://git-scm.com/

### 4. **IntelliJ IDEA (Recommended)**
**Télécharger :** https://www.jetbrains.com/idea/

### 5. **Allure CLI (optionnel pour les rapports locaux)**
```bash
# Windows (PowerShell)
choco install allure

# macOS
brew install allure

# Linux
sudo apt-get install allure
```

---

## 🚀 Installation & Configuration

### Étape 1 : Cloner le projet

```bash
# Ouvrir PowerShell ou Git Bash
git clone https://github.com/votre-organisation/Framework-Playwright.git

# Accéder au dossier du projet
cd Framework-Playwright
```

### Étape 2 : Configurer les variables d'environnement

Créer un fichier `.env` à la racine du projet :

```env
# AI API Keys (optionnel pour les IA)
OPENAI_API_KEY=
GEMINI_API_KEY=
CLAUDE_API_KEY=

# AI Models (optionnel)
OPENAI_MODEL=
GEMINI_MODEL=gemini-2.5-flash
CLAUDE_MODEL=
```

### Étape 3 : Ouvrir dans IntelliJ IDEA

#### **Option A : Depuis IntelliJ**
1. Lancer IntelliJ IDEA
2. Cliquer sur **File** → **Open**
3. Accéder au dossier du projet et sélectionner le fichier `pom.xml`
4. Cliquer sur **Open as Project**
5. IntelliJ va télécharger automatiquement toutes les dépendances Maven
6. Attendre la synchronisation complète (bas de l'écran)

#### **Option B : Depuis le terminal**
```bash
# Depuis le dossier du projet
idea .
```

### Étape 4 : Configurer Java dans IntelliJ

1. Aller à **File** → **Project Structure** → **Project**
2. S'assurer que **SDK** est défini à **Java 17**
3. S'assurer que **Language level** est défini à **17**
4. Cliquer sur **Apply** → **OK**

### Étape 5 : Télécharger les dépendances Maven

```bash
# Faire un refresh du projet
mvn clean install -DskipTests

# Ou directement depuis IntelliJ
# View → Tool Windows → Maven → Reload Projects
```

---

## 🧪 Exécution des tests en local

### **Commande générale**

```bash
mvn clean test
```

### **Exécuter avec des options spécifiques**

#### 1. Exécuter les tests avec un browser spécifique
```bash
mvn clean test -Dbrowser.name=chromium
# Options : chromium, firefox, webkit
```

#### 2. Exécuter les tests avec des tags Cucumber
```bash
# Exécuter les tests avec le tag @ui
mvn clean test -Dcucumber.filter.tags="@ui"

# Exécuter les tests avec @ui ET @hybrid
mvn clean test -Dcucumber.filter.tags="(@ui or @hybrid) and not @ignore"
```

#### 3. Exécuter avec les options de log
```bash
mvn clean test \
  -Dbrowser.name=chromium \
  -Dframework.log.level=DEBUG \
  -Dlibrary.log.level=WARN \
  -Dcucumber.filter.tags="@ui"
```

#### 4. Exécuter depuis IntelliJ IDEA
1. Aller dans le menu **Run** → **Edit Configurations**
2. Créer une nouvelle configuration Maven
3. Dans **Command line**, entrer : `clean test -Dbrowser.name=chromium -Dcucumber.filter.tags="@ui"`
4. Cliquer sur **Run**

### **Options courantes**

| Option | Exemple | Description |
|--------|---------|-------------|
| Browser | `-Dbrowser.name=chromium` | Choisir le browser (chromium, firefox, webkit) |
| Tags | `-Dcucumber.filter.tags="@ui"` | Filtrer par tags Cucumber |
| Log Level | `-Dframework.log.level=DEBUG` | Niveau de log (INFO, DEBUG, WARN, ERROR) |
| Retry | `-Dtest=NomTest` | Exécuter un test spécifique |

---

## 📊 Rapports Allure

### Générer et visualiser les rapports localement

```bash
# Après l'exécution des tests
allure serve target/allure-results

# Cela ouvrira automatiquement le rapport dans le navigateur
```

### Générer un rapport statique

```bash
# Générer le rapport dans le dossier allure-report
allure generate target/allure-results --clean -o allure-report

# Ouvrir le rapport (Windows)
start allure-report/index.html

# Ouvrir le rapport (macOS)
open allure-report/index.html

# Ouvrir le rapport (Linux)
xdg-open allure-report/index.html
```

---

## 🔧 Utilisation de Jenkins

### **Lancer une build Jenkins**

1. Aller au **Dashboard Jenkins**
2. Cliquer sur **"Build with Parameters"**
3. **Modifier les 3 variables obligatoires** ⬇️
4. Cliquer sur **"Build"**

---

### **📝 Variables à modifier à chaque build**

#### **1️⃣ EMAIL_RECIPIENTS 📧**
```
Emails des destinataires du rapport (séparés par virgule)

✅ Exemple :  admin@company.com
✅ Exemple :  qa-lead@company.com, devops@company.com, test@company.com
```

#### **2️⃣ TAGS 🏷️**
```
Type de tests à exécuter

✅ @ui       → Tests UI uniquement
✅ @api      → Tests API uniquement
✅ @hybrid   → Tests UI + API
✅ @ws       → Tests WebServices

Les tests marqués @ignore sont toujours exclus.
```

#### **3️⃣ FRAMEWORK_LOG_LEVEL 📊**
```
Niveau de verbosité des logs

✅ INFO      → Logs standard (recommandé par défaut)
✅ DEBUG     → Logs détaillés (pour dépannage)
✅ WARN      → Seulement les avertissements
✅ ERROR     → Seulement les erreurs
```

---

### **🚀 Exemples d'utilisation**

#### **Exemple 1 : Tests UI en DEBUG**
```
EMAIL_RECIPIENTS  →  test@company.com
TAGS              →  @ui
FRAMEWORK_LOG_LEVEL → DEBUG
```
→ Envoie le rapport à test@company.com avec logs détaillés

#### **Exemple 2 : Tests Hybrid en INFO**
```
EMAIL_RECIPIENTS  →  qa-lead@company.com, devops@company.com
TAGS              →  @hybrid
FRAMEWORK_LOG_LEVEL → INFO
```
→ Envoie le rapport à 2 destinataires avec logs standards

#### **Exemple 3 : Tests API minimaux**
```
EMAIL_RECIPIENTS  →  admin@company.com
TAGS              →  @api
FRAMEWORK_LOG_LEVEL → WARN
```
→ Seulement les avertissements, sans logs détaillés

---

## 📁 Structure du projet

```
Framework-Playwright/
├── jenkins/
│   └── Jenkinsfile                  # Pipeline Jenkins
├── src/
│   ├── main/
│   │   ├── java/                    # Code principal
│   │   └── resources/               # Ressources (config, properties)
│   └── test/
│       ├── java/                    # Tests et steps Cucumber
│       └── resources/               # Feature files Gherkin
├── pom.xml                          # Dépendances Maven
└── .env                             # Variables d'environnement
```

---

## 🔍 Utilisation courante

### **Ajouter un nouveau test**

1. Créer un fichier `.feature` dans `src/test/resources/features/`
2. Écrire les scénarios en Gherkin
3. Implémenter les step definitions en Java
4. Exécuter : `mvn clean test -Dcucumber.filter.tags="@votre-tag"`

### **Debuguer un test dans IntelliJ**

1. Placer un breakpoint dans le code
2. Cliquer sur **Run** → **Debug '...'**
3. Utiliser les outils de debug IntelliJ

### **Réexécuter à partir d'une étape précédente**

Modifiez le fichier feature ou utilisez Cucumber tags pour relancer des test spécifiques.

---

## 🐛 Dépannage

### **Problème : "Java version incompatible"**
```bash
# Vérifier la version Java
java -version

# Mettre à jour le PATH pour pointer vers Java 17
# Windows: Ajouter C:\Program Files\Java\jdk-17.x.x\bin au PATH
```

### **Problème : "Maven command not found"**
```bash
# Ajouter Maven au PATH
# Windows: Ajouter C:\Apache\maven\bin au PATH
```

### **Problème : Playwright browser not installed**
```bash
# Installer les navigateurs Playwright
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

### **Problème : Allure reports not generated**
```bash
# Installer Allure CLI
# Puis exécuter
allure serve target/allure-results
```

---



**Bon testing ! 🚀**



