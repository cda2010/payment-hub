(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('GroupeFse', GroupeFse);

        GroupeFse.$inject = ['$resource'];

    function GroupeFse ($resource) { 
        var resourceUrl =  'api/groupe-fse/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/groupe-fse' ,isArray: true},
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
