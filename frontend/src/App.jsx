import { useState } from "react";
import LoginForm from "./components/LoginForm";
import SanctionForm from "./components/SanctionForm";
import RecentSanctionsList from "./components/RecentSanctionsList";
import AllSanctionsPage from "./components/AllSanctionsPage";
import AdminPage from "./components/AdminPage";
import AppHeader from "./components/AppHeader";
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
  const [showAllSanctions, setShowAllSanctions] = useState(false);

  function handleLoginSuccess(data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    setAuth(data);
  }

  function handleLogout() {
    localStorage.removeItem(STORAGE_KEY);
    setAuth(null);
  }

  if (showAllSanctions) {
    return <AllSanctionsPage onBack={() => setShowAllSanctions(false)} />;
  }

  if (!auth) {
    return (
      <div id="main">
        <h2>
          Willkommen beim Phrasenschwein, wo hohle Phrasen reale Konsequenzen
          haben!
        </h2>
        <section id="center" style={{ alignItems: 'baseline', justifyContent: 'space-around'}}>
          <LoginForm onLoginSuccess={handleLoginSuccess} />
          <RecentSanctionsList onShowAll={() => setShowAllSanctions(true)} />
        </section>
      </div>
    );
  }

  if (auth.admin) {
    return (
      <AdminPage
        adminUsername={auth.username}
        onLogout={handleLogout}
        onShowAllSanctions={() => setShowAllSanctions(true)}
      />
    );
  }

  return (
    <div id="main">
      <AppHeader
        title={`Willkommen, ${auth.username}`}
        subtitle="Du bist eingeloggt."
        onShowAllSanctions={() => setShowAllSanctions(true)}
        onLogout={handleLogout}
      />
      <section id="center">
        <SanctionForm />
      </section>
    </div>
  );
}

export default App;
