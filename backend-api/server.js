require('dotenv').config();
const express = require('express');
const app = express();
const PORT = process.env.PORT || 8080;

// Middleware pour parser le JSON
app.use(express.json());

// Une route de test
app.get('/', (req, res) => {
    res.send('API fonctionne !');
});

// Démarrage du serveur
app.listen(PORT, () => {
    console.log(`Serveur démarré sur le port ${PORT}`);
});