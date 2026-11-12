(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneRepartitionCommission', LigneRepartitionCommission)
        .factory('LigneRepartitionCommissionByTableau', LigneRepartitionCommissionByTableau);

    LigneRepartitionCommission.$inject = ['$resource','BASE_URL_API'];
    LigneRepartitionCommissionByTableau.$inject = ['$resource','BASE_URL_API'];

    function LigneRepartitionCommission($resource,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'ligne-repartition-commissions/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'get': {
                method: 'GET',
                transformResponse: function(data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method: 'PUT' }
        });
    }

    function LigneRepartitionCommissionByTableau($resource,BASE_URL_API) {
        //var resourceUrl = BASE_URL_API+'ligne-repartition-commissions/:id/tableauRepartition';
        var resourceUrl = BASE_URL_API+'ligne-repartition-commissions/tableauRepartition/byId';
        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true },
            'queryOptimise': { method: 'GET', url:BASE_URL_API+'ligne-repartition-commissions/tableauRepartition/byId-optimise' ,isArray: true },
        });
    }
})();
