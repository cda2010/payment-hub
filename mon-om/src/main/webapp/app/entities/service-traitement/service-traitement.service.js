(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('ServiceTraitement', ServiceTraitement);

    ServiceTraitement.$inject = ['$resource'];

    function ServiceTraitement ($resource) {
        var resourceUrl =  'api/service-traitements/:id';

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
