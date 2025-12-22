import { useState } from "react";

function CreateAccountForm({ onAccountCreated, onCancel }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    birthDate: "",
    email: "",
    phoneNumber: "",
    address: "",
    inCouple: false,
    numberOfDependents: 0,
    monthlyIncome: "",
    iban: "",
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!formData.firstName || !formData.lastName || !formData.email) {
      setError("Veuillez remplir au moins le prénom, nom et email.");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch("http://localhost:4444/api/accounts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          ...formData,
          numberOfDependents: parseInt(formData.numberOfDependents) || 0,
          monthlyIncome: parseFloat(formData.monthlyIncome) || 0,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        onAccountCreated(data.beneficiaryAddress);
      } else {
        const errorData = await response.text();
        setError(
          `Erreur lors de la création : ${errorData || response.statusText}`
        );
      }
    } catch (err) {
      setError(
        "Impossible de contacter le serveur. Vérifiez que le service est lancé."
      );
      console.error("Erreur:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 border rounded-lg bg-zinc-800 border-zinc-700">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold text-white">Créer un compte</h2>
        <button onClick={onCancel} className="text-zinc-400 hover:text-white">
          ← Retour
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Identité */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Prénom *
            </label>
            <input
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Nom *
            </label>
            <input
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium text-zinc-300">
            Date de naissance
          </label>
          <input
            type="date"
            name="birthDate"
            value={formData.birthDate}
            onChange={handleChange}
            className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
            disabled={loading}
          />
        </div>

        {/* Contact */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Email *
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Téléphone
            </label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
        </div>

        <div>
          <label className="block mb-1 text-sm font-medium text-zinc-300">
            Adresse postale
          </label>
          <input
            type="text"
            name="address"
            value={formData.address}
            onChange={handleChange}
            className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
            disabled={loading}
          />
        </div>

        {/* Situation */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              name="inCouple"
              id="inCouple"
              checked={formData.inCouple}
              onChange={handleChange}
              className="w-4 h-4 rounded bg-zinc-700 border-zinc-600"
              disabled={loading}
            />
            <label htmlFor="inCouple" className="text-sm text-zinc-300">
              En couple
            </label>
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Personnes à charge
            </label>
            <input
              type="number"
              name="numberOfDependents"
              value={formData.numberOfDependents}
              onChange={handleChange}
              min="0"
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
        </div>

        {/* Finances */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              Revenus mensuels (€)
            </label>
            <input
              type="number"
              name="monthlyIncome"
              value={formData.monthlyIncome}
              onChange={handleChange}
              min="0"
              step="0.01"
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block mb-1 text-sm font-medium text-zinc-300">
              IBAN
            </label>
            <input
              type="text"
              name="iban"
              value={formData.iban}
              onChange={handleChange}
              placeholder="FR76..."
              className="w-full px-3 py-2 text-white border rounded bg-zinc-700 border-zinc-600 focus:outline-none focus:border-blue-500"
              disabled={loading}
            />
          </div>
        </div>

        {error && (
          <div className="p-3 text-sm text-red-300 border border-red-500 rounded bg-red-950/50">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full px-6 py-3 font-medium text-white transition-colors rounded bg-cyan-600 hover:bg-cyan-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? "Création en cours..." : "Créer mon compte"}
        </button>
      </form>
    </div>
  );
}

export default CreateAccountForm;
