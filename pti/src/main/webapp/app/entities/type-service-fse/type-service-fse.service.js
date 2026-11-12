(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeServiceFse', TypeServiceFse);

        TypeServiceFse.$inject = ['$resource'];

    function TypeServiceFse ($resource) { 
        var resourceUrl =  'api/type-service-fse/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/type-service-fse' ,isArray: true},
            'queryByUO': { method: 'GET',url:'api/type-service-fse/:id' ,isArray: true},
            'get': {
                method: 'GET',isArray: true,
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
