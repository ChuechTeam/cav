import { useState } from "react";

function LoginForm({ onLogin, onShowCreateAccount }) {
  const [address, setAddress] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!address.trim()) {
      setError("Veuillez entrer une adresse");
      return;
    }

    // Valider le format de l'adresse (serverId:actorNumber)
    if (!/^[0-9a-f]+:[0-9a-f]+$/.test(address.trim())) {
      setError(
        "Format d'adresse invalide. Utilisez le format serverId:actorNumber (ex: 1:100)"
      );
      return;
    }

    setLoading(true);

    try {
      // Vérifier que le compte existe en appelant l'API (avec timeout)
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000); // 5s timeout

      const response = await fetch(
        `http://localhost:4444/api/accounts/${address.trim()}`,
        { signal: controller.signal }
      );
      clearTimeout(timeoutId);

      console.log("Response status:", response.status);

      if (response.ok) {
        onLogin(address.trim());
      } else if (response.status === 404) {
        setError(
          "Compte non trouvé. Vérifiez l'adresse ou créez un nouveau compte."
        );
      } else {
        setError(`Erreur ${response.status} lors de la connexion. Réessayez.`);
      }
    } catch (err) {
      console.error("Erreur fetch:", err);
      if (err.name === "AbortError") {
        setError(
          "Timeout : le serveur ne répond pas. Vérifiez que le backend est lancé (python run.py all)."
        );
      } else {
        setError(
          "Impossible de contacter le serveur. Vérifiez que le service est lancé."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
      <h2 className="mb-4 text-xl font-semibold text-center text-white">
        Bienvenue sur la CAV
      </h2>
      <p className="mb-6 text-center text-zinc-400">
        Connectez-vous ou créez un compte pour accéder à vos allocations.
      </p>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label
            htmlFor="address"
            className="block mb-2 text-sm font-medium text-zinc-300"
          >
            Adresse de votre compte
          </label>
          <input
            type="text"
            id="address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Ex: 1:100"
            className="w-full px-4 py-2 text-white border rounded bg-zinc-700 border-zinc-600 placeholder-zinc-500 focus:outline-none "
            disabled={loading}
          />
          <p className="mt-1 text-xs text-zinc-500">
            Format : serverId:actorNumber
          </p>
        </div>

        {error && (
          <div className="p-3 text-sm text-red-300 border border-red-500 rounded bg-red-950/50">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="block px-6 py-2 mx-auto font-medium text-white transition-colors rounded w-md bg-cyan-600 hover:bg-cyan-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? "Connexion..." : "Se connecter"}
        </button>
      </form>

      <div className="mt-6 text-center">
        <p className="mb-2 text-sm text-zinc-400">Pas encore de compte ?</p>
        <button
          onClick={onShowCreateAccount}
          className="px-6 py-2 font-medium text-white transition-colors rounded bg-cyan-600 hover:bg-cyan-700"
        >
          Créer un compte
        </button>
      </div>
    </div>
  );
}

export default LoginForm;
