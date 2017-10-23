module.exports = {
	setJSONBody : setJSONBody,
	logRequest : logRequest,
	logHeaders : logHeaders
}
function setJSONBody(requestParams, context, ee, next) {
	return next(); // MUST be called for the scenario to continue
}

function logHeaders(requestParams, response, context, ee, next) {
	console.log(requestParams.headers);
	console.log(response.headers);
	return next(); // MUST be called for the scenario to continue
}

function logRequest(requestParams, context, ee, next) {
	//console.log(requestParams);
	return next(); // MUST be called for the scenario to continue
}