# Travelling Analyst Problem (TAP) Solver 🌐

Ce projet contient plusieurs heuristiques en Java pour résoudre le **Travelling Analyst Problem (TAP)**, une variante combinant des éléments du problème du sac à dos (KP) et du voyageur de commerce (TSP). L'objectif est de sélectionner une séquence de requêtes SQL maximisant l’**intérêt** total sous des contraintes de **temps d'exécution** et de **distance** entre requêtes.

---

## 🎓 Contexte du problème

- Chaque requête est représentée comme un **nœud** dans un graphe complet.
- Chaque nœud est caractérisé par :
  - un **intérêt** (pertinence de l'information)
  - un **coût** (temps pour l’exécuter)
- Chaque arête possède une **distance** (écart sémantique ou structurel entre requêtes)

🌟 Objectif : **maximiser la somme des intérêts**, tout en respectant les contraintes de temps et de distance.

---

## 🚀 Lancer le projet

### Compilation

```bash
mvn package
```

### Exécution

```bash
java -cp target/TP-RO-M1-BDMA-1.0-SNAPSHOT.jar com.alexscode.teaching.Main
```

Dans `Main.java`, change l'heuristique utilisée selon ton choix :

```java
TAPSolver solver = new GreedyLocalSearchMultiStartSolverTabu();
```

---

## ♟️ Heuristiques implémentées

### `GreedyLocalSearchSolver`
- **Principe** : construction gloutonne + amélioration locale par remplacement.
- 🔎 Sélectionne la requête suivante avec `intérêt / (coût + distance)`
- 📈 Remplace les requêtes pour augmenter l’intérêt total.

---

### `GreedyLocalSearchMultiStartSolver`
- **Principe** : exécute `GreedyLocalSearchSolver` depuis **plusieurs points de départ**.
- 🧠 Garde la meilleure solution après plusieurs démarrages.
- 📊 Recommandé pour les instances de taille moyenne.

---

### `GreedyLocalSearchMultiStartSolverSwap`
- **Principe** : même base que ci-dessus, mais avec :
  - **remplacement**
  - **échange (swap)** de deux requêtes
- 🔄 Permet d'explorer plus finement l'espace des solutions.

---

### `GreedyLocalSearchMultiStartSolverTabu`
- **Principe** : ajoute une **Tabu List** pour éviter les cycles
- ⛔️ Interdit les mouvements récents pendant un certain nombre d’itérations
- 🧠 Mémorise la meilleure solution rencontrée
- 🛝 Permet de sortir des optima locaux

---

## 📊 Évaluation

Chaque solution est évaluée par :
- ✅ Son **intérêt total**
- ✅ Le respect des **contraintes** (temps & distance)
- 📉 Le **gap relatif** à l’optimum connu :

```text
Gap = 100 × (optimal - intérêt_obtenu) / optimal
```

---





---

## 🙌 Auteurs

- Ammour Mhena  
- Bouzelat Tarek
- Based on Alexandre Chanson's code 

Projet académique - M1 DS4CS - OR Hackathon
