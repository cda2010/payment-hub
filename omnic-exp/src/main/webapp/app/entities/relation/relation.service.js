(function() {
    'use strict';
    angular
        .module('omnicanalPresentationApp')
        .factory('Relation', Relation);

    Relation.$inject = ['$resource'];

    function Relation ($resource) {
        var resourceUrl =  'api/relations/:id';

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
