(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('TypeService', TypeService);

    TypeService.$inject = ['$resource'];

    function TypeService ($resource) {
        var resourceUrl =  'api/type-services/:id';
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
