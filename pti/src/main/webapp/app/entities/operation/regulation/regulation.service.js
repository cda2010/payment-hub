(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Regulation', Regulation);

    Regulation.$inject = ['$resource'];

    function Regulation ($resource) {
        var resourceUrl =  'api/facture-oneas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryAllClient': { method: 'GET',url:'api/AllClients', isArray: true},
            'queryAllClientEntrp': {method: 'GET',url:'api/AllClientsEntrp', isArray: true},
            'queryAllUO': { method: 'GET',url:'api/unite-organisations-paginer-op', isArray: true},
            'getCompteByUO': {method: 'GET', url:'api/comptesByUO/:uoId', isArray: true},
            'getAllUOrganisation': {method: 'GET', url: 'api/unite-organisations', isArray: true},
            'getLibelleCpt': {method: 'GET', url: 'api/etiquette-comptes-lib', isArray: false},
            'doOperation': {method: 'GET', url: 'api/operations/regulation',isArray: false},
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
