(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Commerciale', Commerciale);

    Commerciale.$inject = ['$resource'];

    function Commerciale ($resource) {
        var resourceUrl =  'api/commerciales/:id';

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
