(function () {
    const TYPE_LABELS = {
        LARGE_TRANSACTION: "Large transaction",
        LOW_BALANCE: "Low balance",
        BILL_REMINDER: "Bill reminder"
    };
    const TOKEN_KEY = "notificationHistory.accessToken";

    const elements = {
        root: document.getElementById("notification-history"),
        form: document.getElementById("session-form"),
        token: document.getElementById("access-token"),
        loading: document.getElementById("loading"),
        error: document.getElementById("error-banner"),
        empty: document.getElementById("empty-state"),
        list: document.getElementById("notification-list")
    };

    function typeLabel(type) {
        if (TYPE_LABELS[type]) {
            return TYPE_LABELS[type];
        }
        const words = type.toLowerCase().split("_").join(" ");
        return words.charAt(0).toUpperCase() + words.slice(1);
    }

    function setState(state) {
        elements.root.dataset.state = state;
        elements.loading.hidden = state !== "loading";
        elements.error.hidden = state !== "error";
        elements.empty.hidden = state !== "empty";
        elements.list.hidden = state !== "loaded";
        elements.list.setAttribute("aria-busy", String(state === "loading"));
    }

    function renderItem(notification) {
        const item = document.createElement("li");
        item.className = "notification";
        item.dataset.notificationId = notification.notificationId;

        const title = document.createElement("h2");
        title.className = "notification-type";
        title.textContent = typeLabel(notification.notificationType);

        const message = document.createElement("p");
        message.className = "notification-message";
        message.textContent = notification.message;

        const meta = document.createElement("p");
        meta.className = "notification-meta";
        const time = document.createElement("time");
        time.dateTime = notification.notificationTimestamp;
        time.textContent = new Date(notification.notificationTimestamp).toLocaleString();
        meta.append(time);

        item.append(title, message, meta);
        return item;
    }

    function render(notifications) {
        if (notifications.length === 0) {
            elements.list.replaceChildren();
            setState("empty");
            return;
        }
        elements.list.replaceChildren(...notifications.map(renderItem));
        setState("loaded");
    }

    function showError(text) {
        elements.error.textContent = text;
        setState("error");
    }

    async function load() {
        setState("loading");
        performance.mark("notification-history:start");
        try {
            const response = await fetch("/api/v1/notifications/history", {
                headers: { Authorization: "Bearer " + elements.token.value, Accept: "application/json" }
            });
            if (response.status === 401 || response.status === 403) {
                showError("Please sign in again to view your notification history.");
                return;
            }
            if (!response.ok) {
                showError("Your notification history could not be loaded. Please try again.");
                return;
            }
            const body = await response.json();
            render(body.notifications || []);
        } catch (error) {
            showError("Your notification history could not be loaded. Please try again.");
        } finally {
            performance.mark("notification-history:end");
            const measure = performance.measure("notification-history:load",
                "notification-history:start", "notification-history:end");
            elements.root.dataset.loadMs = String(Math.round(measure.duration));
        }
    }

    window.NotificationHistory = { load, render };
    elements.token.value = sessionStorage.getItem(TOKEN_KEY) || "";
    elements.form.addEventListener("submit", function (event) {
        event.preventDefault();
        sessionStorage.setItem(TOKEN_KEY, elements.token.value);
        load();
    });
    if (elements.token.value) {
        load();
    }
}());
