import { useState } from 'react';

const API_BASE = 'http://localhost:8080';

const emptyForm = {
  name: '',
  category: '',
  height: '',
  weight: '',
  sprite: '',
  abilities: '',
  region: '',
  classification: '',
  notes: '',
};

async function readApiResponse(response, fallbackMessage) {
  const text = await response.text();
  let payload = text;

  try {
    payload = text ? JSON.parse(text) : {};
  } catch {
    payload = text;
  }

  if (!response.ok) {
    const message = typeof payload === 'string'
      ? payload.trim()
      : payload.error || payload.message || payload.detail;
    throw new Error(message || fallbackMessage);
  }

  return payload;
}

function App() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [accountMode, setAccountMode] = useState('login');
  const [authReady, setAuthReady] = useState(false);
  const [accessToken, setAccessToken] = useState('');
  const [catalog, setCatalog] = useState([]);
  const [catalogOffset, setCatalogOffset] = useState(0);
  const [catalogLimit, setCatalogLimit] = useState(12);
  const [catalogCount, setCatalogCount] = useState(0);
  const [catalogHasNext, setCatalogHasNext] = useState(false);
  const [catalogHasPrevious, setCatalogHasPrevious] = useState(false);
  const [localPokemon, setLocalPokemon] = useState([]);
  const [selectedName, setSelectedName] = useState('pikachu');
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const authHeader = accessToken ? { Authorization: `Bearer ${accessToken}` } : {};

  const setFeedback = (okMessage, errMessage = '') => {
    setMessage(okMessage);
    setError(errMessage);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const registerUser = async (event) => {
    event.preventDefault();
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const payload = await readApiResponse(response, 'Registration failed');

      setAuthReady(false);
      setAccessToken('');
      setLocalPokemon([]);
      setAccountMode('login');
      setFeedback(`Account ${payload.username || username} created. Sign in to continue.`);
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to register user');
    } finally {
      setLoading(false);
    }
  };

  const loginUser = async (event) => {
    event.preventDefault();
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const login = await readApiResponse(response, 'Incorrect username or password.');
      if (!login.accessToken) throw new Error('Login response did not include an access token.');

      const tokenHeader = { Authorization: `Bearer ${login.accessToken}` };
      const recordsResponse = await fetch(`${API_BASE}/api/pokemon/local`, {
        headers: tokenHeader,
      });
      const records = await readApiResponse(recordsResponse, 'Unable to load local Pokémon.');
      setAccessToken(login.accessToken);
      setLocalPokemon(records || []);
      setAuthReady(true);
      setFeedback(`Signed in as ${login.username || username}.`);
      await loadCatalog(0, catalogLimit, login.accessToken);
    } catch (caughtError) {
      setAuthReady(false);
      setAccessToken('');
      setFeedback('', caughtError.message || 'Unable to sign in');
    } finally {
      setLoading(false);
    }
  };

  const logoutUser = () => {
    setAuthReady(false);
    setAccessToken('');
    setLocalPokemon([]);
    setPassword('');
    setFeedback('You have signed out.');
  };

  const loadCatalog = async (requestedOffset = catalogOffset, requestedLimit = catalogLimit, token = accessToken) => {
    try {
      setLoading(true);
      const params = new URLSearchParams({ offset: requestedOffset, limit: requestedLimit });
      const response = await fetch(`${API_BASE}/api/pokemon/catalog?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await readApiResponse(response, 'Unable to load catalog');
      setCatalog(data.results || data.pokemon || []);
      setCatalogOffset(requestedOffset);
      setCatalogCount(Number(data.count) || 0);
      setCatalogHasNext(Boolean(data.next));
      setCatalogHasPrevious(Boolean(data.previous));
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to load catalog');
    } finally {
      setLoading(false);
    }
  };

  const loadLocalPokemon = async () => {
    try {
      const response = await fetch(`${API_BASE}/api/pokemon/local`, {
        headers: authHeader,
      });

      const data = await readApiResponse(response, 'Authentication required to load local Pokémon');
      setLocalPokemon(data || []);
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to load local Pokémon');
    }
  };

  const syncPokemon = async (name) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/pokemon/sync/${encodeURIComponent(name)}`, {
        method: 'POST',
        headers: authHeader,
      });

      const payload = await readApiResponse(response, `Unable to sync ${name}`);

      setFeedback(`Synced ${payload.name || name} successfully.`);
      await loadLocalPokemon();
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to sync Pokémon');
    } finally {
      setLoading(false);
    }
  };

  const loadRemotePokemon = async (name) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/pokemon/catalog/${encodeURIComponent(name)}`, {
        headers: authHeader,
      });
      const payload = await readApiResponse(response, 'Unable to fetch Pokémon');

      setForm({
        name: payload.name || '',
        category: payload.category || '',
        height: payload.height ?? '',
        weight: payload.weight ?? '',
        sprite: payload.sprite || '',
        abilities: Array.isArray(payload.abilities) ? payload.abilities.join(', ') : '',
        region: payload.region || '',
        classification: payload.classification || '',
        notes: payload.notes || '',
      });
      setFeedback(`Loaded ${payload.name || name} details.`);
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to load remote Pokémon');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const persistPokemon = async (event) => {
    event.preventDefault();

    try {
      setLoading(true);
      const payload = {
        name: form.name.trim(),
        category: form.category || null,
        height: form.height === '' ? null : Number(form.height),
        weight: form.weight === '' ? null : Number(form.weight),
        sprite: form.sprite || null,
        abilities: form.abilities
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
        region: form.region || null,
        classification: form.classification || null,
        notes: form.notes || null,
      };

      const method = editingId ? 'PUT' : 'POST';
      const url = editingId
        ? `${API_BASE}/api/pokemon/local/${editingId}`
        : `${API_BASE}/api/pokemon/local`;

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authHeader,
        },
        body: JSON.stringify(payload),
      });

      const data = await readApiResponse(response, 'Unable to save Pokémon');

      setFeedback(`Pokemon ${data.name || form.name} saved successfully.`);
      resetForm();
      await loadLocalPokemon();
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to save Pokémon');
    } finally {
      setLoading(false);
    }
  };

  const editPokemon = (item) => {
    setEditingId(item.id);
    setForm({
      name: item.name || '',
      category: item.category || '',
      height: item.height ?? '',
      weight: item.weight ?? '',
      sprite: item.sprite || '',
      abilities: Array.isArray(item.abilities) ? item.abilities.join(', ') : '',
      region: item.region || '',
      classification: item.classification || '',
      notes: item.notes || '',
    });
  };

  const deletePokemon = async (id) => {
    try {
      setLoading(true);
      const response = await fetch(`${API_BASE}/api/pokemon/local/${id}`, {
        method: 'DELETE',
        headers: authHeader,
      });

      await readApiResponse(response, 'Unable to delete Pokémon');

      setFeedback('Pokemon removed successfully.');
      await loadLocalPokemon();
    } catch (caughtError) {
      setFeedback('', caughtError.message || 'Unable to delete Pokémon');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Pokédex Manager</p>
          <h1>Capture, sync, and manage Pokémon</h1>
        </div>
      </header>

      <main className="layout">
        <section className="panel auth-panel">
          <div className="panel-header">
            <h2>{authReady ? `Signed in as ${username}` : 'Your account'}</h2>
            {authReady && <button type="button" className="secondary" onClick={logoutUser}>Sign out</button>}
          </div>
          {!authReady && (
            <>
              <div className="account-tabs" role="tablist" aria-label="Account action">
                <button type="button" role="tab" aria-selected={accountMode === 'login'} className={accountMode === 'login' ? 'active' : ''} onClick={() => setAccountMode('login')}>Sign in</button>
                <button type="button" role="tab" aria-selected={accountMode === 'register'} className={accountMode === 'register' ? 'active' : ''} onClick={() => setAccountMode('register')}>Register</button>
              </div>
              <form onSubmit={accountMode === 'login' ? loginUser : registerUser}>
                <label>
                  Username
                  <input autoComplete="username" minLength="3" maxLength="40" value={username} onChange={(event) => setUsername(event.target.value)} required />
                </label>
                <label>
                  Password
                  <input autoComplete={accountMode === 'login' ? 'current-password' : 'new-password'} minLength="8" maxLength="72" type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
                </label>
                <button type="submit" disabled={loading}>
                  {loading ? 'Please wait...' : accountMode === 'login' ? 'Sign in' : 'Create account'}
                </button>
              </form>
            </>
          )}
          <p className="status">{authReady ? 'Local collection actions are enabled.' : 'Sign in to manage your local collection.'}</p>
        </section>

        {authReady && <section className="panel catalog-panel">
          <div className="panel-header">
            <h2>Remote catalog</h2>
            <button onClick={loadCatalog} disabled={loading}>Refresh</button>
          </div>

          <div className="catalog-controls">
            <input
              value={selectedName}
              onChange={(event) => setSelectedName(event.target.value)}
              placeholder="Lookup Pokémon"
            />
            <button onClick={() => loadRemotePokemon(selectedName)} disabled={loading}>Load</button>
          </div>

          <div className="catalog-grid">
            {(catalog || []).map((item, index) => (
              <button key={`${item.name || 'pokemon'}-${index}`} className="catalog-card" onClick={() => setSelectedName(item.name || item.name || '')}>
                {item.name || 'Unknown'}
              </button>
            ))}
          </div>

          <div className="catalog-pagination" aria-label="Catalog pagination">
            <label>
              Per page
              <select
                value={catalogLimit}
                disabled={loading}
                onChange={(event) => {
                  const nextLimit = Number(event.target.value);
                  setCatalogLimit(nextLimit);
                  loadCatalog(0, nextLimit);
                }}
              >
                <option value="12">12</option>
                <option value="24">24</option>
                <option value="48">48</option>
              </select>
            </label>
            <p>
              {catalogCount === 0
                ? 'No results'
                : `${catalogOffset + 1}-${Math.min(catalogOffset + catalog.length, catalogCount)} of ${catalogCount} · Page ${Math.floor(catalogOffset / catalogLimit) + 1} of ${Math.ceil(catalogCount / catalogLimit)}`}
            </p>
            <div className="pagination-actions">
              <button
                type="button"
                className="secondary"
                disabled={loading || !catalogHasPrevious}
                onClick={() => loadCatalog(Math.max(0, catalogOffset - catalogLimit))}
              >
                Previous
              </button>
              <button
                type="button"
                className="secondary"
                disabled={loading || !catalogHasNext}
                onClick={() => loadCatalog(catalogOffset + catalogLimit)}
              >
                Next
              </button>
            </div>
          </div>
        </section>}

        {authReady && <section className="panel form-panel">
          <div className="panel-header">
            <h2>{editingId ? 'Edit local Pokémon' : 'Create local Pokémon'}</h2>
            <button type="button" className="secondary" onClick={resetForm}>Clear</button>
          </div>

          <form onSubmit={persistPokemon} className="pokemon-form">
            <label>
              Name
              <input name="name" value={form.name} onChange={handleInputChange} required />
            </label>
            <label>
              Category
              <input name="category" value={form.category} onChange={handleInputChange} />
            </label>
            <div className="two-column">
              <label>
                Height
                <input name="height" type="number" value={form.height} onChange={handleInputChange} />
              </label>
              <label>
                Weight
                <input name="weight" type="number" value={form.weight} onChange={handleInputChange} />
              </label>
            </div>
            <label>
              Sprite URL
              <input name="sprite" value={form.sprite} onChange={handleInputChange} />
            </label>
            <label>
              Abilities
              <input name="abilities" value={form.abilities} onChange={handleInputChange} />
            </label>
            <div className="two-column">
              <label>
                Region
                <input name="region" value={form.region} onChange={handleInputChange} />
              </label>
              <label>
                Classification
                <input name="classification" value={form.classification} onChange={handleInputChange} />
              </label>
            </div>
            <label>
              Notes
              <textarea name="notes" value={form.notes} onChange={handleInputChange} rows="3" />
            </label>

            <div className="form-actions">
              <button type="submit" disabled={loading}>{loading ? 'Saving...' : editingId ? 'Update Pokémon' : 'Create Pokémon'}</button>
              {editingId && (
                <button type="button" className="secondary" onClick={resetForm}>Cancel</button>
              )}
            </div>
          </form>
        </section>}

        {authReady ? <section className="panel local-panel">
          <div className="panel-header">
            <h2>My local collection</h2>
            <button onClick={loadLocalPokemon} disabled={!authReady || loading}>Reload</button>
          </div>

          {localPokemon.length === 0 ? (
            <p className="empty-state">No local Pokémon yet. Sync from the catalog or create one.</p>
          ) : (
            <div className="local-list">
              {localPokemon.map((item) => (
                <article key={item.id} className="local-card">
                  {item.sprite && <img src={item.sprite} alt={item.name} />}
                  <div className="local-card-body">
                    <h3>{item.name}</h3>
                    <p>{item.category || 'Uncategorized'}</p>
                    <div className="meta-row">
                      <span>Height: {item.height ?? '—'}</span>
                      <span>Weight: {item.weight ?? '—'}</span>
                    </div>
                    {item.abilities && item.abilities.length > 0 && (
                      <p>Abilities: {item.abilities.join(', ')}</p>
                    )}
                    <div className="card-actions">
                      <button type="button" onClick={() => syncPokemon(item.name)}>Sync</button>
                      <button type="button" className="secondary" onClick={() => editPokemon(item)}>Edit</button>
                      <button type="button" className="danger" onClick={() => deletePokemon(item.id)}>Delete</button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section> : <section className="panel local-panel">
          <h2>My local collection</h2>
          <p className="empty-state">Sign in to view and manage your Pokémon.</p>
        </section>}
      </main>

      {(message || error) && (
        <div className={`toast ${error ? 'error' : 'success'}`}>
          {error || message}
        </div>
      )}
    </div>
  );
}

export default App;
