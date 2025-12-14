# La CAV - Caisse d'allocations virtuelles

## Prérequis

- Java 25 dans le PATH à télécharger avec [Eclipse Adoptium](https://adoptium.net/fr/temurin/releases?version=25&os=any&arch=any)
- Python 3
- Node.js pour le frontend

## Ce qu'il reste à faire

- **Côté service :**
  - Créer de nouvelles aides (pas forcément réaliste, on met ce qu'on veut, plus c'est bidon mieux c'est)
  - Modifier son profil en tant qu'allocataire
    - Attention à bien recalculer toutes les previsions d'allocations !!
- **Côté API client :**
  - Possibilité de choisir quelle préfecture utiliser pour créer un compte
  - Récupérer l'état d'une préfecture (mois actuel)
  - Passer au mois suivant pour une préfecture
  - Modifier le profil d'un allocataire (faire service avant)
- **Côté frontend :**
  - Faire l'unique page du site où on peut :
    - Créer un compte ou se connecter (pas besoin de mdp, juste l'adresse de l'acteur)
    - Voir les prévisions d'allocations
    - Demander/Refuser une aide
    - Voir l'historique de paiement
    - Passer au mois suivant
- **Côté framework :**
  - Rendre `AckRetryer` un poil plus flexible (si un message est abandonné, pouvoir faire qqch)
  - Pouvoir faire des requêtes sans réessayer l'envoi de message (au cas où le serveur n'existe juste pas)

## Pour lancer le projet

### Lancer les services
Windows : `python ./run.py discovery service`

Linux : `./run.py discovery service`

Si vous voulez lancer le service mais qu'il ne contienne :
- que des acteurs préfectures : `--prefecture-only` ou `-p`
- que des acteurs calculateurs : `--calculators-only` ou `-c`

### Lancer le client (API REST)

Windows : `python ./run.py client`

Linux : `./run.py client`

Le client tourne sur le port 4444.

### Lancer le frontend

Se mettre dans le dossier `client-front` puis faire `npm install` ensuite `npm run dev`

## Documentation de l'API REST

### Créer un compte allocataire

`POST /api/accounts`

**Entrée**

```json
{
  "firstName": "Jean",
  "lastName": "Dupont",
  "birthDate": "1990-01-01",
  "email": "jean.dupont@example.com",
  "phoneNumber": "+33 6 12 34 56 78",
  "address": "10 rue de la Paix, 75002 Paris",
  "inCouple": true,
  "numberOfDependents": 2,
  "monthlyIncome": 1450.75,
  "iban": "FR7612345987650123456789014"
}
```

**Sortie 201 Created** : Le compte a été créé

```json
{
  "beneficiaryAddress": "0000000000000001:0000000000000002"
}
```

---

### Récupérer un compte (profil et prévisions d'allocations)

`GET /api/accounts/{addr}`

Où `addr` est une adresse d'acteur au format hexadécimal `SSSSSSSSSSSSSSSS:NNNNNNNNNNNNNNNN`.

**Entrée**

Sans corps (seulement le paramètre de chemin `addr`).

**Sortie 200 OK**

```json
{
  "profile": {
    "beneficiaryNumber": "BNF-2025-0001",
    "firstName": "Jean",
    "lastName": "Dupont",
    "birthDate": "1990-01-01",
    "email": "jean.dupont@example.com",
    "phoneNumber": "+33 6 12 34 56 78",
    "address": "10 rue de la Paix, 75002 Paris",
    "inCouple": true,
    "numberOfDependents": 2,
    "monthlyIncome": 1450.75,
    "iban": "FR7612345987650123456789014",
    "registrationDate": "2025-12-01"
  },
  "allowancePrevisions": {
    "RSA": {
      "type": "RSA",
      "state": "UP_TO_DATE",
      "lastAmount": 621.25,
      "lastMessage": "Dernier calcul effectué le 2025-12-01"
    }
  }
}
```

**Sortie 404 Not Found** : Aucun bénéficiaire trouvé pour l'adresse fournie

---

### Demander le calcul d'une allocation pour un bénéficiaire

`POST /api/accounts/{addr}/requests/{type}`

Où:
- `addr` est l'adresse d'acteur du bénéficiaire
- `type` est le type d'allocation (ex: `RSA`)

**Entrée**

Sans corps (les paramètres sont dans l'URL).

**Sortie 200 OK** : La demande a été prise en compte

```json
{
  "message": "Demande de calcul RSA acceptée"
}
```

**Sortie 409 Conflict** : La demande ne peut pas être traitée (ex: état incompatible)

```json
{
  "message": "Le calcul RSA est déjà en cours"
}
```

**Sortie 404 Not Found** : Bénéficiaire introuvable pour l'adresse fournie

---

### Lister les serveurs découverts

`GET /api/servers`

**Entrée**

Sans corps.

**Sortie 200 OK**

```json
[
  {
    "id": "8b1a9953c4611296a827abf8c47804d7",
    "name": "cav-service",
    "url": "http://localhost:8081",
    "metadata": {
      "version": "1.0.0"
    }
  }
]
```

---

### Obtenir les informations du serveur local

`GET /api/servers/local`

**Entrée**

Sans corps.

**Sortie 200 OK**

```json
{
  "id": "1f3870be274f6c49b3e31a0c6728957f",
  "name": "cav-client",
  "url": "http://localhost:8080",
  "metadata": {
    "env": "dev"
  }
}
```
