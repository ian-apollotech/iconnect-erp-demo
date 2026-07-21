# Assortment Plan Module — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `PAGES.assortplan` "design pending" stub in `ui-mockup/iconnect-erp.html` with a working Assortment Plan module — a list page plus a 4-step create/duplicate wizard for assigning a SKU list to a Company, Store Cluster, or Store.

**Architecture:** Mirror the existing **Price Event wizard** exactly (`openPriceWizard` → `renderPriceWizard` → `pwStepNBody`/`pwWireStepN`/`pwCapture`/`pwGo`). Add a parallel `AW` state object and `openAssortWizard`/`renderAssortWizard`/`apStepNBody`/… family. Reuse the dependent-scope helper pattern (`initPriceScope` → new `initAssortScope`), the item browse/add pattern (`pwStep2Body`), and duplicate seeding (`priceSeed` + `[data-dup]` → `assortSeed` + `[data-assortdup]`). Reuse shared helpers (`head`, `badge`, `table`, `filterbar`, `noteBox`, `pager`, `btn`, `iAdd`, `iExport`, `BUYERS`, `STORES`, `PRODUCTS`, `pwMoney`).

**Tech Stack:** Single static HTML file with one inline `<script>` (vanilla template-literal rendering). No build, no framework, no git, no test runner.

## Global Constraints

- **Single target file:** `ui-mockup/iconnect-erp.html`. All JS lives in the one `<script>` block ending at line ~1751.
- **No git / no test framework.** Per-task verification = (1) `new Function()` **syntax gate**, (2) **Playwright render gate** (all pages route, 0 console errors except the benign favicon 404), (3) **residual-term grep**.
- **Locked naming (verbatim):** module = `Assortment Plan`; grouping = `Store Cluster`; assignment levels = `Company (all stores)` / `Store Cluster` / `Store`; Assortment Types = `Regular` / `Seasonal` / `Promotional` / `New-Store`; resolution precedence copy = `Store › Store Cluster › Company`.
- **Effectivity = Effective From only.** No end date anywhere.
- **Single-level full replacement** — a plan is a complete SKU list for exactly one level; never a merge/delta.
- **Reuse, don't reinvent:** follow the Price wizard's function shapes and the existing CSS classes (`steps`, `step`, `fg`, `form-grid`, `pw-browse-controls`, `toolbar`, `pw-review`, `tbl-wrap`, `panel`, `foot`). Do not add new CSS.

### Syntax gate command (used in every task)

```bash
node -e "const fs=require('fs');const h=fs.readFileSync('ui-mockup/iconnect-erp.html','utf8');const m=h.match(/<script>([\s\S]*)<\/script>/);new Function(m[1]);console.log('SYNTAX OK');"
```
Expected: `SYNTAX OK` (throws with a line/col on any JS error).

### Playwright render gate (used in every task)

Using the `mcp__playwright__*` tools:
1. `browser_navigate` → `file:///mnt/d/claude-workspace/iconnect-erp/ui-mockup/iconnect-erp.html`
2. `browser_console_messages` → expect **no errors** other than `favicon.ico 404`.
3. Task-specific clicks (below).

---

## Task 1: Data + Nav item + List page

Makes the module visible and navigable. New/Duplicate buttons become live in Tasks 2 and 4.

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — NAV array (~line 227), a new `CLUSTERS` const (after `COSTZONES`, ~line 1001), and `PAGES.assortplan` (replace lines 481–484).

**Interfaces:**
- Produces: `CLUSTERS` (array of `{code,name,co}`); a full `PAGES.assortplan` list page whose rows carry `data-assortdup="AP-000N"`; nav item `id:"assortplan"`.
- Consumes: existing `head`, `noteBox`, `filterbar`, `table`, `badge`, `pager`, `btn`, `iExport`, `iAdd`.

- [ ] **Step 1: Add the `CLUSTERS` dataset** immediately after the `COSTZONES` const (currently ends at line 1001):

```javascript
/* Store Clusters per company (co = index into BUYERS), used by the Assortment Plan target selector */
const CLUSTERS = [
  {code:'CL-URB-L', name:'Urban Large',      co:0},
  {code:'CL-URB-S', name:'Urban Small',      co:0},
  {code:'CL-CONV',  name:'Convenience',      co:0},
  {code:'CL-PROV',  name:'Provincial Mixed', co:0},
  {code:'CL-MK',    name:'Merkado Standard', co:1},
];
```

- [ ] **Step 2: Add the nav item.** In `NAV`, inside the `"Store Grouping"` subhead (line 227), insert between `cluster` and `replenpolicy`:

```javascript
      {id:"cluster",      n:"", label:"Store Cluster",        icon:"layers"},
      {id:"assortplan",   n:"", label:"Assortment Plan",      icon:"book"},
      {id:"replenpolicy", n:"", label:"Replenishment Policy", icon:"refresh"},
```

- [ ] **Step 3: Replace the `PAGES.assortplan` stub** (lines 481–484) with the full list page:

```javascript
PAGES.assortplan = ()=>`
${head('','Assortment Plan','Assign the SKU list a Company, Store Cluster, or Store carries. A store uses its own plan; if none, its Store Cluster’s; otherwise the Company default.', btn('Export','',iExport)+btn('New Assortment Plan','primary',iAdd))}
${noteBox('<b>Single-level, full replacement.</b> Each plan is a complete SKU list for one level. Resolution precedence is <b>Store › Store Cluster › Company</b> — the most specific plan wins; a plan never merges with another level. A newer plan for the same target supersedes the prior one on its effective date.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Plan name or code…'},{label:'Company',html:'<select><option>All Companies</option><option>Puregold Price Club, Inc.</option><option>Ayagold Retailers Inc. (Merkado)</option></select>'},{label:'Level',html:'<select><option>All Levels</option><option>Company</option><option>Store Cluster</option><option>Store</option></select>'},{label:'Status',html:'<select><option>All</option><option>Active</option><option>Scheduled</option><option>Superseded</option><option>Inactive</option></select>'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Plan Name',k:'nm'},{t:'Level',k:'lv'},{t:'Target',k:'tg'},{t:'Company',k:'co'},{t:'Ranged SKUs',k:'sk',num:1},{t:'Effective From',k:'ef'},{t:'Type',k:'ty'},{t:'Status',k:'st'},{t:'',k:'act'}],
    [{code:'<span class="mono">AP-0001</span>',nm:'<span class="row-link">Urban-Large Core Range</span>',lv:badge('blue','Store Cluster'),tg:'CL-URB-L · Urban Large',co:'<span class="mono">PGOLD</span>',sk:'42,500',ef:'2026-06-01',ty:badge('blue','Regular'),st:badge('green','Active'),act:'<button class="btn ghost sm" data-assortdup="AP-0001">Duplicate</button>'},
     {code:'<span class="mono">AP-0002</span>',nm:'<span class="row-link">Convenience Minimart Range</span>',lv:badge('blue','Store Cluster'),tg:'CL-CONV · Convenience',co:'<span class="mono">PGOLD</span>',sk:'6,400',ef:'2026-06-15',ty:badge('blue','Regular'),st:badge('green','Active'),act:'<button class="btn ghost sm" data-assortdup="AP-0002">Duplicate</button>'},
     {code:'<span class="mono">AP-0003</span>',nm:'<span class="row-link">Puregold National Default</span>',lv:badge('gray','Company'),tg:'All Stores',co:'<span class="mono">PGOLD</span>',sk:'12,800',ef:'2026-01-01',ty:badge('blue','Regular'),st:badge('green','Active'),act:'<button class="btn ghost sm" data-assortdup="AP-0003">Duplicate</button>'},
     {code:'<span class="mono">AP-0004</span>',nm:'<span class="row-link">Ortigas Local Add-ins</span>',lv:badge('cyan','Store'),tg:'0102 · Ortigas Ext.',co:'<span class="mono">PGOLD</span>',sk:'320',ef:'2026-07-01',ty:badge('purple','Seasonal'),st:badge('amber','Scheduled'),act:'<button class="btn ghost sm" data-assortdup="AP-0004">Duplicate</button>'}])}</div>
  ${pager(4,4)}
</div>`;
```

- [ ] **Step 4: Syntax gate.** Run the syntax gate command. Expected: `SYNTAX OK`.

- [ ] **Step 5: Residual-term grep.** Run:

```bash
grep -n "Design pending" ui-mockup/iconnect-erp.html
```
Expected: **no matches** (stub text removed).

- [ ] **Step 6: Playwright render gate.**
  - `browser_navigate` to the file.
  - `browser_console_messages` → only favicon 404.
  - Click the sidebar **Assortment Plan** item (`browser_click` on text "Assortment Plan"); confirm the list renders with 4 rows and the nav item is highlighted (`active`).

---

## Task 2: Scope helper + wizard shell + Step 1 (Identity & Scope)

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — add the assortment wizard block after the Cost Event wizard block (after `openCostWizard`/its render, before `const CREATE` at line 1385); wire `openCreate` (line 1446) and `CREATE_BTN` (line 1445).

**Interfaces:**
- Consumes: `CLUSTERS`, `STORES`, `BUYERS`, `head`, `renderSidebar`, `route`.
- Produces: `let AW`; `openAssortWizard(seed)`; `renderAssortWizard()`; `apStep1Body()`; `apWireStep1()`; `apCapture()`; `apGo(n)`; `initAssortScope(root)`. Header shape: `{name, company, level, scope, type, eff, desc}`; item shape: `{code, name, cat}`. Steps 2–4 rendered by placeholder bodies here, replaced in Tasks 3–4.

- [ ] **Step 1: Add `initAssortScope`** (place it just before the new wizard block). Mirrors `initPriceScope` but swaps Price Zone → Store Cluster (`CLUSTERS`):

```javascript
/* ===================== ASSORTMENT PLAN WIZARD ===================== */
function initAssortScope(root){
  const level=root.querySelector('#apLevel'), scope=root.querySelector('#apScope'), company=root.querySelector('#apCompany');
  if(!level||!scope) return;
  function build(){
    const ci=company?Math.max(0,BUYERS.indexOf(company.value)):0;
    const lv=level.value; let opts;
    if(lv.startsWith('Company')) opts=['All Stores (company default)'];
    else if(lv==='Store Cluster') opts=CLUSTERS.filter(z=>z.co===ci).map(z=>`${z.code} · ${z.name}`);
    else opts=STORES.filter(s=>s.co===ci).map(s=>`${s.code} · ${s.name}`);
    scope.innerHTML=(opts.length?opts:['— none for this company —']).map(o=>`<option>${o}</option>`).join('');
    scope.disabled=lv.startsWith('Company');
  }
  level.addEventListener('change',build);
  if(company) company.addEventListener('change',build);
  build();
}
```

- [ ] **Step 2: Add wizard state + open + Step 1 body + wiring**, directly after `initAssortScope`:

```javascript
let AW=null;
function openAssortWizard(seed){
  AW={step:1,
    header: seed&&seed.header ? Object.assign({},seed.header) : {name:'',company:BUYERS[0],level:'Company (all stores)',scope:'All Stores (company default)',type:'Regular',eff:'',desc:''},
    items: seed&&seed.items ? seed.items.map(x=>Object.assign({},x)) : [],
    ref: (seed&&seed.ref) || 'AP-0005 · Draft',
    seededFrom: (seed&&seed.from) || null};
  window.__ret='assortplan';
  renderAssortWizard();
}
function apStep1Body(){
  const h=AW.header;
  const sel=(id,opts,val)=>`<select id="${id}">${opts.map(o=>`<option${o===val?' selected':''}>${o}</option>`).join('')}</select>`;
  return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="form-grid">
    <div class="fg full"><label>Plan Name <span class="req">*</span></label><input id="apName" value="${(h.name||'').replace(/"/g,'&quot;')}"></div>
    <div class="fg"><label>Company <span class="req">*</span></label>${sel('apCompany',BUYERS,h.company)}</div>
    <div class="fg"><label>Assignment Level <span class="req">*</span></label>${sel('apLevel',['Company (all stores)','Store Cluster','Store'],h.level)}</div>
    <div class="fg"><label>Target</label><select id="apScope"></select></div>
    <div class="fg"><label>Assortment Type <span class="req">*</span></label>${sel('apType',['Regular','Seasonal','Promotional','New-Store'],h.type)}</div>
    <div class="fg full"><label>Description</label><textarea id="apDesc" rows="2">${h.desc||''}</textarea></div>
  </div></div>`;
}
function apWireStep1(){
  const root=document.getElementById('main');
  initAssortScope(root);
  const scope=document.getElementById('apScope');
  if(scope&&AW.header.scope){ if(![...scope.options].some(o=>o.value===AW.header.scope)) scope.insertAdjacentHTML('afterbegin',`<option>${AW.header.scope}</option>`); scope.value=AW.header.scope; }
}
function apStep2Body(){ return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="empty">Effectivity — added in Task 3.</div></div>`; }
function apStep3Body(){ return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="empty">SKU List — added in Task 3.</div></div>`; }
function apStep4Body(){ return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="empty">Review — added in Task 4.</div></div>`; }
function apCapture(){
  if(AW.step===1){const g=id=>document.getElementById(id);
    if(g('apName'))AW.header.name=g('apName').value;
    if(g('apCompany'))AW.header.company=g('apCompany').value;
    if(g('apLevel'))AW.header.level=g('apLevel').value;
    if(g('apScope'))AW.header.scope=g('apScope').value;
    if(g('apType'))AW.header.type=g('apType').value;
    if(g('apDesc'))AW.header.desc=g('apDesc').value;
  }
}
function apGo(n){if(n<1||n>4)return;apCapture();AW.step=n;renderAssortWizard();}
function renderAssortWizard(){
  const m=document.getElementById('main');
  const labels=['Identity & Scope','Effectivity','SKU List','Review'];
  const stepbar=`<div class="panel-body"><div class="steps">${labels.map((s,idx)=>{const n=idx+1;const cls=n<AW.step?'step done':(n===AW.step?'step active':'step');return `<div class="${cls}" data-apstep="${n}"><span class="n">${n}</span> ${s}</div>${n<labels.length?'<div class="line"></div>':''}`;}).join('')}</div></div>`;
  const body=AW.step===1?apStep1Body():AW.step===2?apStep2Body():AW.step===3?apStep3Body():apStep4Body();
  const nav=`<div class="foot"><div>${AW.step>1?'<button class="btn" data-apnav="back">‹ Back</button> ':''}<button class="btn" data-apnav="cancel">Cancel</button></div><div class="right">${AW.step<4?'<button class="btn primary" data-apnav="next">Next ›</button>':'<button class="btn primary" data-apnav="submit">Save Assortment Plan</button>'}</div></div>`;
  m.innerHTML=head('','New Assortment Plan','Set identity & scope, effectivity, choose the SKU range, then review and save.','')
    +`<div class="panel"><div class="panel-head"><h3>New Assortment Plan</h3><span class="sub">${AW.ref}${AW.seededFrom?' · duplicated from '+AW.seededFrom:''}</span></div>${stepbar}${body}${nav}</div>`;
  m.scrollTop=0; renderSidebar('assortplan');
  document.querySelectorAll('[data-apstep]').forEach(e=>e.onclick=()=>apGo(+e.dataset.apstep));
  document.querySelectorAll('[data-apnav]').forEach(b=>b.onclick=()=>{const a=b.dataset.apnav;if(a==='cancel'||a==='submit')route('assortplan');else if(a==='back')apGo(AW.step-1);else if(a==='next')apGo(AW.step+1);});
  if(AW.step===1)apWireStep1();
  else if(AW.step===2 && typeof apWireStep2==='function')apWireStep2();
  else if(AW.step===3 && typeof apWireStep3==='function')apWireStep3();
}
```
(The `typeof …==='function'` guards let Steps 2/3 wiring be added later without breaking this task.)

- [ ] **Step 3: Wire the create entrypoints.** In `CREATE_BTN` (line 1445) add the mapping, and in `openCreate` (after the `cost` line, 1448) add the branch:

```javascript
// in CREATE_BTN object — add this key:
'New Assortment Plan':'assort',
```
```javascript
// in openCreate(), right after:  if(key==='cost'){ openCostWizard(); return; }
  if(key==='assort'){ openAssortWizard(); return; }
```

- [ ] **Step 4: Syntax gate.** Run the syntax gate command. Expected: `SYNTAX OK`.

- [ ] **Step 5: Playwright gate.**
  - Navigate; go to **Assortment Plan**; click **New Assortment Plan**.
  - Confirm the wizard renders on **Step 1** with the 4-step bar (`Identity & Scope · Effectivity · SKU List · Review`).
  - Change **Assignment Level** to `Store Cluster` → the **Target** select shows `CL-URB-L …` options; change to `Store` → shows `0102 …`; back to `Company (all stores)` → Target shows `All Stores (company default)` and is disabled.
  - Change **Company** to Ayagold with level `Store Cluster` → Target shows `CL-MK · Merkado Standard`.
  - Click **Next ›** → advances to the Step 2 placeholder. `browser_console_messages` → no errors.

---

## Task 3: Step 2 (Effectivity) + Step 3 (SKU List)

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — replace the `apStep2Body`/`apStep3Body` placeholders from Task 2; add `apWireStep2`, `apWireStep3`, and extend `apCapture` for step 2.

**Interfaces:**
- Consumes: `PRODUCTS`, `AW` (from Task 2), item shape `{code,name,cat}`.
- Produces: `apStep2Body()`, `apWireStep2()`, `apStep3Body()`, `apWireStep3()`; extends `apCapture()` to persist `AW.header.eff`.

- [ ] **Step 1: Replace `apStep2Body` and add `apWireStep2`.** The effectivity step: Effective From (required) + a live Status preview + the supersede note.

```javascript
function apStep2Body(){
  const h=AW.header;
  return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="form-grid">
    <div class="fg"><label>Effective From <span class="req">*</span></label><input type="date" id="apEff" value="${h.eff||''}"></div>
    <div class="fg"><label>Status preview</label><input id="apStatus" readonly value="Draft"></div>
    <div class="fg full"><div class="hint">A newer plan for the same <b>${h.level}</b> target <b>${h.scope||'—'}</b> supersedes this one on its effective date. Two plans for the same target may not share the same start date.</div></div>
  </div></div>`;
}
function apWireStep2(){
  const eff=document.getElementById('apEff'), st=document.getElementById('apStatus');
  if(!eff||!st) return;
  function upd(){ if(!eff.value){st.value='Draft';return;} st.value=(eff.value>'2026-07-20')?'Scheduled':'Active'; }
  eff.addEventListener('input',upd); upd();
}
```

- [ ] **Step 2: Extend `apCapture`** to persist the effective date. Change the `apCapture` function body to add the step-2 branch:

```javascript
function apCapture(){
  if(AW.step===1){const g=id=>document.getElementById(id);
    if(g('apName'))AW.header.name=g('apName').value;
    if(g('apCompany'))AW.header.company=g('apCompany').value;
    if(g('apLevel'))AW.header.level=g('apLevel').value;
    if(g('apScope'))AW.header.scope=g('apScope').value;
    if(g('apType'))AW.header.type=g('apType').value;
    if(g('apDesc'))AW.header.desc=g('apDesc').value;
  } else if(AW.step===2){ const e=document.getElementById('apEff'); if(e)AW.header.eff=e.value; }
}
```

- [ ] **Step 3: Replace `apStep3Body` and add `apWireStep3`.** Selected-SKU table (removable) above a browse/add control (category + search + checkbox + "Add selected") — reuses the `pwStep2Body` pattern over `PRODUCTS`.

```javascript
function apStep3Body(){
  const cats=['All',...Array.from(new Set(PRODUCTS.map(p=>p.cat)))];
  const added=new Set(AW.items.map(i=>i.code));
  const avail=PRODUCTS.filter(p=>!added.has(p.code));
  const browseRows=avail.map(p=>`<tr data-code="${p.code}" data-search="${(p.name+' '+p.code).toLowerCase()}"><td style="text-align:center"><input type="checkbox" class="chk ap-pchk"></td><td><span class="t-title">${p.name}</span> <span class="mono t-sub">${p.code}</span></td><td>${p.cat}</td></tr>`).join('');
  const selRows=AW.items.map((it,i)=>`<tr data-i="${i}"><td><span class="t-title">${it.name}</span> <span class="mono t-sub">${it.code}</span></td><td>${it.cat}</td><td style="text-align:center"><button class="btn ghost sm ap-del" data-i="${i}" title="Remove">✕</button></td></tr>`).join('');
  return `<div class="panel-body flush"><div class="tbl-wrap"><table id="apSelTable"><thead><tr><th>Ranged SKU</th><th>Category</th><th></th></tr></thead><tbody>${selRows||'<tr><td colspan="3" class="t-sub" style="padding:14px">No SKUs yet — add products below.</td></tr>'}</tbody></table></div></div>
  <div class="panel-body" style="border-top:1px solid var(--line)">
    <div class="pw-browse-controls">
      <div class="fg"><label>Category</label><select id="apCat">${cats.map(c=>`<option>${c}</option>`).join('')}</select></div>
      <div class="fg" style="flex:1"><label>Search</label><input id="apSearch" placeholder="Product name or code…"></div>
    </div>
    <div class="tbl-wrap"><table id="apProdTable"><thead><tr><th style="width:40px"></th><th>Product</th><th>Category</th></tr></thead>
      <tbody>${browseRows||'<tr><td colspan="3" class="t-sub" style="padding:16px">All products are already ranged in this plan.</td></tr>'}</tbody></table></div>
  </div>
  <div class="toolbar"><span class="t-sub"><b class="ap-selcount">0</b> selected · <b>${AW.items.length}</b> SKUs ranged</span><button class="btn primary sm" id="apAddSel">Add selected SKUs ›</button></div>`;
}
function apWireStep3(){
  const cat=document.getElementById('apCat'), search=document.getElementById('apSearch'), tbl=document.getElementById('apProdTable');
  if(tbl){
    const filter=()=>{const c=cat.value,q=(search.value||'').toLowerCase();tbl.querySelectorAll('tbody tr[data-code]').forEach(tr=>{const okc=c==='All'||tr.children[2].textContent===c;const okq=!q||tr.dataset.search.includes(q);tr.style.display=(okc&&okq)?'':'none';});};
    const updCount=()=>{document.querySelector('.ap-selcount').textContent=tbl.querySelectorAll('.ap-pchk:checked').length;};
    cat.onchange=filter; search.oninput=filter;
    tbl.addEventListener('change',e=>{if(e.target.classList.contains('ap-pchk'))updCount();});
    document.getElementById('apAddSel').onclick=()=>{
      [...tbl.querySelectorAll('tbody tr[data-code]')].forEach(tr=>{const cb=tr.querySelector('.ap-pchk');if(cb&&cb.checked){const code=tr.dataset.code;const p=PRODUCTS.find(x=>x.code===code);if(p&&!AW.items.some(i=>i.code===code))AW.items.push({code:p.code,name:p.name,cat:p.cat});}});
      renderAssortWizard();
    };
  }
  const selT=document.getElementById('apSelTable');
  if(selT) selT.addEventListener('click',e=>{const b=e.target.closest('.ap-del');if(b){AW.items.splice(+b.dataset.i,1);renderAssortWizard();}});
}
```

- [ ] **Step 4: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 5: Playwright gate.**
  - New Assortment Plan → Step 1 (fill Plan Name, keep level `Store Cluster`, pick a target, Type `Seasonal`) → Next.
  - Step 2: pick an **Effective From** in the future (e.g. 2026-08-01) → Status preview shows `Scheduled`; pick a past date (2026-06-01) → `Active`. Next.
  - Step 3: **Category** filter and **Search** narrow the browse table; check 2–3 products, click **Add selected SKUs ›** → they move to the top Ranged-SKU table and the "SKUs ranged" count updates; click a row's **✕** → it returns to browse. `browser_console_messages` → no errors.

---

## Task 4: Step 4 (Review) + Submit + Duplicate

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — replace the `apStep4Body` placeholder; add `apRev`, `apAffected`, `ASSORT_SEEDS`, `assortSeed`; bind `[data-assortdup]` in `route()` (near line 1733).

**Interfaces:**
- Consumes: `AW`, `PRODUCTS`, `BUYERS`, `openAssortWizard`.
- Produces: `apStep4Body()`, `apRev(k,v)`, `apAffected(h)`, `ASSORT_SEEDS`, `assortSeed(code)`; `route()` binds Duplicate buttons.

- [ ] **Step 1: Replace `apStep4Body` and add helpers.** Review summary + ranged-SKU table.

```javascript
function apRev(k,v){return `<div><div class="k">${k}</div><div class="v">${v}</div></div>`;}
function apAffected(h){
  if(h.level.startsWith('Company')) return 'All stores (company default)';
  if(h.level==='Store Cluster') return '1 cluster';
  return '1 store';
}
function apStep4Body(){
  const h=AW.header;
  const rows=AW.items.map(it=>`<tr><td><span class="t-title">${it.name}</span> <span class="mono t-sub">${it.code}</span></td><td>${it.cat}</td></tr>`).join('');
  return `<div class="panel-body" style="border-top:1px solid var(--line)"><div class="pw-review">
      ${apRev('Plan Name',h.name||'—')}
      ${apRev('Company',h.company)}
      ${apRev('Assignment Level',h.level)}
      ${apRev('Target',h.scope||'—')}
      ${apRev('Assortment Type',h.type)}
      ${apRev('Effective From',h.eff||'—')}
      ${apRev('Affected Stores',apAffected(h))}
      ${apRev('Ranged SKUs',AW.items.length+' SKU(s)')}
    </div></div>
    <div class="panel-body flush"><div class="tbl-wrap"><table><thead><tr><th>Ranged SKU</th><th>Category</th></tr></thead><tbody>${rows||'<tr><td colspan="2" class="t-sub" style="padding:14px">No SKUs ranged.</td></tr>'}</tbody></table></div></div>`;
}
```

- [ ] **Step 2: Add duplicate seeds + `assortSeed`.** Place after `apStep4Body`. Codes match the list rows in Task 1; `eff` is cleared so each duplicate gets a fresh effective date.

```javascript
const ASSORT_SEEDS = {
  'AP-0001':{name:'Urban-Large Core Range',      company:0,level:'Store Cluster',        scope:'CL-URB-L · Urban Large',       type:'Regular', items:['100234','200145','300102','300140','400210','500301','600401']},
  'AP-0002':{name:'Convenience Minimart Range',  company:0,level:'Store Cluster',        scope:'CL-CONV · Convenience',        type:'Regular', items:['100234','100311','200188','500301','500312']},
  'AP-0003':{name:'Puregold National Default',   company:0,level:'Company (all stores)', scope:'All Stores (company default)', type:'Regular', items:['100234','200145','300102','400210','500301','700501']},
  'AP-0004':{name:'Ortigas Local Add-ins',       company:0,level:'Store',                scope:'0102 · Ortigas Ext.',          type:'Seasonal',items:['200145','300155','500330']},
};
function assortSeed(code){
  const s=ASSORT_SEEDS[code]; if(!s) return null;
  return {from:code, ref:'New (copy of '+code+') · Draft',
    header:{name:s.name+' (copy)',company:BUYERS[s.company],level:s.level,scope:s.scope,type:s.type,eff:''},
    items:s.items.map(c=>{const p=PRODUCTS.find(x=>x.code===c);return {code:p.code,name:p.name,cat:p.cat};})};
}
```

- [ ] **Step 3: Bind Duplicate buttons in `route()`.** After the existing `data-costdup` binding (line 1733), add:

```javascript
  document.querySelectorAll('#main [data-assortdup]').forEach(b=>b.onclick=()=>openAssortWizard(assortSeed(b.dataset.assortdup)));
```

- [ ] **Step 4: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 5: Full Playwright gate.**
  - **New flow:** New Assortment Plan → fill Steps 1–3 → Step 4 shows the review summary (Plan Name, Level, Target, Type, Effective From, Affected Stores, Ranged SKUs count) and the SKU table. Click **Save Assortment Plan** → returns to the Assortment Plan list.
  - **Duplicate flow:** on the list, click **Duplicate** on `AP-0001` → wizard opens on Step 1 with Name `Urban-Large Core Range (copy)`, level `Store Cluster`, target `CL-URB-L · Urban Large`, type `Regular`; the sub-header reads `duplicated from AP-0001`. Advance to Step 3 → the 7 seeded SKUs are pre-listed in the Ranged-SKU table. Remove one, add one → count reflects the change. Step 2 Effective From starts empty (must set a new date).
  - `browser_console_messages` → only favicon 404.

- [ ] **Step 6: Final residual grep.** Run:

```bash
grep -c "data-assortdup\|openAssortWizard\|PAGES.assortplan\|initAssortScope" ui-mockup/iconnect-erp.html
```
Expected: a nonzero count (feature wired); and re-run the Task 1 `Design pending` grep → still no matches.

---

## Self-Review

**Spec coverage** (`docs/superpowers/specs/2026-07-20-assortment-plan-design.md`):
- §5.1 Nav placement → Task 1 Step 2. ✅
- §5.2 List page (columns, filters, Export/New, Duplicate, noteBox) → Task 1 Step 3. ✅
- §5.3 4-step wizard (Identity&Scope / Effectivity / SKU List / Review), dependent target picker → Tasks 2–4. ✅
- §5.4 Duplicate seeding → Task 4 Steps 2–3. ✅
- §4 Fields: Code (ref), Name, Description, Company, Level, Target, Type, Effective From, Status preview, derived Ranged-SKU + Affected-store counts → Tasks 2–4. ✅
- §2/§4 Effectivity from-only, single-level full replacement, precedence copy → list noteBox + Step 2 note + no end-date field. ✅
- §7 Verification (syntax gate, Playwright, grep) → every task. ✅
- Deferred (Resolved viewer, delta model, Season field, Effective-To) → correctly **absent**. ✅

**Placeholder scan:** the only intentional placeholders are the Task-2 `apStep2/3/4Body` stubs, each explicitly replaced in Tasks 3–4 with full code. No other TODO/TBD. ✅

**Type consistency:** header shape `{name,company,level,scope,type,eff,desc}` and item shape `{code,name,cat}` are identical across `openAssortWizard`, `apStep1Body`, `apCapture`, `apStepN`, and `assortSeed`. Level string `'Company (all stores)'` matches `initAssortScope`'s `lv.startsWith('Company')` and `'Store Cluster'` its `lv==='Store Cluster'`. Element ids (`apName/apCompany/apLevel/apScope/apType/apDesc/apEff/apStatus/apCat/apSearch/apProdTable/apSelTable/apAddSel`) are consistent between body and wiring. `[data-assortdup]` matches between Task 1 rows and Task 4 binding. ✅
