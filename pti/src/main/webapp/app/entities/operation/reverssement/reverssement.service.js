(function () {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Reverssement', Reverssement);

    Reverssement.$inject = ['$resource','BASE_URL_API'];

    function Reverssement($resource,BASE_URL_API) {
        var resourceUrl = BASE_URL_API+'machines/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'allow': {method: 'GET', url: BASE_URL_API+'activeMachine',isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'}
        });
    }
})();
