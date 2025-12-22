import { useState } from "react";
import LoginForm from "./LoginForm";
import CreateAccountForm from "./CreateAccountForm";
import AccountView from "./AccountView";

function MainContent() {
  const [actorAddress, setActorAddress] = useState(null);
  const [viewMode, setViewMode] = useState("login");

  const handleLogin = (address) => setActorAddress(address);
  const handleAccountCreated = (address) => setActorAddress(address);
  const handleLogout = () => {
    setActorAddress(null);
    setViewMode("login");
  };

  if (actorAddress) {
    return <AccountView actorAddress={actorAddress} onLogout={handleLogout} />;
  }

  if (viewMode === "createAccount") {
    return (
      <CreateAccountForm
        onAccountCreated={handleAccountCreated}
        onCancel={() => setViewMode("login")}
      />
    );
  }

  return (
    <LoginForm
      onLogin={handleLogin}
      onShowCreateAccount={() => setViewMode("createAccount")}
    />
  );
}

export default MainContent;
