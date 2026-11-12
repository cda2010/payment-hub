(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Api', Api);

    Api.$inject = ['$resource'];

    function Api ($resource) {
        var resourceUrl =  'api/apis/:id';

        return $resource(resourceUrl, {}, {
            'query': { 
                method: 'GET', 
                isArray: true,
                params: {
                    nomApi: '@nomApi',
                    fournisseur: '@fournisseur'
                }
            },
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'duplicate': { 
                method: 'POST',  
                url: 'api/apis/:id/duplicate'
            }
        });
    }
})();
