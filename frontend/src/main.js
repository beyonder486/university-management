const API_BASE = (
  window.UNIFLOW_API_URL || "http://localhost:8080/api"
).replace(/\/$/, "");
const TOKEN_KEY = "university-token";
const app = document.querySelector("#app");

const resources = {
  students: { title: "Students", endpoint: "/students" },
  teachers: { title: "Teachers", endpoint: "/teachers" },
  courses: { title: "Courses", endpoint: "/courses" },
  users: { title: "Users", endpoint: "/users" },
  enrollments: { title: "Enrollments", endpoint: "/enrollments" },
};

const roleLinks = {
  ADMIN: [
    "dashboard",
    "students",
    "teachers",
    "courses",
    "enrollments",
    "users",
  ],
  TEACHER: ["dashboard", "students", "teachers", "courses", "enrollments"],
  STUDENT: ["dashboard", "courses", "enrollments"],
};

const labels = {
  dashboard: "Overview",
  students: "Students",
  teachers: "Teachers",
  courses: "Courses",
  enrollments: "Enrollments",
  users: "Users",
};

const state = {
  user: null,
  route: routeFromHash(),
  modal: null,
  message: null,
  search: "",
  pageData: [],
  summary: null,
  loading: false,
};

function escapeHtml(value) {
  return String(value ?? "").replace(
    /[&<>"']/g,
    (char) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      })[char],
  );
}

function routeFromHash() {
  const path = window.location.hash.replace(/^#\/?/, "");
  return path || "";
}

function navigate(path) {
  const next = path.replace(/^\/+/, "");
  if (routeFromHash() === next) {
    state.route = next;
    void render();
    return;
  }
  window.location.hash = `/${next}`;
}

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (options.body && !(options.body instanceof FormData))
    headers.set("Content-Type", "application/json");
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 10000);
  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
      signal: controller.signal,
    });
  } catch (error) {
    if (controller.signal.aborted) {
      throw new Error(
        'The API did not respond in time. Run "docker compose up --build db backend" from the project folder, then try again.',
      );
    }
    throw new Error(
      'The API is unavailable. Start Docker Desktop, then run "docker compose up --build db backend" from the project folder.',
    );
  } finally {
    window.clearTimeout(timeout);
  }
  const bodyText = await response.text();
  let body = null;
  if (bodyText) {
    try {
      body = JSON.parse(bodyText);
    } catch {
      body = null;
    }
  }
  if (!response.ok) {
    if (response.status === 401 && path !== "/auth/login") {
      localStorage.removeItem(TOKEN_KEY);
      state.user = null;
    }
    const message =
      body?.validationErrors?.join(". ") ||
      body?.message ||
      bodyText ||
      response.statusText;
    throw new Error(message || `Request failed (${response.status})`);
  }
  return body;
}

function request(method, path, payload) {
  return api(path, {
    method,
    body: payload === undefined ? undefined : JSON.stringify(payload),
  });
}

function roleTitle(role) {
  return role ? role.charAt(0) + role.slice(1).toLowerCase() : "";
}

function icon(name) {
  return (
    {
      dashboard: "◫",
      students: "♙",
      teachers: "♧",
      courses: "▤",
      enrollments: "◷",
      users: "♧",
      docs: "▧",
    }[name] || "◦"
  );
}

function setMessage(text, type = "success") {
  state.message = { text, type };
}

function messageHtml() {
  if (!state.message) return "";
  const { text, type } = state.message;
  return `<div class="alert alert-${type}" role="${type === "error" ? "alert" : "status"}">${escapeHtml(text)}<button class="alert-close" data-action="dismiss" aria-label="Dismiss">×</button></div>`;
}

function landingPage() {
  return `
    <main class="landing">
      <nav class="landing-nav">
        <a class="brand" href="#/"><span class="brand-mark">U</span> UniFlow</a>
        <div class="landing-actions">
          <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">API documentation</a>
          <a class="button button-quiet" href="#/login">Sign in</a>
          <span>Accounts are provided by your university</span>
        </div>
      </nav>
      <section class="landing-hero">
        <div class="hero-copy">
          <span class="eyebrow"><span class="eyebrow-dot"></span> ONE CAMPUS. ONE CLEAR VIEW.</span>
          <h1>Make campus life<br><span>flow better.</span></h1>
          <p>A calmer way to manage courses, people, and enrollment—built for the whole university community.</p>
          <div class="hero-actions">
            <a class="button button-primary button-large" href="#/login">Open your workspace <span>↗</span></a>
            <span class="text-link">Student accounts are set up by your university</span>
          </div>
          <div class="hero-footnote"><span class="avatar-stack"><i>AD</i><i>TH</i><i>ST</i></span>One secure workspace for every role</div>
        </div>
        <div class="preview-card">
          <div class="preview-top"><span class="preview-brand"><span class="brand-mark small">U</span> campus overview</span><span class="live-pill">● LIVE</span></div>
          <div class="preview-welcome"><div><span>MONDAY, OCTOBER 19</span><h2>Your campus,<br>at a glance.</h2></div><div class="sun-icon">✳</div></div>
          <div class="preview-stats"><div><span>STUDENTS</span><strong>2,408</strong><small class="positive">↗ 8.2% this term</small></div><div><span>ACTIVE COURSES</span><strong>86</strong><small>Across 12 departments</small></div></div>
          <div class="preview-course"><div class="course-icon">✳</div><div><strong>Today's schedule</strong><small>CS 204 · Data Structures</small></div><span class="course-time">10:30 <small>AM</small></span></div>
          <div class="preview-bottom"><span><i></i> All systems running smoothly</span><span>UniFlow · 2026</span></div>
          <span class="decor decor-one">✳</span><span class="decor decor-two">✳</span>
        </div>
      </section>
      <section class="landing-features"><span>THE CAMPUS, CONNECTED</span><div><strong>People</strong><i>✳</i><strong>Courses</strong><i>✳</i><strong>Enrollment</strong><i>✳</i><strong>Everything in sync</strong></div></section>
    </main>`;
}

function authPage() {
  return `
    <main class="auth-shell">
      <a class="brand auth-brand" href="#/"><span class="brand-mark">U</span> UniFlow</a>
      <section class="auth-card">
        <div class="auth-heading"><span class="auth-symbol">↗</span><p class="eyebrow">WELCOME BACK</p><h1>Good to see you.</h1><p>Sign in to pick up where you left off.</p></div>
        ${messageHtml()}
        <form class="form-stack" data-form="login">
          <label class="field"><span>University email</span><input name="email" type="email" placeholder="you@university.edu" required autocomplete="email"></label>
          <label class="field"><span>Password</span><div class="password-input"><input name="password" type="password" placeholder="Your password" required minlength="8" autocomplete="current-password"><button type="button" class="password-toggle" data-action="toggle-password">Show</button></div></label>
          <button class="button button-primary button-full button-large" type="submit">Sign in <span>↗</span></button>
        </form>
        <p class="auth-note">Start Docker Desktop, then run <code>docker compose up --build db backend</code> from the project folder.</p>
      </section>
    </main>`;
}

function accountProvisioningPage() {
  return `
    <main class="auth-shell">
      <a class="brand auth-brand" href="#/"><span class="brand-mark">U</span> UniFlow</a>
      <section class="auth-card">
        <div class="auth-heading"><span class="auth-symbol">✳</span><p class="eyebrow">UNIVERSITY ACCOUNTS</p><h1>Accounts are set up by your university.</h1><p>Contact your university administrator to create your student account. Once it’s ready, sign in to view and update your profile.</p></div>
        <a class="button button-primary button-full button-large" href="#/login">Go to sign in <span>↗</span></a>
      </section>
    </main>`;
}

function navigation(user) {
  return roleLinks[user.role]
    .map(
      (key) =>
        `<a class="side-link ${state.route === `app/${key}` ? "active" : ""}" href="#/app/${key}"><span class="nav-icon">${icon(key)}</span><span>${labels[key]}</span>${key === "dashboard" ? '<i class="side-active-dot"></i>' : ""}</a>`,
    )
    .join("");
}

function workspaceShell(content, user) {
  const current = state.route.split("/")[1] || "dashboard";
  const currentTitle =
    current === "dashboard"
      ? "Overview"
      : current === "profile"
        ? "My profile"
        : resources[current]?.title || "Overview";
  return `
    <div class="app-shell">
      <aside class="sidebar">
        <a class="brand sidebar-brand" href="#/app/dashboard"><span class="brand-mark">U</span><span>UniFlow<small>UNIVERSITY PORTAL</small></span></a>
        <div class="side-label">WORKSPACE</div>
        <nav class="side-nav">${navigation(user)}</nav>
        <div class="sidebar-bottom">
          <a class="side-link" href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer"><span class="nav-icon">${icon("docs")}</span><span>API documentation</span><span class="external-mark">↗</span></a>
          <a class="profile-button" href="#/app/profile"><span class="user-avatar">${escapeHtml(user.username.slice(0, 1).toUpperCase())}</span><span class="profile-name">${escapeHtml(user.username)}<small>${roleTitle(user.role)}</small></span><span class="profile-dots">···</span></a>
          <button class="signout-button" data-action="logout">↗ <span>Sign out</span></button>
        </div>
      </aside>
      <main class="main-area">
        <header class="topbar"><div class="breadcrumb"><span>Workspace</span><i>/</i><strong>${escapeHtml(currentTitle)}</strong></div><div class="topbar-actions"><span class="top-date">${new Intl.DateTimeFormat("en", { weekday: "short", month: "short", day: "numeric" }).format(new Date())}</span><a class="icon-button mobile-profile" href="#/app/profile" aria-label="Profile">${escapeHtml(user.username.slice(0, 1).toUpperCase())}</a><button class="icon-button desktop-signout" data-action="logout" aria-label="Sign out" title="Sign out">↗</button></div></header>
        <div class="page-content">${messageHtml()}${content}</div>
      </main>
    </div>`;
}

function statCard(label, value, glyph, tint) {
  return `<div class="stat-card"><div class="stat-icon tint-${tint}">${glyph}</div><span class="stat-label">${label}</span><strong>${value ?? "—"}</strong><small><span class="stat-dot"></span> Updated just now</small></div>`;
}

function emptyState(title, text) {
  return `<div class="empty-state"><span class="empty-symbol">✳</span><strong>${escapeHtml(title)}</strong><p>${escapeHtml(text)}</p></div>`;
}

function dashboard(user, courses = []) {
  const summary = state.summary;
  const stats =
    user.role === "ADMIN"
      ? `<section class="stats-grid">
    ${statCard("Total students", summary?.totalStudents, "♙", "mint")}
    ${statCard("Teaching staff", summary?.totalTeachers, "♧", "lavender")}
    ${statCard("Active courses", summary?.totalCourses, "▤", "peach")}
    ${statCard("User accounts", summary?.totalUsers, "◉", "blue")}
  </section>`
      : "";
  const courseRows = courses.length
    ? `<div class="course-list">${courses.map((course, index) => `<div class="course-row"><span class="course-index tint-${["mint", "lavender", "peach", "blue"][index % 4]}">${escapeHtml(String(course.courseCode || "").slice(0, 2))}</span><div class="course-row-title"><strong>${escapeHtml(course.title)}</strong><small>${escapeHtml(course.courseCode)} · ${escapeHtml(course.credits)} credits</small></div><span class="muted-cell">${escapeHtml(course.teacherName || "Faculty pending")}</span><span class="row-arrow">↗</span></div>`).join("")}</div>`
    : emptyState(
        "No courses yet",
        "Courses will appear here once they are added to the catalog.",
      );
  const shortcuts = roleLinks[user.role]
    .filter((key) => key !== "dashboard")
    .slice(0, 4)
    .map(
      (key) => `<a href="#/app/${key}"><span>${labels[key]}</span><i>↗</i></a>`,
    )
    .join("");
  return `<section class="page-enter">
    <div class="page-heading"><div><p class="eyebrow">MONDAY, OCTOBER 19, 2026</p><h1>${user.role === "ADMIN" ? "Your campus, at a glance." : `Welcome, ${escapeHtml(user.username.split(" ")[0])}.`}</h1><p class="page-subtitle">${user.role === "ADMIN" ? "Here’s what’s happening across your university today." : "Your university workspace is ready when you are."}</p></div><span class="heading-spark">✳</span></div>
    ${stats}
    <div class="dashboard-grid">
      <section class="panel course-panel"><div class="panel-heading"><div><span class="eyebrow">ACADEMIC</span><h2>Course catalog</h2></div><a class="text-link" href="#/app/courses">View all <span>↗</span></a></div>${courseRows}</section>
      <section class="panel quick-panel"><span class="eyebrow">QUICK ACCESS</span><h2>Where to next?</h2><p>Jump into the parts of campus you use most.</p><div class="quick-links">${shortcuts}</div></section>
    </div>
    <div class="dashboard-note"><span>✳</span><div><strong>A little more organized, every day.</strong><p>Your campus information is kept in one secure place, with access tailored to your role.</p></div><span class="note-decoration">UNIFLOW</span></div>
  </section>`;
}

const columns = {
  students: [
    ["studentNumber", "STUDENT ID"],
    ["firstName", "FIRST NAME"],
    ["lastName", "LAST NAME"],
    ["department", "DEPARTMENT"],
    ["year", "YEAR"],
    ["email", "EMAIL"],
    ["enabled", "STATUS"],
  ],
  teachers: [
    ["firstName", "FIRST NAME"],
    ["lastName", "LAST NAME"],
    ["department", "DEPARTMENT"],
    ["title", "TITLE"],
    ["email", "EMAIL"],
    ["enabled", "STATUS"],
  ],
  courses: [
    ["courseCode", "COURSE CODE"],
    ["title", "COURSE TITLE"],
    ["teacherName", "INSTRUCTOR"],
    ["credits", "CREDITS"],
    ["maxStudents", "CAPACITY"],
  ],
  users: [
    ["username", "USERNAME"],
    ["email", "EMAIL"],
    ["role", "ROLE"],
    ["enabled", "STATUS"],
  ],
  enrollments: [
    ["studentName", "STUDENT"],
    ["courseCode", "COURSE"],
    ["courseTitle", "COURSE TITLE"],
    ["enrolledAt", "ENROLLED"],
    ["status", "STATUS"],
  ],
};

function cellValue(row, key, resource) {
  const value = row[key];
  if (key === "enabled" || key === "status") {
    const active = value === true || value === "ACTIVE";
    const text =
      value === true ? "Active" : value === false ? "Disabled" : value || "—";
    return `<span class="status-pill ${active ? "status-active" : value === false || value === "WITHDRAWN" ? "status-muted" : "status-other"}"><i></i>${escapeHtml(text)}</span>`;
  }
  if (key === "role")
    return `<span class="role-pill">${escapeHtml(value || "—")}</span>`;
  if (key === "enrolledAt" || key === "createdAt") {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? escapeHtml(value)
      : escapeHtml(date.toLocaleDateString());
  }
  if (key === "firstName" && resource === "students")
    return `<strong>${escapeHtml(value)}</strong>`;
  return value === null || value === undefined || value === ""
    ? '<span class="muted-cell">—</span>'
    : escapeHtml(value);
}

function actionButton(text, action, id, danger = false) {
  return `<button class="action-button ${danger ? "danger" : ""}" data-action="${action}" data-id="${id}">${text}</button>`;
}

function tableView(resource, user) {
  const rows = state.pageData;
  const query = state.search.trim().toLowerCase();
  const filtered = query
    ? rows.filter((row) =>
        Object.values(row).some((value) =>
          String(value ?? "")
            .toLowerCase()
            .includes(query),
        ),
      )
    : rows;
  const editAllowed =
    resource === "students"
      ? user.role === "ADMIN"
      : ["teachers", "courses"].includes(resource) &&
        ["ADMIN", "TEACHER"].includes(user.role);
  const deleteAllowed =
    user.role === "ADMIN" &&
    ["students", "teachers", "users", "courses"].includes(resource);
  const tableRows = filtered
    .map((row) => {
      const ownTeacherRecord = resource === "teachers" && row._own === true;
      const ownCourseRecord = resource === "courses" && row._own === true;
      const canEdit =
        editAllowed &&
        (user.role !== "TEACHER" || ownTeacherRecord || ownCourseRecord);
      const canDelete = deleteAllowed;
      const showActions =
        canEdit ||
        canDelete ||
        (resource === "enrollments" &&
          row.status === "ACTIVE" &&
          user.role !== "TEACHER");
      return `<tr>${columns[resource].map(([key]) => `<td>${cellValue(row, key, resource)}</td>`).join("")}${showActions ? `<td class="table-actions">${canEdit ? actionButton("Edit", "edit", row.id) : ""}${canDelete ? actionButton("Delete", "delete", row.id, true) : ""}${resource === "enrollments" && row.status === "ACTIVE" && user.role !== "TEACHER" ? actionButton("Withdraw", "withdraw", row.id, true) : ""}</td>` : ""}</tr>`;
    })
    .join("");
  const actionsHeader =
    editAllowed || deleteAllowed || resource === "enrollments"
      ? '<th class="actions-heading">ACTIONS</th>'
      : "";
  const dataBody = state.loading
    ? '<div class="loading-inline"><span class="spinner"></span>Loading records…</div>'
    : filtered.length
      ? `<div class="table-scroll"><table><thead><tr>${columns[resource].map(([, title]) => `<th>${title}</th>`).join("")}${actionsHeader}</tr></thead><tbody>${tableRows}</tbody></table></div>`
      : emptyState(
          query
            ? "No matching records"
            : `No ${resources[resource].title.toLowerCase()} yet`,
          query ? "Try another search term." : "Add a record to get started.",
        );
  return `<section class="page-enter">
    <div class="page-heading resource-heading"><div><p class="eyebrow">CAMPUS DIRECTORY</p><h1>${resources[resource].title}</h1><p class="page-subtitle">${resourceDescriptions[resource]}</p></div>${canCreate(resource, user.role) ? `<button class="button button-primary" data-action="create">＋ <span>${resource === "enrollments" ? "Enroll in course" : `Add ${resources[resource].title.slice(0, -1).toLowerCase()}`}</span></button>` : ""}</div>
    <section class="panel table-panel">
      <div class="table-toolbar"><div><strong>${resources[resource].title}</strong><span class="count-chip">${rows.length}</span></div><label class="search-box"><span>⌕</span><input data-search aria-label="Search ${resources[resource].title.toLowerCase()}" placeholder="Search records…" value="${escapeHtml(state.search)}"><kbd>⌘ K</kbd></label></div>${dataBody}
    </section>
  </section>`;
}

const resourceDescriptions = {
  students: "Student records and account status.",
  teachers: "Teaching staff and profile details.",
  courses: "Course catalog and teaching assignments.",
  users: "Application accounts and role assignments.",
  enrollments: "Course registration and enrollment status.",
};

function canCreate(resource, role) {
  if (["students", "teachers", "users"].includes(resource))
    return role === "ADMIN";
  if (resource === "courses") return role === "ADMIN";
  if (resource === "enrollments") return ["ADMIN", "STUDENT"].includes(role);
  return false;
}

function profilePage(user, profile, teacherProfile) {
  if (state.loading)
    return `<section class="page-enter"><div class="page-heading"><div><p class="eyebrow">ACCOUNT SETTINGS</p><h1>My profile</h1></div></div><div class="panel loading-inline"><span class="spinner"></span>Loading profile…</div></section>`;
  const userFields = `<label class="field"><span>Display name</span><input name="username" value="${escapeHtml(user.username)}"></label>
    <label class="field"><span>University email</span><input name="email" type="email" value="${escapeHtml(user.email)}"></label>
    <label class="field"><span>New password</span><input name="password" type="password" minlength="8" placeholder="Leave blank to keep the current password"></label>`;
  let academicFields = "";
  if (profile) {
    academicFields = `<div class="field-grid"><label class="field"><span>First name</span><input name="firstName" value="${escapeHtml(profile.firstName)}"></label><label class="field"><span>Last name</span><input name="lastName" value="${escapeHtml(profile.lastName)}"></label></div>
      <label class="field"><span>Department</span><input name="department" value="${escapeHtml(profile.department)}"></label>
      <label class="field"><span>${user.role === "STUDENT" ? "Major" : "Academic title"}</span><input name="${user.role === "STUDENT" ? "major" : "title"}" value="${escapeHtml(user.role === "STUDENT" ? profile.major : profile.title)}"></label>
      ${user.role === "STUDENT" ? `<label class="field"><span>Year of study</span><input name="year" type="number" min="1" value="${escapeHtml(profile.year)}"></label>` : ""}`;
  }
  const academicPanel =
    user.role === "ADMIN"
      ? ""
      : `<section class="panel profile-panel academic-profile"><div class="panel-heading"><div><span class="eyebrow">ACADEMIC DETAILS</span><h2>${user.role === "STUDENT" ? "Student profile" : "Staff profile"}</h2></div></div>${profile ? `<form class="profile-form" data-form="academic">${academicFields}<p class="field-hint">Your university identifier and account access are managed by an administrator.</p><button class="button button-primary" type="submit">Save academic profile</button></form>` : emptyState("Academic profile unavailable", "Ask an administrator to create or restore your university profile.")}</section>`;
  return `<section class="page-enter"><div class="page-heading"><div><p class="eyebrow">ACCOUNT SETTINGS</p><h1>My profile</h1><p class="page-subtitle">Update the account details you use to access UniFlow.</p></div></div>
    <section class="panel profile-panel"><div class="profile-identity"><span class="user-avatar profile-large">${escapeHtml(user.username.slice(0, 1).toUpperCase())}</span><div><strong>${escapeHtml(user.username)}</strong><span>${escapeHtml(user.email)}</span></div><span class="role-pill">${roleTitle(user.role)}</span></div><form class="profile-form" data-form="profile">${userFields}<p class="field-hint">Leave the password blank if you don’t want to change it. Your role can only be changed by an administrator.</p><button class="button button-primary" type="submit">Save account details</button></form></section>${academicPanel}</section>`;
}

function selectField(name, label, options, value = "", required = false) {
  return `<label class="field"><span>${label}${required ? " <i>*</i>" : ""}</span><select name="${name}" ${required ? "required" : ""}><option value="">Select ${label.toLowerCase()}</option>${options.map((option) => `<option value="${escapeHtml(option.value)}" ${String(option.value) === String(value) ? "selected" : ""}>${escapeHtml(option.label)}</option>`).join("")}</select></label>`;
}

function textField(
  name,
  label,
  value = "",
  type = "text",
  required = false,
  extra = "",
) {
  return `<label class="field"><span>${label}${required ? " <i>*</i>" : ""}</span><input name="${name}" type="${type}" value="${escapeHtml(value)}" ${required ? "required" : ""} ${type === "number" ? 'min="1"' : ""} ${extra}></label>`;
}

function modalView() {
  if (!state.modal) return "";
  const {
    resource,
    record,
    students = [],
    teachers = [],
    courses = [],
  } = state.modal;
  const editing = Boolean(record);
  let fields = "";
  if (resource === "students") {
    fields = `${textField("firstName", "First name", record?.firstName, "text", true)}${textField("lastName", "Last name", record?.lastName, "text", true)}${textField("department", "Department", record?.department, "text", true)}${textField("major", "Major", record?.major)}${textField("year", "Year of study", record?.year, "number", true)}${!editing ? `${textField("studentNumber", "Student number", "", "text", true)}${textField("email", "University email", "", "email", true)}${textField("username", "Username", "", "text", true)}${textField("password", "Temporary password", "", "password", true, 'minlength="8"')}` : ""}${
      editing
        ? selectField(
            "enabled",
            "Account status",
            [
              { label: "Active", value: "true" },
              { label: "Disabled", value: "false" },
            ],
            record.enabled,
          )
        : ""
    }`;
  } else if (resource === "teachers") {
    fields = `${textField("firstName", "First name", record?.firstName, "text", true)}${textField("lastName", "Last name", record?.lastName, "text", true)}${textField("department", "Department", record?.department, "text", true)}${textField("title", "Academic title", record?.title)}${!editing ? `${textField("email", "University email", "", "email", true)}${textField("username", "Username", "", "text", true)}${textField("password", "Temporary password", "", "password", true, 'minlength="8"')}` : ""}${
      editing
        ? selectField(
            "enabled",
            "Account status",
            [
              { label: "Active", value: "true" },
              { label: "Disabled", value: "false" },
            ],
            record.enabled,
          )
        : ""
    }`;
  } else if (resource === "courses") {
    fields = `${textField("courseCode", "Course code", record?.courseCode, "text", true)}${textField("title", "Course title", record?.title, "text", true)}<label class="field field-wide"><span>Description <i>*</i></span><textarea name="description" rows="3" required>${escapeHtml(record?.description)}</textarea></label>${textField("credits", "Credits", record?.credits, "number", true)}${textField("maxStudents", "Maximum students", record?.maxStudents ?? 40, "number", true)}${state.user.role === "ADMIN" ? selectField("teacherId", "Assign teacher", [{ label: "Unassigned", value: "" }, ...teachers.map((teacher) => ({ label: `${teacher.firstName} ${teacher.lastName}`, value: teacher.id }))], record?.teacherId || "") : ""}`;
  } else if (resource === "users") {
    fields = `${textField("username", "Username", "", "text", true)}${textField("email", "University email", "", "email", true)}${textField("password", "Temporary password", "", "password", true, 'minlength="8"')}${selectField(
      "role",
      "Role",
      [
        { label: "Administrator", value: "ADMIN" },
        { label: "Teacher", value: "TEACHER" },
        { label: "Student", value: "STUDENT" },
      ],
      "",
      true,
    )}`;
  } else if (resource === "enrollments") {
    fields = `${
      state.user.role === "STUDENT"
        ? ""
        : selectField(
            "studentId",
            "Student",
            students.map((student) => ({
              label: `${student.firstName} ${student.lastName} · ${student.studentNumber}`,
              value: student.id,
            })),
            "",
            true,
          )
    }${selectField(
      "courseId",
      "Course",
      courses.map((course) => ({
        label: `${course.courseCode} · ${course.title}`,
        value: course.id,
      })),
      "",
      true,
    )}${state.user.role === "STUDENT" ? '<p class="field-hint field-wide">This will enroll the student account currently signed in.</p>' : ""}`;
  }
  const title =
    resource === "enrollments"
      ? "enrollment"
      : resources[resource].title.slice(0, -1).toLowerCase();
  return `<div class="modal-backdrop" data-action="backdrop"><section class="modal-card" role="dialog" aria-modal="true"><div class="modal-heading"><div><span class="eyebrow">CAMPUS DIRECTORY</span><h2>${editing ? "Edit" : "Add"} ${title}</h2></div><button class="icon-button" data-action="close-modal" aria-label="Close">×</button></div><form class="modal-form" data-form="record" data-resource="${resource}" data-id="${record?.id || ""}">${fields}${state.message ? `<div class="alert alert-error">${escapeHtml(state.message.text)}</div>` : ""}<div class="modal-actions"><button type="button" class="button button-quiet" data-action="close-modal">Cancel</button><button class="button button-primary" type="submit">${editing ? "Save changes" : "Create"}</button></div></form></section></div>`;
}

async function loadResource(resource) {
  state.loading = true;
  state.pageData = [];
  try {
    const data = await api(resources[resource].endpoint);
    let result = data;
    if (
      state.user.role === "TEACHER" &&
      ["teachers", "courses"].includes(resource)
    ) {
      const teacher = await api("/teachers/me");
      result =
        resource === "teachers"
          ? data.filter((row) => row.id === teacher.id)
          : data
              .filter((row) => row.teacherId === teacher.id)
              .map((row) => ({ ...row, _own: true }));
      if (resource === "teachers")
        result = result.map((row) => ({ ...row, _own: true }));
      state.modalTeacherId = teacher.id;
    }
    state.pageData = result;
    if (resource === "courses" && state.user.role === "ADMIN") {
      state.pageTeachers = await api("/teachers");
    }
  } catch (error) {
    setMessage(error.message, "error");
  } finally {
    state.loading = false;
  }
}

async function openCreateModal(resource) {
  state.message = null;
  state.modal = { resource };
  if (resource === "enrollments") {
    try {
      const requests = [api("/courses")];
      if (state.user.role !== "STUDENT") requests.push(api("/students"));
      const result = await Promise.all(requests);
      state.modal.courses = result[0];
      state.modal.students = result[1] || [];
    } catch (error) {
      setMessage(error.message, "error");
    }
  } else if (resource === "courses" && state.user.role === "ADMIN") {
    try {
      state.modal.teachers = await api("/teachers");
    } catch (error) {
      setMessage(error.message, "error");
    }
  }
  void render();
}

async function openEditModal(resource, id) {
  const record = state.pageData.find((row) => row.id === Number(id));
  if (!record) return;
  state.message = null;
  state.modal = { resource, record: { ...record } };
  if (resource === "courses" && state.user.role === "ADMIN") {
    try {
      state.modal.teachers = await api("/teachers");
    } catch (error) {
      setMessage(error.message, "error");
    }
  }
  void render();
}

function payloadFromForm(form, resource, editing) {
  const values = Object.fromEntries(new FormData(form).entries());
  const numeric = [
    "year",
    "credits",
    "maxStudents",
    "teacherId",
    "studentId",
    "courseId",
  ];
  for (const key of numeric) {
    if (key in values)
      values[key] = values[key] === "" ? null : Number(values[key]);
  }
  if (resource === "students" || resource === "teachers") {
    if (values.enabled !== undefined)
      values.enabled = values.enabled === "true";
  }
  if (resource === "courses" && values.teacherId === null)
    delete values.teacherId;
  if (
    editing &&
    ["students", "teachers"].includes(resource) &&
    state.user.role !== "ADMIN"
  )
    delete values.enabled;
  return values;
}

async function submitRecord(form) {
  const resource = form.dataset.resource;
  const id = form.dataset.id;
  const endpoint = resources[resource].endpoint;
  let body = payloadFromForm(form, resource, Boolean(id));
  state.message = null;
  try {
    if (resource === "enrollments" && state.user.role === "STUDENT") {
      const student = await api("/students/me");
      body.studentId = student.id;
    }
    await request(
      id ? "PUT" : "POST",
      id ? `${endpoint}/${id}` : endpoint,
      body,
    );
    state.modal = null;
    setMessage(
      `${resource === "enrollments" ? "Enrollment" : resources[resource].title.slice(0, -1)} ${id ? "updated" : "created"}.`,
    );
    await loadResource(resource);
  } catch (error) {
    state.message = { text: error.message, type: "error" };
  }
  void render();
}

async function removeRecord(resource, id, action) {
  const row = state.pageData.find((item) => item.id === Number(id));
  const label =
    action === "withdraw"
      ? "withdraw this enrollment?"
      : `delete this ${resource.slice(0, -1)}?`;
  if (
    !window.confirm(
      `Are you sure you want to ${label} This action cannot be undone.`,
    )
  )
    return;
  try {
    if (action === "withdraw")
      await request("PUT", `/enrollments/${id}/withdraw`);
    else await request("DELETE", `${resources[resource].endpoint}/${id}`);
    setMessage(
      action === "withdraw" ? "Enrollment withdrawn." : "Record deleted.",
    );
    await loadResource(resource);
  } catch (error) {
    setMessage(error.message, "error");
  }
  void render();
}

async function loadDashboard() {
  state.loading = true;
  try {
    if (state.user.role === "ADMIN")
      state.summary = await api("/admin/summary");
    const courses = await api("/courses");
    state.pageData = courses.slice(0, 4);
  } catch (error) {
    setMessage(error.message, "error");
  } finally {
    state.loading = false;
  }
}

async function loadProfile() {
  state.loading = true;
  try {
    if (state.user.role === "STUDENT")
      state.academicProfile = await api("/students/me");
    else if (state.user.role === "TEACHER")
      state.academicProfile = await api("/teachers/me");
  } catch (error) {
    state.academicProfile = null;
    setMessage(error.message, "error");
  } finally {
    state.loading = false;
  }
}

async function submitProfile(form, academic = false) {
  const values = Object.fromEntries(new FormData(form).entries());
  const payload = Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== ""),
  );
  if (academic && payload.year) payload.year = Number(payload.year);
  try {
    if (academic) {
      const endpoint =
        state.user.role === "STUDENT" ? "/students" : "/teachers";
      await request("PUT", `${endpoint}/${state.academicProfile.id}`, payload);
      setMessage("Your academic profile has been updated.");
    } else {
      await request("PUT", `/users/${state.user.id}`, payload);
      state.user = await api("/auth/me");
      setMessage("Your account details have been updated.");
    }
  } catch (error) {
    setMessage(error.message, "error");
  }
  void render();
}

async function render() {
  const route = state.route;
  if (!state.user && localStorage.getItem(TOKEN_KEY) && !state.authChecked) {
    app.innerHTML =
      '<div class="loading-screen"><span class="spinner"></span><span>Loading your workspace…</span></div>';
    try {
      state.user = await api("/auth/me");
    } catch {
      state.user = null;
    } finally {
      state.authChecked = true;
    }
    return render();
  }
  if (
    state.user &&
    (route === "" || route === "login" || route === "register")
  ) {
    state.route = "app/dashboard";
    window.location.hash = "/app/dashboard";
    return;
  }
  if (!state.user) {
    app.innerHTML =
      route === "login"
        ? authPage()
        : route === "register"
          ? accountProvisioningPage()
          : landingPage();
    return;
  }
  const subroute = route.replace(/^app\/?/, "") || "dashboard";
  if (subroute === "dashboard") {
    await loadDashboard();
    app.innerHTML = workspaceShell(
      dashboard(state.user, state.pageData),
      state.user,
    );
  } else if (subroute === "profile") {
    await loadProfile();
    app.innerHTML = workspaceShell(
      profilePage(state.user, state.academicProfile, state.teacherProfile),
      state.user,
    );
  } else if (
    resources[subroute] &&
    roleLinks[state.user.role].includes(subroute)
  ) {
    if (state.lastLoadedRoute !== subroute) {
      state.lastLoadedRoute = subroute;
      state.search = "";
      await loadResource(subroute);
    }
    app.innerHTML = workspaceShell(tableView(subroute, state.user), state.user);
    if (state.modal?.resource === subroute)
      app.insertAdjacentHTML("beforeend", modalView());
  } else {
    state.route = "app/dashboard";
    window.location.hash = "/app/dashboard";
  }
}

app.addEventListener("click", async (event) => {
  const actionButton = event.target.closest("[data-action]");
  if (!actionButton) return;
  const { action, id } = actionButton.dataset;
  if (action === "dismiss") {
    state.message = null;
    void render();
  }
  if (action === "toggle-password") {
    const input = actionButton.parentElement.querySelector("input");
    input.type = input.type === "password" ? "text" : "password";
    actionButton.textContent = input.type === "password" ? "Show" : "Hide";
  }
  if (action === "logout") {
    localStorage.removeItem(TOKEN_KEY);
    state.user = null;
    state.authChecked = true;
    state.route = "";
    state.lastLoadedRoute = null;
    window.location.hash = "/";
    void render();
  }
  if (action === "create") await openCreateModal(state.route.split("/")[1]);
  if (action === "edit") await openEditModal(state.route.split("/")[1], id);
  if (action === "delete" || action === "withdraw")
    await removeRecord(state.route.split("/")[1], id, action);
  if (
    action === "close-modal" ||
    (action === "backdrop" && event.target === actionButton)
  ) {
    state.modal = null;
    state.message = null;
    void render();
  }
});

app.addEventListener("input", (event) => {
  if (event.target.matches("[data-search]")) {
    state.search = event.target.value;
    const resource = state.route.split("/")[1];
    app.innerHTML = workspaceShell(tableView(resource, state.user), state.user);
    if (state.modal?.resource === resource)
      app.insertAdjacentHTML("beforeend", modalView());
    const search = app.querySelector("[data-search]");
    search?.focus();
    search?.setSelectionRange(state.search.length, state.search.length);
  }
});

app.addEventListener("submit", async (event) => {
  const form = event.target.closest("form[data-form]");
  if (!form) return;
  event.preventDefault();
  const kind = form.dataset.form;
  const submitButton = form.querySelector('[type="submit"]');
  if (submitButton) submitButton.disabled = true;
  if (kind === "login") {
    state.message = { text: "Signing you in…", type: "pending" };
    if (submitButton) {
      submitButton.textContent = "Signing in…";
      submitButton.setAttribute("aria-busy", "true");
    }
    const status = document.createElement("div");
    status.className = "auth-progress";
    status.setAttribute("role", "status");
    status.setAttribute("aria-live", "polite");
    status.textContent = state.message.text;
    form.insertAdjacentElement("beforebegin", status);
    try {
      const credentials = Object.fromEntries(new FormData(form).entries());
      const response = await request("POST", "/auth/login", credentials);
      localStorage.setItem(TOKEN_KEY, response.token);
      state.user = await api("/auth/me");
      state.authChecked = true;
      state.route = "app/dashboard";
      state.lastLoadedRoute = null;
      window.location.hash = "/app/dashboard";
    } catch (error) {
      state.message = { text: error.message, type: "error" };
    }
  } else if (kind === "record") {
    await submitRecord(form);
    return;
  } else if (kind === "profile") {
    await submitProfile(form);
  } else if (kind === "academic") {
    await submitProfile(form, true);
  }
  void render();
});

app.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && state.modal) {
    state.modal = null;
    state.message = null;
    void render();
  }
});

window.addEventListener("hashchange", () => {
  state.route = routeFromHash();
  state.modal = null;
  state.message = null;
  state.lastLoadedRoute = null;
  void render();
});

render();
