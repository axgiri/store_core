export {
	personsByIdScenario,
	personsMeScenario,
	personsUpdateMeScenario,
} from './scenarios/person-controller.js';
export {
	productPhotosListScenario,
	productsByPersonScenario,
	productsCatalogScenario,
	productsCrudScenario,
	productsGetByIdScenario,
} from './scenarios/product-controller.js';

function envBoolean(name, defaultValue) {
	const raw = __ENV[name];
	if (raw === undefined || raw === '') {
		return defaultValue;
	}
	return String(raw).toLowerCase() === 'true';
}

function envNumber(name, defaultValue) {
	const raw = Number(__ENV[name]);
	if (!Number.isFinite(raw) || raw <= 0) {
		return defaultValue;
	}
	return raw;
}

function constantVusScenario(exec, vusOverrideEnv, durationOverrideEnv, defaultVus) {
	const vus = envNumber(vusOverrideEnv, defaultVus);
	const duration = __ENV[durationOverrideEnv] || __ENV.K6_DEFAULT_DURATION || '45s';

	return {
		executor: 'constant-vus',
		exec,
		vus,
		duration,
	};
}

const scenarios = {
	personsMe: constantVusScenario(
		'personsMeScenario',
		'K6_PERSONS_ME_VUS',
		'K6_PERSONS_ME_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 6)
	),
	personsById: constantVusScenario(
		'personsByIdScenario',
		'K6_PERSONS_BY_ID_VUS',
		'K6_PERSONS_BY_ID_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 6)
	),
	personsUpdateMe: constantVusScenario(
		'personsUpdateMeScenario',
		'K6_PERSONS_UPDATE_ME_VUS',
		'K6_PERSONS_UPDATE_ME_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 4)
	),
	productsCatalog: constantVusScenario(
		'productsCatalogScenario',
		'K6_PRODUCTS_CATALOG_VUS',
		'K6_PRODUCTS_CATALOG_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 8)
	),
	productsByPerson: constantVusScenario(
		'productsByPersonScenario',
		'K6_PRODUCTS_BY_PERSON_VUS',
		'K6_PRODUCTS_BY_PERSON_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 6)
	),
	productsGetById: constantVusScenario(
		'productsGetByIdScenario',
		'K6_PRODUCTS_GET_BY_ID_VUS',
		'K6_PRODUCTS_GET_BY_ID_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 6)
	),
};

if (envBoolean('K6_ENABLE_PRODUCTS_CRUD_SCENARIO', true)) {
	scenarios.productsCrud = constantVusScenario(
		'productsCrudScenario',
		'K6_PRODUCTS_CRUD_VUS',
		'K6_PRODUCTS_CRUD_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 4)
	);
}

if (envBoolean('K6_ENABLE_PRODUCT_PHOTOS_SCENARIO', true)) {
	scenarios.productPhotosList = constantVusScenario(
		'productPhotosListScenario',
		'K6_PRODUCT_PHOTOS_VUS',
		'K6_PRODUCT_PHOTOS_DURATION',
		envNumber('K6_VUS_PER_SCENARIO', 4)
	);
}

export const options = {
	thresholds: {
		http_req_duration: ['p(95)<500'],
		http_req_failed: ['rate<0.01'],
	},
	scenarios,
};

export default function () {}