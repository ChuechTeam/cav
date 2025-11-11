import Layout from './components/Layout'
import Header from './components/Header'
import ActorList from './components/ActorList'

function App() {
  return (
    <Layout>
      <Header />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <ActorList />
      </main>
    </Layout>
  )
}

export default App
