# La CAV - Caisse d'allocations virtuelles

## Prérequis

- Java 25 dans le PATH à télécharger avec [Eclipse Adoptium](https://adoptium.net/fr/temurin/releases?version=25&os=any&arch=any)
- Python 3
- Node.js pour le frontend

## Pour lancer le projet

### Lancer les services
Windows : `python ./run.py discovery service`

Linux : `./run.py discovery service`

Si vous voulez lancer le service mais qu'il ne contienne :
- que des acteurs préfectures : `--prefecture-only` ou `-p`
- que des acteurs calculateurs : `--calculators-only` ou `-c`

Si vous voulez activer les exemples, créez un fichier `application.local.yml` puis ajoutez les paramètres suivants :
```yaml
cav:
  example1: true
  example2: true
  example3: true
```

Bien sûr, vous pouvez mettre certains à false pour ne pas les lancer.

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
---

### Lister les préfectures disponibles

Permet de découvrir les serveurs du réseau qui agissent en tant que Préfectures (filtrés via métadonnées).

`GET /api/prefectures`

**Entrée**

Sans corps.

**Sortie 200 OK**

```json
[
  {
    "id": 4510756809869306294,
    "name": "Préfecture cav-service (3e996d6b686f29b6)"
  },
  {
    "id": -2003999959543938588,
    "name": "Préfecture cav-service (e430...)"
  }
]
```
---

### Récupérer l'état d'une préfecture
Permet de vérifier le statut d'une préfecture spécifique et de connaître son mois courant (temps virtuel).

`GET /api/prefectures/{id}/state`

Où `{id}` est l'identifiant du serveur préfecture (récupéré via le listing).

**Entrée**

Sans corps.

**Sortie 200 OK**

```json
{
  "status": "ALIVE",
  "currentMonth": "2025-12-01"
}
```
---

### Passer au mois suivant

Déclenche le passage au mois suivant pour une préfecture donnée (et déclenche le paiement des allocations).

`POST /api/prefectures/{id}/next-month`

Où `{id}` est l'identifiant du serveur préfecture.

**Entrée**

Sans corps.

**Sortie 200 OK**

Retourne la nouvelle date après passage du mois.

```json
{
  "currentMonth": "2026-01-01"
}
```

---

### Créer un compte dans une préfecture spécifique

Permet de créer un allocataire sur un serveur précis.

`POST /api/prefectures/{id}/accounts`

Où `{id}` est l'identifiant du serveur préfecture cible.

**Entrée**

```json
{
  "firstName": "Thomas",
  "lastName": "Anderson",
  "birthDate": "1999-03-31",
  "email": "neo@matrix.com",
  "phoneNumber": "0600000000",
  "address": "101 Room",
  "inCouple": false,
  "numberOfDependents": 0,
  "monthlyIncome": 2000,
  "iban": "FR76 1234 5678 9012"
}
```

**Sortie 200 OK**

L'adresse retournée (`beneficiaryAddress`) contient l'ID hexadécimal du serveur ciblé en préfixe, confirmant la localisation de l'acteur.

```json
{
  "type": "cy.cav.protocol.accounts.CreateAccountResponse",
  "beneficiaryId": "122ee289-a795-4695-814a-3ac251dd4237",
  "beneficiaryAddress": "3e996d6b686f29b6:00000000000103fd"
}
```
