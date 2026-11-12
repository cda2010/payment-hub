(function() {
    'use strict';
    angular
        .module('omnicanalApp')
        .factory('Rest', Rest);

    Rest.$inject = ['$resource'];

    function Rest ($resource) {
        var resourceUrl =  'api/rests/:id';

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
