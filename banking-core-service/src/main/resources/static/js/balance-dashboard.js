(function () {
    const state = {
        client: null,
        timer: null,
        stopped: true,
        retryMs: 1000,
        snapshot: null
    };

    const elements = {
        root: document.getElementById("dashboard"),
        form: document.getElementById("connection-form"),
        jwt: document.getElementById("jwt"),
        accountId: document.getElementById("account-id"),
        disconnect: document.getElementById("disconnect"),
        banner: document.getElementById("error-banner"),
        balance: document.getElementById("balance"),
        currency: document.getElementById("currency"),
        timestamp: document.getElementById("timestamp")
    };

    function render() {
        const value = state.snapshot;
        if (!value) {
            elements.balance.textContent = "Loading…";
            elements.currency.textContent = "";
            elements.timestamp.textContent = "";
            return;
        }
        elements.balance.textContent = value.balance;
        elements.currency.textContent = value.currency;
        elements.timestamp.textContent = value.balanceTimestamp +
            " (" + new Date(value.balanceTimestamp).toLocaleString() + ")";
    }

    function setDegraded() {
        if (!state.snapshot) {
            return;
        }
        elements.root.dataset.state = "degraded";
        elements.banner.hidden = false;
        elements.banner.querySelector("span:last-child").textContent =
            "Live balance feed unavailable — showing last known balance as of " +
            state.snapshot.balanceTimestamp;
    }

    async function loadSnapshot() {
        const response = await fetch("/api/accounts/" + encodeURIComponent(elements.accountId.value) + "/balance", {
            headers: { Authorization: "Bearer " + elements.jwt.value }
        });
        if (!response.ok) {
            throw new Error("Balance request failed");
        }
        state.snapshot = await response.json();
        render();
    }

    function scheduleReconnect() {
        if (state.stopped || state.timer) {
            return;
        }
        setDegraded();
        state.timer = setTimeout(async function reconnect() {
            state.timer = null;
            try {
                await loadSnapshot();
                state.retryMs = 1000;
                await connectSocket();
            } catch (error) {
                state.retryMs = Math.min(state.retryMs * 2, 30000);
                scheduleReconnect();
            }
        }, state.retryMs);
    }

    async function connectSocket() {
        const client = new StompJs.Client({
            webSocketFactory: function () {
                return new SockJS("/ws");
            },
            connectHeaders: { Authorization: "Bearer " + elements.jwt.value },
            onConnect: function () {
                state.client.subscribe("/topic/accounts/" + elements.accountId.value + "/balance", function (message) {
                    state.snapshot = JSON.parse(message.body);
                    render();
                });
                elements.root.dataset.state = "connected";
                elements.banner.hidden = true;
            },
            onWebSocketClose: scheduleReconnect,
            onStompError: scheduleReconnect
        });
        state.client = client;
        client.activate();
    }

    async function connect() {
        state.stopped = false;
        elements.disconnect.disabled = false;
        localStorage.setItem("balance.jwt", elements.jwt.value);
        localStorage.setItem("balance.accountId", elements.accountId.value);
        await loadSnapshot();
        await connectSocket();
    }

    function disconnect() {
        state.stopped = true;
        if (state.timer) {
            clearTimeout(state.timer);
            state.timer = null;
        }
        if (state.client) {
            state.client.deactivate();
            state.client = null;
        }
        elements.disconnect.disabled = true;
    }

    window.BalanceDashboard = { connect, disconnect, loadSnapshot, render, state };
    elements.jwt.value = localStorage.getItem("balance.jwt") || "";
    elements.accountId.value = localStorage.getItem("balance.accountId") || "";
    elements.form.addEventListener("submit", function (event) {
        event.preventDefault();
        connect().catch(function () {
            setDegraded();
            scheduleReconnect();
        });
    });
    elements.disconnect.addEventListener("click", disconnect);
    render();
}());
