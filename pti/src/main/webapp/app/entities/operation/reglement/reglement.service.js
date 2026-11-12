(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Reglement', Reglement);

    Reglement.$inject = ['$resource'];

    function Reglement ($resource) {
        var resourceUrl =  'api/operation/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryAllClient': { method: 'GET',url:'api/AllClients', isArray: true},
            'queryAllClientEntrp': {method: 'GET',url:'api/AllClientsEntrp', isArray: true},
            'queryAllUO': { method: 'GET',url:'api/unite-organisations-paginer-op', isArray: true},
            'getCompteByUO': {method: 'GET', url:'api/comptesByUO/:uoId', isArray: true},
            'getAllUOrganisation': {method: 'GET', url: 'api/unite-organisations', isArray: true},
            'valideReglement': {method: 'GET', url:'api/operations/valide-reglement-compte-new',isArray: false},
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
