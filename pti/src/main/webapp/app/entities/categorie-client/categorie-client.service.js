(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CategorieClient', CategorieClient);

    CategorieClient.$inject = ['$resource'];

    function CategorieClient ($resource) {
        var resourceUrl =  'api/categorie-clients/:id';

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
