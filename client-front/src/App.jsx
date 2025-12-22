import Layout from "./components/Layout";
import Header from "./components/Header";
import MainContent from "./components/MainContent";

function App() {
  return (
    <Layout>
      <Header />
      <main className="px-4 py-8 mx-auto max-w-7xl">
        <MainContent />
      </main>
    </Layout>
  );
}

export default App;
