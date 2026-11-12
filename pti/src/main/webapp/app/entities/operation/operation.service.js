(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Operation', Operation);

    Operation.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function Operation($resource, DateUtils,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'operations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'queryByHour': { method: 'GET',url:BASE_URL_API+'operations/byhour/:periode', isArray: true },
            'queryByDay': { method: 'GET',url:BASE_URL_API+'operations/byday/:periode', isArray: true },
            'queryByMonth': { method: 'GET',url:BASE_URL_API+'operations/bymonth/:periode', isArray: true },
            'queryByYear': { method: 'GET',url:BASE_URL_API+'operations/byyear', isArray: true },
            'queryStatut': {method: 'GET', url:BASE_URL_API+'operations', isArray: true},
            'findTransaction': {method: 'GET', url:BASE_URL_API+'findByOperation', isArray: true},
            'findTransactionOptimise': {method: 'GET', url:BASE_URL_API+'findByOperation-optimise', isArray: true},
            'getCommission': {method: 'GET', url:BASE_URL_API+'operation-commission', isArray: true},
            'findCompteByCptAutre': {method: 'GET', url:BASE_URL_API+'findByCptAutre', isArray: false},
            'queryByTop10': { method: 'GET',url:BASE_URL_API+'operations/opt/dashboard', isArray: true },
            'getOneOptimise': {method: 'GET', url:BASE_URL_API+'operations-optimise/:id', isArray: false},
            'getStatut': {method: 'GET', url:BASE_URL_API+'operation/statut-operation', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateOperation = DateUtils.convertDateTimeFromServer(data.dateOperation);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }
})();
