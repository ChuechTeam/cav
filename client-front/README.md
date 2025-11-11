# Client Front

> README rapide — comment lancer, prérequis et technologies utilisées.

## Prérequis

- Node.js (>=16) et npm ou yarn installés.
- (Optionnel) Navigateur moderne pour le développement.

## Lancer le projet

1. Installer les dépendances :

	- npm : `npm install`
	- yarn : `yarn`

2. Lancer le serveur de développement :

	- npm : `npm run dev`
	- yarn : `yarn dev`

	Le serveur de développement Vite démarre habituellement sur `http://localhost:5173`.

3. Pour créer une version de production :

	- npm : `npm run build`
	- yarn : `yarn build`

4. Pour prévisualiser le build :

	- npm : `npm run preview`
	- yarn : `yarn preview`

## Technologies utilisées

- React
- Vite (bundler / dev server)
- Tailwind CSS (présence de `tailwind.config.js`)
- ESLint (configuration présente)

Pour toute configuration spécifique (proxy, variables d'environnement, etc.), consultez les fichiers `vite.config.js` et les scripts dans `package.json`.

