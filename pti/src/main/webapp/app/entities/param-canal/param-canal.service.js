(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ParamCanal', ParamCanal);

    ParamCanal.$inject = ['$resource'];

    function ParamCanal ($resource) { 
        var resourceUrl =  'api/param-canal/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/param-canal' ,isArray: true},
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
