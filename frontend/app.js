const API_URL = "http://localhost:8080/api";

// Récupération des infos session
let currentUserId = sessionStorage.getItem("userId");
let currentToken = sessionStorage.getItem("authToken");
let currentUsername = sessionStorage.getItem("username");

const views = {
  login: document.getElementById("view-login"),
  dashboard: document.getElementById("view-dashboard"),
  editor: document.getElementById("view-editor"),
};

const forms = {
  login: document.getElementById("login-form"),
  note: document.getElementById("note-form"),
};

const inputs = {
  username: document.getElementById("username"),
  password: document.getElementById("password"),
  noteTitle: document.getElementById("note-title"),
  noteContent: document.getElementById("note-content"),
  noteId: document.getElementById("note-id"),
};

const ui = {
  notesList: document.getElementById("notes-list"),
  lockStatus: document.getElementById("lock-status"),
  preview: document.getElementById("note-preview"),
  loading: document.getElementById("loading"),
  loginError: document.getElementById("login-error"),
};

//INITIALISATION
document.addEventListener("DOMContentLoaded", () => {
  checkHealth(); // Vérifie si le back tourne

  if (currentToken && currentUserId) {
    showView("dashboard");
    loadNotes();
  } else {
    showView("login");
  }
  attachListeners();
});

function attachListeners() {
  forms.login.addEventListener("submit", handleLogin);

  document.getElementById("btn-logout").addEventListener("click", logout);

  document
    .getElementById("btn-new-note")
    .addEventListener("click", () => openEditor());

  forms.note.addEventListener("submit", saveNote);

  document.getElementById("btn-back").addEventListener("click", async () => {
    // IMPORTANT: On déverrouille la note si on quitte l'éditeur sans sauver
    if (inputs.noteId.value) {
      await unlockNote(inputs.noteId.value);
    }
    showView("dashboard");
    loadNotes();
  });

  // Sécurité XSS (Abuse Case Defense)
  inputs.noteContent.addEventListener("input", (e) => {
    ui.preview.innerHTML = DOMPurify.sanitize(e.target.value, {
      USE_PROFILES: { html: true },
    });
  });
}

//NAVIGATION
function showView(viewName) {
  Object.values(views).forEach((el) => el.classList.remove("active-view"));
  views[viewName].classList.add("active-view");
}

function toggleLoading(show) {
  ui.loading.style.display = show ? "flex" : "none";
}

//COEUR API (Fetch)
async function apiCall(endpoint, method = "GET", body = null) {
  toggleLoading(true);

  const headers = { "Content-Type": "application/json" };

  if (currentToken) {
    headers["Authorization"] = `Bearer ${currentToken}`;
    console.log(
      "Envoi du Token avec la requête :",
      currentToken.substring(0, 10) + "..."
    );
  } else {
    console.warn("ATTENTION : Aucun token n'est envoyé !");
  }

  try {
    const config = { method, headers };
    if (body) config.body = JSON.stringify(body);

    const response = await fetch(`${API_URL}${endpoint}`, config);
    toggleLoading(false);

    if (response.status === 403) throw new Error("Accès interdit (403)");
    if (response.status === 409)
      throw new Error("Conflit: Note verrouillée par un autre utilisateur");

    // Gestion pour réponses void (pas de json) ou texte
    const text = await response.text();
    return text ? JSON.parse(text) : {};
  } catch (error) {
    toggleLoading(false);
    console.error("Erreur API:", error);
    throw error;
  }
}

//AUTHENTIFICATION

async function handleLogin(e) {
  e.preventDefault();
  ui.loginError.classList.add("d-none");

  const body = {
    username: inputs.username.value,
    password: inputs.password.value,
  };

  try {
    const data = await apiCall("/auth/login", "POST", body);

    // Backend renvoie user_id (avec underscore)
    currentUserId = data.user_id;
    currentToken = data.token;
    currentUsername = data.username;

    if (!currentUserId || !currentToken) {
      console.error("Réponse reçue:", data); // debug
      throw new Error("Données de connexion incomplètes reçues du serveur");
    }

    // Stockage Session
    sessionStorage.setItem("authToken", currentToken);
    sessionStorage.setItem("userId", currentUserId);
    sessionStorage.setItem("username", currentUsername);

    showView("dashboard");
    loadNotes();
  } catch (err) {
    console.error(err);
    ui.loginError.innerText =
      "Erreur : Vérifiez vos identifiants ou le serveur.";
    ui.loginError.classList.remove("d-none");
  }
}

function logout() {
  sessionStorage.clear();
  location.reload();
}

//GESTION DES NOTES

async function loadNotes() {
  ui.notesList.innerHTML = "";
  try {
    const notes = await apiCall(`/notes/user/${currentUserId}`, "GET");

    if (!Array.isArray(notes)) return;

    notes.forEach((note) => {
      // SECURITE: Nettoyage XSS pour l'aperçu
      const safePreview = DOMPurify.sanitize(note.content || "", {
        ALLOWED_TAGS: [],
      });

      // Gestion visuelle du cadenas
      const isLocked = note.lockedBy && note.lockedBy !== currentUserId;
      const lockBadge = isLocked
        ? `<span class="badge bg-danger">Verrouillé par ${note.lockedBy}</span>`
        : `<span class="badge bg-secondary">Libre</span>`;

      const col = document.createElement("div");
      col.className = "col-md-4 mb-3";
      col.innerHTML = `
                <div class="card h-100 shadow-sm note-card">
                    <div class="card-body cursor-pointer">
                        <h5 class="card-title d-flex justify-content-between align-items-start">
                            ${DOMPurify.sanitize(note.title)}
                            ${lockBadge}
                        </h5>
                        <p class="card-text text-muted text-truncate">${safePreview}</p>
                        <small class="text-muted" style="font-size:0.7em">ID: ${
                          note.id
                        }</small>
                    </div>
                    <div class="card-footer bg-white border-top-0 text-end">
                        <button class="btn btn-sm btn-outline-danger btn-delete">Supprimer</button>
                    </div>
                </div>
            `;

      // Click Note -> Ouvrir
      col
        .querySelector(".card-body")
        .addEventListener("click", () => openEditor(note));

      // Click Delete
      col.querySelector(".btn-delete").addEventListener("click", (e) => {
        e.stopPropagation();
        deleteNote(note.id);
      });

      ui.notesList.appendChild(col);
    });
  } catch (err) {
    console.error(err);
    ui.notesList.innerHTML =
      '<div class="alert alert-warning">Impossible de charger les notes</div>';
  }
}

async function openEditor(note = null) {
  showView("editor");

  if (note) {
    //MODE MODIF
    inputs.noteId.value = note.id;
    inputs.noteTitle.value = note.title;
    inputs.noteContent.value = note.content;
    ui.preview.innerHTML = DOMPurify.sanitize(note.content, {
      USE_PROFILES: { html: true },
    });

    // Vérif du verrouillage
    if (note.lockedBy && note.lockedBy !== currentUserId) {
      // si c verrouillé par quelqu'un d'autre alors Lecture Seule
      ui.lockStatus.className = "badge bg-danger";
      ui.lockStatus.innerText = `LECTURE SEULE (Verrouillé par ${note.lockedBy})`;
      disableEditor(true);
    } else {
      // si Libre alrs on tente de verrouiller
      try {
        await lockNote(note.id);
        ui.lockStatus.className = "badge bg-success";
        ui.lockStatus.innerText = "ÉDITION (Verrou acquis)";
        disableEditor(false);
      } catch (e) {
        // Échec du verrouillage (Concurrence)
        ui.lockStatus.className = "badge bg-warning text-dark";
        ui.lockStatus.innerText = "Erreur Verrouillage - Lecture Seule";
        disableEditor(true);
      }
    }
  } else {
    //MODE CREATION
    inputs.noteId.value = "";
    inputs.noteTitle.value = "";
    inputs.noteContent.value = "";
    ui.preview.innerHTML = "";
    ui.lockStatus.className = "badge bg-info";
    ui.lockStatus.innerText = "Nouvelle Note";
    disableEditor(false);
  }
}

function disableEditor(isDisabled) {
  inputs.noteTitle.disabled = isDisabled;
  inputs.noteContent.disabled = isDisabled;
  document.getElementById("btn-save").disabled = isDisabled;
}

//ACTIONS SUR LES NOTES (DTO MAPPING)

async function saveNote(e) {
  e.preventDefault();
  const noteId = inputs.noteId.value;

  try {
    if (noteId) {
      await apiCall("/notes/update", "POST", {
        note_id: noteId,
        user_id: currentUserId,
        title: inputs.noteTitle.value,
        content: inputs.noteContent.value,
      });
      await unlockNote(noteId);
    } else {
      await apiCall("/notes/create", "POST", {
        user_id: currentUserId,
        title: inputs.noteTitle.value,
        content: inputs.noteContent.value,
      });
    }

    showView("dashboard");
    loadNotes();
  } catch (err) {
    alert("Erreur lors de la sauvegarde: " + err.message);
  }
}

async function deleteNote(id) {
  if (!confirm("Supprimer définitivement ?")) return;
  try {
    await apiCall("/notes/delete", "POST", {
      note_id: id,
      user_id: currentUserId,
    });
    loadNotes();
  } catch (err) {
    alert("Erreur suppression: " + err.message);
  }
}

//GESTION DES VERROUS (LOCKS)

async function lockNote(id) {
  await apiCall("/notes/lock", "POST", {
    note_id: id,
    user_id: currentUserId,
  });
}

async function unlockNote(id) {
  try {
    await apiCall("/notes/unlock", "POST", {
      note_id: id,
      user_id: currentUserId,
    });
  } catch (e) {
    console.warn("Erreur déverrouillage (peut être déjà déverrouillé)");
  }
}

//SYSTEME
async function checkHealth() {
  try {
    // J'ajoute le mode cors ici au cas où pour éviter les faux négatifs
    await fetch(`${API_URL}/health`, { mode: "cors" });
    console.log("Backend OK");
  } catch (e) {
    console.error("Backend Down");
  }
}
