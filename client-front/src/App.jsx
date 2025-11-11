import Layout from './components/Layout'
import Header from './components/Header'
import ServerList from './components/ServerList'

function App() {
  return (
    <Layout>
      <Header />
      <main className="px-4 py-8 mx-auto max-w-7xl">
        <ServerList />
      </main>
    </Layout>
  )
}

export default App
