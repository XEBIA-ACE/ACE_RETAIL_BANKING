(function () {
    "use strict";

    const ICONS = { TRANSACTION: "\u21c4", BALANCE: "\u25bc", BILL: "\u2709" };
    const API = "/api/v1/notifications/history";

    const params = new URLSearchParams(window.location.search);
    const state = { labels: {}, token: "" };
    const el = {
        root: document.getElementById("notification-history"),
        form: document.getElementById("settings-form"),
        language: document.getElementById("language"),
        token: document.getElementById("access-token"),
        status: document.getElementById("live-status"),
        error: document.getElementById("error-banner"),
        empty: document.getElementById("empty-state"),
        list: document.getElementById("notification-list"),
        retention: document.getElementById("retention-note"),
        timezone: document.getElementById("timezone-note"),
        returnLink: document.getElementById("return-link")
    };

    function t(key, arg) {
        const text = state.labels[key] || key;
        return arg === undefined ? text : text.replace("{0}", String(arg));
    }

    function browserTimeZone() {
        try {
            return Intl.DateTimeFormat().resolvedOptions().timeZone || "";
        } catch (e) {
            return "";
        }
    }

    function applyLabels() {
        document.documentElement.lang = el.language.value;
        document.querySelectorAll("[data-i18n]").forEach(function (node) {
            node.textContent = t(node.dataset.i18n);
        });
        document.querySelectorAll("[data-i18n-aria-label]").forEach(function (node) {
            node.setAttribute("aria-label", t(node.dataset.i18nAriaLabel));
        });
    }

    async function loadLabels() {
        const response = await fetch(API + "/labels?lang=" + encodeURIComponent(el.language.value),
            { headers: { Accept: "application/json" } });
        if (response.ok) {
            state.labels = (await response.json()).labels;
            applyLabels();
        }
    }

    function setState(name, message) {
        el.root.dataset.state = name;
        el.root.setAttribute("aria-busy", String(name === "loading"));
        el.error.hidden = name !== "error";
        el.empty.hidden = name !== "empty";
        el.list.hidden = name !== "loaded";
        if (name === "error") {
            el.error.textContent = message;
        }
        el.status.textContent = name === "error" ? "" : (message || "");
    }

    function renderItem(n) {
        const item = document.createElement("li");
        item.className = "notification notification--" + n.category.toLowerCase();
        item.dataset.notificationId = n.notificationId;
        item.dataset.category = n.category;

        const article = document.createElement("article");
        article.setAttribute("aria-label", n.categoryLabel + ": " + n.typeLabel + ", " + n.formattedTimestamp);

        const badge = document.createElement("p");
        badge.className = "badge";
        const icon = document.createElement("span");
        icon.className = "badge-icon";
        icon.setAttribute("aria-hidden", "true");
        icon.textContent = ICONS[n.category] || "\u2022";
        const category = document.createElement("span");
        category.textContent = n.categoryLabel;
        badge.append(icon, category);

        const title = document.createElement("h2");
        title.className = "notification-type";
        title.textContent = n.typeLabel;

        const description = document.createElement("p");
        description.className = "notification-description";
        description.textContent = n.description;

        const meta = document.createElement("p");
        meta.className = "notification-meta";
        const sent = document.createElement("span");
        sent.textContent = t("history.timestamp.label") + ": ";
        const time = document.createElement("time");
        time.dateTime = n.timestamp;
        time.textContent = n.formattedTimestamp;
        meta.append(sent, time);

        article.append(badge, title, description, meta);
        item.append(article);
        return item;
    }

    function render(body) {
        el.retention.textContent = t("history.retention", body.retentionDays);
        el.timezone.textContent = t("history.timezone", body.timeZone);
        if (body.returnPath) {
            el.returnLink.href = body.returnPath;
            el.returnLink.textContent = body.returnLabel;
            el.returnLink.hidden = false;
        }
        const notifications = body.notifications || [];
        el.list.replaceChildren.apply(el.list, notifications.map(renderItem));
        setState(notifications.length ? "loaded" : "empty",
            notifications.length ? t("history.count", notifications.length) : t("history.empty"));
    }

    function query() {
        const q = new URLSearchParams();
        q.set("lang", el.language.value);
        const tz = browserTimeZone();
        if (tz) {
            q.set("tz", tz);
        }
        if (params.get("from")) {
            q.set("from", params.get("from"));
        }
        el.form.querySelectorAll("input[name=types]:checked").forEach(function (box) {
            q.append("types", box.value);
        });
        return q.toString();
    }

    async function load() {
        setState("loading", t("history.loading"));
        try {
            const response = await fetch(API + "?" + query(), {
                headers: { Authorization: "Bearer " + state.token, Accept: "application/json" }
            });
            if (response.status === 401 || response.status === 403) {
                setState("error", t("history.error.session"));
            } else if (response.status === 400) {
                setState("error", t("history.error.invalidRequest"));
            } else if (!response.ok) {
                setState("error", t("history.error.generic"));
            } else {
                render(await response.json());
            }
        } catch (e) {
            setState("error", t("history.error.generic"));
        }
    }

    const lang = (params.get("lang") || navigator.language || "en").slice(0, 2).toLowerCase();
    if (el.language.querySelector("option[value='" + lang + "']")) {
        el.language.value = lang;
    }
    el.language.addEventListener("change", async function () {
        await loadLabels();
        if (state.token) {
            load();
        }
    });
    el.form.addEventListener("submit", function (event) {
        event.preventDefault();
        state.token = el.token.value;
        load();
    });
    window.NotificationHistory = { load: load, render: render };
    loadLabels();
}());
