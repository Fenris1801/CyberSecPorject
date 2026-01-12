const REMOTE_URL = "http://localhost:80"
const API_URL = REMOTE_URL + "/api";

// Récupération des infos session
let currentUserId = sessionStorage.getItem("userId");
let currentToken = sessionStorage.getItem("authToken");
let currentUsername = sessionStorage.getItem("username");

let shareModal;

const views = {
    auth: document.getElementById("view-auth"),
    dashboard: document.getElementById("view-dashboard"),
    editor: document.getElementById("view-editor"),
};

const forms = {
    login: document.getElementById("login-form"),
    register: document.getElementById("register-form"),
    note: document.getElementById("note-form"),
};

const inputs = {
    username: document.getElementById("username"),
    password: document.getElementById("password"),
    usernameReg: document.getElementById("username-register"),
    passwordReg: document.getElementById("password-register"),
    emailReg: document.getElementById("email-register"),
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
    registerError: document.getElementById("register-error"),
    shareModalOverlay: document.getElementById("share-modal-overlay"),
    btnCloseShare: document.getElementById("btn-close-share"),
    shareList: document.getElementById("share-list"),
    btnShare: document.getElementById("btn-share"),
    currentUser: document.getElementById("current-user"),
    currentUserId: document.getElementById("current-user-id")
};

//INITIALISATION
document.addEventListener("DOMContentLoaded", () => {
    checkHealth(); // Vérifie si le back tourne

    if (currentToken && currentUserId) {
        showView("dashboard");
        loadNotes();
    } else {
        showView("auth");
    }
    attachListeners();
    showUserInfo();
});

async function showUserInfo() {
    if (currentUsername === null) {
        await new Promise(r => setTimeout(r, 100));
        return showUserInfo(); 
    }

    ui.currentUser.innerText = currentUsername;
    ui.currentUserId.innerText = `ID: ${currentUserId}`;
}

function attachListeners() {

    forms.login.addEventListener("submit", handleLogin);

    forms.register.addEventListener("submit", handleRegister);

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

    document.getElementById("btn-share").addEventListener("click", () => {
        const noteId = inputs.noteId.value;
        if (!noteId) return alert("Sauvegardez la note avant de partager");

        loadShareList(noteId);
        ui.shareModalOverlay.classList.add("show-modal");
    });

    ui.btnCloseShare.addEventListener("click", () => {
        ui.shareModalOverlay.classList.remove("show-modal");
    });

    ui.shareModalOverlay.addEventListener("click", (e) => {
        if (e.target === ui.shareModalOverlay) {
            ui.shareModalOverlay.classList.remove("show-modal");
        }
    });

    document.getElementById("btn-confirm-share").addEventListener("click", executeShare);

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

async function handleRegister(e) {
    e.preventDefault();
    ui.registerError.classList.add("d-none");

    const body = {
        username: inputs.usernameReg.value,
        password: inputs.passwordReg.value,
        email: inputs.emailReg.value,
    };

    try {
        await apiCall("/auth/register", "POST", body);
        alert("Inscription réussie ! Vous pouvez maintenant vous connecter.");
        showView("auth");
    } catch (err) {
        console.error(err);
        ui.registerError.innerText =
            "Erreur : Impossible de s'inscrire. Le nom d'utilisateur ou l'email existe peut-être déjà.";
        ui.registerError.classList.remove("d-none");
    }
}

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
        const notes = await apiCall(`/notes/user`, "GET");

        if (!Array.isArray(notes)) return;

        notes.forEach((note) => async function () {
            // SECURITE: Nettoyage XSS pour l'aperçu
            const safePreview = DOMPurify.sanitize(note.content || "", {
                ALLOWED_TAGS: [],
            });

            // Gestion visuelle du cadenas
            const isLocked = note.lockedBy && note.lockedBy !== currentUserId;
            let lockedByUsername = "";
            if (isLocked) lockedByUsername = await apiCall('/notes/get-owner-name', 'POST', { owner_id: note.lockedBy });
            const lockBadge = isLocked
                ? `<span class="badge bg-danger">Verrouillé par ${lockedByUsername.owner_name}</span>`
                : `<span class="badge bg-secondary">Libre</span>`;

            // Gestion visuelle du partage
            const shared = note.ownerId !== currentUserId;
            let sharedBadge = "";
            let permission = "";
            if (shared) {
                const owner = await apiCall('/notes/get-owner-name', 'POST', { owner_id: note.ownerId });
                for (share of note.shares) {
                    if (share.userId === currentUserId) {
                        permission = share.permission === "READ_ONLY" ? "Lecture seule" : "Édition";
                        break;
                    }
                }
                sharedBadge = shared
                    ? `<span class="badge bg-info ms-2">Partagé par ${owner.owner_name} | ${permission}</span>`
                    : "";
            }

            const col = document.createElement("div");
            col.className = "col-md-4 mb-3";
            col.innerHTML = `
                <div class="card h-100 shadow-sm note-card">
                    <div class="card-body cursor-pointer">
                        <h5 class="card-title d-flex justify-content-between align-items-start">
                            ${DOMPurify.sanitize(note.title)}
                            ${sharedBadge}
                            ${lockBadge}
                        </h5>
                        <p class="card-text text-muted text-truncate">${safePreview}</p>
                        <small class="text-muted" style="font-size:0.7em">ID: ${note.id
                }</small>
                    </div>
                    <div class="card-footer bg-white border-top-0 text-end">
                        ${note.ownerId !== currentUserId ? "" : "<button class=\"btn btn-sm btn-outline-danger btn-delete\">Supprimer</button>"}
                    </div>
                </div>
            `;

            // Click Note -> Ouvrir
            col
                .querySelector(".card-body")
                .addEventListener("click", () => openEditor(note));

            // Click Delete
            try {
                col.querySelector(".btn-delete").addEventListener("click", (e) => {
                    e.stopPropagation();
                    deleteNote(note.id);
                });
            } catch (e) {
                // Pas de bouton delete si pas propriétaire
            }

            ui.notesList.appendChild(col);
        }.call());
    } catch (err) {
        console.error(err);
        ui.notesList.innerHTML =
            '<div class="alert alert-warning">Impossible de charger les notes</div>';
    }
}

async function loadShareList(noteId) {  // liste des personnes avec accès
    ui.shareList.innerHTML = "Chargement...";
    try {
        const list = await apiCall(`/notes/${noteId}/access`, "GET");
        for (let acc of list) {  // on load aussi leur nom
            const owner = await apiCall('/notes/get-owner-name', 'POST', { owner_id: acc.userId });
            acc.userName = owner.owner_name;
        }
        ui.shareList.innerHTML = list.map(acc => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                ${acc.userName} (${acc.permission})
                <button class="btn btn-sm btn-danger" onclick="revokeAccess('${noteId}', '${acc.userId}', '${acc.userName}')">X</button>
            </li>`).join('');
    } catch (e) { ui.shareList.innerHTML = "Erreur."; }
}

async function revokeAccess(noteId, targetUserId, targetUserName) {  // quand on clique sur le X
    if (!confirm(`Révoquer l'accès de ${targetUserName} ?`)) return;
    try {
        await apiCall("/notes/revoke-share", "POST", {
            note_id: noteId,
            target_user_id: targetUserId
        });
        loadShareList(noteId);
    } catch (e) {
        alert("Erreur lors de la révocation : " + e.message);
    }
}

async function executeShare() {  // quand on clique sur confirmer le partage
    const noteId = inputs.noteId.value;
    const target = document.getElementById("share-target-user").value;
    const perm = document.getElementById("share-permission").value;

    if (!target) return alert("Veuillez saisir un ID utilisateur");

    try {
        await apiCall("/notes/share", "POST", {
            noteId: noteId,
            targetUserId: target,
            permission: perm
        });

        document.getElementById("share-target-user").value = "";
        loadShareList(noteId);
    } catch (e) {
        alert("Erreur de partage : " + e.message);
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

        if (note.ownerId !== currentUserId) {
            ui.btnShare.style.display = "none";
        } else {
            ui.btnShare.style.display = "inline-block";
        }

        // Vérif du verrouillage
        if (note.lockedBy && note.lockedBy !== currentUserId) {
            // si c verrouillé par quelqu'un d'autre alors Lecture Seule
            ui.lockStatus.className = "badge bg-danger";
            let lockedByUsername = "";
            if (isLocked) lockedByUsername = await apiCall('/notes/get-owner-name', 'POST', { owner_id: note.lockedBy });
            ui.lockStatus.innerText = `LECTURE SEULE (Verrouillé par ${lockedByUsername.owner_name})`;
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
        await fetch(`${REMOTE_URL}/actuator/health`, { mode: "cors" });
        console.log("Backend OK");
    } catch (e) {
        console.error("Backend Down");
    }
}
