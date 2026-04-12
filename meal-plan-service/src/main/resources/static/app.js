const API_BASE = '/meal-plans';
let currentEtag = null;
let isEditing = false;
let availableFoods = [];
let foodCollectionEtag = null;

// DOM Elements
const statusMessage = document.getElementById('statusMessage');
const httpStatus = document.getElementById('httpStatus');
const etagDisplay = document.getElementById('etagDisplay');
const foodHttpStatus = document.getElementById('foodHttpStatus');
const foodEtagDisplay = document.getElementById('foodEtagDisplay');
const tableBody = document.querySelector('#mealPlanTable tbody');
const refreshBtn = document.getElementById('refreshBtn');
const mealPlanForm = document.getElementById('mealPlanForm');
const editForm = document.getElementById('editForm');
const submitBtn = document.getElementById('submitBtn');
const editSubmitBtn = document.getElementById('editSubmitBtn');
const editCancelBtn = document.getElementById('editCancelBtn');

function updateStatus(text, level = 'ok', statusCode = null) {
    statusMessage.textContent = text;
    statusMessage.className = 'status-message ' + (level || 'ok');

    if (statusCode) {
        httpStatus.textContent = `HTTP ${statusCode}`;
        httpStatus.className = `http-badge status-${statusCode}`;
    }
}

function updateEtagDisplay(etag) {
    if (etag) {
        etagDisplay.textContent = `ETag: ${etag.substring(0, 30)}...`;
        etagDisplay.title = `Full ETag: ${etag}`;
    } else {
        etagDisplay.textContent = '';
    }
}

function updateFoodCacheDisplay(statusCode, etag) {
    if (statusCode) {
        foodHttpStatus.textContent = `HTTP ${statusCode}`;
        foodHttpStatus.className = `http-badge status-${statusCode}`;
    }

    if (etag) {
        foodEtagDisplay.textContent = `ETag: ${etag.substring(0, 30)}...`;
        foodEtagDisplay.title = `Full ETag: ${etag}`;
    } else {
        foodEtagDisplay.textContent = '';
        foodEtagDisplay.title = '';
    }
}

function renderTable(plans) {
    tableBody.innerHTML = '';

    if (!Array.isArray(plans) || plans.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="7" class="empty-state">No meal plans found</td>';
        tableBody.appendChild(row);
        return;
    }

    plans.forEach(plan => {
        const row = document.createElement('tr');
        row.classList.add('clickable-row');
        row.dataset.id = plan.id;
        row.innerHTML = `
            <td>${plan.id}</td>
            <td>${escapeHtml(plan.userName)}</td>
            <td>${escapeHtml(plan.mealType)}</td>
            <td>${plan.foodId}</td>
            <td>${plan.servings}</td>
            <td>${plan.planDate}</td>
            <td>
                <button type="button" data-action="view" data-id="${plan.id}" class="btn-tbl btn-view">View</button>
                <button type="button" data-action="edit" data-id="${plan.id}" class="btn-tbl btn-edit">Edit</button>
                <button type="button" data-action="delete" data-id="${plan.id}" class="btn-tbl btn-del">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function escapeHtml(val) {
    if (val == null) return '';
    return String(val).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

async function loadMealPlans({ refresh = false, showStatus = true } = {}) {
    refreshBtn.disabled = true;
    try {
        const headers = {};
        if (currentEtag && !refresh) {
            headers['If-None-Match'] = currentEtag;
        }

        const response = await fetch(API_BASE, { headers });

        if (response.status === 304) {
            if (showStatus) {
                updateStatus('No changes. Data is fresh (304 Not Modified).', 'warn', 304);
            }
            updateEtagDisplay(currentEtag);
            refreshBtn.disabled = false;
            return;
        }

        if (!response.ok) {
            if (showStatus) {
                updateStatus(`Request failed: ${response.status} ${response.statusText}`, 'error', response.status);
            }
            refreshBtn.disabled = false;
            return;
        }

        const data = await response.json();
        currentEtag = response.headers.get('ETag');
        renderTable(data);
        updateEtagDisplay(currentEtag);
        if (showStatus) {
            updateStatus(`Loaded ${data.length} meal plans. Fresh data retrieved.`, 'ok', 200);
        }
    } catch (err) {
        if (showStatus) {
            updateStatus('Error: ' + err.message, 'error');
        }
    } finally {
        refreshBtn.disabled = false;
    }
}

function setFormForAdd() {
    isEditing = false;
    mealPlanForm.style.display = 'block';
    document.getElementById('editFormWrapper').classList.add('hidden');
    mealPlanForm.reset();
    document.getElementById('mealPlanId').value = '';
}

function setFormForEdit(plan) {
    isEditing = true;
    mealPlanForm.style.display = 'none';
    document.getElementById('editFormWrapper').classList.remove('hidden');
    document.getElementById('editMealPlanId').value = plan.id;
    document.getElementById('editUserName').value = plan.userName;
    document.getElementById('editMealType').value = plan.mealType;
    populateFoodSelect(document.getElementById('editFoodId'), plan.foodId);
    document.getElementById('editServings').value = plan.servings;
    document.getElementById('editPlanDate').value = plan.planDate;

    // Scroll to form
    document.getElementById('editFormWrapper').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Handle Add Form Submission
mealPlanForm.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const payload = {
        userName: document.getElementById('userName').value.trim(),
        mealType: document.getElementById('mealType').value.trim(),
        foodId: Number(document.getElementById('foodId').value),
        servings: Number(document.getElementById('servings').value),
        planDate: document.getElementById('planDate').value
    };

    if (!payload.userName || !payload.mealType || !payload.planDate) {
        updateStatus('Please fill in all fields.', 'error');
        return;
    }

    try {
        submitBtn.disabled = true;
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await response.text();
            updateStatus(`Failed to create meal plan: ${response.status}`, 'error', response.status);
            return;
        }

        const result = await response.json();
        updateStatus(`Meal plan created successfully (ID: ${result.id})`, 'ok', response.status);
        setFormForAdd();
        await loadMealPlans({ refresh: true, showStatus: false });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    } finally {
        submitBtn.disabled = false;
    }
});

// Handle Edit Form Submission
editForm.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const id = document.getElementById('editMealPlanId').value;
    const payload = {
        userName: document.getElementById('editUserName').value.trim(),
        mealType: document.getElementById('editMealType').value.trim(),
        foodId: Number(document.getElementById('editFoodId').value),
        servings: Number(document.getElementById('editServings').value),
        planDate: document.getElementById('editPlanDate').value
    };

    if (!payload.userName || !payload.mealType || !payload.planDate) {
        updateStatus('Please fill in all fields.', 'error');
        return;
    }

    try {
        editSubmitBtn.disabled = true;
        const response = await fetch(`${API_BASE}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const message = await response.text();
            updateStatus(`Failed to update meal plan: ${response.status}`, 'error', response.status);
            return;
        }

        const result = await response.json();
        updateStatus(`Meal plan updated successfully (ID: ${result.id})`, 'ok', response.status);
        setFormForAdd();
        await loadMealPlans({ refresh: true, showStatus: false });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    } finally {
        editSubmitBtn.disabled = false;
    }
});

// Handle Edit Cancel Button
editCancelBtn.addEventListener('click', () => setFormForAdd());

// Handle Table Button Clicks
tableBody.addEventListener('click', (ev) => {
    const viewBtn = ev.target.closest('button[data-action="view"]');
    if (viewBtn) {
        const id = Number(viewBtn.dataset.id);
        loadMealPlanDetail(id);
        return;
    }

    const deleteBtn = ev.target.closest('button[data-action="delete"]');
    if (deleteBtn) {
        const id = Number(deleteBtn.dataset.id);
        const row = deleteBtn.closest('tr');
        const name = row.children[1].textContent;
        if (confirm(`Delete meal plan for "${name}" (ID: ${id})?`)) {
            deleteMealPlan(id);
        }
        return;
    }

    const editBtn = ev.target.closest('button[data-action="edit"]');
    if (!editBtn) return;

    const id = Number(editBtn.dataset.id);
    const row = editBtn.closest('tr');

    const plan = {
        id,
        userName: row.children[1].textContent,
        mealType: row.children[2].textContent,
        foodId: Number(row.children[3].textContent),
        servings: Number(row.children[4].textContent),
        planDate: row.children[5].textContent,
    };

    setFormForEdit(plan);
});

async function deleteMealPlan(id) {
    try {
        const response = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            updateStatus(`Failed to delete meal plan: ${response.status}`, 'error', response.status);
            return;
        }
        updateStatus(`Meal plan #${id} deleted.`, 'ok', response.status);
        detailSection.classList.add('hidden');
        currentEtag = null;
        await loadMealPlans({ refresh: true, showStatus: false });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    }
}

async function deleteAllMealPlans() {
    try {
        const response = await fetch(API_BASE, { method: 'DELETE' });
        if (!response.ok) {
            updateStatus(`Failed to delete all: ${response.status}`, 'error', response.status);
            return;
        }
        updateStatus('All meal plans deleted.', 'ok', response.status);
        detailSection.classList.add('hidden');
        currentEtag = null;
        await loadMealPlans({ refresh: true, showStatus: false });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    }
}

// Delete All button
const deleteAllBtn = document.getElementById('deleteAllBtn');
deleteAllBtn.addEventListener('click', () => {
    if (confirm('Delete ALL meal plans? This cannot be undone.')) {
        deleteAllMealPlans();
    }
});

// ===== Food selector =====
async function fetchFoodsCollection({ refresh = false } = {}) {
    const headers = {};
    if (foodCollectionEtag && !refresh) {
        headers['If-None-Match'] = foodCollectionEtag;
    }

    const response = await fetch(`${API_BASE}/foods`, { headers });

    if (response.status === 304) {
        updateFoodCacheDisplay(304, foodCollectionEtag);
        return availableFoods;
    }

    if (!response.ok) {
        throw new Error(`Failed to load foods: HTTP ${response.status}`);
    }

    availableFoods = await response.json();
    foodCollectionEtag = response.headers.get('ETag');
    updateFoodCacheDisplay(response.status, foodCollectionEtag);
    return availableFoods;
}

async function refreshFoodsView({ refresh = false } = {}) {
    try {
        const foods = await fetchFoodsCollection({ refresh });
        populateFoodSelect(document.getElementById('foodId'));
        populateFoodSelect(document.getElementById('editFoodId'));
        renderFoodTable(foods);
    } catch (e) {
        console.error('Error loading foods:', e);
        updateStatus('Failed to load foods from nutrition service.', 'error');
    }
}

function populateFoodSelect(selectEl, selectedId) {
    selectEl.innerHTML = '<option value="">-- Choose a food --</option>';
    availableFoods.forEach(food => {
        const opt = document.createElement('option');
        opt.value = food.id;
        opt.textContent = `${food.foodName} (${food.caloriesPerServing} kcal | P:${food.protein}g C:${food.carbs}g F:${food.fat}g)`;
        if (selectedId && food.id === selectedId) opt.selected = true;
        selectEl.appendChild(opt);
    });
}

// Detail panel elements
const detailSection = document.getElementById('detailSection');
const detailContent = document.getElementById('detailContent');
const closeDetailBtn = document.getElementById('closeDetailBtn');

closeDetailBtn.addEventListener('click', () => {
    detailSection.classList.add('hidden');
});

async function loadMealPlanDetail(id) {
    detailSection.classList.remove('hidden');
    detailContent.innerHTML = '<p class="loading">Loading nutrition details...</p>';
    detailSection.scrollIntoView({ behavior: 'smooth', block: 'start' });

    try {
        const response = await fetch(`${API_BASE}/${id}`);

        if (!response.ok) {
            if (response.status === 503) {
                detailContent.innerHTML = '<p class="detail-error">Nutrition service is unavailable. Please try again later.</p>';
            } else {
                detailContent.innerHTML = `<p class="detail-error">Failed to load details (HTTP ${response.status}).</p>`;
            }
            return;
        }

        const data = await response.json();
        renderDetail(data);
    } catch (err) {
        detailContent.innerHTML = `<p class="detail-error">Error: ${escapeHtml(err.message)}</p>`;
    }
}

function renderDetail(plan) {
    const totalCalories = (plan.caloriesPerServing || 0) * (plan.servings || 1);
    const totalProtein = (plan.protein || 0) * (plan.servings || 1);
    const totalCarbs = (plan.carbs || 0) * (plan.servings || 1);
    const totalFat = (plan.fat || 0) * (plan.servings || 1);

    detailContent.innerHTML = `
        <div class="detail-grid">
            <div class="detail-card">
                <h3>Meal Plan</h3>
                <dl>
                    <dt>User</dt><dd>${escapeHtml(plan.userName)}</dd>
                    <dt>Meal Type</dt><dd>${escapeHtml(plan.mealType)}</dd>
                    <dt>Date</dt><dd>${escapeHtml(plan.planDate)}</dd>
                    <dt>Servings</dt><dd>${plan.servings}</dd>
                </dl>
            </div>
            <div class="detail-card">
                <h3>Food: ${escapeHtml(plan.foodName)}</h3>
                <span class="category-badge">${escapeHtml(plan.category)}</span>
                <dl>
                    <dt>Calories / serving</dt><dd>${plan.caloriesPerServing} kcal</dd>
                    <dt>Protein / serving</dt><dd>${plan.protein}g</dd>
                    <dt>Carbs / serving</dt><dd>${plan.carbs}g</dd>
                    <dt>Fat / serving</dt><dd>${plan.fat}g</dd>
                </dl>
            </div>
            <div class="detail-card totals-card">
                <h3>Totals (${plan.servings} serving${plan.servings > 1 ? 's' : ''})</h3>
                <div class="totals-grid">
                    <div class="total-item"><span class="total-value">${totalCalories}</span><span class="total-label">kcal</span></div>
                    <div class="total-item"><span class="total-value">${totalProtein}g</span><span class="total-label">protein</span></div>
                    <div class="total-item"><span class="total-value">${totalCarbs}g</span><span class="total-label">carbs</span></div>
                    <div class="total-item"><span class="total-value">${totalFat}g</span><span class="total-label">fat</span></div>
                </div>
            </div>
        </div>
    `;
}

// Handle Refresh Button Click
refreshBtn.addEventListener('click', () => loadMealPlans());

// Initialize
window.addEventListener('DOMContentLoaded', () => {
    setFormForAdd();
    refreshFoodsView();
    loadMealPlans();
});

// ===== FOOD MANAGEMENT =====
const FOODS_API = '/meal-plans/foods';
const foodTableBody = document.querySelector('#foodTable tbody');
const addFoodForm = document.getElementById('addFoodForm');
const editFoodForm = document.getElementById('editFoodForm');
const editFoodWrapper = document.getElementById('editFoodWrapper');
const cancelEditFoodBtn = document.getElementById('cancelEditFoodBtn');

function renderFoodTable(foods) {
    foodTableBody.innerHTML = '';
    if (!Array.isArray(foods) || foods.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="6" class="empty-state">No foods found</td>';
        foodTableBody.appendChild(row);
        return;
    }
    foods.forEach(food => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${food.id}</td>
            <td>${escapeHtml(food.foodName)}</td>
            <td><span class="category-badge">${escapeHtml(food.category)}</span></td>
            <td>${food.caloriesPerServing} kcal</td>
            <td>${food.protein}g / ${food.carbs}g / ${food.fat}g</td>
            <td>
                <button type="button" data-action="editFood" data-id="${food.id}" class="btn-tbl btn-edit">Edit</button>
                <button type="button" data-action="deleteFood" data-id="${food.id}" class="btn-tbl btn-del">Delete</button>
            </td>
        `;
        foodTableBody.appendChild(row);
    });
}

async function loadFoodsTable() {
    try {
        const foods = await fetchFoodsCollection();
        renderFoodTable(foods);
    } catch (e) {
        updateStatus('Failed to load foods from nutrition service.', 'error');
        console.error('Error loading foods table:', e);
    }
}

// Add Food
addFoodForm.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const payload = {
        foodName: document.getElementById('foodName').value.trim(),
        category: document.getElementById('foodCategory').value.trim(),
        caloriesPerServing: Number(document.getElementById('foodCalories').value),
        protein: Number(document.getElementById('foodProtein').value),
        carbs: Number(document.getElementById('foodCarbs').value),
        fat: Number(document.getElementById('foodFat').value)
    };
    if (!payload.foodName || !payload.category) {
        updateStatus('Please fill in all food fields.', 'error');
        return;
    }
    try {
        document.getElementById('addFoodBtn').disabled = true;
        const response = await fetch(FOODS_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            updateStatus(`Failed to create food: ${response.status}`, 'error', response.status);
            return;
        }
        const created = await response.json();
        updateStatus(`Food "${created.foodName}" created (ID: ${created.id})`, 'ok', response.status);
        addFoodForm.reset();
        foodCollectionEtag = null;
        await refreshFoodsView({ refresh: true });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    } finally {
        document.getElementById('addFoodBtn').disabled = false;
    }
});

// Edit Food - populate form
function showEditFoodForm(food) {
    addFoodForm.style.display = 'none';
    editFoodWrapper.classList.remove('hidden');
    document.getElementById('editFoodIdField').value = food.id;
    document.getElementById('editFoodName').value = food.foodName;
    document.getElementById('editFoodCategory').value = food.category;
    document.getElementById('editFoodCalories').value = food.caloriesPerServing;
    document.getElementById('editFoodProtein').value = food.protein;
    document.getElementById('editFoodCarbs').value = food.carbs;
    document.getElementById('editFoodFat').value = food.fat;
    editFoodWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function hideEditFoodForm() {
    addFoodForm.style.display = 'block';
    editFoodWrapper.classList.add('hidden');
}

cancelEditFoodBtn.addEventListener('click', hideEditFoodForm);

// Edit Food - submit
editFoodForm.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const id = document.getElementById('editFoodIdField').value;
    const payload = {
        foodName: document.getElementById('editFoodName').value.trim(),
        category: document.getElementById('editFoodCategory').value.trim(),
        caloriesPerServing: Number(document.getElementById('editFoodCalories').value),
        protein: Number(document.getElementById('editFoodProtein').value),
        carbs: Number(document.getElementById('editFoodCarbs').value),
        fat: Number(document.getElementById('editFoodFat').value)
    };
    if (!payload.foodName || !payload.category) {
        updateStatus('Please fill in all food fields.', 'error');
        return;
    }
    try {
        document.getElementById('editFoodBtn').disabled = true;
        const response = await fetch(`${FOODS_API}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            updateStatus(`Failed to update food: ${response.status}`, 'error', response.status);
            return;
        }
        const updated = await response.json();
        updateStatus(`Food "${updated.foodName}" updated.`, 'ok', response.status);
        hideEditFoodForm();
        foodCollectionEtag = null;
        await refreshFoodsView({ refresh: true });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    } finally {
        document.getElementById('editFoodBtn').disabled = false;
    }
});

// Delete Food
async function deleteFood(id) {
    try {
        const response = await fetch(`${FOODS_API}/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            updateStatus(`Failed to delete food: ${response.status}`, 'error', response.status);
            return;
        }
        updateStatus(`Food #${id} deleted.`, 'ok', response.status);
        foodCollectionEtag = null;
        await refreshFoodsView({ refresh: true });
    } catch (e) {
        updateStatus('Error: ' + e.message, 'error');
    }
}

// Food table button clicks
foodTableBody.addEventListener('click', async (ev) => {
    const editBtn = ev.target.closest('button[data-action="editFood"]');
    if (editBtn) {
        const id = Number(editBtn.dataset.id);
        try {
            const response = await fetch(`${FOODS_API}/${id}`);
            if (!response.ok) {
                updateStatus('Failed to fetch food details.', 'error', response.status);
                return;
            }
            const food = await response.json();
            showEditFoodForm(food);
        } catch (e) {
            updateStatus('Error: ' + e.message, 'error');
        }
        return;
    }

    const deleteBtn = ev.target.closest('button[data-action="deleteFood"]');
    if (deleteBtn) {
        const id = Number(deleteBtn.dataset.id);
        const row = deleteBtn.closest('tr');
        const name = row.children[1].textContent;
        if (confirm(`Delete food "${name}" (ID: ${id})? Meal plans using this food will lose their nutrition data.`)) {
            deleteFood(id);
        }
    }
});
