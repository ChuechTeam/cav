function Header() {
  return (
    <header className="bg-zinc-950 border-b border-zinc-800 shadow-xl">
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-3">
              <span className="text-3xl">🏛️</span>
            <div>
              <h1 className="text-2xl md:text-3xl font-bold text-white">CAV</h1>
              <p className="text-sm text-zinc-400">
                Caisse d'Allocations Virtuelle
              </p>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

export default Header;
