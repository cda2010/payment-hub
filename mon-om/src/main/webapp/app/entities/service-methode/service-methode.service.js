(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('ServiceMethode', ServiceMethode);

    ServiceMethode.$inject = ['$resource'];

    function ServiceMethode ($resource) {
        var resourceUrl =  'api/service-methodes/:id';

        return $resource(resourceUrl, {}, {
            'queryService': { method: 'GET', url: 'api/service-methodes/service/:idService', isArray: true},
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
