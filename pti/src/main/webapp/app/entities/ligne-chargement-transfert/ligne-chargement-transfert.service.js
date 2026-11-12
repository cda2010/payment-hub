(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneChargementTransfert', LigneChargementTransfert);

    LigneChargementTransfert.$inject = ['$resource'];

    function LigneChargementTransfert ($resource) {
        var resourceUrl =  'api/ligne-chargement-transferts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryDetail': { method: 'GET', url: 'api/requete/:id', isArray: true },
            'queryDetailByStatut': { method: 'GET', url: 'api/requete/:id/:statut', isArray: true },
            'findById': {method: 'GET', url:'api/ligne/:id'},
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
