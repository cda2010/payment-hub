(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Terminal', Terminal);

    Terminal.$inject = ['$resource'];

    function Terminal ($resource) {
        var resourceUrl =  'api/terminals/:id';

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
