import http from 'k6/http';

import { authHeaders, getCoreBaseUrl, getVuAuthContext } from '../helpers/auth.js';

export const CORE_BASE_URL = getCoreBaseUrl();
export const ZERO_UUID = '00000000-0000-0000-0000-000000000000';
export const PERSONS_ME_ALLOW_404 =
	String(__ENV.PERSONS_ME_ALLOW_404 || 'false').toLowerCase() === 'true';

export const PERSONS_ME_EXPECTED_STATUSES = PERSONS_ME_ALLOW_404
	? http.expectedStatuses(200, 404)
	: http.expectedStatuses(200);

const CATEGORY_VALUES = [
	'LAPTOPS',
	'PC',
	'PSCOMPONENTS',
	'TABLETS',
	'SMARTPHONES',
	'ACCESSORIES',
	'DEVICES',
	'OTHERS',
];

const vuStateCache = new Map();

export function parseJsonSafe(response) {
	try {
		return response.json();
	} catch {
		return null;
	}
}

export function jsonHeaders(accessToken) {
	return {
		...authHeaders(accessToken),
		'Content-Type': 'application/json',
	};
}

export function getVuState() {
	const key = String(__VU);
	if (!vuStateCache.has(key)) {
		vuStateCache.set(key, {
			personId: null,
			lastProductId: null,
			lastProductName: null,
		});
	}
	return vuStateCache.get(key);
}

export function getCurrentPersonContext(endpoint = 'persons-me-bootstrap') {
	const authContext = getVuAuthContext();
	const response = http.get(`${CORE_BASE_URL}/api/v1/persons/me`, {
		headers: authHeaders(authContext.accessToken),
		responseCallback: PERSONS_ME_EXPECTED_STATUSES,
		tags: { endpoint },
	});
	const body = parseJsonSafe(response);
	const state = getVuState();

	if (response.status === 200 && body?.id) {
		state.personId = body.id;
	}

	return {
		authContext,
		response,
		body,
		state,
	};
}

export function pickCategory() {
	const index = (__VU + __ITER) % CATEGORY_VALUES.length;
	return CATEGORY_VALUES[index];
}

export function buildPersonUpdatePayload() {
	const phoneSuffix = 1000000000 + ((__VU * 1000 + __ITER) % 9000000000);
	return {
		first_name: `K6First${__VU}`,
		last_name: `K6Last${__ITER % 1000}`,
		phone_number: `+7${phoneSuffix}`,
	};
}

export function buildProductPayload(kind = 'base') {
	const seed = `${kind}-${__VU}-${__ITER}`;
	return {
		name: `k6-product-${seed}`,
		description: `k6 generated payload ${seed}`,
		price: 100 + ((__VU + __ITER) % 50),
		category: pickCategory(),
		tags: ['k6', 'stress'],
		hidden_labels: [`vu-${__VU}`, `iter-${__ITER}`],
		attributes: {
			source: 'k6',
			vu: String(__VU),
			iter: String(__ITER),
		},
	};
}

export function resolveProductIdFromList(responseBody) {
	if (!Array.isArray(responseBody) || responseBody.length === 0) {
		return null;
	}

	const firstWithId = responseBody.find(
		(item) => item && typeof item.id === 'number'
	);
	return firstWithId?.id ?? null;
}
