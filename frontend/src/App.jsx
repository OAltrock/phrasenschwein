import { useState } from "react";
import LoginForm from "./components/LoginForm";
import SanctionForm from "./components/SanctionForm";
import AccountResetForm from "./components/AccountResetForm";
import RecentSanctionsList from "./components/RecentSanctionsList";
import "./App.css";

const STORAGE_KEY = "phrasenschwein.auth";

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function App() {
  const [auth, setAuth] = useState(loadStoredAuth);

  function handleLoginSuccess(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    setAuth(data);
  }

  function handleLogout() {
    localStorage.removeItem(STORAGE_KEY);
    setAuth(null);
  }

  if (!auth) {
    return (
      <div id="main">
        <h2>
          Willkommen beim Phrasenschwein, wo hohle Phrasen reale Konsequenzen
          haben!
        </h2>
        <section id="center">
          <LoginForm onLoginSuccess={handleLoginSuccess} />
          <RecentSanctionsList />
        </section>
      </div>
    );
  }

  return (
    <div>
      <h1>Welcome, {auth.username}</h1>
      <p>You are logged in.</p>
      <section id="center">
        <SanctionForm />
        <AccountResetForm currentUsername={auth.username} />
      </section>
      <button type="button" className="counter" onClick={handleLogout} style={{ marginTop: '15px'}}>
        Log out
      </button>
    </div>
  );
}

export default App;
// style={{ flexDirection: 'column' }}
