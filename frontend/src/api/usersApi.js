import { authHeader } from "./authHeader";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function fetchUsers() {
  const response = await fetch(`${API_URL}/api/users`, {
    headers: authHeader(),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "Failed to load users");
  }

  return response.json();
}

export async function resetAccount(username) {
  const response = await fetch(`${API_URL}/api/users/reset`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeader() },
    body: JSON.stringify({ username }),
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || "Failed to reset account");
  }

  return response.json();
}
