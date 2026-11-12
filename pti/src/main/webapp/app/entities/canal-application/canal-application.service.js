
(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CanalApplication', CanalApplication);

        CanalApplication.$inject = ['$resource'];

    function CanalApplication ($resource) {    
        var resourceUrl =  'api/canal-application/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/canal-application' ,isArray: true},
            'queryByCanal':{method: 'GET',url:'api/canal-application-by-statut',isArray:true},
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
