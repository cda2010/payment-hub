(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TransactionComptePti', TransactionComptePti);

    TransactionComptePti.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function TransactionComptePti ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'transaction-compte-ptis/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'findListTransaction': {method: 'GET',url:BASE_URL_API+'transaction-compte-ptis/list/ByCompteAutre', isArray: true},
            'queryByCompteAutre': {method: 'GET',url:BASE_URL_API+'transaction-compte-ptis/ByCompteAutre', isArray: true},
            'queryByClientId': {method: 'GET', url: BASE_URL_API+'transaction-compte-ptis/Byclient', isArray: true},
            'queryByCompteAutreOptimise': {method: 'GET',url:BASE_URL_API+'transaction-compte-ptis/ByCompteAutre-optimise', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.date = DateUtils.convertDateTimeFromServer(data.date);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
