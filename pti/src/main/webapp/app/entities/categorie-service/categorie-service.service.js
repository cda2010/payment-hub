(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CategorieService', CategorieService);

    CategorieService.$inject = ['$resource'];

    function CategorieService ($resource) { 
        var resourceUrl =  'api/categorie-service/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/categorie-service' ,isArray: true},
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
