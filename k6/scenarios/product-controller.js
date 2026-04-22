import { check, sleep } from 'k6';
import http from 'k6/http';

import {
	CORE_BASE_URL,
	PERSONS_ME_ALLOW_404,
	ZERO_UUID,
	buildProductPayload,
	getCurrentPersonContext,
	getVuState,
	jsonHeaders,
	parseJsonSafe,
	pickCategory,
	resolveProductIdFromList,
} from './shared.js';

const PAGE_SIZE = Number(__ENV.PRODUCTS_PAGE_SIZE || '20');
const SEARCH_QUERY = __ENV.PRODUCTS_SEARCH_QUERY || 'k6';

const STATUS_200 = http.expectedStatuses(200);
const STATUS_200_OR_404 = http.expectedStatuses(200, 404);
const STATUS_200_OR_404_IF_PERSON_OPTIONAL = PERSONS_ME_ALLOW_404
	? http.expectedStatuses(200, 404)
	: http.expectedStatuses(200);
const STATUS_204_OR_404_IF_PERSON_OPTIONAL = PERSONS_ME_ALLOW_404
	? http.expectedStatuses(204, 404)
	: http.expectedStatuses(204);

function productsListUrl() {
	return `${CORE_BASE_URL}/api/v1/products/list?page=0&size=${PAGE_SIZE}`;
}

function resolveProductIdForRead(state) {
	if (typeof state.lastProductId === 'number') {
		return state.lastProductId;
	}

	const listResponse = http.get(productsListUrl(), {
		responseCallback: STATUS_200,
		tags: { endpoint: 'products-list-bootstrap' },
	});

	const listBody = parseJsonSafe(listResponse);
	const productId = resolveProductIdFromList(listBody);
	if (typeof productId === 'number') {
		state.lastProductId = productId;
		return productId;
	}

	return 1;
}

export function productsCatalogScenario() {
	const category = pickCategory();
	const encodedQuery = encodeURIComponent(SEARCH_QUERY);

	const listResponse = http.get(productsListUrl(), {
		responseCallback: STATUS_200,
		tags: { endpoint: 'products-list' },
	});
	const searchResponse = http.get(
		`${CORE_BASE_URL}/api/v1/products/search?q=${encodedQuery}&page=0&size=${PAGE_SIZE}`,
		{
			responseCallback: STATUS_200,
			tags: { endpoint: 'products-search' },
		}
	);
	const listByCategoryResponse = http.get(
		`${CORE_BASE_URL}/api/v1/products/list/${category}?page=0&size=${PAGE_SIZE}`,
		{
			responseCallback: STATUS_200,
			tags: { endpoint: 'products-list-category' },
		}
	);
	const searchByCategoryResponse = http.get(
		`${CORE_BASE_URL}/api/v1/products/search/categories/${category}?q=${encodedQuery}&page=0&size=${PAGE_SIZE}`,
		{
			responseCallback: STATUS_200,
			tags: { endpoint: 'products-search-category' },
		}
	);

	const listBody = parseJsonSafe(listResponse);
	const searchBody = parseJsonSafe(searchResponse);
	const listByCategoryBody = parseJsonSafe(listByCategoryResponse);
	const searchByCategoryBody = parseJsonSafe(searchByCategoryResponse);
	const state = getVuState();
	const firstId =
		resolveProductIdFromList(listBody) ??
		resolveProductIdFromList(searchBody) ??
		resolveProductIdFromList(listByCategoryBody);
	if (typeof firstId === 'number') {
		state.lastProductId = firstId;
	}

	check(listResponse, {
		'products/list status is 200': (r) => r.status === 200,
		'products/list returns array': () => Array.isArray(listBody),
	});
	check(searchResponse, {
		'products/search status is 200': (r) => r.status === 200,
		'products/search returns array': () => Array.isArray(searchBody),
	});
	check(listByCategoryResponse, {
		'products/list/{category} status is 200': (r) => r.status === 200,
		'products/list/{category} returns array': () => Array.isArray(listByCategoryBody),
	});
	check(searchByCategoryResponse, {
		'products/search/categories/{category} status is 200': (r) => r.status === 200,
		'products/search/categories/{category} returns array': () =>
			Array.isArray(searchByCategoryBody),
	});

	sleep(0.5);
}

export function productsByPersonScenario() {
	const personContext = getCurrentPersonContext('persons-me-for-products-by-person');
	const hasPerson = personContext.response.status === 200 && !!personContext.body?.id;
	const personId = hasPerson ? personContext.body.id : ZERO_UUID;
	const encodedQuery = encodeURIComponent(SEARCH_QUERY);

	const listResponse = http.get(
		`${CORE_BASE_URL}/api/v1/products/persons/${personId}?page=0&size=${PAGE_SIZE}`,
		{
			responseCallback: STATUS_200_OR_404_IF_PERSON_OPTIONAL,
			tags: { endpoint: 'products-list-person' },
		}
	);
	const searchResponse = http.get(
		`${CORE_BASE_URL}/api/v1/products/persons/${personId}/search?q=${encodedQuery}&page=0&size=${PAGE_SIZE}`,
		{
			responseCallback: STATUS_200_OR_404_IF_PERSON_OPTIONAL,
			tags: { endpoint: 'products-search-person' },
		}
	);

	const listBody = parseJsonSafe(listResponse);
	const searchBody = parseJsonSafe(searchResponse);
	const expectedStatus = hasPerson ? 200 : PERSONS_ME_ALLOW_404 ? 404 : 200;

	check(listResponse, {
		'products/persons/{personId} has expected status': (r) =>
			r.status === expectedStatus,
		'products/persons/{personId} returns array on 200': () => {
			if (listResponse.status !== 200) {
				return true;
			}
			return Array.isArray(listBody);
		},
		'products/persons/{personId} has USER_NOT_FOUND on 404': () => {
			if (listResponse.status !== 404) {
				return true;
			}
			return listBody?.code === 'USER_NOT_FOUND';
		},
	});
	check(searchResponse, {
		'products/persons/{personId}/search has expected status': (r) =>
			r.status === expectedStatus,
		'products/persons/{personId}/search returns array on 200': () => {
			if (searchResponse.status !== 200) {
				return true;
			}
			return Array.isArray(searchBody);
		},
		'products/persons/{personId}/search has USER_NOT_FOUND on 404': () => {
			if (searchResponse.status !== 404) {
				return true;
			}
			return searchBody?.code === 'USER_NOT_FOUND';
		},
	});

	const state = getVuState();
	const foundId = resolveProductIdFromList(listBody) ?? resolveProductIdFromList(searchBody);
	if (typeof foundId === 'number') {
		state.lastProductId = foundId;
	}

	sleep(0.5);
}

export function productsGetByIdScenario() {
	const state = getVuState();
	const productId = resolveProductIdForRead(state);

	const response = http.get(`${CORE_BASE_URL}/api/v1/products/${productId}`, {
		responseCallback: STATUS_200_OR_404,
		tags: { endpoint: 'products-get' },
	});
	const body = parseJsonSafe(response);

	check(response, {
		'products/{id} has expected status': (r) => r.status === 200 || r.status === 404,
		'products/{id} returns same id when 200': () => {
			if (response.status !== 200) {
				return true;
			}
			return body?.id === productId;
		},
		'products/{id} has PRODUCT_NOT_FOUND on 404': () => {
			if (response.status !== 404) {
				return true;
			}
			return body?.code === 'PRODUCT_NOT_FOUND';
		},
	});

	if (response.status === 200 && typeof body?.id === 'number') {
		state.lastProductId = body.id;
		state.lastProductName = body?.name || state.lastProductName;
	}

	sleep(0.5);
}

export function productsCrudScenario() {
	const personContext = getCurrentPersonContext('persons-me-for-products-crud');
	const state = getVuState();
	const hasPerson = personContext.response.status === 200 && !!personContext.body?.id;
	const createPayload = buildProductPayload('create');

	const createResponse = http.post(
		`${CORE_BASE_URL}/api/v1/products`,
		JSON.stringify(createPayload),
		{
			headers: jsonHeaders(personContext.authContext.accessToken),
			responseCallback: STATUS_200_OR_404_IF_PERSON_OPTIONAL,
			tags: { endpoint: 'products-create' },
		}
	);
	const createBody = parseJsonSafe(createResponse);
	const expectedCreateStatus = hasPerson ? 200 : PERSONS_ME_ALLOW_404 ? 404 : 200;

	check(createResponse, {
		'products create has expected status': (r) => r.status === expectedCreateStatus,
		'products create returns id on 200': () => {
			if (createResponse.status !== 200) {
				return true;
			}
			return typeof createBody?.id === 'number';
		},
		'products create has USER_NOT_FOUND on 404': () => {
			if (createResponse.status !== 404) {
				return true;
			}
			return createBody?.code === 'USER_NOT_FOUND';
		},
	});

	if (createResponse.status !== 200 || typeof createBody?.id !== 'number') {
		sleep(0.5);
		return;
	}

	const productId = createBody.id;
	state.lastProductId = productId;
	state.lastProductName = createBody?.name || state.lastProductName;

	const updatePayload = {
		...buildProductPayload('update'),
		name: `${createBody.name}-updated`,
	};

	const updateResponse = http.put(
		`${CORE_BASE_URL}/api/v1/products/${productId}`,
		JSON.stringify(updatePayload),
		{
			headers: jsonHeaders(personContext.authContext.accessToken),
			responseCallback: STATUS_200,
			tags: { endpoint: 'products-update' },
		}
	);
	const updateBody = parseJsonSafe(updateResponse);

	const getResponse = http.get(`${CORE_BASE_URL}/api/v1/products/${productId}`, {
		responseCallback: STATUS_200,
		tags: { endpoint: 'products-get-after-update' },
	});
	const getBody = parseJsonSafe(getResponse);

	const deleteResponse = http.del(`${CORE_BASE_URL}/api/v1/products/${productId}`, null, {
		headers: jsonHeaders(personContext.authContext.accessToken),
		responseCallback: STATUS_204_OR_404_IF_PERSON_OPTIONAL,
		tags: { endpoint: 'products-delete' },
	});
	const deleteBody = parseJsonSafe(deleteResponse);

	check(updateResponse, {
		'products update status is 200': (r) => r.status === 200,
		'products update returns updated name': () =>
			updateBody?.name === updatePayload.name,
	});
	check(getResponse, {
		'products get-after-update status is 200': (r) => r.status === 200,
		'products get-after-update keeps id': () => getBody?.id === productId,
	});
	check(deleteResponse, {
		'products delete has expected status': (r) => r.status === 204,
		'products delete response is empty on 204': () => {
			if (deleteResponse.status !== 204) {
				return true;
			}
			return deleteBody === null;
		},
	});

	state.lastProductId = null;
	state.lastProductName = null;

	sleep(0.5);
}

export function productPhotosListScenario() {
	const state = getVuState();
	const productId = resolveProductIdForRead(state);

	const response = http.get(`${CORE_BASE_URL}/api/v1/photos/products/${productId}`, {
		responseCallback: STATUS_200_OR_404,
		tags: { endpoint: 'photos-products-list' },
	});
	const body = parseJsonSafe(response);

	check(response, {
		'photos/products/{id} has expected status': (r) =>
			r.status === 200 || r.status === 404,
		'photos/products/{id} returns array on 200': () => {
			if (response.status !== 200) {
				return true;
			}
			return Array.isArray(body);
		},
		'photos/products/{id} has PRODUCT_NOT_FOUND on 404': () => {
			if (response.status !== 404) {
				return true;
			}
			return body?.code === 'PRODUCT_NOT_FOUND';
		},
	});

	sleep(0.5);
}
