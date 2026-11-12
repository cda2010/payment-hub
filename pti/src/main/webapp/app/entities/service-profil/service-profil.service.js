(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ServiceProfil', ServiceProfil);

    ServiceProfil.$inject = ['$resource'];

    function ServiceProfil ($resource) {
        var resourceUrl =  'api/service-profils/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET' ,isArray: true},
            'queryAll': { method: 'GET',url:'api/type-services_list' ,isArray: true},
            'queryByType': { method: 'GET',url:'api/service-profils/byType/:type', isArray: true},
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
