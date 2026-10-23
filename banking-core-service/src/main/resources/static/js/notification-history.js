/*
 * US-028 notification history screen (DS-005, DS-006, DS-007).
 * The customer is identified only by the bearer token issued at sign-in; the page never sends a
 * customer id, so it cannot request another customer's notifications (FR-008, FR-009).
 */
(function () {
    'use strict';

    var API = '/api/v1/notifications/history';
    var TOKEN_KEY = 'bankingAccessToken';
    var LANGUAGE_KEY = 'bankingLanguage';
    var SUPPORTED = ['en', 'de', 'fr', 'it'];
    var ICONS = { TRANSACTION: '\u21c4', BALANCE: '\u25d4', BILL: '\u2709' };

    var state = { lang: 'en', labels: {}, page: 0, totalPages: 0, from: null };
    var params = new URLSearchParams(window.location.search);
    var el = {};

    function byId(id) {
        return document.getElementById(id);
    }

    function resolveLanguage() {
        var candidates = [params.get('lang'), window.localStorage.getItem(LANGUAGE_KEY), navigator.language];
        for (var i = 0; i < candidates.length; i++) {
            var lang = (candidates[i] || '').toLowerCase().split('-')[0];
            if (SUPPORTED.indexOf(lang) >= 0) {
                return lang;
            }
        }
        return 'en';
    }

    function timeZone() {
        try {
            return Intl.DateTimeFormat().resolvedOptions().timeZone || '';
        } catch (e) {
            return '';
        }
    }

    function label(key) {
        return state.labels[key] || '';
    }

    function applyLabels(response) {
        state.labels = response.labels;
        document.documentElement.lang = state.lang;
        document.title = label('page.title');
        document.querySelectorAll('[data-i18n]').forEach(function (node) {
            var text = label(node.getAttribute('data-i18n'));
            if (text) {
                node.textContent = text;
            }
        });
        renderBackLink();
    }

    function renderBackLink() {
        var key = { 'dashboard': 'DASHBOARD', 'security-center': 'SECURITY_CENTER', 'bill-payments': 'BILL_PAYMENTS' }[state.from];
        if (!key) {
            el.backNav.hidden = true;
            return;
        }
        el.backLink.textContent = label('back.' + key);
        el.backLink.href = '/' + state.from;
        el.backNav.hidden = false;
    }

    function setStatus(text) {
        el.status.textContent = text;
    }

    function showError(text) {
        el.error.textContent = text;
        el.error.hidden = !text;
    }

    function historyUrl(page) {
        var q = new URLSearchParams({ lang: state.lang, page: String(page) });
        var tz = timeZone();
        if (tz) { q.set('tz', tz); }
        if (el.filter.value) { q.set('category', el.filter.value); }
        if (state.from) { q.set('from', state.from); }
        return API + '?' + q.toString();
    }

    function renderEntry(item, index) {
        var li = document.createElement('li');
        var category = item.category.toLowerCase();
        var typeId = 'nh-type-' + item.id;
        var descId = 'nh-desc-' + item.id;
        var timeId = 'nh-time-' + item.id;
        li.className = 'nh-entry nh-entry--' + category;
        li.tabIndex = 0;
        li.setAttribute('data-category', item.category);
        li.setAttribute('aria-labelledby', typeId + ' ' + descId + ' ' + timeId);
        li.setAttribute('aria-posinset', String(index + 1));

        var badge = document.createElement('span');
        badge.className = 'nh-badge';
        badge.id = typeId;
        var icon = document.createElement('span');
        icon.className = 'nh-badge__icon';
        icon.setAttribute('aria-hidden', 'true');
        icon.textContent = ICONS[item.category] || '';
        var typeText = document.createElement('span');
        typeText.textContent = item.typeLabel + ' (' + item.categoryLabel + ')';
        badge.appendChild(icon);
        badge.appendChild(typeText);

        var body = document.createElement('div');
        body.className = 'nh-entry__body';
        var desc = document.createElement('p');
        desc.className = 'nh-entry__description';
        desc.id = descId;
        desc.textContent = item.description;
        var time = document.createElement('time');
        time.className = 'nh-entry__time';
        time.id = timeId;
        time.dateTime = item.occurredAt;
        time.textContent = label('entry.sentAt') + ': ' + item.occurredAtFormatted;
        body.appendChild(desc);
        body.appendChild(time);

        li.appendChild(badge);
        li.appendChild(body);
        return li;
    }

    function renderPage(response, append) {
        var offset = append ? el.list.children.length : 0;
        if (!append) {
            el.list.textContent = '';
        }
        var first = null;
        response.items.forEach(function (item, i) {
            var node = renderEntry(item, offset + i);
            el.list.appendChild(node);
            first = first || node;
        });
        state.page = response.page;
        state.totalPages = response.totalPages;
        el.loadMore.hidden = state.page + 1 >= state.totalPages;
        setStatus(response.totalElements === 0 ? label('status.empty') : response.summary);
        if (append && first) {
            first.focus();
        }
    }

    function fetchJson(url, token) {
        var headers = { 'Accept': 'application/json', 'Accept-Language': state.lang };
        if (token) { headers.Authorization = 'Bearer ' + token; }
        return fetch(url, { headers: headers, credentials: 'same-origin' }).then(function (res) {
            if (!res.ok) {
                var error = new Error('HTTP ' + res.status);
                error.status = res.status;
                throw error;
            }
            return res.json();
        });
    }

    function loadHistory(page, append) {
        var token = window.sessionStorage.getItem(TOKEN_KEY);
        showError('');
        if (!token) {
            showError(label('error.signInRequired'));
            return Promise.resolve();
        }
        setStatus(label('status.loading'));
        el.list.setAttribute('aria-busy', 'true');
        return fetchJson(historyUrl(page), token)
            .then(function (response) { renderPage(response, append); })
            .catch(function (err) {
                setStatus('');
                showError(err.status === 401 ? label('error.signInRequired') : label('error.loadFailed'));
            })
            .finally(function () { el.list.removeAttribute('aria-busy'); });
    }

    function loadLabels() {
        return fetchJson(API + '/labels?lang=' + encodeURIComponent(state.lang)).then(applyLabels);
    }

    function init() {
        el = {
            list: byId('notification-list'), status: byId('status'), error: byId('error'),
            filter: byId('category-filter'), language: byId('language-select'), loadMore: byId('load-more'),
            backNav: byId('back-nav'), backLink: byId('back-link')
        };
        state.lang = resolveLanguage();
        state.from = params.get('from');
        el.language.value = state.lang;
        el.filter.addEventListener('change', function () { loadHistory(0, false); });
        el.language.addEventListener('change', function () {
            state.lang = el.language.value;
            window.localStorage.setItem(LANGUAGE_KEY, state.lang);
            loadLabels().then(function () { return loadHistory(0, false); });
        });
        el.loadMore.addEventListener('click', function () { loadHistory(state.page + 1, true); });
        loadLabels()
            .catch(function () { showError('Notification history is unavailable.'); })
            .then(function () { return loadHistory(0, false); });
    }

    document.addEventListener('DOMContentLoaded', init);
}());
