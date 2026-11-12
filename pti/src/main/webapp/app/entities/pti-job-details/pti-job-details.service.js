(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('PtiJobDetails', PtiJobDetails);

    PtiJobDetails.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function PtiJobDetails ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+ 'pti-job-details/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateDerniereExecution = DateUtils.convertDateTimeFromServer(data.dateDerniereExecution);
                        data.dateProchaineExecution = DateUtils.convertDateTimeFromServer(data.dateProchaineExecution);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
