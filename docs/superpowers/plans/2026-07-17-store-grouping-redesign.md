# Store Grouping Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Retire the single hardcoded "Zone" page in `ui-mockup/iconnect-erp.html` and replace it with four purpose-named store-grouping master pages — Price Zone, Cost Zone, Store Cluster, Replenishment Policy — built on a two-layer split, with all downstream references repointed.

**Architecture:** Single-file vanilla-JS HTML mockup. UI is a set of `PAGES.<id> = () => \`…html…\`` template functions rendered by `route(id)`; the left nav is data in the `NAV` array rendered by `renderSidebar`; "New …" modals are data in the `CREATE` object rendered by `openCreate`/`createForm` and wired by the `CREATE_BTN` map. This plan adds four new `PAGES.*` + `CREATE.*` entries, one nav sub-group, one stub page, a generic `data-route` click binding, and edits existing price/cost/RBAC/replen references.

**Tech Stack:** HTML + vanilla JS (no framework, no build). No test runner. Not a git repo.

## Global Constraints

- **Single file only:** all edits are inside `ui-mockup/iconnect-erp.html`. Do not add new files (except this plan's own docs).
- **No git:** there is no repository. "Checkpoint" = save the file; do not run `git`.
- **Design reference:** JDA / Blue Yonder + `docs/puregold/Puregold JDA ERP_1.pdf` only. Spec: `docs/superpowers/specs/2026-07-17-store-grouping-redesign-design.md`.
- **GateGuard hooks active:** state the required facts before the first `Bash` command and before every `Write`/`Edit` (the harness will prompt; comply).
- **Naming (verbatim):** `Price Zone` · `Cost Zone` · `Store Cluster` · `Replenishment Policy`; assortment Layer-2 object = `Assortment Plan`. Never reintroduce "Zone Group", "Exclusive/Overlapping", "Replenishment Profile/Rule", or "Assortment Zone".
- **Verification per task (replaces TDD):**
  - **Syntax gate (run after every edit):**
    ```bash
    node -e "const fs=require('fs');const h=fs.readFileSync('ui-mockup/iconnect-erp.html','utf8');const m=h.match(/<script>([\s\S]*)<\/script>/);if(!m){throw new Error('no script block')};new Function(m[1]);console.log('SYNTAX OK');"
    ```
    Expected output: `SYNTAX OK`.
  - **Render gate (Playwright, final task):** open the file, `route()` each page id, assert no console errors beyond the benign `favicon.ico 404`.

---

### Task 1: Add the "Store Grouping" nav sub-group; drop the "Zone" nav item

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` (the `NAV` array, ~lines 197-200)

**Interfaces:**
- Produces: nav item ids `pricezone`, `costzone`, `cluster`, `replenpolicy` (consumed by `route`/`PAGES` in later tasks). Removes id `zone`.

- [ ] **Step 1: Replace the "Store Branch Maintenance" sub-group block.** Find:

```javascript
    {subhead:"Store Branch Maintenance", items:[
      {id:"zone",    n:"",  label:"Zone", icon:"mappin"},
      {id:"branch",  n:"2", label:"Branch", icon:"store"},
    ]},
```

Replace with:

```javascript
    {subhead:"Store Branch Maintenance", items:[
      {id:"branch",  n:"2", label:"Branch", icon:"store"},
    ]},
    {subhead:"Store Grouping", items:[
      {id:"pricezone",    n:"", label:"Price Zone",           icon:"tag"},
      {id:"costzone",     n:"", label:"Cost Zone",            icon:"coins"},
      {id:"cluster",      n:"", label:"Store Cluster",        icon:"layers"},
      {id:"replenpolicy", n:"", label:"Replenishment Policy", icon:"refresh"},
    ]},
```

(All four icons — `tag`, `coins`, `layers`, `refresh` — already exist in `ICONS`; no icon additions needed.)

- [ ] **Step 2: Syntax gate.** Run the syntax command from Global Constraints. Expected: `SYNTAX OK`.

- [ ] **Step 3: Checkpoint.** Save. Nav now shows four grouping items; clicking them will show "Module not found" until their `PAGES.*` exist (added next) — acceptable interim state.

---

### Task 2: Price Zone and Cost Zone master pages

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — add two `PAGES.*` functions. Insert them immediately **after** the `PAGES.branch` function's closing `` `; `` (currently ends at ~line 399, just before `PAGES.zone`).

**Interfaces:**
- Consumes: helpers `head`, `noteBox`, `filterbar`, `table`, `badge`, `pager`, `btn`, `iExport`, `iAdd`.
- Produces: `PAGES.pricezone`, `PAGES.costzone`. Buttons "New Price Zone" / "New Cost Zone" (wired in Task 5).

- [ ] **Step 1: Add `PAGES.pricezone`.** Insert:

```javascript
PAGES.pricezone = ()=>`
${head('','Price Zone','Store groupings that share a retail price. A Price Event targets a Price Zone (or Company / Store). Define the zone here; create the dated price events in Price Change Management.', btn('Export','',iExport)+btn('New Price Zone','primary',iAdd))}
${noteBox('A <b>Price Zone</b> is a reusable set of stores — the “who”. The dated, approved <b>Price Events</b> that set actual prices live in <b>Price Change Management</b> and point at a zone. <b>Basis</b> shows how the zone was drawn (by store Format, by Region, or a manual pick). Price Zone and <b>Cost Zone</b> may look alike today but are independent — a cost move need not change retail.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Zone name or code…'},{label:'Basis',html:'<select><option>All</option><option>Format</option><option>Region</option><option>Manual</option></select>'},{label:'Company',html:'<select><option>All Companies</option><option>Puregold Price Club, Inc.</option><option>Ayagold Retailers Inc.</option></select>'},{label:'Status',html:'<select><option>All</option><option>Active</option><option>Inactive</option></select>'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Price Zone',k:'z'},{t:'Basis',k:'ba'},{t:'Company',k:'co'},{t:'Assigned Stores',k:'ms'},{t:'Active Price Events',k:'ev',num:1},{t:'Status',k:'st'}],
    [{code:'<span class="mono">PZ-PC</span>',z:'<span class="row-link">Price Club</span>',ba:badge('gray','Format'),co:'<span class="mono">PGOLD</span>',ms:'268 <span class="t-sub">(0102, 0107, 0233 …)</span>',ev:'12',st:badge('green','Active')},
     {code:'<span class="mono">PZ-EX</span>',z:'<span class="row-link">Extra</span>',ba:badge('gray','Format'),co:'<span class="mono">PGOLD</span>',ms:'92 <span class="t-sub">(0146, 0088 …)</span>',ev:'7',st:badge('green','Active')},
     {code:'<span class="mono">PZ-MM</span>',z:'<span class="row-link">Minimart</span>',ba:badge('gray','Format'),co:'<span class="mono">PGOLD</span>',ms:'77 <span class="t-sub">(0531, 0548 …)</span>',ev:'5',st:badge('green','Active')},
     {code:'<span class="mono">PZ-MK</span>',z:'<span class="row-link">Merkado</span>',ba:badge('gray','Format'),co:'<span class="mono">AYAG</span>',ms:'3 <span class="t-sub">(AYA-03, AYA-05 …)</span>',ev:'2',st:badge('green','Active')},
     {code:'<span class="mono">PZ-VIS</span>',z:'<span class="row-link">Visayas</span>',ba:badge('blue','Region'),co:'<span class="mono">PGOLD</span>',ms:'42 <span class="t-sub">(0233 …)</span>',ev:'3',st:badge('green','Active')},
     {code:'<span class="mono">PZ-NCR</span>',z:'<span class="row-link">NCR Metro</span>',ba:badge('blue','Region'),co:'<span class="mono">PGOLD</span>',ms:'184 <span class="t-sub">(0102, 0107 …)</span>',ev:'4',st:badge('green','Active')}])}</div>
  ${pager(6,6)}
</div>`;
```

- [ ] **Step 2: Add `PAGES.costzone`** immediately after `PAGES.pricezone`:

```javascript
PAGES.costzone = ()=>`
${head('','Cost Zone','Store groupings that share a vendor cost. A Cost Event targets a Cost Zone (or Company / Store). Define the zone here; create the dated cost events in Cost Change Management.', btn('Export','',iExport)+btn('New Cost Zone','primary',iAdd))}
${noteBox('A <b>Cost Zone</b> groups stores that pay the same landed cost for a vendor’s items. Dated <b>Cost Events</b> live in <b>Cost Change Management</b> and point here. It often mirrors <b>Price Zone</b> today — that overlap is expected, not duplication: cost and retail move independently.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Zone name or code…'},{label:'Basis',html:'<select><option>All</option><option>Format</option><option>Region</option><option>Manual</option></select>'},{label:'Company',html:'<select><option>All Companies</option><option>Puregold Price Club, Inc.</option><option>Ayagold Retailers Inc.</option></select>'},{label:'Status',html:'<select><option>All</option><option>Active</option><option>Inactive</option></select>'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Cost Zone',k:'z'},{t:'Basis',k:'ba'},{t:'Company',k:'co'},{t:'Assigned Stores',k:'ms'},{t:'Vendors Using',k:'vn',num:1},{t:'Active Cost Events',k:'ev',num:1},{t:'Status',k:'st'}],
    [{code:'<span class="mono">CZ-LUZ</span>',z:'<span class="row-link">Luzon Cost</span>',ba:badge('blue','Region'),co:'<span class="mono">PGOLD</span>',ms:'272 <span class="t-sub">(0102, 0461 …)</span>',vn:'38',ev:'9',st:badge('green','Active')},
     {code:'<span class="mono">CZ-VM</span>',z:'<span class="row-link">Visayas–Mindanao Cost</span>',ba:badge('blue','Region'),co:'<span class="mono">PGOLD</span>',ms:'72 <span class="t-sub">(0233, 0311 …)</span>',vn:'31',ev:'6',st:badge('green','Active')},
     {code:'<span class="mono">CZ-PC</span>',z:'<span class="row-link">Price Club National</span>',ba:badge('gray','Format'),co:'<span class="mono">PGOLD</span>',ms:'268 <span class="t-sub">(0102, 0107 …)</span>',vn:'44',ev:'11',st:badge('green','Active')},
     {code:'<span class="mono">CZ-MK</span>',z:'<span class="row-link">Merkado Cost</span>',ba:badge('gray','Format'),co:'<span class="mono">AYAG</span>',ms:'3 <span class="t-sub">(AYA-03 …)</span>',vn:'12',ev:'1',st:badge('green','Active')}])}</div>
  ${pager(4,4)}
</div>`;
```

- [ ] **Step 3: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 4: Checkpoint.** Save. Nav "Price Zone" / "Cost Zone" now render their lists.

---

### Task 3: Store Cluster page + Assortment Plan stub + generic `data-route` binding

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — add `PAGES.cluster` and `PAGES.assortplan` (after `PAGES.costzone`); add one line to `route()` (~line 1260).

**Interfaces:**
- Consumes: `head`, `noteBox`, `filterbar`, `table`, `badge`, `pager`, `btn`, `ph`, `route`.
- Produces: `PAGES.cluster`, `PAGES.assortplan`; generic click binding for any element with `data-route="<id>"`.

- [ ] **Step 1: Add the generic route binding.** In `route(id)`, find:

```javascript
  bindUM();
  bindBranchAccess();
}
```

Replace with:

```javascript
  bindUM();
  bindBranchAccess();
  document.querySelectorAll('#main [data-route]').forEach(el=>el.onclick=()=>route(el.dataset.route));
}
```

- [ ] **Step 2: Add `PAGES.cluster`:**

```javascript
PAGES.cluster = ()=>`
${head('','Store Cluster','Stores grouped by what they range (assortment). A Store Cluster carries one or more Assortment Plans — the SKU lists applied to its stores.', btn('Export','',iExport)+btn('New Store Cluster','primary',iAdd))}
${noteBox('Faithful to JDA: the <b>Store Cluster</b> is the store grouping; the SKU list it ranges is an <b>Assortment Plan</b> (created on its own page). Clusters track store size / trade area / velocity, not price — two stores in the same cluster may sit in different Price Zones.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Cluster name or code…'},{label:'Company',html:'<select><option>All Companies</option><option>Puregold Price Club, Inc.</option><option>Ayagold Retailers Inc.</option></select>'},{label:'Status',html:'<select><option>All</option><option>Active</option><option>Inactive</option></select>'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Store Cluster',k:'z'},{t:'Profile',k:'pr'},{t:'Company',k:'co'},{t:'Assigned Stores',k:'ms'},{t:'Ranged SKUs',k:'sk',num:1},{t:'Assortment Plans',k:'ap',num:1},{t:'Status',k:'st'}],
    [{code:'<span class="mono">CL-URB-L</span>',z:'<span class="row-link">Urban Large</span>',pr:'Big-box · high range',co:'<span class="mono">PGOLD</span>',ms:'118 <span class="t-sub">(0102, 0107 …)</span>',sk:'42,500',ap:'3',st:badge('green','Active')},
     {code:'<span class="mono">CL-URB-S</span>',z:'<span class="row-link">Urban Small</span>',pr:'Compact grocery',co:'<span class="mono">PGOLD</span>',ms:'96 <span class="t-sub">(0146 …)</span>',sk:'18,200',ap:'2',st:badge('green','Active')},
     {code:'<span class="mono">CL-CONV</span>',z:'<span class="row-link">Convenience</span>',pr:'Minimart range',co:'<span class="mono">PGOLD</span>',ms:'77 <span class="t-sub">(0531, 0548 …)</span>',sk:'6,400',ap:'2',st:badge('green','Active')},
     {code:'<span class="mono">CL-PROV</span>',z:'<span class="row-link">Provincial Mixed</span>',pr:'Regional staples bias',co:'<span class="mono">PGOLD</span>',ms:'149 <span class="t-sub">(0311, 0450 …)</span>',sk:'22,900',ap:'4',st:badge('green','Active')}])}</div>
  ${pager(4,4)}
</div>
<div class="panel">${ph('Assortment Plans','The SKU lists (Layer-2) applied to clusters — managed on their own page')}
  <div class="panel-body"><span class="t-sub">Assortment Plans are the operational SKU ranges that target a Store Cluster.</span>
  <div style="margin-top:10px"><button class="btn primary" data-route="assortplan">Open Assortment Plan →</button></div></div></div>`;
```

- [ ] **Step 3: Add `PAGES.assortplan` (stub):**

```javascript
PAGES.assortplan = ()=>`
${head('','Assortment Plan','SKU range lists applied to Store Clusters.', '<button class="btn" data-route="cluster">← Store Cluster</button>')}
${noteBox('<b>Design pending.</b> The Assortment Plan operational page — where a SKU range is built and applied to a Store Cluster — is scoped for a later pass. Store Clusters themselves are maintained on the <b>Store Cluster</b> page.','indigo')}
<div class="panel"><div class="panel-body"><span class="t-sub">No Assortment Plans yet — this page is a placeholder.</span></div></div>`;
```

- [ ] **Step 4: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 5: Checkpoint.** Save. On the Store Cluster page, "Open Assortment Plan →" routes to the stub; the stub's "← Store Cluster" routes back.

---

### Task 4: Replenishment Policy page

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — add `PAGES.replenpolicy` (after `PAGES.assortplan`).

**Interfaces:**
- Consumes: `head`, `noteBox`, `filterbar`, `table`, `badge`, `pager`, `btn`.
- Produces: `PAGES.replenpolicy`; button "New Replenishment Policy" (wired in Task 5).

- [ ] **Step 1: Add `PAGES.replenpolicy`:**

```javascript
PAGES.replenpolicy = ()=>`
${head('','Replenishment Policy','How a group of stores is resupplied — method, review cadence, source and lead time. Stores are assigned directly to a policy; there are no dated “events”.', btn('Export','',iExport)+btn('New Replenishment Policy','primary',iAdd))}
${noteBox('Unlike Price / Cost Zones, a <b>Replenishment Policy</b> carries <b>both</b> its store list and its parameters — the policy <i>is</i> the behaviour, so there is no separate operational page. Policy usually tracks store velocity + supply route (cross-dock), so it may differ from a store’s Store Cluster.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Policy name or code…'},{label:'Method',html:'<select><option>All Methods</option><option>Min / Max</option><option>Days-of-Supply</option></select>'},{label:'Source',html:'<select><option>All</option><option>DC / Cross-dock</option><option>Direct-to-Store</option></select>'},{label:'Status',html:'<select><option>All</option><option>Active</option><option>Inactive</option></select>'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Replenishment Policy',k:'z'},{t:'Method',k:'me'},{t:'Review Cadence',k:'rc'},{t:'Source',k:'src'},{t:'Lead Time',k:'lt'},{t:'Assigned Stores',k:'ms'},{t:'Status',k:'st'}],
    [{code:'<span class="mono">RP-METRO-D</span>',z:'<span class="row-link">Metro Fast-Movers</span>',me:badge('blue','Days-of-Supply'),rc:'Daily',src:'DC / Cross-dock',lt:'1 day',ms:'118 <span class="t-sub">(0102, 0107 …)</span>',st:badge('green','Active')},
     {code:'<span class="mono">RP-PROV-W</span>',z:'<span class="row-link">Provincial Weekly</span>',me:badge('gray','Min / Max'),rc:'Weekly',src:'Direct-to-Store',lt:'4 days',ms:'149 <span class="t-sub">(0311, 0450 …)</span>',st:badge('green','Active')},
     {code:'<span class="mono">RP-MINI-2</span>',z:'<span class="row-link">Minimart 2×/Week</span>',me:badge('blue','Days-of-Supply'),rc:'Mon · Thu',src:'DC / Cross-dock',lt:'1 day',ms:'77 <span class="t-sub">(0531, 0548 …)</span>',st:badge('green','Active')}])}</div>
  ${pager(3,3)}
</div>`;
```

- [ ] **Step 2: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 3: Checkpoint.** Save. All four grouping list pages now render.

---

### Task 5: CREATE forms for the four groupings; remove the old Zone create; rewire `CREATE_BTN`; remove `PAGES.zone`

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — the `CREATE` object (~lines 928-982), `CREATE_BTN` (~line 983), and remove `PAGES.zone` (~lines 457-475).

**Interfaces:**
- Consumes: `BUYERS` (company list). `createForm`/`openCreate` render the `CREATE` entries; `CREATE_BTN` maps button text → create key.
- Produces: `CREATE.pricezone`, `CREATE.costzone`, `CREATE.cluster`, `CREATE.replenpolicy`. Removes `CREATE.zone`, `PAGES.zone`, and the `'New Zone':'zone'` mapping.

- [ ] **Step 1: Remove `PAGES.zone`.** Delete the entire `PAGES.zone = ()=>\`…\`;` block (starts `PAGES.zone = ()=>\`` and ends with the closing `` `; `` before `PAGES.allowance`). Removing it means the id `zone` is now unrouted — already dropped from nav in Task 1.

- [ ] **Step 2: Replace `CREATE.zone` with the four new create entries.** Find the `zone:{…}` entry (the block beginning `  zone:{no:'',list:'zone',title:'New Zone',` and ending at `hint:'A store already in another zone of an Exclusive group is reassigned to this one.'}]}]},`) and replace the whole `zone:{…},` block with:

```javascript
  pricezone:{no:'',list:'pricezone',title:'New Price Zone',desc:'Define a store group that shares a retail price.',ref:'New Price Zone · Draft',cta:'Save Price Zone',footNote:'The zone is only a set of stores. Actual prices are set as dated Price Events in Price Change Management, which target this zone.',
    sections:[{l:'Identity',f:[{l:'Zone Name',req:1,span:'full',ph:'e.g. Visayas Metro'},{l:'Zone Code',ph:'Auto if left blank'},{l:'Company',type:'select',req:1,opt:BUYERS},{l:'Status',type:'select',opt:['Active','Inactive']}]},
      {l:'Membership',f:[{l:'Basis',type:'select',opt:['Manual pick','By Format','By Region'],hint:'How stores are drawn into this zone.'},{l:'Format',type:'select',opt:['— Not applicable —','Price Club','Extra','Minimart','Merkado'],hint:'Used when Basis = By Format.'},{l:'Region',type:'select',opt:['— Not applicable —','NCR','N.Luzon','S.Luzon','Visayas','Mindanao'],hint:'Used when Basis = By Region.'}]},
      {l:'Assign Stores',f:[{l:'Stores',type:'area',span:'full',ph:'Search or paste store codes (e.g. 0102, 0107, 0233)…',hint:'Manual additions on top of the Basis rule.'}]}]},
  costzone:{no:'',list:'costzone',title:'New Cost Zone',desc:'Define a store group that shares a vendor cost.',ref:'New Cost Zone · Draft',cta:'Save Cost Zone',footNote:'The zone is only a set of stores. Actual costs are set as dated Cost Events in Cost Change Management, which target this zone.',
    sections:[{l:'Identity',f:[{l:'Zone Name',req:1,span:'full',ph:'e.g. Luzon Cost'},{l:'Zone Code',ph:'Auto if left blank'},{l:'Company',type:'select',req:1,opt:BUYERS},{l:'Status',type:'select',opt:['Active','Inactive']}]},
      {l:'Membership',f:[{l:'Basis',type:'select',opt:['Manual pick','By Format','By Region'],hint:'How stores are drawn into this zone.'},{l:'Format',type:'select',opt:['— Not applicable —','Price Club','Extra','Minimart','Merkado'],hint:'Used when Basis = By Format.'},{l:'Region',type:'select',opt:['— Not applicable —','NCR','N.Luzon','S.Luzon','Visayas','Mindanao'],hint:'Used when Basis = By Region.'}]},
      {l:'Assign Stores',f:[{l:'Stores',type:'area',span:'full',ph:'Search or paste store codes…',hint:'Manual additions on top of the Basis rule.'}]}]},
  cluster:{no:'',list:'cluster',title:'New Store Cluster',desc:'Group stores that range a similar assortment.',ref:'New Store Cluster · Draft',cta:'Save Cluster',footNote:'The cluster is the store group; its SKU range is an Assortment Plan, created separately.',
    sections:[{l:'Identity',f:[{l:'Cluster Name',req:1,span:'full',ph:'e.g. Urban Small'},{l:'Cluster Code',ph:'Auto if left blank'},{l:'Company',type:'select',req:1,opt:BUYERS},{l:'Status',type:'select',opt:['Active','Inactive']}]},
      {l:'Profile',f:[{l:'Store Profile',span:'full',ph:'e.g. Compact grocery, high-velocity'},{l:'Trade Area',type:'select',opt:['Urban','Suburban','Provincial','Transit / Mall']}]},
      {l:'Assign Stores',f:[{l:'Stores',type:'area',span:'full',ph:'Search or paste store codes…'}]}]},
  replenpolicy:{no:'',list:'replenpolicy',title:'New Replenishment Policy',desc:'Set resupply behaviour for a group of stores.',ref:'New Replenishment Policy · Draft',cta:'Save Policy',footNote:'A policy carries both its stores and its parameters — there is no separate dated event.',
    sections:[{l:'Identity',f:[{l:'Policy Name',req:1,span:'full',ph:'e.g. Metro Fast-Movers'},{l:'Policy Code',ph:'Auto if left blank'},{l:'Company',type:'select',req:1,opt:BUYERS},{l:'Status',type:'select',opt:['Active','Inactive']}]},
      {l:'Parameters',f:[{l:'Method',type:'select',opt:['Min / Max (reorder point)','Days-of-Supply','Top-off']},{l:'Review Cadence',type:'select',opt:['Daily','2×/week (Mon · Thu)','Weekly']},{l:'Source',type:'select',opt:['DC / Cross-dock','Direct-to-Store']},{l:'Lead Time',ph:'e.g. 1 day'},{l:'Safety Stock (days)',type:'number',v:'5'},{l:'Service Level',type:'select',opt:['95.0%','97.5%','98.0%','99.0%']}]},
      {l:'Assign Stores',f:[{l:'Stores',type:'area',span:'full',ph:'Search or paste store codes…'}]}]},
```

- [ ] **Step 3: Rewire `CREATE_BTN`.** In the `CREATE_BTN` object, find `'New Zone':'zone',` and replace with:

```javascript
'New Price Zone':'pricezone','New Cost Zone':'costzone','New Store Cluster':'cluster','New Replenishment Policy':'replenpolicy',
```

- [ ] **Step 4: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 5: Grep gate — no stray Zone-page/create leftovers.** Run:

```bash
grep -n "PAGES.zone\|CREATE.zone\|'New Zone'\|id:\"zone\"\|Exclusive\|Overlapping\|Zone Group" ui-mockup/iconnect-erp.html
```

Expected: **no output** (all removed).

- [ ] **Step 6: Checkpoint.** Save. "New Price Zone/Cost Zone/Store Cluster/Replenishment Policy" buttons open their modals.

---

### Task 6: Branch — "Grouping Assignments" on the detail panel + CREATE.branch pickers

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — the Branch Detail `kv` block in `PAGES.branch` (~lines 386-393) and `CREATE.branch` (~line 947-948).

**Interfaces:**
- Consumes: `badge`. Uses the seed group names from Tasks 2-4 (Price Club / Price Club National / Urban Large / Metro Fast-Movers) for the sample store 0102.
- Produces: reciprocal display of a store's four grouping assignments; four pickers on the New Branch form.

- [ ] **Step 1: Add the four assignment rows to the Branch Detail kv block.** Find:

```javascript
    <div class="k">Delivery Feed</div><div class="v">DTS + Cross-dock</div>
    <div class="k">Status</div><div class="v">${badge('green','Active')}</div>
  </div></div>
```

Replace with:

```javascript
    <div class="k">Delivery Feed</div><div class="v">DTS + Cross-dock</div>
    <div class="k">Status</div><div class="v">${badge('green','Active')}</div>
    <div class="k">Price Zone</div><div class="v">${badge('gray','Price Club')} <span class="t-sub">PZ-PC</span></div>
    <div class="k">Cost Zone</div><div class="v">${badge('gray','Price Club National')} <span class="t-sub">CZ-PC</span></div>
    <div class="k">Store Cluster</div><div class="v">${badge('blue','Urban Large')} <span class="t-sub">CL-URB-L</span></div>
    <div class="k">Replen. Policy</div><div class="v">${badge('blue','Metro Fast-Movers')} <span class="t-sub">RP-METRO-D</span></div>
  </div></div>
```

- [ ] **Step 2: Add a "Grouping Assignments" section to `CREATE.branch`.** In the `branch:{…}` create entry, find its `Address` section:

```javascript
{l:'Address',f:[{l:'Street / Building',span:'full'},{l:'City / Municipality'},{l:'Province'}]}]},
```

Replace with (inserts a new section before Address):

```javascript
{l:'Grouping Assignments',f:[{l:'Price Zone',type:'select',opt:['Price Club','Extra','Minimart','Merkado','Visayas','NCR Metro'],hint:'Retail-price group this store follows.'},{l:'Cost Zone',type:'select',opt:['Price Club National','Luzon Cost','Visayas–Mindanao Cost','Merkado Cost']},{l:'Store Cluster',type:'select',opt:['Urban Large','Urban Small','Convenience','Provincial Mixed']},{l:'Replenishment Policy',type:'select',opt:['Metro Fast-Movers','Provincial Weekly','Minimart 2×/Week']}]},
{l:'Address',f:[{l:'Street / Building',span:'full'},{l:'City / Municipality'},{l:'Province'}]}]},
```

- [ ] **Step 3: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 4: Checkpoint.** Save.

---

### Task 7: Repoint Price & Cost event scope to Price Zone / Cost Zone

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — `PAGES.price` (head ~628, list level badges ~634/637, `CREATE.price` ~955-956), `PAGES.cost` (head ~662, level badge ~669, `CREATE.cost` ~958-959).

**Interfaces:**
- Consumes: nothing new. Produces: price/cost event scope selectors that name Price Zone / Cost Zone instead of a bare "Zone".

- [ ] **Step 1: Price page head.** In `PAGES.price`, find `at Company, Zone or Store level.` and replace with `at Company, Price Zone or Store level.`

- [ ] **Step 2: Price list level badges.** In `PAGES.price`, replace every occurrence of `lv:badge('purple','Zone')` with `lv:badge('purple','Price Zone')` (`replace_all`). The scope text values (`sc:'S.Luzon'`, `sc:'Visayas'`) stay — those are valid Price Zone names.

- [ ] **Step 3: CREATE.price.** In the `price:{…}` create entry, find:

```javascript
desc:'Create a price update batch at Company / Zone / Store level.',
```
replace with:
```javascript
desc:'Create a price update batch at Company / Price Zone / Store level.',
```

Then find:
```javascript
{l:'Price Level',type:'select',opt:['Company (all stores)','Zone','Store']},{l:'Scope',type:'select',opt:['Company — All Stores','Zone — NCR','Zone — N.Luzon','Zone — S.Luzon','Zone — Visayas','Zone — Mindanao','0102 · Ortigas Ext.']},
```
replace with:
```javascript
{l:'Price Level',type:'select',opt:['Company (all stores)','Price Zone','Store']},{l:'Scope',type:'select',opt:['Company — All Stores','Price Zone — Price Club','Price Zone — Extra','Price Zone — Minimart','Price Zone — Visayas','Price Zone — NCR Metro','0102 · Ortigas Ext.']},
```

- [ ] **Step 4: Cost page head.** In `PAGES.cost`, find `at Company, Zone or Store level.` and replace with `at Company, Cost Zone or Store level.`

- [ ] **Step 5: Cost list level badge.** In `PAGES.cost`, replace every occurrence of `lv:badge('purple','Zone')` with `lv:badge('purple','Cost Zone')` (`replace_all`). The `sc:'S.Luzon'` sample stays.

- [ ] **Step 6: CREATE.cost.** In the `cost:{…}` create entry, find:

```javascript
{l:'Cost Level',type:'select',opt:['Company (all stores)','Zone','Store']},
```
replace with:
```javascript
{l:'Cost Level',type:'select',opt:['Company (all stores)','Cost Zone','Store']},
```

- [ ] **Step 7: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 8: Checkpoint.** Save.

---

### Task 8: Repoint RBAC — access scope "Zone" → "Region"

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — `scopeCell` (~1074), `CREATE.user` Branch Access (~932), `BA_SCOPES` (~1156-1170), `PAGES.branchaccess` head + scope select (~1191-1195). Users-page rows call `scopeCell('zone',…)` and need no change (values are regions; display now reads "Region").

**Interfaces:**
- Consumes: `badge`. Produces: RBAC access scope labelled "Region" (geographic) everywhere; `BA_SCOPES` keyed `Region`.

- [ ] **Step 1: `scopeCell` display label.** Find:

```javascript
function scopeCell(kind,txt){return kind==='company'?badge('purple','Company')+' <span class="t-sub">all 437</span>':kind==='zone'?badge('blue','Zone')+` <span class="t-sub">${txt}</span>`:badge('gray','Store')+` <span class="t-sub">${txt}</span>`;}
```

Replace with:

```javascript
function scopeCell(kind,txt){return kind==='company'?badge('purple','Company')+' <span class="t-sub">all 437</span>':kind==='zone'?badge('blue','Region')+` <span class="t-sub">${txt}</span>`:badge('gray','Store')+` <span class="t-sub">${txt}</span>`;}
```

(The `'zone'` key is kept internally to avoid touching every caller; only the displayed badge text changes to "Region".)

- [ ] **Step 2: CREATE.user Branch Access.** Find:

```javascript
{l:'Branch Access',f:[{l:'Access Scope',type:'select',req:1,opt:['Company-wide (all 437 stores)','By Zone','By Store']},{l:'Zone',type:'select',opt:['— Not applicable —','NCR','N.Luzon','S.Luzon','Visayas','Mindanao'],hint:'Used when scope is “By Zone”.'},{l:'Home Branch',type:'select',opt:['— Not applicable —',...BR],hint:'Used when scope is “By Store”. Add more stores after creation on the Branch Access tab.'}]},
```

Replace with:

```javascript
{l:'Branch Access',f:[{l:'Access Scope',type:'select',req:1,opt:['Company-wide (all 437 stores)','By Region','By Store']},{l:'Region',type:'select',opt:['— Not applicable —','NCR','N.Luzon','S.Luzon','Visayas','Mindanao'],hint:'Used when scope is “By Region”.'},{l:'Home Branch',type:'select',opt:['— Not applicable —',...BR],hint:'Used when scope is “By Store”. Add more stores after creation on the Branch Access tab.'}]},
```

- [ ] **Step 3: `BA_SCOPES` — rename `Zone` key/label to `Region`.** Find:

```javascript
  Zone:{label:'Zone', items:[
    {v:'NCR', sum:'Metro Manila stores'},
    {v:'N.Luzon', sum:'Northern Luzon stores'},
    {v:'S.Luzon', sum:'Southern Luzon stores'},
    {v:'Visayas', sum:'Visayas stores'},
    {v:'Mindanao', sum:'Mindanao stores'}
  ]},
```

Replace with:

```javascript
  Region:{label:'Region', items:[
    {v:'NCR', sum:'Metro Manila stores'},
    {v:'N.Luzon', sum:'Northern Luzon stores'},
    {v:'S.Luzon', sum:'Southern Luzon stores'},
    {v:'Visayas', sum:'Visayas stores'},
    {v:'Mindanao', sum:'Mindanao stores'}
  ]},
```

Also update the comment above `BA_SCOPES` — find `Company lists companies, Zone lists geographic zones, Store lists stores.` and replace with `Company lists companies, Region lists geographic regions, Store lists stores.`

- [ ] **Step 4: `PAGES.branchaccess` head + scope select.** Find:

```javascript
${head('','Branch Access','Who can access a given Company, Zone or Store. Select a scope to see the users and roles with access there.', btn('Export','',iExport)+btn('Assign User','primary',iAdd))}
```
replace with:
```javascript
${head('','Branch Access','Who can access a given Company, Region or Store. Select a scope to see the users and roles with access there.', btn('Export','',iExport)+btn('Assign User','primary',iAdd))}
```

Then find:
```javascript
    {label:'Scope Level',html:'<select id="ba-scope"><option>Company</option><option>Zone</option><option>Store</option></select>'},
```
replace with:
```javascript
    {label:'Scope Level',html:'<select id="ba-scope"><option>Company</option><option>Region</option><option>Store</option></select>'},
```

(`bindBranchAccess` reads `scope.value` as the `BA_SCOPES` key — with the option now "Region" and the key renamed to `Region`, the lookup stays valid.)

- [ ] **Step 5: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 6: Functional check.** In Playwright: open the file, `route('branchaccess')`, change the "Scope Level" select to "Region", confirm the dependent value select repopulates with NCR/N.Luzon/… and the summary line updates (no console error). See Task 9 for the harness command.

- [ ] **Step 7: Checkpoint.** Save.

---

### Task 9: Repoint replen reference; full verification sweep

**Files:**
- Modify: `ui-mockup/iconnect-erp.html` — the replen MPO-allocation table (~line 743). Read the surrounding block first (offset ~735-755) to get exact strings before editing.

**Interfaces:**
- Consumes: existing replen table. Produces: replen allocation grouped/labelled by Replenishment Policy instead of "Zone".

- [ ] **Step 1: Read the replen allocation region.** Read `ui-mockup/iconnect-erp.html` lines ~735-755 to see the exact `{t:'Zone',k:'z'}` column header and its row values.

- [ ] **Step 2: Relabel the replen "Zone" column.** In that table's column list, change the header `{t:'Zone',k:'z'}` to `{t:'Replen Policy',k:'z'}`. If the row `z:` values are format names (e.g. `Price Club`), leave the values as-is (a policy row can display its dominant format) — the header relabel is the required change. Do **not** invent new data; only relabel the column header.

- [ ] **Step 3: Syntax gate.** Expected: `SYNTAX OK`.

- [ ] **Step 4: Residual-term grep sweep.** Run:

```bash
grep -noE "Zone Group|Exclusive|Overlapping|Replenishment Profile|Replenishment Rule|Assortment Zone|id:\"zone\"|PAGES\.zone|CREATE\.zone|'New Zone'" ui-mockup/iconnect-erp.html
```

Expected: **no output.** Then run:

```bash
grep -noE "Zone" ui-mockup/iconnect-erp.html | grep -viE "Price Zone|Cost Zone" | head -40
```

Expected: only intentional/cosmetic hits remain (e.g. the audit-log sample string "Zone Price Update PC-88"). If a functional selector or label still says a bare "Zone", fix it to Price Zone / Cost Zone / Region as appropriate. **Optional cosmetic:** update the audit sample (`PAGES.audit`) string `Zone Price Update PC-88 (S.Luzon)` → `Price Zone Update PC-88 (S.Luzon)`.

- [ ] **Step 5: Playwright click-through.** Using the Playwright MCP browser, navigate to `file:///mnt/d/claude-workspace/iconnect-erp/ui-mockup/iconnect-erp.html`, then for each id in `['home','company','branch','pricezone','costzone','cluster','assortplan','replenpolicy','price','cost','users','branchaccess']` run `route(id)` via `browser_evaluate` and capture `browser_console_messages`. Expected: each page renders; the only console error is the benign `favicon.ico 404`. Confirm specifically:
  - `pricezone`, `costzone`, `cluster`, `replenpolicy` render their tables.
  - On `cluster`, the "Open Assortment Plan →" button routes to the `assortplan` stub; the stub's "← Store Cluster" routes back.
  - Nav shows the "Store Grouping" sub-group with four items and **no** "Zone" item.

- [ ] **Step 6: Checkpoint.** Save. Feature complete.

---

## Self-Review

**1. Spec coverage** (against `2026-07-17-store-grouping-redesign-design.md`):
- Four grouping masters → Tasks 2, 3, 4. ✔
- Two-layer split (grouping reads event counts; events on own pages) → Price/Cost show "Active … Events" columns (Task 2); Store Cluster shows "Assortment Plans" + stub (Task 3); Replen merged (Task 4). ✔
- Shared basic details (Name/Code/Status/Company/Assigned stores as count+sample) → all four pages. ✔
- Native columns per page → Task 2 (Basis, Vendors, event counts), Task 4 (Method/Cadence/Source/Lead time). ✔
- Branch reciprocal "Grouping Assignments" + CREATE.branch pickers → Task 6. ✔
- Retire old Zone page/nav/CREATE → Tasks 1, 5. ✔
- Repoint Price/Cost scope → Task 7. ✔
- RBAC "By Zone" → "By Region" → Task 8. ✔
- Replen references → Replenishment Policy → Task 9. ✔
- Nav "Store Grouping" sub-group → Task 1. ✔
- Seed data + explanatory notes → notes in Tasks 2-4; seed rows in Tasks 2-4. ✔
- Assortment Plan stubbed → Task 3. ✔
- **Gap check:** none found.

**2. Placeholder scan:** No "TBD/TODO/implement later" left in the plan; every code step shows full code; the only intentional "read first" is Task 9 Step 1 (replen table), justified because the exact row strings weren't captured.

**3. Type/name consistency:** page ids (`pricezone`,`costzone`,`cluster`,`replenpolicy`,`assortplan`) are identical across NAV (Task 1), PAGES (Tasks 2-4), CREATE `list:` targets, and CREATE_BTN (Task 5). Seed group names for store 0102 in Task 6 (Price Club / Price Club National / Urban Large / Metro Fast-Movers) match rows seeded in Tasks 2-4. `data-route` binding (Task 3) is what makes the Task 3 stub buttons work. `BA_SCOPES` key `Region` matches the branchaccess select option `Region` (Task 8).
