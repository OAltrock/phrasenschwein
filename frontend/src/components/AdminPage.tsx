import { useState } from "react";
import { createUser, deleteUser } from "../api/usersApi";
import SanctionForm from "./SanctionForm";
import AccountResetForm from "./AccountResetForm";
import UserSelect from "./UserSelect";
import AppHeader from "./AppHeader";

interface AdminPageProps {
  adminUsername: string;
  onLogout: () => void;
  onShowAllSanctions: () => void;
}

function AdminPage({ adminUsername, onLogout, onShowAllSanctions }: AdminPageProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [deleteUsername, setDeleteUsername] = useState<string | null>(null);
  const [deleteMessage, setDeleteMessage] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleteWarning, setDeleteWarning] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [userListKey, setUserListKey] = useState(0);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);
    setMessage(null);

    setIsSubmitting(true);
    try {
      const created = await createUser(username, password);
      setMessage(`Nutzer "${created.username}" wurde angelegt.`);
      setUsername("");
      setPassword("");
      setUserListKey((key) => key + 1);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(event) {
    event.preventDefault();
    setDeleteError(null);
    setDeleteWarning(null);
    setDeleteMessage(null);

    if (!deleteUsername) {
      setDeleteError("Bitte Nutzer auswählen.");
      return;
    }

    const confirmed = window.confirm(
      `Nutzer "${deleteUsername}" wirklich entfernen? Dessen Sanktionshistorie wird ebenfalls gelöscht.`,
    );
    if (!confirmed) return;

    setIsDeleting(true);
    try {
      await deleteUser(deleteUsername);
      setDeleteMessage(`Nutzer "${deleteUsername}" wurde entfernt.`);
      setDeleteUsername(null);
      setUserListKey((key) => key + 1);
    } catch (err) {
      if (err.isBalanceWarning) {
        setDeleteWarning(err.message);
      } else {
        setDeleteError(err.message);
      }
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <div id="main">
      <AppHeader
        title={`Willkommen, ${adminUsername}`}
        subtitle="Du bist als Admin eingeloggt."
        onShowAllSanctions={onShowAllSanctions}
        onLogout={onLogout}
        showBalance={false}
      />
      <section id="center">
        <form className="login-form" onSubmit={handleSubmit}>
          <h5>Nutzer hinzufügen</h5>
          <label htmlFor="new-username">Username</label>
          <input
            id="new-username"
            type="text"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="off"
            required
          />
          <label htmlFor="new-password">Passwort</label>
          <input
            id="new-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            minLength={8}
            required
          />
          {error && <p className="login-error">{error}</p>}
          {message && <p className="login-success">{message}</p>}
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Lege an…" : "Nutzer anlegen"}
          </button>
        </form>

        <form className="login-form" onSubmit={handleDelete}>
          <h5>Nutzer entfernen</h5>
          <UserSelect
            refreshKey={userListKey}
            value={deleteUsername}
            onChange={setDeleteUsername}
            label="Welcher Nutzer?"
            required
          />
          {deleteError && <p className="login-error">{deleteError}</p>}
          {deleteWarning && <p className="login-warning">{deleteWarning}</p>}
          {deleteMessage && <p className="login-success">{deleteMessage}</p>}
          <button type="submit" disabled={isDeleting}>
            {isDeleting ? "Entferne…" : "Nutzer entfernen"}
          </button>
        </form>
        <SanctionForm refreshKey={userListKey} />
        <AccountResetForm refreshKey={userListKey} />
      </section>
    </div>
  );
}

export default AdminPage;
