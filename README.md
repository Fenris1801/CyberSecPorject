# CyberSecProject
Software for storing and accessing personal textual notes security focused on abuses Cases

Lancement du projet
Pour démarrer l'application, suivez les étapes ci-dessous selon la partie du projet que vous souhaitez lancer.

1. Lancer le Backend (Docker)
Assurez-vous de vous trouver dans le dossier **backend-api** et exécutez la commande suivante :
```bash
docker-compose up --build
```

2. Lancer le Frontend
Pour visualiser l'interface utilisateur, ouvrez un nouveau terminal, placez-vous dans le dossier **frontend** et lancez un serveur HTTP local :

Lancez le serveur :
```bash
python3 -m http.server 5500
```

Une fois le serveur lancé, vous pourrez accéder au frontend via votre navigateur à l'adresse : http://localhost:5500
