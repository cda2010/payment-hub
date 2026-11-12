(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('FournisseurService', FournisseurService);

    FournisseurService.$inject = ['$resource'];

    function FournisseurService ($resource) {
        var resourceUrl =  'api/fournisseur-services/:id';

        return $resource(resourceUrl, {}, {
            'getAllFse':{method: 'GET', url:'api/fournisseur-services/list', isArray: true},
            'queryCompense':{method: 'GET', url:'api/fournisseur-services/compense/:idFse', isArray: true},
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
