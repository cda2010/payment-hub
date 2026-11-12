(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('TypeLocalite', TypeLocalite);

    TypeLocalite.$inject = ['$resource'];

    function TypeLocalite ($resource) {
        var resourceUrl =  'api/type-localites/:id';

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
