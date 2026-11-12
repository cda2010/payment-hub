(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneChargementCompte', LigneChargementCompte);

    LigneChargementCompte.$inject = ['$resource'];

    function LigneChargementCompte ($resource) {
        var resourceUrl =  'api/ligne-chargement-comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryDetailCpt': { method: 'GET', url: 'api/clientDetail/:id', isArray: true },
            'findByIdCpte': {method: 'GET', url:'api/clientId/:id'},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
