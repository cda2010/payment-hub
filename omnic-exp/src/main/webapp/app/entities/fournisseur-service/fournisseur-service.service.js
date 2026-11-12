(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('FournisseurService', FournisseurService);

    FournisseurService.$inject = ['$resource'];

    function FournisseurService ($resource) {
        var resourceUrl =  'api/fournisseur-services/:id';

        return $resource(resourceUrl, {}, {
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
