(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ChargementCompte', ChargementCompte);

    ChargementCompte.$inject = ['$resource','BASE_URL_API'];

    function ChargementCompte ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'chargement-comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'valider': { method: 'GET', url:BASE_URL_API+'chargement-compte/valider/:id/:user/:canal', isArray: true},
            'validerCpte': {method: 'POST', isArray: true},
            'imprimerCpte': {method: 'GET', isArray: true},
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
