// Simple JWT Helper
function getToken() {
  return localStorage.getItem('token');
}

function setSession(token, username, role) {
  localStorage.setItem('token', token);
  localStorage.setItem('username', username);
  localStorage.setItem('role', role);
  document.cookie = 'jwt_token=' + token + '; path=/; max-age=86400';
}

function logout() {
  localStorage.clear();
  document.cookie = 'jwt_token=; path=/; max-age=0';
  window.location.href = '/login';
}

// Fetch with JWT Authorization
async function fetchApi(url, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }

  const response = await fetch(url, { ...options, headers });
  if (response.status === 401) {
    logout();
    throw new Error('Unauthorized');
  }
  return response;
}

// Dashboard Functions
let currentPage = 0;
let pageSize = 10;

async function loadProducts(page = 0) {
  currentPage = page;
  const name = document.getElementById('searchName')?.value.trim() || '';
  const minPrice = document.getElementById('minPrice')?.value || '';
  const maxPrice = document.getElementById('maxPrice')?.value || '';

  const params = new URLSearchParams({
    page: currentPage,
    size: pageSize,
    sortBy: 'id',
    sortDir: 'desc'
  });
  if (name) params.append('name', name);
  if (minPrice) params.append('minPrice', minPrice);
  if (maxPrice) params.append('maxPrice', maxPrice);

  const tbody = document.getElementById('productTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" class="text-center py-3 text-muted">Loading...</td></tr>';

  try {
    const res = await fetchApi('/api/v1/products/search?' + params.toString());
    const result = await res.json();

    if (!res.ok) {
      alert(result.message || 'Failed to load products');
      return;
    }

    const data = result.data;
    const items = data.content || [];

    if (items.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No products found.</td></tr>';
      renderPagination(0, 0);
      return;
    }

    tbody.innerHTML = items.map(p => `
      <tr>
        <td>${p.id}</td>
        <td><strong>${escapeHtml(p.name)}</strong></td>
        <td>${escapeHtml(p.description || '-')}</td>
        <td>$${Number(p.price).toFixed(2)}</td>
        <td>${formatDate(p.createdAt)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-secondary me-1" onclick="openEditModal(${p.id})">Edit</button>
          <button class="btn btn-sm btn-outline-danger" onclick="confirmDelete(${p.id}, '${escapeAttr(p.name)}')">Delete</button>
        </td>
      </tr>
    `).join('');

    renderPagination(data.totalPages, data.pageNumber);
  } catch (err) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-3 text-danger">' + err.message + '</td></tr>';
  }
}

function renderPagination(totalPages, pageNumber) {
  const container = document.getElementById('pagination');
  if (!container) return;

  if (totalPages <= 1) {
    container.innerHTML = '';
    return;
  }

  let html = '';
  html += `<li class="page-item ${pageNumber === 0 ? 'disabled' : ''}">
    <button class="page-link" onclick="loadProducts(${pageNumber - 1})">Prev</button>
  </li>`;

  for (let i = 0; i < totalPages; i++) {
    html += `<li class="page-item ${i === pageNumber ? 'active' : ''}">
      <button class="page-link" onclick="loadProducts(${i})">${i + 1}</button>
    </li>`;
  }

  html += `<li class="page-item ${pageNumber === totalPages - 1 ? 'disabled' : ''}">
    <button class="page-link" onclick="loadProducts(${pageNumber + 1})">Next</button>
  </li>`;

  container.innerHTML = html;
}

// Add/Edit Product
function openAddModal() {
  document.getElementById('productId').value = '';
  document.getElementById('productForm').reset();
  document.getElementById('modalTitle').textContent = 'Add Product';
  new bootstrap.Modal(document.getElementById('productModal')).show();
}

async function openEditModal(id) {
  try {
    const res = await fetchApi('/api/v1/products/' + id);
    const json = await res.json();
    if (res.ok && json.data) {
      const p = json.data;
      document.getElementById('productId').value = p.id;
      document.getElementById('productName').value = p.name;
      document.getElementById('productDescription').value = p.description || '';
      document.getElementById('productPrice').value = p.price;
      document.getElementById('modalTitle').textContent = 'Edit Product';
      new bootstrap.Modal(document.getElementById('productModal')).show();
    }
  } catch (err) {
    alert('Failed to load product details: ' + err.message);
  }
}

async function saveProduct(event) {
  event.preventDefault();
  const id = document.getElementById('productId').value;
  const name = document.getElementById('productName').value.trim();
  const description = document.getElementById('productDescription').value.trim();
  const price = parseFloat(document.getElementById('productPrice').value);

  const isEdit = !!id;
  const url = isEdit ? '/api/v1/products/' + id : '/api/v1/products';
  const method = isEdit ? 'PUT' : 'POST';

  try {
    const res = await fetchApi(url, {
      method: method,
      body: JSON.stringify({ name, description, price })
    });
    const json = await res.json();

    if (res.ok) {
      bootstrap.Modal.getInstance(document.getElementById('productModal')).hide();
      loadProducts(currentPage);
    } else {
      alert(json.message || 'Failed to save product');
    }
  } catch (err) {
    alert(err.message);
  }
}

// Delete
let deleteId = null;
function confirmDelete(id, name) {
  deleteId = id;
  document.getElementById('deleteItemName').textContent = name;
  new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

async function doDelete() {
  if (!deleteId) return;
  try {
    const res = await fetchApi('/api/v1/products/' + deleteId, { method: 'DELETE' });
    if (res.ok) {
      bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
      loadProducts(currentPage);
    } else {
      const json = await res.json();
      alert(json.message || 'Failed to delete product');
    }
  } catch (err) {
    alert(err.message);
  } finally {
    deleteId = null;
  }
}

// Helpers
function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m]);
}

function escapeAttr(str) {
  if (!str) return '';
  return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toISOString().slice(0, 10);
}
