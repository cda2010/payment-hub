(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Compte', Compte);

    Compte.$inject = ['$resource'];

    function Compte ($resource) {
        var resourceUrl =  'api/comptes/:id';

        return $resource(resourceUrl, {}, {
            'queryMirroir': { method: 'GET', url:'api/comptes/mirroir/:idCompte', isArray: true},
            'queryCompte': { method: 'GET', url:'api/comptes/relation/:idrelation', isArray: true},
            'queryCompteTeneur': { method: 'GET', url:'api/comptes/:teneur/:titulaire/:type', isArray: true},
            'query': { method: 'GET', isArray: true},
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
