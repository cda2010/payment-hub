(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Commission', Commission);

    Commission.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function Commission($resource, DateUtils,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'commissions/:id';
        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.datePaiement = DateUtils.convertDateTimeFromServer(data.datePaiement);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }
})();