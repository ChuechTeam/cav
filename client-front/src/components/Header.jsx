function Header() {
  return (
    <header className="border-b shadow-xl bg-zinc-950 border-zinc-800">
      <div className="p-6 px-4 mx-auto max-w-8xl">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-3">
              <span className="text-3xl">🏛️</span>
            <div>
              <h1 className="text-2xl font-bold text-white md:text-3xl">CAV</h1>
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
