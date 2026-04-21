import { check, sleep } from 'k6';
import http from 'k6/http';

import {
	CORE_BASE_URL,
	PERSONS_ME_ALLOW_404,
	PERSONS_ME_EXPECTED_STATUSES,
	ZERO_UUID,
	buildPersonUpdatePayload,
	getCurrentPersonContext,
	jsonHeaders,
	parseJsonSafe,
} from './shared.js';

const PERSONS_BY_ID_EXPECTED_STATUSES = http.expectedStatuses(200, 404);
const PERSONS_UPDATE_EXPECTED_STATUSES = PERSONS_ME_ALLOW_404
	? http.expectedStatuses(200, 404)
	: http.expectedStatuses(200);

export function personsMeScenario() {
	const personContext = getCurrentPersonContext('persons-me');
	const response = personContext.response;
	const body = personContext.body;
	const expectedStatus = PERSONS_ME_ALLOW_404
		? response.status === 200 || response.status === 404
		: response.status === 200;

	check(response, {
		'persons/me has expected status': () => expectedStatus,
		'persons/me has person id when 200': () => {
			if (response.status !== 200) {
				return true;
			}
			return !!body?.id;
		},
		'persons/me has not-found code when 404': () => {
			if (response.status !== 404) {
				return true;
			}
			return body?.code === 'USER_NOT_FOUND';
		},
	});

	sleep(0.5);
}

export function personsByIdScenario() {
	const personContext = getCurrentPersonContext('persons-me-for-by-id');
	const hasCurrentPerson =
		personContext.response.status === 200 && !!personContext.body?.id;
	const personId = hasCurrentPerson ? personContext.body.id : ZERO_UUID;

	const response = http.get(`${CORE_BASE_URL}/api/v1/persons/${personId}`, {
		headers: jsonHeaders(personContext.authContext.accessToken),
		responseCallback: PERSONS_BY_ID_EXPECTED_STATUSES,
		tags: { endpoint: 'persons-get-by-id' },
	});

	const body = parseJsonSafe(response);
	const expectedStatus = hasCurrentPerson ? 200 : 404;

	check(response, {
		'persons/{id} has expected status': (r) => r.status === expectedStatus,
		'persons/{id} has person id when 200': () => {
			if (response.status !== 200) {
				return true;
			}
			return body?.id === personId;
		},
		'persons/{id} has USER_NOT_FOUND when 404': () => {
			if (response.status !== 404) {
				return true;
			}
			return body?.code === 'USER_NOT_FOUND';
		},
	});

	sleep(0.5);
}

export function personsUpdateMeScenario() {
	const personContext = getCurrentPersonContext('persons-me-for-update');
	const hasCurrentPerson =
		personContext.response.status === 200 && !!personContext.body?.id;
	const payload = buildPersonUpdatePayload();

	const response = http.put(
		`${CORE_BASE_URL}/api/v1/persons/me`,
		JSON.stringify(payload),
		{
			headers: jsonHeaders(personContext.authContext.accessToken),
			responseCallback: PERSONS_UPDATE_EXPECTED_STATUSES,
			tags: { endpoint: 'persons-update-me' },
		}
	);

	const body = parseJsonSafe(response);
	const expectedStatus = hasCurrentPerson ? 200 : PERSONS_ME_ALLOW_404 ? 404 : 200;

	check(response, {
		'persons/me update has expected status': (r) => r.status === expectedStatus,
		'persons/me update returns updated fields when 200': () => {
			if (response.status !== 200) {
				return true;
			}
			return (
				body?.first_name === payload.first_name &&
				body?.last_name === payload.last_name
			);
		},
		'persons/me update has USER_NOT_FOUND when 404': () => {
			if (response.status !== 404) {
				return true;
			}
			return body?.code === 'USER_NOT_FOUND';
		},
	});

	sleep(0.5);
}
