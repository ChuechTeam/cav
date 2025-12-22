function AccountView({ actorAddress, onLogout }) {
  return (
    <div className="p-6 text-center border rounded-lg bg-zinc-800 border-zinc-700">
      <p className="text-zinc-300">
        Connecté en tant que :{" "}
        <span className="font-mono text-white">{actorAddress}</span>
      </p>
      <button
        onClick={onLogout}
        className="px-4 py-2 mt-4 text-white transition-colors bg-red-600 rounded hover:bg-red-700"
      >
        Se déconnecter
      </button>
    </div>
  );
}

export default AccountView;
