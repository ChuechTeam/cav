import { useState, useEffect } from "react";

function ActorList() {
  const [servers, setServers] = useState([]);
  const [localServer, setLocalServer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchServers();
  }, []);

  const fetchServers = async () => {
    try {
      setLoading(true);
      setError(null);

      // Récupérer le serveur local
      const localResponse = await fetch(
        "http://localhost:4444/api/servers/local"
      );
      if (!localResponse.ok)
        throw new Error("Erreur lors de la récupération du serveur local");
      const localData = await localResponse.json();
      setLocalServer(localData);

      // Récupérer tous les serveurs du réseau
      const serversResponse = await fetch("http://localhost:4444/api/servers");
      if (!serversResponse.ok)
        throw new Error("Erreur lors de la récupération des serveurs");
      const serversData = await serversResponse.json();
      setServers(serversData);
    } catch (err) {
      setError(err.message);
      console.error("Erreur:", err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-12 border rounded-lg shadow-xl bg-zinc-800 border-zinc-700">
        <div className="flex flex-col items-center justify-center">
          <div className="w-12 h-12 mb-4 border-b-4 border-red-500 rounded-full animate-spin"></div>
          <p className="text-zinc-300">Chargement des serveurs...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 border rounded-lg shadow-xl bg-zinc-800 border-zinc-700">
        <div className="p-6 border-l-4 border-red-500 rounded bg-red-950/50">
          <div className="flex items-start">
            <div className="flex-1 ml-3">
              <h3 className="mb-2 text-lg font-semibold text-red-400">
                Erreur de connexion
              </h3>
              <p className="mb-4 text-red-300">{error}</p>
              <button
                onClick={fetchServers}
                className="px-6 py-2 font-medium text-white transition-colors duration-200 bg-blue-600 rounded hover:bg-blue-700"
              >
                Réessayer
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* En-tête avec bouton actualiser */}
      <div className="p-6 border rounded-lg shadow-xl bg-zinc-800 border-zinc-700">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-white">
              Réseau de serveurs
            </h2>
            <p className="mt-1 text-zinc-400">
              Vue d'ensemble du système d'acteurs distribués
            </p>
          </div>
          <button
            onClick={fetchServers}
            className="flex items-center gap-2 px-6 py-2 font-medium text-white transition-colors duration-200 rounded-xl bg-zinc-900 hover:bg-zinc-950"
          >
            Actualiser
          </button>
        </div>
      </div>

      {/* Serveur Local */}
      {localServer && (
        <div className="overflow-hidden border rounded-lg shadow-xl bg-zinc-800 border-zinc-700">
          <div className="px-6 py-4 border-b bg-zinc-900 border-zinc-700">
            <h3 className="flex items-center gap-2 text-xl font-semibold text-white">
              Serveur Local
            </h3>
          </div>
          <div className="p-6">
            <div className="p-4 rounded-2xl bg-blue-950/30">
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-3">
                    <span className="text-sm font-semibold text-zinc-400">
                      ID du serveur
                    </span>
                    <code className="px-3 py-1 font-mono text-sm text-blue-400 border rounded bg-zinc-900 border-zinc-700">
                      {localServer.id}
                    </code>
                  </div>
                  <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    <div>
                      <span className="text-sm font-semibold text-zinc-400">
                        Application
                      </span>
                      <p className="font-medium text-white">
                        {localServer.name}
                      </p>
                    </div>
                    <div>
                      <span className="text-sm font-semibold text-zinc-400">
                        URL
                      </span>
                      <p className="font-medium text-white">
                        {localServer.url || "Non disponible"}
                      </p>
                    </div>
                  </div>
                </div>
                <span className="px-3 py-1 text-xs font-bold text-white rounded-full">
                  LOCAL
                </span>
              </div>
              {Object.keys(localServer.metadata).length > 0 && (
                <div className="pt-4 border-t border-zinc-700">
                  <span className="block mb-2 text-sm font-semibold text-zinc-400">
                    Métadonnées
                  </span>
                  <div className="space-y-1">
                    {Object.entries(localServer.metadata).map(
                      ([key, value]) => (
                        <div
                          key={key}
                          className="flex items-center gap-2 text-sm"
                        >
                          <code className="px-2 py-1 font-mono font-semibold text-blue-400 rounded bg-zinc-900">
                            {key}
                          </code>
                          <span className="text-zinc-500">→</span>
                          <span className="text-zinc-300">{value}</span>
                        </div>
                      )
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Serveurs Distants */}
      <div className="overflow-hidden border rounded-lg shadow-xl bg-zinc-800 border-zinc-700">
        <div className="flex items-center justify-between px-6 py-4 border-b bg-zinc-900 border-zinc-700">
          <h3 className="flex items-center gap-2 text-xl font-semibold text-white">
            Serveurs Distants
          </h3>
          <span className="px-3 py-1 text-sm font-bold text-white rounded-full bg-zinc-700">
            {servers.length}
          </span>
        </div>
        <div className="p-6">
          {servers.length === 0 ? (
            <div className="p-8 text-center border-2 border-dashed rounded-lg bg-zinc-900/50 border-zinc-700">
              <p className="mb-1 font-medium text-zinc-300">
                Aucun serveur distant détecté
              </p>
              <p className="text-sm text-zinc-500">
                Assurez-vous que d'autres instances de service sont démarrées
                sur le réseau
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {servers.map((server) => (
                <div
                  key={server.id}
                  className="p-4 transition-all duration-200 border-2 rounded-lg bg-zinc-900 border-zinc-700"
                >
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-xs font-semibold text-zinc-500">
                          ID
                        </span>
                        <code className="px-2 py-1 font-mono text-xs text-blue-400 rounded bg-zinc-800">
                          {server.id}
                        </code>
                      </div>
                      <div className="space-y-2">
                        <div>
                          <span className="text-xs font-semibold text-zinc-500">
                            Application
                          </span>
                          <p className="text-sm font-medium text-white">
                            {server.name}
                          </p>
                        </div>
                        <div>
                          <span className="text-xs font-semibold text-zinc-500">
                            URL
                          </span>
                          <p className="text-sm truncate text-zinc-300">
                            {server.url}
                          </p>
                        </div>
                      </div>
                    </div>
                    <span className="px-2 py-1 text-xs font-bold text-white rounded-full">
                      ACTIF
                    </span>
                  </div>
                  {Object.keys(server.metadata).length > 0 && (
                    <div className="pt-3 border-t border-zinc-700">
                      <span className="block mb-2 text-xs font-semibold text-zinc-500">
                        Métadonnées
                      </span>
                      <div className="space-y-1">
                        {Object.entries(server.metadata).map(([key, value]) => (
                          <div
                            key={key}
                            className="flex items-center gap-2 text-xs"
                          >
                            <code className="px-2 py-1 font-mono text-blue-400 rounded bg-zinc-800">
                              {key}
                            </code>
                            <span className="text-zinc-600">:</span>
                            <span className="text-zinc-300">{value}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ActorList;
