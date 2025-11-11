# 🚀 Guide de Démarrage - Client Front-End

## Prérequis
- Node.js installé
- Le client Spring Boot démarré (port 4444)
- Au moins un service démarré pour voir les serveurs distants

## Installation des dépendances

Dans le dossier `client-front/frontend` :

```bash
npm install
```

## Lancer le front-end en développement

```bash
npm run dev
```

Le front sera accessible sur : http://localhost:5173

## Technologies utilisées

- **React** - Framework UI
- **Vite** - Build tool et dev server
- **Tailwind CSS** - Framework CSS utility-first

## Ce qui a été ajouté

### Backend (Spring)
- **ActorController.java** : Nouveau contrôleur REST dans le module `client`
  - `GET /api/servers` : Liste tous les serveurs du réseau
  - `GET /api/servers/local` : Informations sur le serveur local

### Frontend (React + Tailwind)
- **App.jsx** : Composant principal avec design moderne
- **components/ActorList.jsx** : Composant qui affiche tous les serveurs
- **Configuration Tailwind** : `tailwind.config.js` et `postcss.config.js`
- **index.css** : Directives Tailwind de base

## Fonctionnalités

✅ Affichage du serveur local  
✅ Affichage des serveurs distants du réseau  
✅ Rafraîchissement manuel  
✅ Gestion des erreurs  
✅ Design moderne et responsive avec Tailwind  
✅ Animations et transitions fluides  

## Prochaines étapes possibles

- [ ] Afficher les acteurs de chaque serveur
- [ ] Envoyer des messages aux acteurs
- [ ] Auto-refresh toutes les X secondes
- [ ] WebSocket pour les mises à jour en temps réel
- [ ] Historique des messages
