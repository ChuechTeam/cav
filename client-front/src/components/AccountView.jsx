import { useState, useEffect } from "react";

// Convertit l'adresse en string pour l'API
function addressToString(addr) {
  if (typeof addr === "string") return addr;
  if (addr && addr.serverId !== undefined && addr.actorNumber !== undefined) {
    return `${addr.serverId}:${addr.actorNumber}`;
  }
  return String(addr);
}

// Labels pour les états de prévision
const STATE_LABELS = {
  UNWANTED: { text: "Non demandé", color: "text-zinc-400" },
  PENDING: { text: "En attente", color: "text-yellow-400" },
  UP_TO_DATE: { text: "À jour", color: "text-green-400" },
};

// Labels pour les types d'allocation
const ALLOWANCE_LABELS = {
  RSA: "RSA (Revenu de Solidarité Active)",
};

function AccountView({ actorAddress, onLogout }) {
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);
  const [actionMessage, setActionMessage] = useState(null);

  const addrString = addressToString(actorAddress);

  const fetchAccount = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `http://localhost:4444/api/accounts/${addrString}`
      );
      if (response.ok) {
        const data = await response.json();
        setAccount(data);
      } else {
        setError("Impossible de charger les données du compte.");
      }
    } catch (err) {
      setError("Erreur de connexion au serveur.");
      console.error("Erreur:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccount();
  }, [addrString]);

  const handleRequestAllowance = async (type) => {
    setActionLoading(type);
    setActionMessage(null);
    try {
      const response = await fetch(
        `http://localhost:4444/api/accounts/${addrString}/requests/${type}`,
        { method: "POST" }
      );

      if (response.ok) {
        const data = await response.json();
        setActionMessage({
          type: "success",
          text: data.message || "Demande envoyée avec succès",
        });
        // Recharger les données du compte
        setTimeout(() => fetchAccount(), 1000);
      } else {
        const data = await response.json();
        setActionMessage({
          type: "error",
          text: data.message || "Erreur lors de la demande",
        });
      }
    } catch (err) {
      setActionMessage({
        type: "error",
        text: "Impossible de contacter le serveur",
      });
      console.error("Erreur:", err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleRefuseAllowance = async (type) => {
    // Implémenter dans le back pour refuser une aide (en attete)
    setActionMessage({
      type: "info",
      text: "Fonctionnalite refuser une aide à implémenter du cote back",
    });
  };

  if (loading) {
    return (
      <div className="p-12 border rounded-lg bg-zinc-800 border-zinc-700">
        <div className="flex flex-col items-center justify-center">
          <div className="w-12 h-12 mb-4 border-b-4 rounded-full border-cyan-500 animate-spin"></div>
          <p className="text-zinc-300">Chargement du compte...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
        <div className="p-4 mb-4 border-l-4 border-red-500 rounded bg-red-950/50">
          <p className="text-red-300">{error}</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={fetchAccount}
            className="px-4 py-2 text-white transition-colors rounded bg-cyan-800 hover:bg-cyan-900"
          >
            Réessayer
          </button>
          <button
            onClick={onLogout}
            className="px-4 py-2 text-white transition-colors bg-red-800 rounded hover:bg-red-900"
          >
            Se déconnecter
          </button>
        </div>
      </div>
    );
  }

  const { profile, payments, allowancePrevisions } = account;

  return (
    <div className="space-y-6">
      {/* En-tête avec infos utilisateur */}
      <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-2xl font-bold text-white">
              {profile.firstName} {profile.lastName}
            </h2>
            <p className="font-mono text-sm text-zinc-500">{addrString}</p>
          </div>
          <button
            onClick={onLogout}
            className="px-4 py-2 text-white transition-colors bg-red-800 rounded hover:bg-red-900"
          >
            Se déconnecter
          </button>
        </div>

        {/* Infos profil */}
        <div className="grid grid-cols-2 gap-4 text-sm md:grid-cols-3">
          <div>
            <span className="text-zinc-500">Email</span>
            <p className="text-white">{profile.email || "-"}</p>
          </div>
          <div>
            <span className="text-zinc-500">Téléphone</span>
            <p className="text-white">{profile.phoneNumber || "-"}</p>
          </div>
          <div>
            <span className="text-zinc-500">Adresse</span>
            <p className="text-white">{profile.address || "-"}</p>
          </div>
          <div>
            <span className="text-zinc-500">Situation</span>
            <p className="text-white">
              {profile.inCouple ? "En couple" : "Célibataire"}
            </p>
          </div>
          <div>
            <span className="text-zinc-500">Personnes à charge</span>
            <p className="text-white">{profile.numberOfDependents}</p>
          </div>
          <div>
            <span className="text-zinc-500">Revenus mensuels</span>
            <p className="text-white">{profile.monthlyIncome} €</p>
          </div>
        </div>
      </div>

      {/* Prévisions d'allocations */}
      <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-white">Mes allocations</h3>
          <button
            onClick={fetchAccount}
            className="px-3 py-1 text-sm transition-colors rounded text-zinc-400 hover:text-white hover:bg-zinc-700"
          >
            ↻ Actualiser
          </button>
        </div>

        {actionMessage && (
          <div
            className={`mb-4 p-3 rounded text-sm ${
              actionMessage.type === "success"
                ? "bg-green-950/50 border border-green-500 text-green-300"
                : actionMessage.type === "error"
                ? "bg-red-950/50 border border-red-500 text-red-300"
                : "bg-blue-950/50 border border-blue-500 text-blue-300"
            }`}
          >
            {actionMessage.text}
          </div>
        )}

        {allowancePrevisions && Object.keys(allowancePrevisions).length > 0 ? (
          <div className="space-y-3">
            {Object.entries(allowancePrevisions).map(([type, prevision]) => (
              <div key={type} className="p-4 rounded bg-zinc-700/50">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="font-medium text-white">
                      {ALLOWANCE_LABELS[type] || type}
                    </p>
                    <p
                      className={`text-sm ${
                        STATE_LABELS[prevision.state]?.color || "text-zinc-400"
                      }`}
                    >
                      {STATE_LABELS[prevision.state]?.text || prevision.state}
                    </p>
                    {prevision.lastMessage && (
                      <p className="mt-1 text-xs text-zinc-500">
                        {prevision.lastMessage}
                      </p>
                    )}
                  </div>
                  <div className="text-right">
                    {prevision.lastAmount !== null &&
                      prevision.lastAmount !== undefined && (
                        <p className="mb-2 text-xl font-bold text-green-400">
                          {prevision.lastAmount} €
                        </p>
                      )}
                  </div>
                </div>

                {/* Boutons d'action */}
                <div className="flex gap-2 mt-3">
                  {prevision.state === "UNWANTED" && (
                    <button
                      onClick={() => handleRequestAllowance(type)}
                      disabled={actionLoading === type}
                      className="px-4 py-2 text-sm font-medium text-white transition-colors rounded bg-cyan-600 hover:bg-cyan-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {actionLoading === type
                        ? "Demande en cours..."
                        : "Demander cette aide"}
                    </button>
                  )}
                  {(prevision.state === "PENDING" ||
                    prevision.state === "UP_TO_DATE") && (
                    <button
                      onClick={() => handleRefuseAllowance(type)}
                      disabled={actionLoading === type}
                      className="px-4 py-2 text-sm font-medium text-white transition-colors bg-red-600 rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {actionLoading === type
                        ? "Annulation en cours..."
                        : "Refuser cette aide"}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-zinc-500">Aucune allocation configurée.</p>
        )}
      </div>

      {/* Historique des paiements */}
      <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
        <h3 className="mb-4 text-lg font-semibold text-white">
          Historique des paiements
        </h3>

        {payments && payments.length > 0 ? (
          <div className="space-y-2">
            {payments.map((payment, index) => (
              <div
                key={index}
                className="flex items-center justify-between p-3 rounded bg-zinc-700/50"
              >
                <span className="text-zinc-300">{payment.label}</span>
                <span className="font-medium text-green-400">
                  +{payment.amount} €
                </span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-zinc-500">Aucun paiement enregistré.</p>
        )}
      </div>
    </div>
  );
}

export default AccountView;
