const byId = (id) => document.getElementById(id);
let authorization = null;

function showStatus(message, type = 'danger') {
  const status = byId('status');
  status.className = 'alert alert-' + type + ' mb-4';
  status.textContent = message;
  status.hidden = !message;
}

function endSession() {
  authorization = null;
  byId('people').replaceChildren();
  byId('prediction').textContent = '';
  byId('create').reset();
  byId('nationality').reset();
  byId('workspace').hidden = true;
  byId('logout').hidden = true;
  byId('login-panel').hidden = false;
  byId('username').focus();
}

async function api(url, options = {}) {
  const token = authorization;
  if (!token) throw new Error('Entre para acessar o cadastro.');

  const headers = { Authorization: token };
  if (options.body) headers['Content-Type'] = 'application/json';
  const response = await fetch(url, { ...options, headers, credentials: 'omit' });

  if (token !== authorization) throw new Error('A sessão foi encerrada.');
  if (response.status === 401) {
    endSession();
    throw new Error('Usuário ou senha inválidos.');
  }
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    const fallback = { 400: 'Dados inválidos.', 404: 'Pessoa não encontrada.', 409: 'Documento ou e-mail já cadastrado.' };
    throw new Error(error.errors?.join(' • ') || error.detail || error.message ||
      fallback[response.status] || error.error || 'Erro HTTP ' + response.status);
  }
  return response.status === 204 ? null : response.json();
}

function renderPeople(people) {
  const rows = people.map((person) => {
    const row = document.createElement('tr');
    for (const value of [person.id, person.name + ' ' + person.surname, person.document, person.email]) {
      row.insertCell().textContent = value;
    }

    const remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'btn btn-outline-danger btn-sm';
    remove.textContent = 'Excluir';
    remove.setAttribute('aria-label', 'Excluir ' + person.name + ' ' + person.surname);
    remove.addEventListener('click', async () => {
      if (!confirm('Excluir ' + person.name + ' ' + person.surname + '?')) return;
      remove.disabled = true;
      try {
        await api('/list/' + person.id, { method: 'DELETE' });
        row.remove();
        const hasPeople = byId('people').childElementCount > 0;
        byId('empty').hidden = hasPeople;
        byId('people-table').hidden = !hasPeople;
        showStatus('Pessoa excluída.', 'success');
      } catch (error) {
        showStatus(error.message);
      } finally {
        remove.disabled = false;
      }
    });
    row.insertCell().append(remove);
    return row;
  });
  byId('people').replaceChildren(...rows);
  byId('empty').hidden = people.length > 0;
  byId('people-table').hidden = people.length === 0;
}

function onSubmit(id, action) {
  byId(id).addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = event.submitter || event.currentTarget.querySelector('[type="submit"]');
    button.disabled = true;
    showStatus('');
    try {
      await action(event.currentTarget);
    } catch (error) {
      showStatus(error.message);
    } finally {
      button.disabled = false;
    }
  });
}

onSubmit('login', async () => {
  const username = byId('username').value;
  const password = byId('password').value;
  byId('password').value = '';
  const bytes = new TextEncoder().encode(username + ':' + password);
  authorization = 'Basic ' + btoa(Array.from(bytes, (byte) => String.fromCharCode(byte)).join(''));
  try {
    renderPeople(await api('/list'));
    byId('login-panel').hidden = true;
    byId('workspace').hidden = false;
    byId('logout').hidden = false;
    byId('name').focus();
  } catch (error) {
    endSession();
    throw error;
  }
});

byId('logout').addEventListener('click', () => {
  endSession();
  showStatus('');
});

onSubmit('create', async (form) => {
  const body = JSON.stringify(Object.fromEntries(new FormData(form)));
  const person = await api('/registrarName', { method: 'POST', body });
  form.reset();
  byId('person-id').value = person.id;
  try {
    renderPeople(await api('/list'));
    showStatus('Pessoa cadastrada. ID ' + person.id + '.', 'success');
  } catch {
    showStatus('Pessoa cadastrada com ID ' + person.id + '. Não foi possível atualizar a lista.', 'warning');
  }
});

onSubmit('nationality', async () => {
  const result = byId('prediction');
  result.textContent = '';
  const prediction = await api('/findNacionalityByPerson/' + byId('person-id').value);
  result.textContent = prediction.countryCode
    ? prediction.nationality + ' (' + prediction.countryCode + ')'
    : prediction.nationality;
});

byId('refresh').addEventListener('click', async () => {
  const refresh = byId('refresh');
  refresh.disabled = true;
  try {
    renderPeople(await api('/list'));
    showStatus('');
  } catch (error) {
    showStatus(error.message);
  } finally {
    refresh.disabled = false;
  }
});
