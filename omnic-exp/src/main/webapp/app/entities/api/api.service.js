(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('Api', Api);

    Api.$inject = ['$resource'];

    function Api ($resource) {
        var resourceUrl =  'api/apis/:id';

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
