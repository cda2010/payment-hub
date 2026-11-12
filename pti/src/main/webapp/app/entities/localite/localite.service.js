(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Localite', Localite);

    Localite.$inject = ['$resource'];

    function Localite ($resource) {
        var resourceUrl =  'api/localites/:id';

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
